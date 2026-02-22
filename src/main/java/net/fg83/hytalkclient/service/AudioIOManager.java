
package net.fg83.hytalkclient.service;

import net.fg83.hytalkclient.util.AppConstants;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;

public class AudioIOManager {

    private final PreferenceManager preferenceManager;

    private AudioDevice selectedInputDevice;
    private AudioDevice selectedOutputDevice;

    private float inputGain = 1.0f;
    private float outputGain = 1.0f;

    /**
     * Represents an audio device with its capabilities
     */
    public record AudioDevice(
            String name,
            Mixer.Info mixerInfo,
            boolean isInput,
            AudioFormat nativeFormat,
            boolean requiresStereoToMonoConversion
    ) {
        @Override
        public String toString() {
            return name;
        }

        /**
         * Get the number of bytes per frame for this device
         */
        public int getBytesPerFrame() {
            return nativeFormat.getFrameSize() * AppConstants.Audio.FRAME_SIZE;
        }

        /**
         * Get the number of channels this device uses
         */
        public int getChannels() {
            return nativeFormat.getChannels();
        }
    }

    public AudioIOManager(PreferenceManager preferenceManager) {
        this.preferenceManager = preferenceManager;

        // Try to restore saved devices, fall back to defaults
        this.selectedInputDevice = restoreSavedInputDevice();
        this.selectedOutputDevice = restoreSavedOutputDevice();

        // Restore saved gains
        this.inputGain = preferenceManager.getInputGain();
        this.outputGain = preferenceManager.getOutputGain();

        System.out.println("AudioIOManager initialized with saved preferences");
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
            if (mixerInfo.getName().toLowerCase().contains("default")){
                continue;
            }

            if (forInput) {
                Line.Info[] targetLineInfo = mixer.getTargetLineInfo();
                for (Line.Info info : targetLineInfo) {
                    if (info.getLineClass().equals(TargetDataLine.class)) {
                        // Probe what format this device actually supports
                        AudioFormat format = probeBestInputFormat(mixer);
                        if (format != null) {
                            boolean needsConversion = format.getChannels() > 1;
                            devices.add(new AudioDevice(
                                    mixerInfo.getName(),
                                    mixerInfo,
                                    true,
                                    format,
                                    needsConversion
                            ));
                        }
                        break;
                    }
                }
            } else {
                Line.Info[] sourceLineInfo = mixer.getSourceLineInfo();
                for (Line.Info info : sourceLineInfo) {
                    if (info.getLineClass().equals(SourceDataLine.class)) {
                        AudioFormat format = probeBestOutputFormat(mixer);
                        if (format != null) {
                            devices.add(new AudioDevice(
                                    mixerInfo.getName(),
                                    mixerInfo,
                                    false,
                                    format,
                                    false // Output doesn't need conversion
                            ));
                        }
                        break;
                    }
                }
            }
        }

