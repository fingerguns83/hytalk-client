package net.fg83.hytalkclient.model;

import java.util.UUID;

/**
 * Represents a player in the voice chat system.
 * Tracks player identity, location, and calculates distance-based audio attenuation.
 */
public class VoiceChatPlayer {
    // Flag indicating whether this player is the local user
    private boolean isLocalUser = false;

    // The display name of the player
    private final String playerName;
    // The unique identifier for the player
    private final UUID playerUUID;

    // The current location of the player in the world
    private Location playerLocation;
    // The last calculated distance to another location, initialized to maximum value
    private double currentDistance = Double.MAX_VALUE;

    /**
     * Creates a new VoiceChatPlayer with the specified name and UUID.
     *
     * @param playerName the display name of the player
     * @param playerUUID the unique identifier for the player
     */
    public VoiceChatPlayer(String playerName, UUID playerUUID) {
        this.playerName = playerName;
        this.playerUUID = playerUUID;
    }

    /**
     * Creates a new VoiceChatPlayer with the specified name, UUID, and local user flag.
     *
     * @param playerName  the display name of the player
     * @param playerUUID  the unique identifier for the player
     * @param isLocalUser whether this player is the local user
     */
    public VoiceChatPlayer(String playerName, UUID playerUUID, boolean isLocalUser) {
        this(playerName, playerUUID);
        this.isLocalUser = isLocalUser;
    }

    /**
     * Sets whether this player is the local user.
     *
     * @param localUser true if this is the local user, false otherwise
     */
    public void setLocalUser(boolean localUser) {
        isLocalUser = localUser;
    }

    /**
     * Updates the player's location in the world.
     *
     * @param playerLocation the new location of the player
     */
    public void setPlayerLocation(Location playerLocation) {
        this.playerLocation = playerLocation;
    }

    /**
     * Gets the display name of the player.
     *
     * @return the player's name
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Gets the unique identifier for the player.
     *
     * @return the player's UUID
     */
    public UUID getPlayerId() {
        return playerUUID;
    }

    /**
     * Checks if this player is the local user.
     *
     * @return true if this is the local user, false otherwise
     */
    public boolean isLocalUser() {
        return isLocalUser;
    }

    /**
     * Gets the current location of the player.
     *
     * @return the player's location
     */
    public Location getPlayerLocation() {
        return playerLocation;
    }

    /**
     * Gets the last calculated distance to another location.
     *
     * @return the current distance value
     */
    public double getCurrentDistance() {
        return currentDistance;
    }

    /**
     * Calculates and stores the distance between this player's location and a test location.
     *
     * @param testLocation the location to measure distance to
     * @return the calculated distance
     */
    public double calculateDistance(Location testLocation) {
        currentDistance = Location.calculateDistance(playerLocation, testLocation);
        return currentDistance;
    }

    /**
     * Calculates audio attenuation based on distance using an exponential decay curve.
     * The attenuation value ranges from 1.0 (no attenuation at distance 0) to 0.0 (full attenuation at or beyond attenuationDistance).
     *
     * @param attenuationDistance the distance at which full attenuation occurs
     * @return the attenuation factor between 0.0 (silent) and 1.0 (full volume)
     */
    public float calculateAttenuation(int attenuationDistance) {
        // Normalize the distance to a value between 0 and 1
        float normalized = (float) Math.min(currentDistance / attenuationDistance, 1.0);
        // Decay constant controlling the steepness of the attenuation curve
        double k = 4.0;
        // Apply exponential decay formula: (e^(-k*x) - e^(-k)) / (1 - e^(-k))
        return (float) ((Math.exp(-k * normalized) - Math.exp(-k)) / (1.0 - Math.exp(-k)));
    }
}