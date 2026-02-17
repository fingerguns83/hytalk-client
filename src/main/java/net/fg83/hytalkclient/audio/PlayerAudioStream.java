package net.fg83.hytalkclient.audio;

import org.concentus.OpusDecoder;
import org.concentus.OpusException;

import java.util.TreeMap;

import static net.fg83.hytalkclient.util.AppConstants.Audio.*;

public class PlayerAudioStream {

    private final OpusDecoder decoder;

    private final TreeMap<Integer, byte[]> jitterBuffer = new TreeMap<>();

    private int expectedSequence = -1;
    private float gain = 1.0f;

    // Reused buffers to reduce allocations
    private final short[] pcmBuffer = new short[FRAME_SIZE];
    private final float[] floatBuffer = new float[FRAME_SIZE];

    public PlayerAudioStream() throws OpusException {
        this.decoder = new OpusDecoder(SAMPLE_RATE, CHANNELS);
    }

    // Called from UDP thread
    public synchronized void pushPacket(int sequence, byte[] opusFrame) {

        if (expectedSequence != -1 && sequence < expectedSequence - 100) {
            return; // too old
        }

        jitterBuffer.put(sequence, opusFrame);

        while (jitterBuffer.size() > MAX_BUFFER_SIZE) {
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
                    FRAME_SIZE,
                    false
            );
        }
        catch (Exception e) {
            return silence();
        }

        convertToFloat();
        return floatBuffer;
    }

    private float[] decodePLC() {
        try {
            decoder.decode(
                    null,
                    0,
                    0,
                    pcmBuffer,
                    0,
                    FRAME_SIZE,
                    true // enable packet loss concealment
            );
        }
        catch (Exception e) {
            return silence();
        }

        convertToFloat();
        return floatBuffer;
    }

    private void convertToFloat() {
        for (int i = 0; i < FRAME_SIZE; i++) {
            floatBuffer[i] = (pcmBuffer[i] / 32768f) * gain;
        }
    }

    private float[] silence() {
        for (int i = 0; i < FRAME_SIZE; i++) {
            floatBuffer[i] = 0f;
        }
        return floatBuffer;
    }

    public void setGain(float gain) {
        this.gain = gain;
    }

    public float getGain() {
        return gain;
    }
}
