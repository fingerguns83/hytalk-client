// Copyright (C) 2026 fingerguns83
// SPDX-License-Identifier: GPL-3.0-or-later

package net.fg83.hytalkclient.service;

import net.fg83.hytalkclient.util.AppConstants;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages audio input/output devices and their configurations.
 * Handles device enumeration, selection, and gain control for audio streaming.
 */
public class AudioIOManager {

    // Currently selected audio input device (microphone)
    private AudioDevice selectedInputDevice;
    // Currently selected audio output device (speakers/headphones)
    private AudioDevice selectedOutputDevice;

    // Gain multiplier for input audio (0.0 to 1.25)
    private float inputGain = 1.0f;
    // Gain multiplier for output audio (0.0 to 1.25)
    private float outputGain = 1.0f;

    /**
     * Represents an audio device with its configuration and capabilities.
     *
     * @param name                           Human-readable name of the device
     * @param mixerInfo                      System mixer information for the device
     * @param isInput                        True if this is an input device, false for output
     * @param nativeFormat                   The audio format supported by this device
     * @param requiresStereoToMonoConversion True if stereo input needs to be converted to mono
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
         * Calculates the total bytes per audio frame buffer.
         *
         * @return Number of bytes needed for one frame buffer
         */
        public int getBytesPerFrame() {
            return nativeFormat.getFrameSize() * AppConstants.Audio.FRAME_SIZE;
        }

