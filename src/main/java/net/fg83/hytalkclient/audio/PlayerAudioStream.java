package net.fg83.hytalkclient.audio;

import net.fg83.hytalkclient.util.AppConstants;
import org.concentus.OpusDecoder;
import org.concentus.OpusException;

import java.util.Arrays;
import java.util.TreeMap;

/**
 * Manages audio playback from received Opus-encoded packets.
 * Handles jitter buffering, packet loss concealment (PLC), and audio decoding.
 */
public class PlayerAudioStream extends AudioStream {

    // Opus decoder for converting compressed audio to PCM
    private final OpusDecoder decoder;
    // Jitter buffer to reorder and store incoming packets by sequence number
    private final TreeMap<Integer, byte[]> jitterBuffer = new TreeMap<>();

    // The next expected sequence number to play (-1 means not initialized)
    private int expectedSequence = -1;
    // Whether we're currently buffering before playback
    private boolean buffering = true; // Start in buffering mode

    // Minimum frames to buffer before starting playback (3 frames = 60ms)
    private static final int MIN_BUFFER_FRAMES = 3; // Wait for 3 frames (60ms) before playing
    // Maximum frames to keep in buffer to prevent excessive memory usage
    private static final int MAX_BUFFER_FRAMES = AppConstants.Audio.MAX_BUFFER_SIZE;

    // Buffer to hold decoded PCM samples (16-bit integers)
    private final short[] pcmBuffer = new short[AppConstants.Audio.FRAME_SIZE];
    // Buffer to hold converted floating-point samples for output
    private final float[] floatBuffer = new float[AppConstants.Audio.FRAME_SIZE];

    // Statistics: total packets received
    private int packetsReceived = 0;
    // Statistics: packets dropped due to being too old
    private int packetsDropped = 0;
    // Statistics: frames synthesized using packet loss concealment
    private int plcFrames = 0;

    /**
     * Creates a new player audio stream with an Opus decoder.
     *
     * @throws OpusException if the decoder cannot be initialized
     */
    public PlayerAudioStream() throws OpusException {
        this.decoder = new OpusDecoder(AppConstants.Audio.SAMPLE_RATE, 1);
        running.set(true);
    }

    /**
     * Adds a received audio packet to the jitter buffer.
     * Handles late packets, buffer overflow, and triggers playback when ready.
     *
     * @param sequence  the sequence number of the packet
     * @param opusFrame the Opus-encoded audio data
     */
    public synchronized void pushPacket(int sequence, byte[] opusFrame) {
        packetsReceived++;

        // Drop packets that are too old (more than 100 sequences behind expected)
        if (expectedSequence != -1 && sequence < expectedSequence - 100) {
            packetsDropped++;
            return;
        }

        // Add packet to jitter buffer
        jitterBuffer.put(sequence, opusFrame);

        // Prevent buffer from growing too large
        while (jitterBuffer.size() > MAX_BUFFER_FRAMES) {
            jitterBuffer.pollFirstEntry();
        }

        // Exit buffering mode once we have enough frames
        if (buffering && jitterBuffer.size() >= MIN_BUFFER_FRAMES) {
            buffering = false;
            System.out.println("PlayerAudioStream: Buffering complete, starting playback (buffer size: " + jitterBuffer.size() + ")");
        }
    }

    /**
     * Retrieves the next audio frame for playback.
     * Handles buffering, packet loss concealment, and sequence management.
     *
     * @return float array containing the audio samples for this frame
     */
    public synchronized float[] getNextFrame() {
        // Return silence while buffering
        if (buffering) {
            return silence();
        }

        // Initialize expected sequence on first playback
        if (expectedSequence == -1) {
            if (jitterBuffer.isEmpty()) {
                return silence();
            }
            expectedSequence = jitterBuffer.firstKey();
            System.out.println("PlayerAudioStream: Starting playback at sequence " + expectedSequence);
        }

        // Try to retrieve the expected packet from buffer
        byte[] opusFrame = jitterBuffer.remove(expectedSequence);
        expectedSequence++;

        // Handle missing packet (packet loss)
        if (opusFrame == null) {
            plcFrames++;

            // If buffer is empty, re-enter buffering mode
            if (jitterBuffer.isEmpty()) {
                System.out.println("PlayerAudioStream: Buffer underrun, re-buffering...");
                buffering = true;
                expectedSequence = -1; // Reset
                return silence();
            }

            // Use packet loss concealment to synthesize missing frame
            return decodePLC();
        }

        // Decode the received packet normally
        return decodeFrame(opusFrame);
    }

