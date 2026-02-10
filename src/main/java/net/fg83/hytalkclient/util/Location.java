package net.fg83.hytalkclient.util;

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

    public static double calculateDistance(Location location1, Location location2){
        if (!location1.getWorldId().equals(location2.getWorldId())){
            return -1;
        }

        return Math.sqrt( Math.pow(location1.getX() - location2.getX(), 2) + Math.pow(location1.getY() - location2.getY(), 2) + Math.pow(location1.getZ() - location2.getZ(), 2));
    }
}
