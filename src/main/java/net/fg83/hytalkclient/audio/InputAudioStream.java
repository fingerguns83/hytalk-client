
package net.fg83.hytalkclient.audio;

import net.fg83.hytalkclient.service.AudioIOManager;
import net.fg83.hytalkclient.util.AppConstants;

import javax.sound.sampled.*;
import java.util.function.Consumer;

/**
 * Captures audio from the microphone and provides mono PCM frames for encoding.
 * Hardware format negotiation is handled by AudioIOManager.
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

        // Use the format that AudioIOManager determined the device supports
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, device.nativeFormat());

        Mixer mixer = AudioSystem.getMixer(device.mixerInfo());
        microphone = (TargetDataLine) mixer.getLine(info);

        int bufferSize = device.getBytesPerFrame() * 2;
        microphone.open(device.nativeFormat(), bufferSize);

        System.out.println("=== Input Audio Stream ===");
        System.out.println("Device: " + device.name());
        System.out.println("Format: " + device.nativeFormat());
        System.out.println("Channels: " + device.getChannels());
        System.out.println("Needs conversion: " + device.requiresStereoToMonoConversion());
        System.out.println("Buffer size: " + microphone.getBufferSize());

        microphone.start();

        System.out.println("Microphone active: " + microphone.isActive());

        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Audio capture already started");
        }

        captureThread = new Thread(this::captureLoop, "Audio-Input-Capture");
        captureThread.setDaemon(true);
        captureThread.start();

        System.out.println("Started audio input");
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
        byte[] captureBuffer = new byte[device.getBytesPerFrame()];
        int frameCount = 0;

        while (running.get()) {
            try {
                int bytesRead = microphone.read(captureBuffer, 0, captureBuffer.length);

                if (bytesRead == captureBuffer.length) {
                    frameCount++;

                    // Convert to mono if device captured stereo
                    byte[] monoFrame = device.requiresStereoToMonoConversion()
                            ? stereoToMono(captureBuffer)
                            : captureBuffer;

                    byte[] processedFrame = processFrame(monoFrame);

                    // Only send if not muted
                    if (!muted.get() && frameCallback != null) {
                        frameCallback.accept(processedFrame);
                    }
                } else {
                    System.err.println("Partial read: " + bytesRead + "/" + captureBuffer.length + " bytes");
                }

            } catch (Exception e) {
                if (running.get()) {
                    System.err.println("Error capturing audio: " + e.getMessage());
                }
            }
        }

        System.out.println("Capture loop exited after " + frameCount + " frames");
    }

    /**
     * Convert stereo PCM to mono by averaging channels
     */
    private byte[] stereoToMono(byte[] stereoSamples) {
        byte[] monoSamples = new byte[stereoSamples.length / 2];

        for (int i = 0, j = 0; i < stereoSamples.length; i += 4, j += 2) {
            short left = (short) ((stereoSamples[i + 1] << 8) | (stereoSamples[i] & 0xFF));
            short right = (short) ((stereoSamples[i + 3] << 8) | (stereoSamples[i + 2] & 0xFF));
            short mono = (short) ((left + right) / 2);

            monoSamples[j] = (byte) (mono & 0xFF);
            monoSamples[j + 1] = (byte) ((mono >> 8) & 0xFF);
        }

        return monoSamples;
    }

    private byte[] processFrame(byte[] samples) {
        float currentGain = gain.get();

        // Calculate peak level and update meter
        float peakLevel = calculatePeakLevel(samples);
        updateLevel(peakLevel);

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