    /**
     * Decodes an Opus frame to PCM samples and converts to float format.
     *
     * @param opusFrame the Opus-encoded audio data
     * @return float array containing decoded audio samples
     */
    private float[] decodeFrame(byte[] opusFrame) {
        try {
            // Decode Opus frame to PCM samples
            int samplesDecoded = decoder.decode(
                    opusFrame,
                    0,
                    opusFrame.length,
                    pcmBuffer,
                    0,
                    AppConstants.Audio.FRAME_SIZE,
                    false
            );

            // Warn if decoded size doesn't match expected frame size
            if (samplesDecoded != AppConstants.Audio.FRAME_SIZE) {
                System.err.println("Unexpected decode size: " + samplesDecoded + " (expected " + AppConstants.Audio.FRAME_SIZE + ")");
            }

        }
        catch (Exception e) {
            System.err.println("Opus decode error: " + e.getMessage());
            return silence();
        }

        // Convert PCM to float and apply gain
        return convertToFloat();
    }

    /**
     * Uses packet loss concealment to synthesize a frame when a packet is missing.
     * The Opus decoder generates plausible audio based on previous frames.
     *
     * @return float array containing synthesized audio samples
     */
    private float[] decodePLC() {
        try {
            // Decode with null input to trigger PLC mode
            decoder.decode(
                    null,
                    0,
                    0,
                    pcmBuffer,
                    0,
                    AppConstants.Audio.FRAME_SIZE,
                    true
            );
        }
        catch (Exception e) {
            System.err.println("PLC decode error: " + e.getMessage());
            return silence();
        }

        // Convert PCM to float and apply gain
        return convertToFloat();
    }

    /**
     * Converts PCM samples (16-bit shorts) to floating-point format.
     * Applies gain and calculates peak level for VU meter.
     *
     * @return float array containing converted and processed audio samples
     */
    private float[] convertToFloat() {
        float currentGain = gain.get();
        float peakLevel = 0.0f;

        // Convert each sample from 16-bit to float in range [-1.0, 1.0]
        for (int i = 0; i < AppConstants.Audio.FRAME_SIZE; i++) {
            // Normalize from 16-bit range to [-1.0, 1.0]
            float sample = (pcmBuffer[i] / 32768f);
            // Apply gain setting
            float gained = applyGainToSample(sample, currentGain);
            floatBuffer[i] = gained;
            // Track peak level for VU meter
            peakLevel = Math.max(peakLevel, Math.abs(gained));
        }

        // Update the audio level meter
        updateLevel(peakLevel);
        return floatBuffer;
    }

    /**
     * Returns a silent audio frame (all zeros).
     *
     * @return float array filled with zeros
     */
    private float[] silence() {
        Arrays.fill(floatBuffer, 0f);
        updateLevel(0.0f);
        return floatBuffer;
    }

    /**
     * Checks if audio is currently being played.
     *
     * @return true if playback is active, false if buffering or stopped
     */
    public boolean isPlaying() {
        return !buffering && (!jitterBuffer.isEmpty() || expectedSequence != -1);
    }

    /**
     * Returns statistics about the audio stream performance.
     *
     * @return formatted string with packet counts and buffer status
     */
    public synchronized String wgetStats() {
        return String.format("RX:%d Dropped:%d PLC:%d Buffer:%d Buffering:%b",
                packetsReceived, packetsDropped, plcFrames, jitterBuffer.size(), buffering);
    }

    /**
     * Resets the audio stream to initial state.
     * Clears buffer, resets sequence tracking, and re-enters buffering mode.
     */
    public synchronized void reset() {
        jitterBuffer.clear();
        expectedSequence = -1;
        buffering = true;
        packetsReceived = 0;
        packetsDropped = 0;
        plcFrames = 0;
        System.out.println("PlayerAudioStream: Reset");
    }
}