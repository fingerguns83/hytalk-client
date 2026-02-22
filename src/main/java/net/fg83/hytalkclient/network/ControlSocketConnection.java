package net.fg83.hytalkclient.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import net.fg83.hytalkclient.message.MessageType;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A WebSocket client implementation that facilitates communication with a server
 * using a control socket. This class extends {@link WebSocketClient} and manages
 * handling WebSocket events such as opening connections, receiving messages,
 * handling errors, and closing connections.
 *
 * This implementation is designed to support callbacks for handling incoming
 * messages, errors, and connection closures.
 */
public class ControlSocketConnection extends WebSocketClient {

    private final Consumer<Void> openCallback;
    private final BiConsumer<MessageType, JsonElement> messageCallback;
    private final Consumer<Exception> errorCallback;
    private final TriConsumer<Integer, String, Boolean> closeCallback;

    public ControlSocketConnection(
            URI serverURI,
            Consumer<Void> openCallback,
            BiConsumer<MessageType, JsonElement> messageCallback,
            Consumer<Exception> errorCallback,
            TriConsumer<Integer, String, Boolean> closeCallback
    ) {
        super(serverURI);
        this.openCallback = openCallback;
        this.messageCallback = messageCallback;
        this.errorCallback = errorCallback;
        this.closeCallback = closeCallback;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        try{
            Platform.runLater(() -> openCallback.accept(null));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        System.out.println("WebSocket connection opened");
    }

    @Override
    public void onMessage(String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String typeString = json.get("type").getAsString();
            MessageType messageType = MessageType.ValueOf(typeString);
            JsonElement data = json.get("data");

            if (messageType == null) {
                System.err.println("Unknown message type: " + typeString);
                return;
            }

            // Invoke callback on JavaFX Application Thread
            Platform.runLater(() -> messageCallback.accept(messageType, data));

        } catch (Exception e) {
            System.err.println("Error parsing message: " + e.getMessage());
            Platform.runLater(() -> errorCallback.accept(e));
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WebSocket connection closed");
        if (remote){
            Platform.runLater(() -> closeCallback.accept(code, reason, remote));
        }
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("WebSocket error: " + ex.getMessage());
        Platform.runLater(() -> errorCallback.accept(ex));
    }

    @FunctionalInterface
    public interface TriConsumer<T, U, V> {
        void accept(T t, U u, V v);
    }
}