package net.fg83.hytalkclient.model;

import javafx.scene.layout.VBox;
import net.fg83.hytalkclient.util.Location;

import java.util.UUID;

public class VoiceChatPlayer {
    private VBox channelStrip;

    private boolean isLocalUser = false;

    private String playerName;
    private UUID playerUUID;

    private Location playerLocation;

    public VoiceChatPlayer(String playerName, UUID playerUUID) {
        this.playerName = playerName;
        this.playerUUID = playerUUID;
    }

    public void setLocalUser(boolean localUser) {
        isLocalUser = localUser;
    }
    public void setChannelStrip(VBox channelStrip) {
        this.channelStrip = channelStrip;
    }
    public void setPlayerLocation(Location playerLocation) {
        this.playerLocation = playerLocation;
    }

    public String getPlayerName() {
        return playerName;
    }
    public UUID getPlayerId() {
        return playerUUID;
    }
    public boolean isLocalUser() {
        return isLocalUser;
    }
    public VBox getChannelStrip() {
        return channelStrip;
    }
    public Location getPlayerLocation() {
        return playerLocation;
    }

    public double calculateDistance(Location testLocation){
        return Location.calculateDistance(playerLocation, testLocation);
    }
}
