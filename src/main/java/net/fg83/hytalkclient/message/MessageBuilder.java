// Copyright (C) 2026 fingerguns83
// SPDX-License-Identifier: GPL-3.0-or-later

package net.fg83.hytalkclient.message;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.time.Instant;
import java.util.Map;

/**
 * Builder class for creating JSON-formatted messages with a type and data payload.
 * Provides both instance-based and static methods to construct messages.
 */
public class MessageBuilder {
    // The type of message being built
    private final MessageType type;
    // The data payload as a JSON object
    private final JsonObject data;

    /**
     * Creates a message builder with the specified type and an empty data object.
     *
     * @param type the message type
     */
    public MessageBuilder(MessageType type) {
        this.type = type;
        this.data = new JsonObject();
    }

    /**
     * Creates a message builder with the specified type and existing JSON data.
     *
     * @param type the message type
     * @param data the JSON data object
     */
    public MessageBuilder(MessageType type, JsonObject data) {
        this.type = type;
        this.data = data;
    }

    /**
     * Creates a message builder with the specified type and converts a map to JSON data.
     * All values in the map are converted to strings.
     *
     * @param type the message type
     * @param data the data map to convert
     */
    public MessageBuilder(MessageType type, Map<String, Object> data) {
        this.type = type;
        this.data = new JsonObject();
        // Convert each map entry to a JSON property
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            this.data.addProperty(entry.getKey(), entry.getValue().toString());
        }
    }

    /**
     * Adds a key-value pair to the message data.
     * The value is converted to a string.
     *
     * @param key   the property key
     * @param value the property value
     * @return this builder for method chaining
     */
    public MessageBuilder add(String key, Object value) {
        data.addProperty(key, value.toString());
        return this;
    }

    /**
     * Adds multiple key-value pairs from a map to the message data.
     * All values are converted to strings.
     *
     * @param data the map of properties to add
     * @return this builder for method chaining
     */
    public MessageBuilder add(Map<String, Object> data) {
        // Add each map entry as a JSON property
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            this.data.addProperty(entry.getKey(), entry.getValue().toString());
        }
        return this;
    }

    /**
     * Builds and returns the final message as a JSON string.
     * Uses the type and data set on this builder instance.
     *
     * @return the JSON message string
     */
    public String build() {
        return build(type, data);
    }

    /**
     * Builds a message with the specified type and JSON object data.
     * Creates a message with a header (type and timestamp) and data payload.
     *
     * @param type the message type
     * @param data the JSON data object
     * @return the JSON message string
     */
    public static String build(MessageType type, JsonObject data) {
        // Create header with type and timestamp
        JsonObject payload = makeHeader(type);
        // Add data payload
        payload.add("data", data);
        return payload.toString();
    }

    /**
     * Builds a message with the specified type and JSON array data.
     * Creates a message with a header (type and timestamp) and data payload.
     *
     * @param type the message type
     * @param data the JSON data array
     * @return the JSON message string
     */
    public static String build(MessageType type, JsonArray data) {
        // Create header with type and timestamp
        JsonObject payload = makeHeader(type);
        // Add data payload
        payload.add("data", data);
        return payload.toString();
    }

    /**
     * Builds a message with the specified type and map data.
     * Converts the map to a JSON object where all values are strings.
     *
     * @param type the message type
     * @param data the data map
     * @return the JSON message string
     */
    public static String build(MessageType type, Map<String, Object> data) {
        // Create header with type and timestamp
        JsonObject payload = makeHeader(type);

        // Convert map to JSON object
        JsonObject dataObj = new JsonObject();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            dataObj.addProperty(entry.getKey(), entry.getValue().toString());
        }
        payload.add("data", dataObj);

        return payload.toString();
    }

    /**
     * Creates a JSON header object with message type and current timestamp.
     *
     * @param type the message type
     * @return JSON object containing type and timestamp
     */
    public static JsonObject makeHeader(MessageType type) {
        JsonObject header = new JsonObject();
        // Add message type identifier
        header.addProperty("type", type.getType());
        // Add current Unix timestamp in seconds
        header.addProperty("timestamp", Instant.now().getEpochSecond());
        return header;
    }
}