package net.fg83.hytalkclient.ui.event;

import javafx.event.Event;
import javafx.event.EventType;

public class ConnectionSetupEvent extends Event {
    public static final EventType<ConnectionSetupEvent> CONNECTION_SETUP_EVENT = new EventType<>(Event.ANY, "CONNECTION_SETUP_EVENT");
    private final String serverAddress;
    private final int serverPort;


    public ConnectionSetupEvent(String serverAddress, int serverPort) {
        super(CONNECTION_SETUP_EVENT);
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }


}
