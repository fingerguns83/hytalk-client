package net.fg83.hytalkclient.ui.event;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * Custom JavaFX event that carries server connection configuration details.
 * This event is fired when the user initiates a connection setup to a server.
 */
public class ConnectionSetupEvent extends Event {
    // Event type identifier for connection setup events
    public static final EventType<ConnectionSetupEvent> CONNECTION_SETUP_EVENT = new EventType<>(Event.ANY, "CONNECTION_SETUP_EVENT");

    // The server address (hostname or IP) to connect to
    private final String serverAddress;

    // The server port number to connect to
    private final int serverPort;

    /**
     * Constructs a new ConnectionSetupEvent with the specified server details.
     *
     * @param serverAddress the hostname or IP address of the server
     * @param serverPort    the port number of the server
     */
    public ConnectionSetupEvent(String serverAddress, int serverPort) {
        super(CONNECTION_SETUP_EVENT);
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    /**
     * Returns the server address for this connection setup.
     *
     * @return the server address (hostname or IP)
     */
    public String getServerAddress() {
        return serverAddress;
    }

    /**
     * Returns the server port for this connection setup.
     *
     * @return the server port number
     */
    public int getServerPort() {
        return serverPort;
    }
}