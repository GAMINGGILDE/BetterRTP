package me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds;

import me.SuperRonanCraft.BetterRTP.player.commands.RTP_SETUP_TYPE;
import me.SuperRonanCraft.BetterRTP.player.rtp.RtpWorldSnapshot;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Normalizes configured world data into an immutable per-request snapshot. */
public final class RtpWorldResolver {

    private RtpWorldResolver() {
    }

    public static ResolvedRtpWorld resolve(
            RTPWorld configuredWorld,
            World requestWorld,
            @Nullable List<String> biomeOverride,
            int fallbackMinRadius,
            WORLD_TYPE worldType,
            @Nullable String setupName,
            @Nullable WorldPermissionGroup permissionGroup) {
        Objects.requireNonNull(configuredWorld, "configuredWorld");
        Objects.requireNonNull(requestWorld, "requestWorld");
        Objects.requireNonNull(worldType, "worldType");

        int centerX = configuredWorld.getCenterX();
        int centerZ = configuredWorld.getCenterZ();
        int maxRadius = configuredWorld.getMaxRadius();
        int minRadius = configuredWorld.getMinRadius();

        if (configuredWorld.getUseWorldborder()) {
            WorldBorder border = requestWorld.getWorldBorder();
            maxRadius = Math.min(maxRadius, (int) border.getSize() / 2);
            centerX = border.getCenter().getBlockX();
            centerZ = border.getCenter().getBlockZ();
        }
        if (maxRadius <= minRadius) {
            minRadius = Math.max(0, fallbackMinRadius);
            if (maxRadius <= minRadius) {
                minRadius = 0;
            }
        }

        List<String> biomes = biomeOverride == null
                ? new ArrayList<>(configuredWorld.getBiomes())
                : new ArrayList<>(biomeOverride);
        int price = configuredWorld instanceof WorldDefault defaultWorld
                ? defaultWorld.getPrice(requestWorld.getName())
                : configuredWorld.getPrice();

        RtpWorldSnapshot settings = new RtpWorldSnapshot(
                configuredWorld.getUseWorldborder(),
                centerX,
                centerZ,
                maxRadius,
                minRadius,
                price,
                biomes,
                requestWorld,
                configuredWorld.getShape(),
                configuredWorld.getMinY(),
                configuredWorld.getMaxY(),
                configuredWorld.getID(),
                configuredWorld.getCooldown(),
                configuredWorld.getRTPOnDeath(),
                worldType);
        return new ResolvedRtpWorld(
                settings, setupType(configuredWorld), setupName, permissionGroup);
    }

    private static RTP_SETUP_TYPE setupType(RTPWorld world) {
        if (world instanceof WorldLocation) return RTP_SETUP_TYPE.LOCATION;
        if (world instanceof WorldCustom) return RTP_SETUP_TYPE.CUSTOM_WORLD;
        if (world instanceof WorldPermissionGroup) return RTP_SETUP_TYPE.PERMISSIONGROUP;
        return RTP_SETUP_TYPE.DEFAULT;
    }
}
