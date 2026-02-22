package net.fg83.hytalkclient.ui.event.mixer;

import javafx.event.Event;
import javafx.event.EventType;

import java.util.UUID;

public class GainChangeEvent extends Event {
    public static final EventType<GainChangeEvent> PLAYER_GAIN_CHANGE_EVENT = new EventType<>(Event.ANY, "PLAYER_GAIN_CHANGE_EVENT");
    public static final EventType<GainChangeEvent> OUTPUT_GAIN_CHANGE_EVENT = new EventType<>(Event.ANY, "OUTPUT_GAIN_CHANGE_EVENT");
    public static final EventType<GainChangeEvent> INPUT_GAIN_CHANGE_EVENT = new EventType<>(Event.ANY, "INPUT_GAIN_CHANGE_EVENT");

    private final UUID playerUUID;
    private final double gainPercentage;

    public GainChangeEvent(UUID playerUUID, double percentage) {
        super(PLAYER_GAIN_CHANGE_EVENT);
        this.playerUUID = playerUUID;
        this.gainPercentage = percentage;
    }
    public GainChangeEvent(EventType<? extends Event> eventType, double percentage) {
        super(eventType);
        this.gainPercentage = percentage;

        playerUUID = null;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }
    public double getGainPercentage() {
        return gainPercentage;
    }
}