        return devices;
    }

    private static AudioFormat probeBestInputFormat(Mixer mixer) {
        // Try mono first (ideal)
        AudioFormat monoFormat = new AudioFormat(
                AppConstants.Audio.SAMPLE_RATE,
                AppConstants.Audio.BIT_DEPTH,
                1,
                AppConstants.Audio.SIGNED,
                AppConstants.Audio.BIG_ENDIAN
        );

        DataLine.Info monoInfo = new DataLine.Info(TargetDataLine.class, monoFormat);
        if (mixer.isLineSupported(monoInfo)) {
            return monoFormat;
        }

        // Fall back to stereo
        AudioFormat stereoFormat = new AudioFormat(
                AppConstants.Audio.SAMPLE_RATE,
                AppConstants.Audio.BIT_DEPTH,
                2,
                AppConstants.Audio.SIGNED,
                AppConstants.Audio.BIG_ENDIAN
        );

        DataLine.Info stereoInfo = new DataLine.Info(TargetDataLine.class, stereoFormat);
        if (mixer.isLineSupported(stereoInfo)) {
            return stereoFormat;
        }

        return null; // Device doesn't support our requirements
    }

    private static AudioFormat probeBestOutputFormat(Mixer mixer) {
        // Output is always stereo
        AudioFormat stereoFormat = new AudioFormat(
                AppConstants.Audio.SAMPLE_RATE,
                AppConstants.Audio.BIT_DEPTH,
                2,
                AppConstants.Audio.SIGNED,
                AppConstants.Audio.BIG_ENDIAN
        );

        DataLine.Info stereoInfo = new DataLine.Info(SourceDataLine.class, stereoFormat);
        if (mixer.isLineSupported(stereoInfo)) {
            return stereoFormat;
        }

        return null;
    }

    // === Default Device Detection ===
    public static AudioDevice getDefaultInputDevice() {
        try {
            Mixer.Info defaultMixerInfo = AudioSystem.getMixer(null).getMixerInfo();
            Mixer mixer = AudioSystem.getMixer(defaultMixerInfo);

            AudioFormat format = probeBestInputFormat(mixer);
            if (format != null) {
                boolean needsConversion = format.getChannels() > 1;
                return new AudioDevice(
                        "Default In",
                        defaultMixerInfo,
                        true,
                        format,
                        needsConversion
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
            Mixer.Info defaultMixerInfo = AudioSystem.getMixer(null).getMixerInfo();
            Mixer mixer = AudioSystem.getMixer(defaultMixerInfo);

            AudioFormat format = probeBestOutputFormat(mixer);
            if (format != null) {
                return new AudioDevice(
                        "Default Out",
                        defaultMixerInfo,
                        false,
                        format,
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

    private AudioDevice restoreSavedInputDevice() {
        String savedName = preferenceManager.getInputDevice();

        if (savedName != null) {
            // Try to find the device by name
            List<AudioDevice> devices = getInputDevices();
            for (AudioDevice device : devices) {
                if (device.name().equals(savedName)) {
                    System.out.println("Restored input device: " + savedName);
                    return device;
                }
            }
            System.out.println("Saved input device not found: " + savedName + ", using default");
        }
        return getDefaultInputDevice();
    }

    private AudioDevice restoreSavedOutputDevice() {
        String savedName = preferenceManager.getOutputDevice();

        if (savedName != null) {
            // Try to find the device by name
            List<AudioDevice> devices = getOutputDevices();
            for (AudioDevice device : devices) {
                if (device.name().equals(savedName)) {
                    System.out.println("Restored output device: " + savedName);
                    return device;
                }
            }
            System.out.println("Saved output device not found: " + savedName + ", using default");
        }

        return getDefaultOutputDevice();
    }

    public AudioDevice getSelectedInputDevice() {
        if (selectedInputDevice == null) {
            return getDefaultInputDevice();
        }
        return selectedInputDevice;
    }

    public void setSelectedInputDevice(AudioDevice device) {
        this.selectedInputDevice = device;
        if (device != null) {
            preferenceManager.saveInputDevice(device.name());
        }
    }

    public AudioDevice getSelectedOutputDevice() {
        if (selectedOutputDevice == null) {
            return getDefaultOutputDevice();
        }
        return selectedOutputDevice;
    }

    public void setSelectedOutputDevice(AudioDevice device) {
        this.selectedOutputDevice = device;
        if (device != null) {
            preferenceManager.saveOutputDevice(device.name());
        }
    }

    // === Gain Management ===

    public float getInputGain() {
        return inputGain;
    }

    public void setInputGain(float gain) {
        this.inputGain = Math.max(0.0f, Math.min(1.25f, gain));
        preferenceManager.saveInputGain(this.inputGain);
    }

    public float getOutputGain() {
        return outputGain;
    }

    public void setOutputGain(float gain) {
        this.outputGain = Math.max(0.0f, Math.min(1.25f, gain));
        preferenceManager.saveOutputGain(this.outputGain);
    }
}