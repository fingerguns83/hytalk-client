// Copyright (C) 2026 fingerguns83
// SPDX-License-Identifier: GPL-3.0-or-later

package net.fg83.hytalkclient.ui.event.mixer;

import javafx.event.Event;
import javafx.event.EventType;
import net.fg83.hytalkclient.ui.controller.channelstrip.ChannelStripController;

import java.util.UUID;

/**
 * Custom JavaFX event for registering a channel strip controller in the mixer.
 * This event is fired when a new channel controller needs to be registered with the application.
 */
public class RegisterChannelControllerEvent extends Event {
    /**
     * Event type identifier for channel controller registration events.
     */
    public static final EventType<RegisterChannelControllerEvent> REGISTER_CHANNEL_CONTROLLER_EVENT = new EventType<>(Event.ANY, "REGISTER_CHANNEL_CONTROLLER_EVENT");

    /**
     * The unique identifier of the player associated with this channel.
     */
    private final UUID playerUUID;

    /**
     * The channel strip controller to be registered.
     */
    private final ChannelStripController controller;

    /**
     * Flag indicating whether this channel is an input channel.
     */
    private boolean isInput = false;

    /**
     * Flag indicating whether this channel is an output channel.
     */
    private boolean isOutput = false;


    /**
     * Constructs a new RegisterChannelControllerEvent.
     *
     * @param playerUUID the unique identifier of the player
     * @param controller the channel strip controller to register
     * @param isInput    true if this is an input channel, false otherwise
     * @param isOutput   true if this is an output channel, false otherwise
     */
    public RegisterChannelControllerEvent(UUID playerUUID, ChannelStripController controller, boolean isInput, boolean isOutput) {
        super(REGISTER_CHANNEL_CONTROLLER_EVENT);
        this.playerUUID = playerUUID;
        this.controller = controller;
        this.isInput = isInput;
        this.isOutput = isOutput;
    }

    /**
     * Gets the player UUID associated with this channel.
     *
     * @return the unique identifier of the player
     */
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Checks if this channel is an input channel.
     *
     * @return true if this is an input channel, false otherwise
     */
    public boolean isInput() {
        return isInput;
    }

    /**
     * Checks if this channel is an output channel.
     *
     * @return true if this is an output channel, false otherwise
     */
    public boolean isOutput() {
        return isOutput;
    }

    /**
     * Gets the channel strip controller associated with this event.
     *
     * @return the channel strip controller to be registered
     */
    public ChannelStripController getController() {
        return controller;
    }
}