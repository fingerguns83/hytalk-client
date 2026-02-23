// Copyright (C) 2026 fingerguns83
// SPDX-License-Identifier: GPL-3.0-or-later

package net.fg83.hytalkclient.message;

/**
 * Enumeration of message types used in the HyTalk client-server communication protocol.
 * Each type represents a different kind of message that can be exchanged.
 */
public enum MessageType {
    // Initial information message
    INFO("info"),
    // Device pairing request/response
    PAIR("pair"),
    // Acknowledgment message
    ACK("ack"),
    // Ready state notification
    READY("ready"),
    // Player position update data
    POSITION_DATA("position_data"),
    // Group/party information data
    GROUP_DATA("group_data");

    // The string identifier for this message type
    private final String type;

    /**
     * Creates a message type with the specified string identifier.
     *
     * @param type the string identifier for this message type
     */
    MessageType(String type) {
        this.type = type;
    }

    /**
     * Finds a MessageType enum constant by its string identifier.
     * Case-sensitive comparison is used.
     *
     * @param type the string identifier to search for
     * @return the matching MessageType, or null if no match is found
     */
    public static MessageType ValueOf(String type) {
        // Iterate through all enum constants
        for (MessageType m : MessageType.values()) {
            // Check if the type matches
            if (m.getType().equals(type)) {
                return m;
            }
        }
        // No match found
        return null;
    }

    /**
     * Gets the string identifier for this message type.
     *
     * @return the string identifier
     */
    public String getType() {
        return type;
    }
}