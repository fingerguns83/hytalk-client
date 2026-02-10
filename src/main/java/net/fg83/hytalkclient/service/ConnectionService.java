package net.fg83.hytalkclient.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fg83.hytalkclient.model.ApplicationState;
import net.fg83.hytalkclient.model.VoiceChatPlayer;
import net.fg83.hytalkclient.network.ControlSocketConnection;
import net.fg83.hytalkclient.util.message.MessageType;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static net.fg83.hytalkclient.HytalkClientApplication.getView;

public class ConnectionService {
    private final ApplicationState applicationState;
    private final ErrorDialogService errorDialogService;
    private final List<Consumer<MessageEvent>> messageHandlers = new ArrayList<>();
    private final ViewNavigationService viewNavigationService;

    private ControlSocketConnection connection;

    public ConnectionService(ApplicationState applicationState, ViewNavigationService viewNavigationService, ErrorDialogService errorDialogService) {
        this.applicationState = applicationState;
        this.viewNavigationService = viewNavigationService;
        this.errorDialogService = errorDialogService;
    }

    public void connect(String serverAddress, int serverPort) {
        try {
            URI serverURI = new URI("ws://" + serverAddress + ":" + serverPort);
            connection = new ControlSocketConnection(serverURI, this::handleMessage, this::handleError, this::handleClose);
            connection.connect();
        } catch (URISyntaxException e) {
            errorDialogService.showError("Connection Error", "Invalid server address: " + e.getMessage());
        }

        try {
            viewNavigationService.navigateToView(getView("subviews/ConnectionView.fxml"), null);
        }
        catch (IOException e) {
            errorDialogService.showError("View Error", "Failed to load view: " + e.getMessage());
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
    private void handleMessage(MessageType type, JsonElement data) {
        // Update application state based on message type
        switch (type) {
            case PAIR -> {
                JsonObject dataObj = data.getAsJsonObject();
                String code = dataObj.get("code").getAsString();
                applicationState.setPairingCode(code);
                applicationState.setPairingExpiration(
                        Instant.ofEpochSecond(Long.parseLong(dataObj.get("expires_at").getAsString()))
                );
            }
            case READY -> {
                JsonObject dataObj = data.getAsJsonObject();
                applicationState.setPlayer(new VoiceChatPlayer(
                        dataObj.get("player_name").getAsString(),
                        UUID.fromString(dataObj.get("player_uuid").getAsString())
                ));
            }
            case POSITION_DATA -> {

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
        errorDialogService.showError("Connection Error", "An error occurred: " + ex.getMessage());
    }

    private void handleClose(int code, String reason, boolean remote) {
        String source = remote ? "server" : "client";
        errorDialogService.showError(
                "Connection Closed",
                String.format("Connection closed by %s\nCode: %d\nReason: %s", source, code, reason)
        );
        try {
            viewNavigationService.navigateToView(getView("subviews/ConnectionView.fxml"), null);
        }
        catch (IOException e) {
            errorDialogService.showError("View Error", "Failed to load view: " + e.getMessage());
        }
    }
}