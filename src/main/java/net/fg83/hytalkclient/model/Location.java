package net.fg83.hytalkclient.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a 3D location in a specific world.
 * Used to track player positions for proximity-based voice chat.
 */
public class Location {
    // The unique identifier of the world/dimension
    private final UUID worldId;
    // The X coordinate in the world
    private final double x;
    // The Y coordinate in the world
    private final double y;
    // The Z coordinate in the world
    private final double z;

    /**
     * Creates a new Location with the specified coordinates and world.
     *
     * @param worldId the unique identifier of the world
     * @param x       the X coordinate
     * @param y       the Y coordinate
     * @param z       the Z coordinate
     */
    public Location(UUID worldId, double x, double y, double z) {
        this.worldId = worldId;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Gets the world identifier for this location.
     *
     * @return the world UUID
     */
    public UUID getWorldId() {
        return worldId;
    }

    /**
     * Gets the X coordinate of this location.
     *
     * @return the X coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * Gets the Y coordinate of this location.
     *
     * @return the Y coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * Gets the Z coordinate of this location.
     *
     * @return the Z coordinate
     */
    public double getZ() {
        return z;
    }


    /**
     * Calculates the Euclidean distance between two locations.
     * Returns -1 if locations are in different worlds.
     * Returns Double.MAX_VALUE if either location is null.
     *
     * @param location1 the first location
     * @param location2 the second location
     * @return the distance between the two locations, -1 if in different worlds,
     * or Double.MAX_VALUE if either location is null
     */
    public static double calculateDistance(Location location1, Location location2) {
        // Handle null locations
        if (location1 == null || location2 == null) {
            return Double.MAX_VALUE;
        }
        // Check if locations are in the same world
        if (!location1.getWorldId().equals(location2.getWorldId())) {
            return -1;
        }

        // Calculate Euclidean distance using the Pythagorean theorem in 3D
        return Math.sqrt(Math.pow(location1.getX() - location2.getX(), 2) + Math.pow(location1.getY() - location2.getY(), 2) + Math.pow(location1.getZ() - location2.getZ(), 2));
    }

    /**
     * Parses a JSON array containing player location data and creates a map of players.
     * Expected JSON structure:
     * [
     * {
     * "player_name": "PlayerName",
     * "player_uuid": "uuid-string",
     * "location": {
     * "world_id": "world-uuid-string",
     * "x": 100.0,
     * "y": 64.0,
     * "z": 200.0
     * }
     * }
     * ]
     *
     * @param dataArray JSON array containing player location data
     * @return a map of player UUIDs to VoiceChatPlayer objects with their locations
     */
    public static Map<UUID, VoiceChatPlayer> parsePlayerLocations(JsonArray dataArray) {
        // Initialize map to store players
        Map<UUID, VoiceChatPlayer> players = new HashMap<>();
        // Iterate through each player entry in the JSON array
        dataArray.forEach(jsonElement -> {
            // Extract player data from JSON
            JsonObject playerData = jsonElement.getAsJsonObject();
            String playerName = playerData.get("player_name").getAsString();
            UUID playerUuid = UUID.fromString(playerData.get("player_uuid").getAsString());
            // Extract location data from nested JSON object
            JsonObject locationData = playerData.get("location").getAsJsonObject();
            UUID worldId = UUID.fromString(locationData.get("world_id").getAsString());
            double x = locationData.get("x").getAsDouble();
            double y = locationData.get("y").getAsDouble();
            double z = locationData.get("z").getAsDouble();
            // Create location object from parsed data
            Location playerLocation = new Location(worldId, x, y, z);
            // Create player object and set their location
            VoiceChatPlayer player = new VoiceChatPlayer(playerName, playerUuid);
            player.setPlayerLocation(playerLocation);
            // Add player to the map using their UUID as the key
            players.put(playerUuid, player);
        });
        return players;
    }
}