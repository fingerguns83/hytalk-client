package net.fg83.hytalkclient.audio;

import net.fg83.hytalkclient.service.AudioIOManager;
import net.fg83.hytalkclient.util.AppConstants;

import javax.sound.sampled.*;
import java.util.function.Consumer;

/**
 * Captures audio from the microphone and provides it in chunks for encoding.
 */
public class InputAudioStream extends AudioStream {

    private final AudioIOManager.AudioDevice device;
    private final Consumer<byte[]> frameCallback;

    private TargetDataLine microphone;
    private Thread captureThread;

    public InputAudioStream(AudioIOManager.AudioDevice device, float initialGain, Consumer<byte[]> frameCallback) {
        this.device = device;
        this.frameCallback = frameCallback;
        setGain(initialGain);
    }

    public void start() throws LineUnavailableException {
        if (running.get()) {
            return;
        }

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, AppConstants.Audio.INPUT_AUDIO_FORMAT);

        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException(
                    "Audio format not supported by device: " + device.name()
            );
        }

        Mixer mixer = AudioSystem.getMixer(device.mixerInfo());
        microphone = (TargetDataLine) mixer.getLine(info);

        int bufferSize = AppConstants.Audio.BYTES_PER_FRAME * 2;
        microphone.open(AppConstants.Audio.INPUT_AUDIO_FORMAT, bufferSize);

        // Check if we actually got a working line
        System.out.println("Microphone opened: " + microphone.isOpen());
        System.out.println("Microphone active: " + microphone.isActive());
        System.out.println("Microphone format: " + microphone.getFormat());
        System.out.println("Buffer size: " + microphone.getBufferSize());

        microphone.start();

        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Audio capture already started");
        }

        captureThread = new Thread(this::captureLoop, "Audio-Input-Capture");
        captureThread.setDaemon(true);
        captureThread.start();

        System.out.println("Started audio input: " + device.name());
    }

    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        if (captureThread != null) {
            try {
                captureThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (microphone != null) {
            microphone.stop();
            microphone.close();
            microphone = null;
        }

        System.out.println("Stopped audio input");
    }

    private void captureLoop() {
        byte[] buffer = new byte[AppConstants.Audio.BYTES_PER_FRAME];
        int frameCount = 0;

        while (running.get()) {
            try {
                int bytesRead = microphone.read(buffer, 0, buffer.length);

                if (bytesRead == buffer.length) {
                    byte[] processedFrame = processFrame(buffer);

                    // Only send if not muted
                    if (!muted.get() && frameCallback != null) {
                        frameCallback.accept(processedFrame);
                    }
                } else {
                    System.err.println("Partial read: " + bytesRead + " bytes (expected " + buffer.length + ")");
                }

            } catch (Exception e) {
                if (running.get()) {
                    System.err.println("Error capturing audio: " + e.getMessage());
                }
            }
        }
    }

    private byte[] processFrame(byte[] samples) {
        float currentGain = gain.get();

        // Calculate peak level
        float peakLevel = calculatePeakLevel(samples);

        updateLevel(peakLevel);

        // If unity gain, return as-is
        if (currentGain == 1.0f) {
            return samples;
        }

        // Apply gain
        byte[] output = new byte[samples.length];

        for (int i = 0; i < samples.length; i += AppConstants.Audio.BYTES_PER_SAMPLE) {
            short sample = (short) ((samples[i + 1] << 8) | (samples[i] & 0xFF));
            short gained = applyGainToSample(sample, currentGain);

            output[i] = (byte) (gained & 0xFF);
            output[i + 1] = (byte) ((gained >> 8) & 0xFF);
        }

        return output;
    }
}