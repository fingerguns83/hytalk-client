package net.fg83.hytalkclient.ui.event.mixer;

import javafx.event.Event;
import javafx.event.EventType;

import java.util.UUID;

public class ChannelMuteEvent extends Event {
    public static final EventType<ChannelMuteEvent> CHANNEL_MUTE_EVENT = new EventType<>(Event.ANY, "CHANNEL_MUTE_EVENT");

    private final UUID uuid;
    private final boolean isInput;
    private final boolean isOutput;
    private final boolean isMuted;

    public ChannelMuteEvent(UUID uuid, boolean isInput, boolean isOutput, boolean isMuted) {
        super(CHANNEL_MUTE_EVENT);
        this.uuid = uuid;
        this.isInput = isInput;
        this.isOutput = isOutput;
        this.isMuted = isMuted;
    }

    public UUID getUuid() { return this.uuid; }
    public boolean isMuted() { return this.isMuted; }
    public boolean isInput() { return this.isInput; }
    public boolean isOutput() { return this.isOutput; }
}
