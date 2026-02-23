// Copyright (C) 2026 fingerguns83
// SPDX-License-Identifier: GPL-3.0-or-later

package net.fg83.hytalkclient.message;

import com.google.gson.JsonElement;

/**
 * Represents a message event with a specific type and associated data payload.
 * Immutable container for passing messages through the application.
 */
public class MessageEvent {
    // The type of message (e.g., INFO, PAIR, POSITION_DATA)
    private final MessageType type;
    // The message data as a JSON element
    private final JsonElement data;

    /**
     * Creates a new message event with the specified type and data.
     *
     * @param type the message type
     * @param data the message data as a JSON element
     */
    public MessageEvent(MessageType type, JsonElement data) {
        this.type = type;
        this.data = data;
    }

    /**
     * Gets the type of this message event.
     *
     * @return the message type
     */
    public MessageType getType() {
        return type;
    }

    /**
     * Gets the data payload of this message event.
     *
     * @return the message data as a JSON element
     */
    public JsonElement getData() {
        return data;
    }
}