package net.fg83.hytalkclient.ui.event.handler;

import net.fg83.hytalkclient.model.ApplicationState;
import net.fg83.hytalkclient.ui.controller.channelstrip.InputChannelStripController;
import net.fg83.hytalkclient.ui.controller.channelstrip.OutputChannelStripController;
import net.fg83.hytalkclient.ui.event.mixer.AudioDeviceEvent;
import net.fg83.hytalkclient.ui.event.mixer.ChannelMuteEvent;
import net.fg83.hytalkclient.ui.event.mixer.GainChangeEvent;
import net.fg83.hytalkclient.ui.event.mixer.RegisterChannelControllerEvent;

import javax.sound.sampled.LineUnavailableException;

public class MixerEventHandler {

    public static void handleControllerRegistration(RegisterChannelControllerEvent event, ApplicationState applicationState) {
        if (event.isInput()) {
            applicationState.getMixerManager().setInputController((InputChannelStripController) event.getController());
        }
        else if (event.isOutput()) {
            applicationState.getMixerManager().setOutputController((OutputChannelStripController) event.getController());

        }
        else {
            applicationState.getMixerManager().addPlayerController(event.getPlayerUUID(), event.getController());
        }
    }

    public static void handlePlayerGainChange(GainChangeEvent event, ApplicationState applicationState) {
        System.out.println("Gain change event received: " + event.getPlayerUUID());
        applicationState.getAudioStreamManager().getPlayerStream(event.getPlayerUUID()).setGain((float) (event.getGainPercentage() * 1.25F));
    }
    public static void handleInputGainChange(GainChangeEvent event, ApplicationState applicationState) {
        applicationState.getAudioStreamManager().getInputStream().setGain((float) (event.getGainPercentage() * 1.25F));
    }
    public static void handleOutputGainChange(GainChangeEvent event, ApplicationState applicationState) {
        applicationState.getAudioStreamManager().getOutputStream().setGain((float) (event.getGainPercentage() * 1.25F));
    }
    public static void handleInputDeviceChange(AudioDeviceEvent event, ApplicationState applicationState){
        applicationState.getAudioStreamManager().getAudioIOManager().setSelectedInputDevice(event.getDevice());
        try {
            applicationState.getAudioStreamManager().restartInput();
        }
        catch (LineUnavailableException e) {
            applicationState.getErrorDialogManager().showError("Audio Error", "Failed to restart audio input: " + e.getMessage());
        }
    }
    public static void handleOutputDeviceChange(AudioDeviceEvent event, ApplicationState applicationState){
        applicationState.getAudioStreamManager().getAudioIOManager().setSelectedOutputDevice(event.getDevice());
        try {
            applicationState.getAudioStreamManager().restartOutput();
        }
        catch (LineUnavailableException e) {
            applicationState.getErrorDialogManager().showError("Audio Error", "Failed to restart audio output: " + e.getMessage());
        }
    }
    public static void handleChannelMuteEvent(ChannelMuteEvent event, ApplicationState applicationState) {
        if (event.isInput()) {
            applicationState.getAudioStreamManager().getInputStream().setMuted(event.isMuted());
            return;
        }
        if (event.isOutput()) {
            applicationState.getAudioStreamManager().getOutputStream().setMuted(event.isMuted());
            return;
        }
        applicationState.getAudioStreamManager().getPlayerStream(event.getUuid()).setMuted(event.isMuted());
    }
}
