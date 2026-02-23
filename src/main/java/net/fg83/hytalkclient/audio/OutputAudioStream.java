package net.fg83.hytalkclient.audio;

import net.fg83.hytalkclient.service.AudioIOManager;
import net.fg83.hytalkclient.util.AppConstants;

import javax.sound.sampled.*;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages audio output stream to a speaker device.
 * Mixes multiple player audio streams, applies gain, and outputs to speakers.
 */
public class OutputAudioStream extends AudioStream {

    // The audio device to output to
    private final AudioIOManager.AudioDevice device;
    // Map of player audio streams by player ID
    private final Map<UUID, PlayerAudioStream> playerStreams = new ConcurrentHashMap<>();

    // Java Sound API line for playing audio
    private SourceDataLine speakers;
    // Background thread that continuously writes audio data
    private Thread playbackThread;

    // Total number of samples per stereo frame (left + right channels)
    private static final int STEREO_FRAME_SIZE = AppConstants.Audio.FRAME_SIZE * 2; // samples per frame (L+R)
    // Total bytes per stereo frame
    private static final int STEREO_BYTES_PER_FRAME = STEREO_FRAME_SIZE * AppConstants.Audio.BYTES_PER_SAMPLE;

    /**
     * Creates a new output audio stream.
     *
     * @param device the audio device to output to
     */
    public OutputAudioStream(AudioIOManager.AudioDevice device) {
        this.device = device;
    }

    /**
     * Starts playing audio to the speakers.
     * Opens the audio line, starts the playback thread, and begins writing audio data.
     *
     * @throws LineUnavailableException if the audio line cannot be opened
     */
    public void start() throws LineUnavailableException {
        // Prevent multiple starts
        if (running.get()) {
            return;
        }

        // Get the audio line info for the output device
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, AppConstants.Audio.OUTPUT_AUDIO_FORMAT);