        /**
         * Gets the number of audio channels for this device.
         *
         * @return Number of channels (1 for mono, 2 for stereo)
         */
        public int getChannels() {
            return nativeFormat.getChannels();
        }
    }

    /**
     * Constructs an AudioIOManager and restores saved device preferences.
     */
    public AudioIOManager() {

        // Restore previously selected devices from preferences
        this.selectedInputDevice = restoreSavedInputDevice();
        this.selectedOutputDevice = restoreSavedOutputDevice();

        // Restore saved gain values
        this.inputGain = PreferenceManager.getInputGain();
        this.outputGain = PreferenceManager.getOutputGain();

        System.out.println("AudioIOManager initialized with saved preferences");
    }

    /**
     * Retrieves all available audio input devices.
     *
     * @return List of available input devices
     */
    public static List<AudioDevice> getInputDevices() {
        return getDevices(true);
    }

    /**
     * Retrieves all available audio output devices.
     *
     * @return List of available output devices
     */
    public static List<AudioDevice> getOutputDevices() {
        return getDevices(false);
    }

    /**
     * Enumerates audio devices from the system.
     *
     * @param forInput True to get input devices, false for output devices
     * @return List of audio devices matching the requested type
     */
    private static List<AudioDevice> getDevices(boolean forInput) {
        List<AudioDevice> devices = new ArrayList<>();
        // Get all available audio mixers from the system
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();

        for (Mixer.Info mixerInfo : mixers) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            // Skip default device entries (we handle them separately)
            if (mixerInfo.getName().toLowerCase().contains("default")) {
                continue;
            }

            if (forInput) {
                // Check for input capabilities (TargetDataLine)
                Line.Info[] targetLineInfo = mixer.getTargetLineInfo();
                for (Line.Info info : targetLineInfo) {
                    if (info.getLineClass().equals(TargetDataLine.class)) {
                        // Find the best supported audio format for this input device
                        AudioFormat format = probeBestInputFormat(mixer);
                        if (format != null) {
                            // Stereo inputs need conversion to mono for transmission
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
            }
            else {
                // Check for output capabilities (SourceDataLine)
                Line.Info[] sourceLineInfo = mixer.getSourceLineInfo();
                for (Line.Info info : sourceLineInfo) {
                    if (info.getLineClass().equals(SourceDataLine.class)) {
                        // Find the best supported audio format for this output device
                        AudioFormat format = probeBestOutputFormat(mixer);
                        if (format != null) {
                            devices.add(new AudioDevice(
                                    mixerInfo.getName(),
                                    mixerInfo,
                                    false,
                                    format,
                                    false
                            ));
                        }
                        break;
                    }
                }
            }
        }

        return devices;
    }

    /**
     * Probes a mixer to find the best supported input audio format.
     * Prefers mono, falls back to stereo if mono is not supported.
     *
     * @param mixer The audio mixer to probe
     * @return Best supported AudioFormat, or null if none supported
     */
    private static AudioFormat probeBestInputFormat(Mixer mixer) {
        // Try mono format first (preferred for voice input)
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

        // Fall back to stereo format if mono is not supported
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

        return null;
    }

    /**
     * Probes a mixer to find the best supported output audio format.
     * Uses stereo format for output devices.
     *
     * @param mixer The audio mixer to probe
     * @return Best supported AudioFormat, or null if none supported
     */
    private static AudioFormat probeBestOutputFormat(Mixer mixer) {
        // Output devices use stereo format
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

    /**
     * Gets the system's default input device.
     *
     * @return Default input AudioDevice, or null if none available
     */
    public static AudioDevice getDefaultInputDevice() {
        try {
            // Get the system default mixer
            Mixer.Info defaultMixerInfo = AudioSystem.getMixer(null).getMixerInfo();
            Mixer mixer = AudioSystem.getMixer(defaultMixerInfo);

            // Probe for supported format
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

            // Fall back to first available input device if default doesn't work
            List<AudioDevice> inputs = getInputDevices();
            if (!inputs.isEmpty()) {
                return inputs.getFirst();
            }

        }
        catch (Exception e) {
            System.err.println("Error getting default input device: " + e.getMessage());
        }

        return null;
    }

    /**
     * Gets the system's default output device.
     *
     * @return Default output AudioDevice, or null if none available
     */
    public static AudioDevice getDefaultOutputDevice() {
        try {
            // Get the system default mixer
            Mixer.Info defaultMixerInfo = AudioSystem.getMixer(null).getMixerInfo();
            Mixer mixer = AudioSystem.getMixer(defaultMixerInfo);

            // Probe for supported format
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

            // Fall back to first available output device if default doesn't work
            List<AudioDevice> outputs = getOutputDevices();
            if (!outputs.isEmpty()) {
                return outputs.getFirst();
            }

        }
        catch (Exception e) {
            System.err.println("Error getting default output device: " + e.getMessage());
        }

        return null;
    }

    /**
     * Restores the previously saved input device from preferences.
     * Falls back to default device if saved device is not found.
     *
     * @return The restored or default input device
     */
    private AudioDevice restoreSavedInputDevice() {
        String savedName = PreferenceManager.getInputDevice();

        if (savedName != null) {
            // Search for the saved device in available devices
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

    /**
     * Restores the previously saved output device from preferences.
     * Falls back to default device if saved device is not found.
     *
     * @return The restored or default output device
     */
    private AudioDevice restoreSavedOutputDevice() {
        String savedName = PreferenceManager.getOutputDevice();

        if (savedName != null) {
            // Search for the saved device in available devices
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

    /**
     * Gets the currently selected input device.
     * Returns default device if none is selected.
     *
     * @return The selected input device
     */
    public AudioDevice getSelectedInputDevice() {
        if (selectedInputDevice == null) {
            return getDefaultInputDevice();
        }
        return selectedInputDevice;
    }

    /**
     * Sets the selected input device and saves it to preferences.
     *
     * @param device The device to select, or null to clear selection
     */
    public void setSelectedInputDevice(AudioDevice device) {
        this.selectedInputDevice = device;
        if (device != null) {
            PreferenceManager.saveInputDevice(device.name());
        }
    }

    /**
     * Gets the currently selected output device.
     * Returns default device if none is selected.
     *
     * @return The selected output device
     */
    public AudioDevice getSelectedOutputDevice() {
        if (selectedOutputDevice == null) {
            return getDefaultOutputDevice();
        }
        return selectedOutputDevice;
    }

    /**
     * Sets the selected output device and saves it to preferences.
     *
     * @param device The device to select, or null to clear selection
     */
    public void setSelectedOutputDevice(AudioDevice device) {
        this.selectedOutputDevice = device;
        if (device != null) {
            PreferenceManager.saveOutputDevice(device.name());
        }
    }

    /**
     * Gets the current input gain multiplier.
     *
     * @return Input gain value (0.0 to 1.25)
     */
    public float getInputGain() {
        return inputGain;
    }

    /**
     * Sets the input gain multiplier and saves it to preferences.
     * Value is clamped between 0.0 and 1.25.
     *
     * @param gain The desired gain multiplier
     */
    public void setInputGain(float gain) {
        // Clamp gain to valid range
        this.inputGain = Math.max(0.0f, Math.min(1.25f, gain));
        PreferenceManager.saveInputGain(this.inputGain);
    }

    /**
     * Gets the current output gain multiplier.
     *
     * @return Output gain value (0.0 to 1.25)
     */
    public float getOutputGain() {
        return outputGain;
    }

    /**
     * Sets the output gain multiplier and saves it to preferences.
     * Value is clamped between 0.0 and 1.25.
     *
     * @param gain The desired gain multiplier
     */
    public void setOutputGain(float gain) {
        // Clamp gain to valid range
        this.outputGain = Math.max(0.0f, Math.min(1.25f, gain));
        PreferenceManager.saveOutputGain(this.outputGain);
    }
}