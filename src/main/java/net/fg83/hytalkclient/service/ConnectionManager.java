// Copyright (C) 2026 fingerguns83
// SPDX-License-Identifier: GPL-3.0-or-later

package net.fg83.hytalkclient.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fg83.hytalkclient.util.AppConstants;
import net.fg83.hytalkclient.util.WindowDimensions;
import net.fg83.hytalkclient.model.ApplicationState;
import net.fg83.hytalkclient.model.VoiceChatPlayer;
import net.fg83.hytalkclient.network.ControlSocketConnection;
import net.fg83.hytalkclient.model.Location;
import net.fg83.hytalkclient.message.MessageBuilder;
import net.fg83.hytalkclient.message.MessageEvent;
import net.fg83.hytalkclient.message.MessageType;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static net.fg83.hytalkclient.HytalkClientApplication.getView;

/**
 * Manages WebSocket connections to the voice chat server.
 * Handles connection lifecycle, message routing, and protocol handshaking.
 */
public class ConnectionManager {
    // Application state containing shared managers and configuration
    private final ApplicationState applicationState;
    // List of callbacks to be notified when messages are received
    private final List<Consumer<MessageEvent>> messageHandlers = new ArrayList<>();

    // Active WebSocket connection to the server
    private ControlSocketConnection connection;

    /**
     * Constructs a new ConnectionManager.
     *
     * @param applicationState the shared application state
     */
    public ConnectionManager(ApplicationState applicationState) {
        this.applicationState = applicationState;
    }

    /**
     * Establishes a WebSocket connection to the specified server.
     *
     * @param serverAddress the hostname or IP address of the server
     * @param serverPort    the port number to connect to
     */
    public void connect(String serverAddress, int serverPort) {
        try {
            // Create WebSocket URI
            URI serverURI = new URI("ws://" + serverAddress + ":" + serverPort);
            // Initialize connection with callback handlers
            connection = new ControlSocketConnection(serverURI, this::handleOpen, this::handleMessage, this::handleError, this::handleClose);
            // Initiate connection
            connection.connect();
        }
        catch (URISyntaxException e) {
            // Show error dialog if URI is malformed
            applicationState.getErrorDialogManager().showError("Connection Error", "Invalid server address: " + e.getMessage());
            reshowConnectionView();
        }


    }

    /**
     * Closes the active connection if one exists.
     */
    public void disconnect() {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    /**
     * Sends a message through the active connection.
     *
     * @param message the message to send
     */
    public void send(String message) {
        if (connection != null && connection.isOpen()) {
            connection.send(message);
        }
    }

    /**
     * Registers a callback to be invoked when messages are received.
     *
     * @param handler the message handler callback
     */
    public void addMessageHandler(Consumer<MessageEvent> handler) {
        messageHandlers.add(handler);
    }

    /**
     * Checks if there is an active connection to the server.
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return connection != null && connection.isOpen();
    }

    /**
     * Handles the connection open event.
     * Sends client version information to the server.
     *
     * @param ignored unused parameter
     */
    private void handleOpen(Void ignored) {
        // Build INFO message with client version
        JsonObject data = new JsonObject();
        data.addProperty("client_version", AppConstants.VERSION);
        connection.send(MessageBuilder.build(MessageType.INFO, data));
    }

    /**
     * Handles incoming messages from the server based on message type.
     *
     * @param type the message type
     * @param data the message payload
     */
    private void handleMessage(MessageType type, JsonElement data) {
        switch (type) {
            case INFO -> {
                // Process server information and validate protocol version
                JsonObject dataObj = data.getAsJsonObject();

                String protocolVersion = dataObj.get("protocol_version").getAsString();
                // Check for protocol mismatch
                if (!protocolVersion.equals(AppConstants.PROTO_VERSION)) {
                    connection.close();
                    applicationState.getErrorDialogManager().showError(
                            "Connection Error",
                            "Invalid protocol version [" + AppConstants.PROTO_VERSION + "]. (Server supports " + protocolVersion + ")");
                }

                // Setup audio UDP connection with server's audio port
                int audioPort = dataObj.get("audio_port").getAsInt();
                applicationState.getAudioNetworkManager().setupUdpClient(connection.getRemoteSocketAddress().getHostName(), audioPort, applicationState);
                // Configure audio attenuation distance
                int attenuationDistance = dataObj.get("attenuation_distance").getAsInt();
                applicationState.getAudioStreamManager().setAttenuationDistance(attenuationDistance);
            }
            case PAIR -> {
                // Process pairing information from server
                JsonObject dataObj = data.getAsJsonObject();
                String code = dataObj.get("code").getAsString();
                // Store pairing code and expiration time
                applicationState.getPairingManager().setPairingCode(code);
                applicationState.getPairingManager().setPairingExpiration(
                        Instant.ofEpochSecond(Long.parseLong(dataObj.get("expires_at").getAsString()))
                );
            }
            case READY -> {
                // Initialize client player with server-provided information
                JsonObject dataObj = data.getAsJsonObject();
                applicationState.getPlayerManager().setClientPlayer(new VoiceChatPlayer(
                        dataObj.get("player_name").getAsString(),
                        UUID.fromString(dataObj.get("player_uuid").getAsString()),
                        true
                ));

                // Send acknowledgment to server
                connection.send(MessageBuilder.build(MessageType.ACK, new JsonObject()));
            }
            case POSITION_DATA -> {
                // Update player positions and recalculate audio attenuation
                JsonArray dataArray = data.getAsJsonArray();
                applicationState.getPlayerManager().updateVoiceChatPlayers(Location.parsePlayerLocations(dataArray));
                applicationState.getAudioStreamManager().updatePlayerAttenuation(applicationState.getPlayerManager().getVoiceChatPlayers());

            }
            case GROUP_DATA -> {
                // Group data handling (placeholder)
            }
            case null -> {
                System.err.println("Received null message type");
            }
            default -> System.err.println("Unknown message type: " + type);
        }

        // Notify all registered handlers
        MessageEvent event = new MessageEvent(type, data);
        messageHandlers.forEach(handler -> handler.accept(event));
    }

    /**
     * Handles connection errors.
     *
     * @param ex the exception that occurred
     */
    private void handleError(Exception ex) {
        applicationState.getErrorDialogManager().showError("Connection Error", "An error occurred: " + ex.getMessage());
        reshowConnectionView();
    }

    /**
     * Handles connection close events.
     *
     * @param code   the close status code
     * @param reason the close reason message
     * @param remote true if closed by server, false if closed by client
     */
    private void handleClose(int code, String reason, boolean remote) {
        // Determine who initiated the close
        String source = remote ? "server" : "client";
        applicationState.getErrorDialogManager().showError(
                "Connection Closed",
                String.format("Connection closed by %s\nCode: %d\nReason: %s", source, code, reason)
        );
        reshowConnectionView();
    }

    /**
     * Navigates back to the connection view after disconnection or error.
     */
    private void reshowConnectionView() {
        try {
            applicationState.getViewNavigationManager().navigateToView(
                    getView("subviews/ConnectionView.fxml"),
                    null,
                    WindowDimensions.CONNECTION_WIDTH,
                    WindowDimensions.CONNECTION_HEIGHT
            );
        }
        catch (IOException e) {
            applicationState.getErrorDialogManager().showError("View Error", "Failed to load view: " + e.getMessage());
        }
    }
}