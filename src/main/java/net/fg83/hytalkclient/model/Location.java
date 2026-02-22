package net.fg83.hytalkclient.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Location {
    private final UUID worldId;
    private final double x;
    private final double y;
    private final double z;

    public Location(UUID worldId, double x, double y, double z) {
        this.worldId = worldId;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public UUID getWorldId() {
        return worldId;
    }
    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public double getZ() {
        return z;
    }


    /* STATIC UTILITY METHODS */
    public static double calculateDistance(Location location1, Location location2){
        if (location1 == null || location2 == null) {
            return Double.MAX_VALUE;
        }
        if (!location1.getWorldId().equals(location2.getWorldId())){
            return -1;
        }

        return Math.sqrt( Math.pow(location1.getX() - location2.getX(), 2) + Math.pow(location1.getY() - location2.getY(), 2) + Math.pow(location1.getZ() - location2.getZ(), 2));
    }
    public static Map<UUID, VoiceChatPlayer> parsePlayerLocations(JsonArray dataArray){
        Map<UUID, VoiceChatPlayer> players = new HashMap<>();
        dataArray.forEach(jsonElement -> {
            JsonObject playerData = jsonElement.getAsJsonObject();
            String playerName = playerData.get("player_name").getAsString();
            UUID playerUuid = UUID.fromString(playerData.get("player_uuid").getAsString());
            JsonObject locationData = playerData.get("location").getAsJsonObject();
            UUID worldId = UUID.fromString(locationData.get("world_id").getAsString());
            double x = locationData.get("x").getAsDouble();
            double y = locationData.get("y").getAsDouble();
            double z = locationData.get("z").getAsDouble();
            Location playerLocation = new Location(worldId, x, y, z);
            VoiceChatPlayer player = new VoiceChatPlayer(playerName, playerUuid);
            player.setPlayerLocation(playerLocation);
            players.put(playerUuid, player);
        });
        return players;
    }
}
