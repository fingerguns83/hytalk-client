package net.fg83.hytalkclient.message;

import com.google.gson.JsonElement;

public class MessageEvent {
    private final MessageType type;
    private final JsonElement data;

    public MessageEvent(MessageType type, JsonElement data) {
        this.type = type;
        this.data = data;
    }

    public MessageType getType() {
        return type;
    }

    public JsonElement getData() {
        return data;
    }
}