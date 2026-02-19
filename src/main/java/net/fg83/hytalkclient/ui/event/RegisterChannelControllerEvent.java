package net.fg83.hytalkclient.ui.event;

import javafx.event.Event;
import javafx.event.EventType;
import net.fg83.hytalkclient.ui.controller.channelstrip.ChannelStripController;

import java.util.UUID;

public class RegisterChannelControllerEvent extends Event {
    public static final EventType<RegisterChannelControllerEvent> REGISTER_CHANNEL_CONTROLLER_EVENT = new EventType<>(Event.ANY, "REGISTER_CHANNEL_CONTROLLER_EVENT");

    private final UUID playerUUID;
    private ChannelStripController controller;
    private boolean isInput = false;
    private boolean isOutput = false;


    public RegisterChannelControllerEvent(UUID playerUUID, ChannelStripController controller, boolean isInput, boolean isOutput) {
        super(REGISTER_CHANNEL_CONTROLLER_EVENT);
        this.playerUUID = playerUUID;
        this.controller = controller;
        this.isInput = isInput;
        this.isOutput = isOutput;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }
    public boolean isInput() { return isInput; }
    public boolean isOutput() { return isOutput; }

    public ChannelStripController getController() { return controller; }
}
