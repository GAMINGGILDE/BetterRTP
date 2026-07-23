package me.SuperRonanCraft.BetterRTP.player.rtp;

import me.SuperRonanCraft.BetterRTP.references.file.FileOther;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.PermissionGroup;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.RTPWorld;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WORLD_TYPE;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WorldCustom;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WorldDefault;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WorldLocation;
import me.SuperRonanCraft.BetterRTP.references.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/** Loads the RTP model without hiding its runtime dependencies in the loader. */
final class RTPLoader {

    private final Settings settings;
    private final Supplier<Logger> logger;

    RTPLoader(Settings settings, Supplier<Logger> logger) {
        this.settings = settings;
        this.logger = Objects.requireNonNull(logger);
    }

    void loadWorlds(
            @NotNull WorldDefault defaultWorld,
            @NotNull Map<String, RTPWorld> customWorlds) {
        defaultWorld.load();
        customWorlds.clear();
        debug("Loading Custom Worlds...");
        for (Map<?, ?> configured : FileOther.FILETYPE.CONFIG.getMapList("CustomWorlds")) {
            for (Map.Entry<?, ?> entry : configured.entrySet()) {
                String worldName = entry.getKey().toString();
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    debug("Custom World '" + worldName + "' registered:");
                    customWorlds.put(
                            worldName, new WorldCustom(world, defaultWorld, true));
                } else {
                    debug("[WARN] - Custom World '" + worldName
                            + "' was not registered because world does NOT exist");
                }
            }
        }
    }

    void loadOverrides(@NotNull Map<String, String> overrides) {
        debug("Loading Overrides...");
        overrides.clear();
        for (Map<?, ?> configured : FileOther.FILETYPE.CONFIG.getMapList("Overrides")) {
            for (Map.Entry<?, ?> entry : configured.entrySet()) {
                String source = entry.getKey().toString();
                String target = entry.getValue().toString();
                overrides.put(source, target);
                debug("- Override '" + source + "' -> '" + target + "' added");
                if (Bukkit.getWorld(target) == null) {
                    logger.get().warning("The world `" + target
                            + "` doesn't seem to exist! Please update `" + source
                            + "'s` override! Maybe there are capital letters?");
                }
            }
        }
    }

    void loadWorldTypes(@NotNull Map<String, WORLD_TYPE> worldTypes) {
        debug("Loading World Types...");
        worldTypes.clear();
        for (Map<?, ?> configured : FileOther.FILETYPE.CONFIG.getMapList("WorldType")) {
            for (Map.Entry<?, ?> entry : configured.entrySet()) {
                String world = entry.getKey().toString();
                try {
                    WORLD_TYPE type = WORLD_TYPE.valueOf(
                            entry.getValue().toString().toUpperCase());
                    worldTypes.put(world, type);
                    debug("- World Type for '" + world + "' set to '" + type + "'");
                } catch (IllegalArgumentException exception) {
                    String valid = Arrays.stream(WORLD_TYPE.values())
                            .map(Enum::name)
                            .collect(Collectors.joining(", "));
                    logger.get().severe("World Type for '" + world + "' is INVALID '"
                            + entry.getValue() + "'. Valid IDs are: " + valid);
                }
            }
        }
    }

    void loadLocations(
            @NotNull Map<String, RTPWorld> worlds, RTPWorld defaultWorld) {
        worlds.clear();
        if (!settings.isLocationEnabled()) {
            return;
        }
        debug("Loading Locations...");
        List<Map<?, ?>> configuredLocations =
                FileOther.FILETYPE.LOCATIONS.getMapList("Locations");
        for (Map<?, ?> configured : configuredLocations) {
            for (Map.Entry<?, ?> entry : configured.entrySet()) {
                String name = entry.getKey().toString();
                WorldLocation location = new WorldLocation(name, defaultWorld);
                if (location.isValid()) {
                    worlds.put(name, location);
                    debug("- Location '" + name + "' registered");
                }
            }
        }
    }

    void loadPermissionGroups(
            @NotNull Map<String, PermissionGroup> groups, RTPWorld defaultWorld) {
        groups.clear();
        if (!settings.isPermissionGroupEnabled()) {
            return;
        }
        debug("Loading Permission Groups...");
        for (Map<?, ?> configured :
                FileOther.FILETYPE.CONFIG.getMapList("PermissionGroup.Groups")) {
            for (Map.Entry<?, ?> entry : configured.entrySet()) {
                String group = entry.getKey().toString();
                groups.put(group, new PermissionGroup(entry, defaultWorld));
            }
        }
    }

    private void debug(String message) {
        if (settings != null && settings.isDebug()) {
            logger.get().info(message);
        }
    }
}
