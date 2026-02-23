package net.fg83.hytalkclient.ui.event.mixer;

import javafx.event.Event;
import javafx.event.EventType;

import java.util.UUID;

/**
 * Custom JavaFX event for handling channel mute/unmute actions.
 * This event is fired when audio channels (input, output, or player channels) are muted or unmuted.
 */
public class ChannelMuteEvent extends Event {
    /**
     * Event type for channel mute events.
     */
    public static final EventType<ChannelMuteEvent> CHANNEL_MUTE_EVENT = new EventType<>(Event.ANY, "CHANNEL_MUTE_EVENT");

    /**
     * The unique identifier for the channel (player UUID or channel ID).
     */
    private final UUID uuid;

    /**
     * Indicates whether this event applies to an input channel.
     */
    private final boolean isInput;

    /**
     * Indicates whether this event applies to an output channel.
     */
    private final boolean isOutput;

    /**
     * The mute state of the channel (true if muted, false if unmuted).
     */
    private final boolean isMuted;

    /**
     * Constructs a new ChannelMuteEvent.
     *
     * @param uuid     the unique identifier for the channel
     * @param isInput  true if this is an input channel
     * @param isOutput true if this is an output channel
     * @param isMuted  true if the channel is being muted, false if unmuted
     */
    public ChannelMuteEvent(UUID uuid, boolean isInput, boolean isOutput, boolean isMuted) {
        super(CHANNEL_MUTE_EVENT);
        this.uuid = uuid;
        this.isInput = isInput;
        this.isOutput = isOutput;
        this.isMuted = isMuted;
    }

    /**
     * Gets the unique identifier for the channel.
     *
     * @return the channel UUID
     */
    public UUID getUuid() {
        return this.uuid;
    }

    /**
     * Gets the mute state of the channel.
     *
     * @return true if the channel is muted, false otherwise
     */
    public boolean isMuted() {
        return this.isMuted;
    }

    /**
     * Checks if this event applies to an input channel.
     *
     * @return true if this is an input channel event
     */
    public boolean isInput() {
        return this.isInput;
    }

    /**
     * Checks if this event applies to an output channel.
     *
     * @return true if this is an output channel event
     */
    public boolean isOutput() {
        return this.isOutput;
    }
}