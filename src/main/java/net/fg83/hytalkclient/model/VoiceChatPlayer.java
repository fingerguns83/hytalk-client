package net.fg83.hytalkclient.model;

import java.util.UUID;

public class VoiceChatPlayer {
    private boolean isLocalUser = false;

    private final String playerName;
    private final UUID playerUUID;

    private Location playerLocation;
    private double currentDistance = Double.MAX_VALUE;

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
    public double getCurrentDistance() { return currentDistance;}


    public double calculateDistance(Location testLocation){
        currentDistance = Location.calculateDistance(playerLocation, testLocation);
        return currentDistance;
    }

    public float calculateAttenuation(int attenuationDistance) {
        float normalized = (float) Math.min(currentDistance / attenuationDistance, 1.0);
        double k = 4.0;
        return (float) ((Math.exp(-k * normalized) - Math.exp(-k)) / (1.0 - Math.exp(-k)));
    }
}
