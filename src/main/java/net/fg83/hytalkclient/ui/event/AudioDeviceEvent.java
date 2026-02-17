package net.fg83.hytalkclient.ui.event;

import javafx.event.Event;
import javafx.event.EventType;
import net.fg83.hytalkclient.service.AudioIOManager;

public class AudioDeviceEvent extends Event {
    public static final EventType<AudioDeviceEvent> AUDIO_DEVICE_EVENT =
            new EventType<>(Event.ANY, "AUDIO_DEVICE_EVENT");
    public static final EventType<AudioDeviceEvent> INPUT_DEVICE_CHANGED =
            new EventType<>(AUDIO_DEVICE_EVENT, "INPUT_DEVICE_CHANGED");
    public static final EventType<AudioDeviceEvent> OUTPUT_DEVICE_CHANGED =
            new EventType<>(AUDIO_DEVICE_EVENT, "OUTPUT_DEVICE_CHANGED");

    private final AudioIOManager.AudioDevice device;

    public AudioDeviceEvent(EventType<? extends AudioDeviceEvent> eventType, AudioIOManager.AudioDevice device) {
        super(eventType);
        this.device = device;
    }

    public AudioIOManager.AudioDevice getDevice() {
        return device;
    }
}