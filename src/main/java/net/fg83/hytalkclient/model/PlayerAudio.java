package net.fg83.hytalkclient.model;

import javafx.scene.layout.VBox;

import java.util.UUID;

public class PlayerAudio {
    private VBox channelStrip;

    private String playerName;
    private UUID playerUUID;

    public PlayerAudio(String playerName, UUID playerUUID) {

    }

    public void setChannelStrip(VBox channelStrip) {
        this.channelStrip = channelStrip;
    }
}
