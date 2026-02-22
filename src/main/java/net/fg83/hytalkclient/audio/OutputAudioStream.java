
package net.fg83.hytalkclient.audio;

import net.fg83.hytalkclient.service.AudioIOManager;
import net.fg83.hytalkclient.util.AppConstants;

import javax.sound.sampled.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OutputAudioStream mixes all PlayerAudioStreams and plays them to the speakers.
 *
 * Responsibilities:
 * - Sum audio from all active PlayerAudioStreams
 * - Apply output gain and mute
 * - Convert mono mix to stereo for output device
 * - Play mixed audio to output device
 * - Track output level for metering
 *
 * Audio Format (Output):
 * - 48kHz sample rate
 * - 16-bit PCM
 * - Stereo (2 channels)
 * - 20ms frames
 */
public class OutputAudioStream extends AudioStream {

    private final AudioIOManager.AudioDevice device;
    private final Map<UUID, PlayerAudioStream> playerStreams = new ConcurrentHashMap<>();

    private SourceDataLine speakers;
    private Thread playbackThread;

    private static final int STEREO_FRAME_SIZE = AppConstants.Audio.FRAME_SIZE * 2; // samples per frame (L+R)
    private static final int STEREO_BYTES_PER_FRAME = STEREO_FRAME_SIZE * AppConstants.Audio.BYTES_PER_SAMPLE;

    /**
     * Create a new output audio stream
     * @param device The audio output device to play to
     */
    public OutputAudioStream(AudioIOManager.AudioDevice device) {
        this.device = device;
    }

    /**
     * Start audio playback
     */
    public void start() throws LineUnavailableException {
        if (running.get()) {
            return;
        }

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, AppConstants.Audio.OUTPUT_AUDIO_FORMAT);

        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException(
                    "Audio format not supported by device: " + device.name()
            );
        }

        // Get the mixer for the specific device
        Mixer mixer = AudioSystem.getMixer(device.mixerInfo());
        speakers = (SourceDataLine) mixer.getLine(info);

        // Buffer size: 4 frames worth for smooth playback
        int bufferSize = STEREO_BYTES_PER_FRAME * 4;
        speakers.open(AppConstants.Audio.OUTPUT_AUDIO_FORMAT, bufferSize);
        speakers.start();

        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Audio playback already started");
        }

        playbackThread = new Thread(this::playbackLoop, "Audio-Output-Playback");
        playbackThread.setDaemon(true);
        playbackThread.start();

        System.out.println("Started audio output: " + device.name());
    }

    /**
     * Stop audio playback
     */
    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        if (playbackThread != null) {
            try {
                playbackThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (speakers != null) {
            speakers.drain(); // Let existing audio finish
            speakers.stop();
            speakers.close();
            speakers = null;
        }

        System.out.println("Stopped audio output");
    }

    /**
     * Main playback loop - runs in separate thread
     */
    private void playbackLoop() {
        byte[] stereoBuffer = new byte[STEREO_BYTES_PER_FRAME];

        // Target frame time in nanoseconds (20ms)
        long frameTimeNs = (long) AppConstants.Audio.FRAME_SIZE * 1_000_000_000L / AppConstants.Audio.SAMPLE_RATE;
        long nextFrameTime = System.nanoTime() + frameTimeNs;

        while (running.get()) {
            try {
                // Mix all player streams
                mixStreams(stereoBuffer);

                // Write to speakers (blocking)
                speakers.write(stereoBuffer, 0, stereoBuffer.length);

                // Timing control to maintain consistent frame rate
                long now = System.nanoTime();
                if (now < nextFrameTime) {
                    long sleepMs = (nextFrameTime - now) / 1_000_000;
                    if (sleepMs > 0) {
                        Thread.sleep(sleepMs);
                    }
                }
                nextFrameTime += frameTimeNs;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                if (running.get()) {
                    System.err.println("Error in playback loop: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Mix all active player streams into stereo output buffer
     */
    private void mixStreams(byte[] stereoBuffer) {
        // Clear buffer
        Arrays.fill(stereoBuffer, (byte) 0);

        // If muted or no streams, output silence
        if (playerStreams.isEmpty()) {
            updateLevel(0.0f);
            return;
        }

        // Accumulator for mixing (using floats to prevent overflow)
        float[] mixAccumulator = new float[AppConstants.Audio.FRAME_SIZE];
        int activeStreams = 0;

        // Sum all player streams (mono input)
        for (PlayerAudioStream stream : playerStreams.values()) {
            if (!stream.isPlaying()) {
                continue;
            }

            float[] playerData = stream.getNextFrame();

            if (muted.get() || stream.isMuted()) {
                continue;
            }

            if (playerData != null && playerData.length == AppConstants.Audio.FRAME_SIZE) {
                activeStreams++;

                // Add this stream to the mix (already includes player's gain)
                for (int i = 0; i < AppConstants.Audio.FRAME_SIZE; i++) {
                    mixAccumulator[i] += playerData[i];
                }
            }
        }

        if (activeStreams == 0) {
            updateLevel(0.0f);
            return;
        }

        // Apply output gain and convert to stereo PCM
        float outputGain = gain.get();
        float peakLevel = 0.0f;

        for (int i = 0; i < AppConstants.Audio.FRAME_SIZE; i++) {
            // Apply output gain
            float mixed = mixAccumulator[i] * outputGain;

            // Clamp to prevent clipping
            mixed = Math.max(-1.0f, Math.min(1.0f, mixed));

            // Track peak level
            peakLevel = Math.max(peakLevel, Math.abs(mixed));

            // Convert to 16-bit PCM
            short sample = (short) (mixed * 32767);

            // Write to both stereo channels (L and R)
            int stereoIndex = i * 2 * AppConstants.Audio.BYTES_PER_SAMPLE;

            // Left channel
            stereoBuffer[stereoIndex] = (byte) (sample & 0xFF);
            stereoBuffer[stereoIndex + 1] = (byte) ((sample >> 8) & 0xFF);

            // Right channel (same as left for now - spatial audio could differ later)
            stereoBuffer[stereoIndex + 2] = (byte) (sample & 0xFF);
            stereoBuffer[stereoIndex + 3] = (byte) ((sample >> 8) & 0xFF);
        }

        // Update level meter
        updateLevel(peakLevel);
    }

    // === Player Stream Management ===

    /**
     * Add a player's audio stream to the mix
     */
    public void addPlayerStream(UUID playerId, PlayerAudioStream stream) {
        playerStreams.put(playerId, stream);
    }

    /**
     * Remove a player's audio stream from the mix
     */
    public void removePlayerStream(UUID playerId) {
        playerStreams.remove(playerId);
        System.out.println("Removed player stream from output: " + playerId);
    }

    /**
     * Get the number of active player streams
     */
    public int getActiveStreamCount() {
        return (int) playerStreams.values().stream()
                .filter(stream -> !stream.isMuted() && stream.isPlaying())
                .count();
    }

    public Map<UUID, PlayerAudioStream> getPlayerStreams() {
        return playerStreams;
    }

    /**
     * Clear all player streams
     */
    public void clearAllStreams() {
        playerStreams.clear();
        System.out.println("Cleared all player streams from output");
    }
}