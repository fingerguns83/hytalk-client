package net.fg83.hytalkclient.ui.event.mixer;

import javafx.event.Event;
import javafx.event.EventType;
import net.fg83.hytalkclient.service.AudioIOManager;

/**
 * Custom JavaFX event for handling audio device changes.
 * This event is fired when audio input or output devices are changed in the application.
 */
public class AudioDeviceEvent extends Event {
    /**
     * Base event type for all audio device events.
     */
    public static final EventType<AudioDeviceEvent> AUDIO_DEVICE_EVENT =
            new EventType<>(Event.ANY, "AUDIO_DEVICE_EVENT");

    /**
     * Event type fired when the input audio device is changed.
     */
    public static final EventType<AudioDeviceEvent> INPUT_DEVICE_CHANGED =
            new EventType<>(AUDIO_DEVICE_EVENT, "INPUT_DEVICE_CHANGED");

    /**
     * Event type fired when the output audio device is changed.
     */
    public static final EventType<AudioDeviceEvent> OUTPUT_DEVICE_CHANGED =
            new EventType<>(AUDIO_DEVICE_EVENT, "OUTPUT_DEVICE_CHANGED");

    /**
     * The audio device associated with this event.
     */
    private final AudioIOManager.AudioDevice device;

    /**
     * Constructs a new AudioDeviceEvent.
     *
     * @param eventType the type of audio device event
     * @param device    the audio device that triggered this event
     */
    public AudioDeviceEvent(EventType<? extends AudioDeviceEvent> eventType, AudioIOManager.AudioDevice device) {
        super(eventType);
        this.device = device;
    }

    /**
     * Gets the audio device associated with this event.
     *
     * @return the audio device that triggered this event
     */
    public AudioIOManager.AudioDevice getDevice() {
        return device;
    }
}