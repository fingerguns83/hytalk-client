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

public class ConnectionManager {
    private final ApplicationState applicationState;
    private final List<Consumer<MessageEvent>> messageHandlers = new ArrayList<>();

    private ControlSocketConnection connection;

    public ConnectionManager(ApplicationState applicationState) {
        this.applicationState = applicationState;
    }

    public void connect(String serverAddress, int serverPort) {
        try {
            URI serverURI = new URI("ws://" + serverAddress + ":" + serverPort);
            connection = new ControlSocketConnection(serverURI, this::handleOpen, this::handleMessage, this::handleError, this::handleClose);
            connection.connect();
        } catch (URISyntaxException e) {
            applicationState.getErrorDialogManager().showError("Connection Error", "Invalid server address: " + e.getMessage());
        }

        try {
            applicationState.getViewNavigationManager().navigateToView(getView("subviews/ConnectionView.fxml"), null);
        }
        catch (IOException e) {
            applicationState.getErrorDialogManager().showError("View Error", "Failed to load view: " + e.getMessage());
        }
    }

    public void disconnect() {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public void send(String message) {
        if (connection != null && connection.isOpen()) {
            connection.send(message);
        }
    }

    public void addMessageHandler(Consumer<MessageEvent> handler) {
        messageHandlers.add(handler);
    }

    public boolean isConnected() {
        return connection != null && connection.isOpen();
    }

    // Callback from ControlSocketConnection
    private void handleOpen(Void ignored){
        JsonObject data = new JsonObject();
        data.addProperty("client_version", AppConstants.VERSION);
        connection.send(MessageBuilder.build(MessageType.INFO, data));
    }

    private void handleMessage(MessageType type, JsonElement data) {
        // Update application state based on message type
        switch (type) {
            case INFO -> {
                JsonObject dataObj = data.getAsJsonObject();
                System.out.println("Server version: " + dataObj.get("server_version").getAsString());
            }
            case PAIR -> {
                JsonObject dataObj = data.getAsJsonObject();
                String code = dataObj.get("code").getAsString();
                applicationState.getPairingManager().setPairingCode(code);
                applicationState.getPairingManager().setPairingExpiration(
                        Instant.ofEpochSecond(Long.parseLong(dataObj.get("expires_at").getAsString()))
                );
            }
            case READY -> {
                JsonObject dataObj = data.getAsJsonObject();
                applicationState.getPlayerManager().setClientPlayer(new VoiceChatPlayer(
                        dataObj.get("player_name").getAsString(),
                        UUID.fromString(dataObj.get("player_uuid").getAsString()),
                        true
                ));
                connection.send(MessageBuilder.build(MessageType.ACK, new JsonObject()));
            }
            case POSITION_DATA -> {
                JsonArray dataArray = data.getAsJsonArray();
                applicationState.getPlayerManager().updateVoiceChatPlayers(Location.parsePlayerLocations(dataArray));

            }
            case GROUP_DATA, PING, PONG -> {
                // Handle other message types as needed
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

    private void handleError(Exception ex) {
        applicationState.getErrorDialogManager().showError("Connection Error", "An error occurred: " + ex.getMessage());
    }

    private void handleClose(int code, String reason, boolean remote) {
        String source = remote ? "server" : "client";
        applicationState.getErrorDialogManager().showError(
                "Connection Closed",
                String.format("Connection closed by %s\nCode: %d\nReason: %s", source, code, reason)
        );
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