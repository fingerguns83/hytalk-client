package net.fg83.hytalkclient.audio;

import net.fg83.hytalkclient.util.AppConstants;
import org.concentus.OpusDecoder;
import org.concentus.OpusException;

import java.util.Arrays;
import java.util.TreeMap;

/**
 * Decodes and buffers audio from a remote player.
 *
 * Includes jitter buffer with pre-buffering to handle network delays.
 */
public class PlayerAudioStream extends AudioStream {

    private final OpusDecoder decoder;
    private final TreeMap<Integer, byte[]> jitterBuffer = new TreeMap<>();

    private int expectedSequence = -1;
    private boolean buffering = true; // Start in buffering mode

    // Pre-buffer settings (wait for a few frames before starting playback)
    private static final int MIN_BUFFER_FRAMES = 3; // Wait for 3 frames (60ms) before playing
    private static final int MAX_BUFFER_FRAMES = AppConstants.Audio.MAX_BUFFER_SIZE;

    // Reused buffers to reduce allocations
    private final short[] pcmBuffer = new short[AppConstants.Audio.FRAME_SIZE];
    private final float[] floatBuffer = new float[AppConstants.Audio.FRAME_SIZE];

    // Statistics for debugging
    private int packetsReceived = 0;
    private int packetsDropped = 0;
    private int plcFrames = 0;

    public PlayerAudioStream() throws OpusException {
        this.decoder = new OpusDecoder(AppConstants.Audio.SAMPLE_RATE, 1);
        running.set(true);
    }

    // Called from UDP thread
    public synchronized void pushPacket(int sequence, byte[] opusFrame) {
        packetsReceived++;

        // Drop packets that are way too old
        if (expectedSequence != -1 && sequence < expectedSequence - 100) {
            packetsDropped++;
            return;
        }

        jitterBuffer.put(sequence, opusFrame);

        // Enforce maximum buffer size
        while (jitterBuffer.size() > MAX_BUFFER_FRAMES) {
            jitterBuffer.pollFirstEntry();
        }

        // Check if we should exit buffering mode
        if (buffering && jitterBuffer.size() >= MIN_BUFFER_FRAMES) {
            buffering = false;
            System.out.println("PlayerAudioStream: Buffering complete, starting playback (buffer size: " + jitterBuffer.size() + ")");
        }
    }

    // Called from mixer thread every 20ms
    public synchronized float[] getNextFrame() {
        // If we're still buffering, return silence
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

        // Try to get the expected frame
        byte[] opusFrame = jitterBuffer.remove(expectedSequence);
        expectedSequence++;

        // If frame is missing, use packet loss concealment
        if (opusFrame == null) {
            plcFrames++;

            // If buffer is getting low, consider re-buffering
            if (jitterBuffer.isEmpty()) {
                System.out.println("PlayerAudioStream: Buffer underrun, re-buffering...");
                buffering = true;
                expectedSequence = -1; // Reset
                return silence();
            }

            return decodePLC();
        }

        return decodeFrame(opusFrame);
    }

    private float[] decodeFrame(byte[] opusFrame) {
        try {
            int samplesDecoded = decoder.decode(
                    opusFrame,
                    0,
                    opusFrame.length,
                    pcmBuffer,
                    0,
                    AppConstants.Audio.FRAME_SIZE,
                    false
            );

            if (samplesDecoded != AppConstants.Audio.FRAME_SIZE) {
                System.err.println("Unexpected decode size: " + samplesDecoded + " (expected " + AppConstants.Audio.FRAME_SIZE + ")");
            }

        } catch (Exception e) {
            System.err.println("Opus decode error: " + e.getMessage());
            return silence();
        }

        return convertToFloat();
    }

    private float[] decodePLC() {
        try {
            decoder.decode(
                    null,
                    0,
                    0,
                    pcmBuffer,
                    0,
                    AppConstants.Audio.FRAME_SIZE,
                    true // enable packet loss concealment
            );
        } catch (Exception e) {
            System.err.println("PLC decode error: " + e.getMessage());
            return silence();
        }

        return convertToFloat();
    }

    private float[] convertToFloat() {
        float currentGain = gain.get();
        float peakLevel = 0.0f;

        for (int i = 0; i < AppConstants.Audio.FRAME_SIZE; i++) {
            float sample = (pcmBuffer[i] / 32768f);
            float gained = applyGainToSample(sample, currentGain);
            floatBuffer[i] = gained;
            peakLevel = Math.max(peakLevel, Math.abs(gained));
        }

        updateLevel(peakLevel);
        return floatBuffer;
    }

    private float[] silence() {
        Arrays.fill(floatBuffer, 0f);
        updateLevel(0.0f);
        return floatBuffer;
    }

    public boolean isPlaying() {
        return !buffering && (!jitterBuffer.isEmpty() || expectedSequence != -1);
    }

    /**
     * Get statistics for debugging
     */
    public synchronized String wgetStats() {
        return String.format("RX:%d Dropped:%d PLC:%d Buffer:%d Buffering:%b",
                packetsReceived, packetsDropped, plcFrames, jitterBuffer.size(), buffering);
    }

    /**
     * Reset the stream (useful when player reconnects)
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