        // Check if the audio format is supported
        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException(
                    "Audio format not supported by device: " + device.name()
            );
        }

        // Get the mixer and open the speaker line
        Mixer mixer = AudioSystem.getMixer(device.mixerInfo());
        speakers = (SourceDataLine) mixer.getLine(info);

        // Set buffer size to hold 4 frames
        int bufferSize = STEREO_BYTES_PER_FRAME * 4;
        speakers.open(AppConstants.Audio.OUTPUT_AUDIO_FORMAT, bufferSize);
        speakers.start();

        // Atomically set running flag to true
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Audio playback already started");
        }

        // Start the playback thread
        playbackThread = new Thread(this::playbackLoop, "Audio-Output-Playback");
        playbackThread.setDaemon(true);
        playbackThread.start();

        System.out.println("Started audio output: " + device.name());
    }

    /**
     * Stops playing audio to the speakers.
     * Shuts down the playback thread and closes the audio line.
     */
    public void stop() {
        // Atomically set running flag to false
        if (!running.compareAndSet(true, false)) {
            return;
        }

        // Wait for playback thread to finish
        if (playbackThread != null) {
            try {
                playbackThread.join(1000);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Close the speaker line
        if (speakers != null) {
            speakers.drain();
            speakers.stop();
            speakers.close();
            speakers = null;
        }

        System.out.println("Stopped audio output");
    }


    /**
     * Main playback loop that continuously mixes and writes audio data to speakers.
     * Runs in a separate thread until stopped.
     * Uses timing to maintain consistent frame rate.
     */
    private void playbackLoop() {
        // Buffer to hold one frame of stereo audio data
        byte[] stereoBuffer = new byte[STEREO_BYTES_PER_FRAME];

        // Calculate the time duration of one frame in nanoseconds
        long frameTimeNs = (long) AppConstants.Audio.FRAME_SIZE * 1_000_000_000L / AppConstants.Audio.SAMPLE_RATE;
        // Track when the next frame should be sent
        long nextFrameTime = System.nanoTime() + frameTimeNs;

        while (running.get()) {
            try {
                // Mix all player streams into the stereo buffer
                mixStreams(stereoBuffer);

                // Write the mixed audio to speakers
                speakers.write(stereoBuffer, 0, stereoBuffer.length);

                // Calculate sleep time to maintain frame rate
                long now = System.nanoTime();
                if (now < nextFrameTime) {
                    long sleepMs = (nextFrameTime - now) / 1_000_000;
                    if (sleepMs > 0) {
                        Thread.sleep(sleepMs);
                    }
                }
                // Update the next frame time
                nextFrameTime += frameTimeNs;

            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            catch (Exception e) {
                if (running.get()) {
                    System.err.println("Error in playback loop: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Mixes all player audio streams into a single stereo output buffer.
     * Applies gain, clipping, and converts mono to stereo.
     *
     * @param stereoBuffer the buffer to write mixed stereo audio to
     */
    private void mixStreams(byte[] stereoBuffer) {
        // Clear the output buffer
        Arrays.fill(stereoBuffer, (byte) 0);

        // If no streams, set level to zero and return silence
        if (playerStreams.isEmpty()) {
            updateLevel(0.0f);
            return;
        }

        // Accumulator for mixing audio samples as floating-point values
        float[] mixAccumulator = new float[AppConstants.Audio.FRAME_SIZE];
        // Count of active (playing, non-muted) streams
        int activeStreams = 0;

        // Iterate through all player streams
        for (PlayerAudioStream stream : playerStreams.values()) {
            // Skip streams that are not currently playing
            if (!stream.isPlaying()) {
                continue;
            }

            // Get the next frame of audio data from this player
            float[] playerData = stream.getNextFrame();

            // Skip if output is muted or this stream is muted
            if (muted.get() || stream.isMuted()) {
                continue;
            }

            // Mix valid audio data into the accumulator
            if (playerData != null && playerData.length == AppConstants.Audio.FRAME_SIZE) {
                activeStreams++;

                // Add each sample to the mix accumulator
                for (int i = 0; i < AppConstants.Audio.FRAME_SIZE; i++) {
                    mixAccumulator[i] += playerData[i];
                }
            }
        }

        // If no active streams, set level to zero and return silence
        if (activeStreams == 0) {
            updateLevel(0.0f);
            return;
        }

        // Get current output gain setting
        float outputGain = gain.get();
        // Track the peak level for this frame
        float peakLevel = 0.0f;

        // Process each sample: apply gain, clip, convert to PCM, duplicate to stereo
        for (int i = 0; i < AppConstants.Audio.FRAME_SIZE; i++) {
            // Apply output gain to the mixed sample
            float mixed = mixAccumulator[i] * outputGain;

            // Clip the sample to the valid range [-1.0, 1.0]
            mixed = Math.max(-1.0f, Math.min(1.0f, mixed));

            // Update peak level for volume monitoring
            peakLevel = Math.max(peakLevel, Math.abs(mixed));

            // Convert from float [-1.0, 1.0] to 16-bit PCM [-32767, 32767]
            short sample = (short) (mixed * 32767);

            // Calculate the index in the stereo buffer (4 bytes per mono sample: 2 for L, 2 for R)
            int stereoIndex = i * 2 * AppConstants.Audio.BYTES_PER_SAMPLE;

            // Write left channel (little-endian 16-bit)
            stereoBuffer[stereoIndex] = (byte) (sample & 0xFF);
            stereoBuffer[stereoIndex + 1] = (byte) ((sample >> 8) & 0xFF);

            // Write right channel (duplicate of left for mono-to-stereo conversion)
            stereoBuffer[stereoIndex + 2] = (byte) (sample & 0xFF);
            stereoBuffer[stereoIndex + 3] = (byte) ((sample >> 8) & 0xFF);
        }

        // Update the output level for monitoring
        updateLevel(peakLevel);
    }

    /**
     * Adds a player audio stream to the output mixer.
     *
     * @param playerId the unique ID of the player
     * @param stream   the player's audio stream
     */
    public void addPlayerStream(UUID playerId, PlayerAudioStream stream) {
        playerStreams.put(playerId, stream);
    }


    /**
     * Removes a player audio stream from the output mixer.
     *
     * @param playerId the unique ID of the player to remove
     */
    public void removePlayerStream(UUID playerId) {
        playerStreams.remove(playerId);
        System.out.println("Removed player stream from output: " + playerId);
    }

    /**
     * Gets the count of currently active (playing, non-muted) streams.
     *
     * @return the number of active streams
     */
    public int getActiveStreamCount() {
        return (int) playerStreams.values().stream()
                .filter(stream -> !stream.isMuted() && stream.isPlaying())
                .count();
    }

    /**
     * Gets the map of all player audio streams.
     *
     * @return map of player IDs to their audio streams
     */
    public Map<UUID, PlayerAudioStream> getPlayerStreams() {
        return playerStreams;
    }

    /**
     * Removes all player audio streams from the output mixer.
     */
    public void clearAllStreams() {
        playerStreams.clear();
        System.out.println("Cleared all player streams from output");
    }
}