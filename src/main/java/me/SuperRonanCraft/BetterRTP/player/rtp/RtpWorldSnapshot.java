package me.SuperRonanCraft.BetterRTP.player.rtp;

import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.RTPWorld;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WORLD_TYPE;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WorldPlayer;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/** Immutable world settings used throughout one asynchronous RTP attempt. */
public record RtpWorldSnapshot(
        boolean useWorldBorder,
        int centerX,
        int centerZ,
        int maxRadius,
        int minRadius,
        int price,
        List<String> biomes,
        @NotNull World world,
        RTP_SHAPE shape,
        int minY,
        int maxY,
        @Nullable String id,
        long cooldown,
        boolean rtpOnDeath,
        WORLD_TYPE worldType) implements RTPWorld {

    public RtpWorldSnapshot {
        biomes = List.copyOf(Objects.requireNonNull(biomes, "biomes"));
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(shape, "shape");
        Objects.requireNonNull(worldType, "worldType");
    }

    public static RtpWorldSnapshot from(WorldPlayer source) {
        Objects.requireNonNull(source, "source");
        return new RtpWorldSnapshot(
                source.getUseWorldborder(),
                source.getCenterX(),
                source.getCenterZ(),
                source.getMaxRadius(),
                source.getMinRadius(),
                source.getPrice(),
                source.getBiomes(),
                source.getWorld(),
                source.getShape(),
                source.getMinY(),
                source.getMaxY(),
                source.getID(),
                source.getCooldown(),
                source.getRTPOnDeath(),
                source.getWorldtype());
    }

    @Override public boolean getUseWorldborder() { return useWorldBorder; }
    @Override public int getCenterX() { return centerX; }
    @Override public int getCenterZ() { return centerZ; }
    @Override public int getMaxRadius() { return maxRadius; }
    @Override public int getMinRadius() { return minRadius; }
    @Override public int getPrice() { return price; }
    @Override public List<String> getBiomes() { return biomes; }
    @Override public @NotNull World getWorld() { return world; }
    @Override public RTP_SHAPE getShape() { return shape; }
    @Override public int getMinY() { return minY; }
    @Override public int getMaxY() { return maxY; }
    @Override public @Nullable String getID() { return id; }
    @Override public long getCooldown() { return cooldown; }
    @Override public boolean getRTPOnDeath() { return rtpOnDeath; }
}
