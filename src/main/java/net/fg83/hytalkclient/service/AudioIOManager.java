package net.fg83.hytalkclient.service;

import net.fg83.hytalkclient.util.AppConstants;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;

public class AudioIOManager {

    private AudioDevice selectedInputDevice;
    private AudioDevice selectedOutputDevice;

    private float inputGain = 1.0f;
    private float outputGain = 1.0f;

    public record AudioDevice(
            String name,
            Mixer.Info mixerInfo,
            boolean isInput
    ) {
        @Override
        public String toString() {
            return name;
        }
    }

    public AudioIOManager() {
        // Initialize with system defaults
        this.selectedInputDevice = getDefaultInputDevice();
        this.selectedOutputDevice = getDefaultOutputDevice();
    }

    // === Device Enumeration ===

    public static List<AudioDevice> getInputDevices() {
        return getDevices(true);
    }

    public static List<AudioDevice> getOutputDevices() {
        return getDevices(false);
    }

    private static List<AudioDevice> getDevices(boolean forInput) {
        List<AudioDevice> devices = new ArrayList<>();
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();

        for (Mixer.Info mixerInfo : mixers) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);

            if (forInput) {
                // Check if it has target data lines (input capability)
                Line.Info[] targetLineInfo = mixer.getTargetLineInfo();
                for (Line.Info info : targetLineInfo) {
                    if (info.getLineClass().equals(TargetDataLine.class)) {
                        devices.add(new AudioDevice(
                                mixerInfo.getName(),
                                mixerInfo,
                                true
                        ));
                        break;
                    }
                }
            } else {
                // Check if it has source data lines (output capability)
                Line.Info[] sourceLineInfo = mixer.getSourceLineInfo();
                for (Line.Info info : sourceLineInfo) {
                    if (info.getLineClass().equals(SourceDataLine.class)) {
                        devices.add(new AudioDevice(
                                mixerInfo.getName(),
                                mixerInfo,
                                false
                        ));
                        break;
                    }
                }
            }
        }

        return devices;
    }

    // === Default Device Detection ===

    public static AudioDevice getDefaultInputDevice() {
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, AppConstants.Audio.INPUT_AUDIO_FORMAT);

            if (AudioSystem.isLineSupported(info)) {
                TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
                Mixer.Info mixerInfo = AudioSystem.getMixer(null).getMixerInfo();

                return new AudioDevice(
                        mixerInfo.getName() + " (Default)",
                        mixerInfo,
                        true
                );
            }

            // Fallback: first available input device
            List<AudioDevice> inputs = getInputDevices();
            if (!inputs.isEmpty()) {
                return inputs.getFirst();
            }

        } catch (Exception e) {
            System.err.println("Error getting default input device: " + e.getMessage());
        }

        return null;
    }

    public static AudioDevice getDefaultOutputDevice() {
        try {
            // Stereo output format
            AudioFormat format = new AudioFormat(
                    AppConstants.Audio.SAMPLE_RATE,
                    AppConstants.Audio.BIT_DEPTH,
                    2, // Stereo for output
                    AppConstants.Audio.SIGNED,
                    AppConstants.Audio.BIG_ENDIAN
            );

            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

            if (AudioSystem.isLineSupported(info)) {
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                Mixer.Info mixerInfo = AudioSystem.getMixer(null).getMixerInfo();

                return new AudioDevice(
                        mixerInfo.getName() + " (Default)",
                        mixerInfo,
                        false
                );
            }

            // Fallback: first available output device
            List<AudioDevice> outputs = getOutputDevices();
            if (!outputs.isEmpty()) {
                return outputs.getFirst();
            }

        } catch (Exception e) {
            System.err.println("Error getting default output device: " + e.getMessage());
        }

        return null;
    }

    // === Selected Device Management ===

    public AudioDevice getSelectedInputDevice() {
        if (selectedInputDevice == null) {
            return getDefaultInputDevice();
        }
        return selectedInputDevice;
    }

    public void setSelectedInputDevice(AudioDevice device) {
        this.selectedInputDevice = device;
    }

    public AudioDevice getSelectedOutputDevice() {
        if (selectedOutputDevice == null) {
            return getDefaultOutputDevice();
        }
        return selectedOutputDevice;
    }

    public void setSelectedOutputDevice(AudioDevice device) {
        this.selectedOutputDevice = device;
    }

    // === Gain Management ===

    public float getInputGain() {
        return inputGain;
    }

    public void setInputGain(float gain) {
        this.inputGain = Math.max(0.0f, Math.min(2.0f, gain)); // Clamp 0-2
    }

    public float getOutputGain() {
        return outputGain;
    }

    public void setOutputGain(float gain) {
        this.outputGain = Math.max(0.0f, Math.min(2.0f, gain)); // Clamp 0-2
    }
}