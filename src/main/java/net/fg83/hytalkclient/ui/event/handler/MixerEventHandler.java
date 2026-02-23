// Copyright (C) 2026 fingerguns83
// SPDX-License-Identifier: GPL-3.0-or-later

package net.fg83.hytalkclient.ui.event.handler;

import net.fg83.hytalkclient.model.ApplicationState;
import net.fg83.hytalkclient.ui.controller.channelstrip.InputChannelStripController;
import net.fg83.hytalkclient.ui.controller.channelstrip.OutputChannelStripController;
import net.fg83.hytalkclient.ui.event.mixer.AudioDeviceEvent;
import net.fg83.hytalkclient.ui.event.mixer.ChannelMuteEvent;
import net.fg83.hytalkclient.ui.event.mixer.GainChangeEvent;
import net.fg83.hytalkclient.ui.event.mixer.RegisterChannelControllerEvent;

import javax.sound.sampled.LineUnavailableException;

/**
 * Handler class for processing mixer-related events in the audio application.
 * Provides static methods to handle controller registration, gain changes, device changes, and mute events.
 */
public class MixerEventHandler {

    /**
     * Handles registration of channel strip controllers with the mixer manager.
     * Routes the controller to the appropriate manager based on channel type (input, output, or player).
     *
     * @param event            the registration event containing controller and channel type information
     * @param applicationState the application state containing the mixer manager
     */
    public static void handleControllerRegistration(RegisterChannelControllerEvent event, ApplicationState applicationState) {
        // Check if this is an input channel controller
        if (event.isInput()) {
            applicationState.getMixerManager().setInputController((InputChannelStripController) event.getController());
        }
        // Check if this is an output channel controller
        else if (event.isOutput()) {
            applicationState.getMixerManager().setOutputController((OutputChannelStripController) event.getController());

        }
        // Otherwise, it's a player channel controller
        else {
            applicationState.getMixerManager().addPlayerController(event.getPlayerUUID(), event.getController());
        }
    }

    /**
     * Handles gain (volume) changes for a player audio stream.
     * Applies a 1.25x multiplier to the gain percentage for audio processing.
     *
     * @param event            the gain change event containing player UUID and new gain percentage
     * @param applicationState the application state containing the audio stream manager
     */
    public static void handlePlayerGainChange(GainChangeEvent event, ApplicationState applicationState) {
        // Set the gain for the player's audio stream with a 1.25x multiplier
        applicationState.getAudioStreamManager().getPlayerStream(event.getPlayerUUID()).setGain((float) (event.getGainPercentage() * 1.25F));
    }

    /**
     * Handles gain (volume) changes for the input audio stream.
     * Applies a 1.25x multiplier to the gain percentage for audio processing.
     *
     * @param event            the gain change event containing the new gain percentage
     * @param applicationState the application state containing the audio stream manager
     */
    public static void handleInputGainChange(GainChangeEvent event, ApplicationState applicationState) {
        // Set the gain for the input stream with a 1.25x multiplier
        applicationState.getAudioStreamManager().getInputStream().setGain((float) (event.getGainPercentage() * 1.25F));
    }

    /**
     * Handles gain (volume) changes for the output audio stream.
     * Applies a 1.25x multiplier to the gain percentage for audio processing.
     *
     * @param event            the gain change event containing the new gain percentage
     * @param applicationState the application state containing the audio stream manager
     */
    public static void handleOutputGainChange(GainChangeEvent event, ApplicationState applicationState) {
        // Set the gain for the output stream with a 1.25x multiplier
        applicationState.getAudioStreamManager().getOutputStream().setGain((float) (event.getGainPercentage() * 1.25F));
    }

    /**
     * Handles changes to the input audio device selection.
     * Updates the selected device and attempts to restart the input stream with the new device.
     * Displays an error dialog if the restart fails.
     *
     * @param event            the audio device event containing the new device selection
     * @param applicationState the application state containing the audio stream manager
     */
    public static void handleInputDeviceChange(AudioDeviceEvent event, ApplicationState applicationState) {
        // Update the selected input device
        applicationState.getAudioStreamManager().getAudioIOManager().setSelectedInputDevice(event.getDevice());
        try {
            // Attempt to restart the input stream with the new device
            applicationState.getAudioStreamManager().restartInput();
        }
        catch (LineUnavailableException e) {
            // Show error dialog if the audio line cannot be opened
            applicationState.getErrorDialogManager().showError("Audio Error", "Failed to restart audio input: " + e.getMessage());
        }
    }

    /**
     * Handles changes to the output audio device selection.
     * Updates the selected device and attempts to restart the output stream with the new device.
     * Displays an error dialog if the restart fails.
     *
     * @param event            the audio device event containing the new device selection
     * @param applicationState the application state containing the audio stream manager
     */
    public static void handleOutputDeviceChange(AudioDeviceEvent event, ApplicationState applicationState) {
        // Update the selected output device
        applicationState.getAudioStreamManager().getAudioIOManager().setSelectedOutputDevice(event.getDevice());
        try {
            // Attempt to restart the output stream with the new device
            applicationState.getAudioStreamManager().restartOutput();
        }
        catch (LineUnavailableException e) {
            // Show error dialog if the audio line cannot be opened
            applicationState.getErrorDialogManager().showError("Audio Error", "Failed to restart audio output: " + e.getMessage());
        }
    }

    /**
     * Handles mute/unmute events for audio channels.
     * Routes the mute state to the appropriate stream based on channel type (input, output, or player).
     *
     * @param event            the channel mute event containing channel type, UUID, and mute state
     * @param applicationState the application state containing the audio stream manager
     */
    public static void handleChannelMuteEvent(ChannelMuteEvent event, ApplicationState applicationState) {
        // Handle input channel mute
        if (event.isInput()) {
            applicationState.getAudioStreamManager().getInputStream().setMuted(event.isMuted());
            return;
        }
        // Handle output channel mute
        if (event.isOutput()) {
            applicationState.getAudioStreamManager().getOutputStream().setMuted(event.isMuted());
            return;
        }
        // Handle player channel mute
        applicationState.getAudioStreamManager().getPlayerStream(event.getUuid()).setMuted(event.isMuted());
    }
}