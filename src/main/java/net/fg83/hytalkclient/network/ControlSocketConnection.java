// Copyright (C) 2026 fingerguns83
// SPDX-License-Identifier: GPL-3.0-or-later

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
 * WebSocket client connection for controlling the HyTalk client-server communication.
 * Extends WebSocketClient to handle real-time messaging between the client and server.
 * All callbacks are executed on the JavaFX application thread for UI safety.
 */
public class ControlSocketConnection extends WebSocketClient {

    // Callback invoked when the WebSocket connection is successfully opened
    private final Consumer<Void> openCallback;
    // Callback invoked when a message is received, providing the message type and data
    private final BiConsumer<MessageType, JsonElement> messageCallback;
    // Callback invoked when an error occurs during connection or message processing
    private final Consumer<Exception> errorCallback;
    // Callback invoked when the connection is closed, providing close code, reason, and whether it was remote
    private final TriConsumer<Integer, String, Boolean> closeCallback;

    /**
     * Creates a new ControlSocketConnection with the specified server URI and callbacks.
     *
     * @param serverURI       the URI of the WebSocket server to connect to
     * @param openCallback    callback to execute when the connection opens
     * @param messageCallback callback to execute when a message is received
     * @param errorCallback   callback to execute when an error occurs
     * @param closeCallback   callback to execute when the connection closes
     */
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

    /**
     * Called when the WebSocket connection is successfully established.
     * Executes the open callback on the JavaFX application thread.
     *
     * @param handshakedata the server handshake data
     */
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        try {
            // Execute callback on JavaFX thread for UI safety
            Platform.runLater(() -> openCallback.accept(null));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }


        System.out.println("WebSocket connection opened");
    }

    /**
     * Called when a message is received from the WebSocket server.
     * Parses the JSON message, extracts the message type and data, and invokes the message callback.
     *
     * @param message the raw message string received from the server
     */
    @Override
    public void onMessage(String message) {
        try {
            // Parse the incoming JSON message
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            // Extract the message type string
            String typeString = json.get("type").getAsString();
            // Convert the type string to a MessageType enum
            MessageType messageType = MessageType.ValueOf(typeString);
            // Extract the message data payload
            JsonElement data = json.get("data");

            // Validate that the message type is recognized
            if (messageType == null) {
                System.err.println("Unknown message type: " + typeString);
                return;
            }

            // Execute callback on JavaFX thread with the parsed message type and data
            Platform.runLater(() -> messageCallback.accept(messageType, data));

        }
        catch (Exception e) {
            // Log parsing errors and notify the error callback
            System.err.println("Error parsing message: " + e.getMessage());
            Platform.runLater(() -> errorCallback.accept(e));
        }
    }

    /**
     * Called when the WebSocket connection is closed.
     * If the closure was initiated by the remote server, invokes the close callback.
     *
     * @param code   the close code indicating the reason for closure
     * @param reason a human-readable explanation for the closure
     * @param remote true if the closure was initiated by the remote server, false if local
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WebSocket connection closed");
        // Only invoke callback if the connection was closed remotely
        if (remote) {
            Platform.runLater(() -> closeCallback.accept(code, reason, remote));
        }
    }

    /**
     * Called when an error occurs with the WebSocket connection.
     * Logs the error and invokes the error callback on the JavaFX application thread.
     *
     * @param ex the exception that occurred
     */
    @Override
    public void onError(Exception ex) {
        System.err.println("WebSocket error: " + ex.getMessage());
        // Execute error callback on JavaFX thread for UI safety
        Platform.runLater(() -> errorCallback.accept(ex));
    }

    /**
     * Functional interface for consuming three arguments.
     * Used for the close callback to provide close code, reason, and remote flag.
     *
     * @param <T> the type of the first argument
     * @param <U> the type of the second argument
     * @param <V> the type of the third argument
     */
    @FunctionalInterface
    public interface TriConsumer<T, U, V> {
        /**
         * Performs this operation on the given arguments.
         *
         * @param t the first input argument
         * @param u the second input argument
         * @param v the third input argument
         */
        void accept(T t, U u, V v);
    }
}