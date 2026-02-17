package net.fg83.hytalkclient.model;

import java.util.UUID;

public class VoiceChatPlayer {
    private boolean isLocalUser = false;

    private String playerName;
    private UUID playerUUID;
    private float gain = 1.0f;

    private Location playerLocation;

    public VoiceChatPlayer(String playerName, UUID playerUUID) {
        this.playerName = playerName;
        this.playerUUID = playerUUID;
    }
    public VoiceChatPlayer(String playerName, UUID playerUUID, boolean isLocalUser) {
        this(playerName, playerUUID);
        this.isLocalUser = isLocalUser;
    }

    public void setLocalUser(boolean localUser) {
        isLocalUser = localUser;
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
    public Location getPlayerLocation() {
        return playerLocation;
    }

    public double calculateDistance(Location testLocation){
        return Location.calculateDistance(playerLocation, testLocation);
    }
    public float getGain() { return gain; }
    public void setGain(float gain) { this.gain = gain; }
}
