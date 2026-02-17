package net.fg83.hytalkclient.service;

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

    public AudioDevice getDefaultInputDevice() {
        // Get the system default
        try {
            Mixer.Info info = AudioSystem.getMixerInfo()[0]; // Simplified
            return new AudioDevice("Default Input", info, true);
        } catch (Exception e) {
            return null;
        }
    }

    public AudioDevice getDefaultOutputDevice() {
        try {
            Mixer.Info info = AudioSystem.getMixerInfo()[0]; // Simplified
            return new AudioDevice("Default Output", info, false);
        } catch (Exception e) {
            return null;
        }
    }
}
