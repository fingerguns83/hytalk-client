// Copyright (C) 2026 fingerguns83
// SPDX-License-Identifier: GPL-3.0-or-later

package net.fg83.hytalkclient.audio;

import net.fg83.hytalkclient.service.AudioIOManager;
import net.fg83.hytalkclient.util.AppConstants;

import javax.sound.sampled.*;
import java.util.function.Consumer;

/**
 * Manages audio input stream from a microphone device.
 * Captures audio data, processes it (gain, mono conversion), and delivers frames via callback.
 */
public class InputAudioStream extends AudioStream {

    // The audio device to capture from
    private final AudioIOManager.AudioDevice device;
    // Callback to deliver processed audio frames
    private final Consumer<byte[]> frameCallback;

    // Java Sound API line for capturing audio
    private TargetDataLine microphone;
    // Background thread that continuously reads audio data
    private Thread captureThread;

    /**
     * Creates a new input audio stream.
     *
     * @param device        the audio device to capture from
     * @param initialGain   the initial gain level to apply
     * @param frameCallback callback invoked for each captured audio frame
     */
    public InputAudioStream(AudioIOManager.AudioDevice device, float initialGain, Consumer<byte[]> frameCallback) {
        this.device = device;
        this.frameCallback = frameCallback;
        setGain(initialGain);
    }

    /**
     * Starts capturing audio from the microphone.
     * Opens the audio line, starts the capture thread, and begins reading audio data.
     *
     * @throws LineUnavailableException if the audio line cannot be opened
     */
    public void start() throws LineUnavailableException {
        // Prevent multiple starts
        if (running.get()) {
            return;
        }

        // Get the audio line info for the target device
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, device.nativeFormat());

        // Get the mixer and open the microphone line
        Mixer mixer = AudioSystem.getMixer(device.mixerInfo());
        microphone = (TargetDataLine) mixer.getLine(info);

        // Set buffer size to hold 2 frames
        int bufferSize = device.getBytesPerFrame() * 2;
        microphone.open(device.nativeFormat(), bufferSize);

        // Log device configuration
        System.out.println("=== Input Audio Stream ===");
        System.out.println("Device: " + device.name());
        System.out.println("Format: " + device.nativeFormat());
        System.out.println("Channels: " + device.getChannels());
        System.out.println("Needs conversion: " + device.requiresStereoToMonoConversion());
        System.out.println("Buffer size: " + microphone.getBufferSize());

        // Begin capturing audio
        microphone.start();

        // Atomically set running flag to true
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Audio capture already started");
        }

        // Start the capture thread
        captureThread = new Thread(this::captureLoop, "Audio-Input-Capture");
        captureThread.setDaemon(true);
        captureThread.start();

        System.out.println("Started audio input");
    }

    /**
     * Stops capturing audio from the microphone.
     * Shuts down the capture thread and closes the audio line.
     */
    public void stop() {
        // Atomically set running flag to false
        if (!running.compareAndSet(true, false)) {
            return;
        }

        // Wait for capture thread to finish
        if (captureThread != null) {
            try {
                captureThread.join(1000);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Close the microphone line
        if (microphone != null) {
            microphone.stop();
            microphone.close();
            microphone = null;
        }

        System.out.println("Stopped audio input");
    }

    /**
     * Main capture loop that continuously reads audio data from the microphone.
     * Runs in a separate thread until stopped.
     */
    private void captureLoop() {
        // Buffer to hold one frame of audio data
        byte[] captureBuffer = new byte[device.getBytesPerFrame()];
        int frameCount = 0;

        while (running.get()) {
            try {
                // Read audio data from microphone
                int bytesRead = microphone.read(captureBuffer, 0, captureBuffer.length);

                // Process only complete frames
                if (bytesRead == captureBuffer.length) {
                    frameCount++;

                    // Convert to mono if device captured stereo
                    byte[] monoFrame = device.requiresStereoToMonoConversion()
                            ? stereoToMono(captureBuffer)
                            : captureBuffer;

                    // Apply gain and update level
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
     * Converts stereo audio samples to mono by averaging left and right channels.
     * Assumes 16-bit PCM samples (2 bytes per sample, 4 bytes per stereo frame).
     *
     * @param stereoSamples stereo audio data
     * @return mono audio data (half the size of input)
     */
    private byte[] stereoToMono(byte[] stereoSamples) {
        // Output will be half the size (one channel instead of two)
        byte[] monoSamples = new byte[stereoSamples.length / 2];

        // Process each stereo frame (4 bytes: 2 for left, 2 for right)
        for (int i = 0, j = 0; i < stereoSamples.length; i += 4, j += 2) {
            // Extract left channel (little-endian 16-bit)
            short left = (short) ((stereoSamples[i + 1] << 8) | (stereoSamples[i] & 0xFF));
            // Extract right channel (little-endian 16-bit)
            short right = (short) ((stereoSamples[i + 3] << 8) | (stereoSamples[i + 2] & 0xFF));
            // Average the two channels
            short mono = (short) ((left + right) / 2);

            // Store as little-endian 16-bit
            monoSamples[j] = (byte) (mono & 0xFF);
            monoSamples[j + 1] = (byte) ((mono >> 8) & 0xFF);
        }

        return monoSamples;
    }

    /**
     * Processes an audio frame by calculating peak level and applying gain.
     *
     * @param samples raw audio samples
     * @return processed audio samples with gain applied
     */
    private byte[] processFrame(byte[] samples) {
        // Get current gain setting
        float currentGain = gain.get();

        // Calculate and update audio level
        float peakLevel = calculatePeakLevel(samples);
        updateLevel(peakLevel);

        // Apply gain to each sample
        byte[] output = new byte[samples.length];
        for (int i = 0; i < samples.length; i += AppConstants.Audio.BYTES_PER_SAMPLE) {
            // Extract 16-bit sample (little-endian)
            short sample = (short) ((samples[i + 1] << 8) | (samples[i] & 0xFF));
            // Apply gain
            short gained = applyGainToSample(sample, currentGain);

            // Store result as little-endian 16-bit
            output[i] = (byte) (gained & 0xFF);
            output[i + 1] = (byte) ((gained >> 8) & 0xFF);
        }

        return output;
    }
}