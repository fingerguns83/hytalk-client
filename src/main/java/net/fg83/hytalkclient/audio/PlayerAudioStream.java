package net.fg83.hytalkclient.audio;

import net.fg83.hytalkclient.util.AppConstants;
import org.concentus.OpusDecoder;
import org.concentus.OpusException;

import java.util.Arrays;
import java.util.TreeMap;

/**
 * Decodes and buffers audio from a remote player.
 */
public class PlayerAudioStream extends AudioStream {

    private final OpusDecoder decoder;
    private final TreeMap<Integer, byte[]> jitterBuffer = new TreeMap<>();

    private int expectedSequence = -1;

    // Reused buffers to reduce allocations
    private final short[] pcmBuffer = new short[AppConstants.Audio.FRAME_SIZE];
    private final float[] floatBuffer = new float[AppConstants.Audio.FRAME_SIZE];

    public PlayerAudioStream() throws OpusException {
        this.decoder = new OpusDecoder(AppConstants.Audio.SAMPLE_RATE, 1);
        running.set(true); // PlayerAudioStream is always "running" once created
    }

    // Called from UDP thread
    public synchronized void pushPacket(int sequence, byte[] opusFrame) {
        if (expectedSequence != -1 && sequence < expectedSequence - 100) {
            return; // too old
        }

        jitterBuffer.put(sequence, opusFrame);

        while (jitterBuffer.size() > AppConstants.Audio.MAX_BUFFER_SIZE) {
            jitterBuffer.pollFirstEntry();
        }
    }

    // Called from mixer thread every 20ms
    public synchronized float[] getNextFrame() {
        if (expectedSequence == -1) {
            if (jitterBuffer.isEmpty()) {
                return silence();
            }
            expectedSequence = jitterBuffer.firstKey();
        }

        byte[] opusFrame = jitterBuffer.remove(expectedSequence);
        expectedSequence++;

        if (opusFrame == null) {
            return decodePLC();
        }

        return decodeFrame(opusFrame);
    }

    private float[] decodeFrame(byte[] opusFrame) {
        try {
            decoder.decode(
                    opusFrame,
                    0,
                    opusFrame.length,
                    pcmBuffer,
                    0,
                    AppConstants.Audio.FRAME_SIZE,
                    false
            );
        } catch (Exception e) {
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
            return silence();
        }

        return convertToFloat();
    }

    private float[] convertToFloat() {
        float currentGain = gain.get();
        float peakLevel = 0.0f;

        for (int i = 0; i < AppConstants.Audio.FRAME_SIZE; i++) {
            float sample = (pcmBuffer[i] / 32768f) * currentGain;
            floatBuffer[i] = sample;
            peakLevel = Math.max(peakLevel, Math.abs(sample));
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
        return !jitterBuffer.isEmpty() || expectedSequence != -1;
    }
}