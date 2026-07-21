package me.SuperRonanCraft.BetterRTP.references.rtpinfo;

import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.RTPWorld;
import org.bukkit.World;

import java.util.Objects;

/** Immutable database search bounds for one RTP world snapshot. */
public record QueueRange(
        int xLow,
        int xHigh,
        int zLow,
        int zHigh,
        World world,
        String worldName) {

    public QueueRange {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(worldName, "worldName");
    }

    public static QueueRange from(RTPWorld world) {
        Objects.requireNonNull(world, "world");
        World bukkitWorld = world.getWorld();
        return new QueueRange(
                world.getCenterX() - world.getMaxRadius(),
                world.getCenterX() + world.getMaxRadius(),
                world.getCenterZ() - world.getMaxRadius(),
                world.getCenterZ() + world.getMaxRadius(),
                bukkitWorld,
                bukkitWorld.getName());
    }
}
