package me.SuperRonanCraft.BetterRTP.references.rtpinfo;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

/**
 * Immutable queue persistence data captured while the owning region is active.
 * Database work must use this snapshot instead of reading Bukkit location state
 * from an asynchronous thread.
 */
public record QueuePosition(World world, String worldName, int blockX, int blockZ) {

    public QueuePosition {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(worldName, "worldName");
    }

    public static QueuePosition capture(Location location) {
        Objects.requireNonNull(location, "location");
        World world = Objects.requireNonNull(location.getWorld(), "location.world");
        return new QueuePosition(
                world, world.getName(), location.getBlockX(), location.getBlockZ());
    }

    public Location toLocation() {
        return new Location(world, blockX, 69, blockZ);
    }
}
