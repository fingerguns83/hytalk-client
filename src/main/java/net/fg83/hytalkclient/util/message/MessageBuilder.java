package net.fg83.hytalkclient.util.message;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class MessageBuilder {
    private final MessageType type;
    private final Instant timestamp = Instant.now();
    private JsonObject data;

    public MessageBuilder(MessageType type){
        this.type = type;
        this.data = new JsonObject();
    }
    public MessageBuilder(MessageType type, JsonObject data){
        this.type = type;
        this.data = data;
    }
    public MessageBuilder(MessageType type, Map<String, Object> data){
        this.type = type;
        this.data = new JsonObject();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            this.data.addProperty(entry.getKey(), entry.getValue().toString());
        }
    }

    public MessageBuilder add(String key, Object value){
        data.addProperty(key, value.toString());
        return this;
    }
    public MessageBuilder add(Map<String, Object> data){
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            this.data.addProperty(entry.getKey(), entry.getValue().toString());
        }
        return this;
    }
    public String build(){
        return build(type, data);
    }

    public static String build(MessageType type, JsonObject data){
        JsonObject payload = makeHeader(type);
        payload.add("data", data);
        return payload.toString();
    }
    public static String build(MessageType type, JsonArray data){
        JsonObject payload = makeHeader(type);
        payload.add("data", data);
        return payload.toString();
    }
    public static String build(MessageType type, Map<String, Object> data){
        JsonObject payload = makeHeader(type);

        JsonObject dataObj = new JsonObject();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            dataObj.addProperty(entry.getKey(), entry.getValue().toString());
        }
        payload.add("data", dataObj);

        return payload.toString();
    }
    public static JsonObject makeHeader(MessageType type){
        JsonObject header = new JsonObject();
        header.addProperty("type", type.getType());
        header.addProperty("timestamp", Instant.now().getEpochSecond());
        return header;
    }
}

