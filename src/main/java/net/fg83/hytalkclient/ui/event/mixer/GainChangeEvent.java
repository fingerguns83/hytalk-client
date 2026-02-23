// Copyright (C) 2026 fingerguns83
// SPDX-License-Identifier: GPL-3.0-or-later

package net.fg83.hytalkclient.ui.event.mixer;

import javafx.event.Event;
import javafx.event.EventType;

import java.util.UUID;

/**
 * Custom JavaFX event for handling audio gain (volume) changes.
 * This event is fired when the gain level changes for player channels, input devices, or output devices.
 */
public class GainChangeEvent extends Event {
    /**
     * Event type fired when a player channel's gain is changed.
     */
    public static final EventType<GainChangeEvent> PLAYER_GAIN_CHANGE_EVENT = new EventType<>(Event.ANY, "PLAYER_GAIN_CHANGE_EVENT");

    /**
     * Event type fired when the output device gain is changed.
     */
    public static final EventType<GainChangeEvent> OUTPUT_GAIN_CHANGE_EVENT = new EventType<>(Event.ANY, "OUTPUT_GAIN_CHANGE_EVENT");

    /**
     * Event type fired when the input device gain is changed.
     */
    public static final EventType<GainChangeEvent> INPUT_GAIN_CHANGE_EVENT = new EventType<>(Event.ANY, "INPUT_GAIN_CHANGE_EVENT");

    /**
     * The UUID of the player associated with this gain change.
     * Null for input/output device gain changes.
     */
    private final UUID playerUUID;

    /**
     * The new gain value as a percentage (0.0 to 100.0).
     */
    private final double gainPercentage;

    /**
     * Constructs a new GainChangeEvent for a player channel.
     *
     * @param playerUUID the UUID of the player whose gain is changing
     * @param percentage the new gain percentage value
     */
    public GainChangeEvent(UUID playerUUID, double percentage) {
        super(PLAYER_GAIN_CHANGE_EVENT);
        this.playerUUID = playerUUID;
        this.gainPercentage = percentage;
    }

    /**
     * Constructs a new GainChangeEvent for input or output devices.
     *
     * @param eventType  the type of gain change event (INPUT or OUTPUT)
     * @param percentage the new gain percentage value
     */
    public GainChangeEvent(EventType<? extends Event> eventType, double percentage) {
        super(eventType);
        this.gainPercentage = percentage;
        // No player UUID for device gain changes
        playerUUID = null;
    }

    /**
     * Gets the UUID of the player associated with this gain change.
     *
     * @return the player UUID, or null if this is a device gain change
     */
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Gets the new gain percentage value.
     *
     * @return the gain percentage
     */
    public double getGainPercentage() {
        return gainPercentage;
    }
}