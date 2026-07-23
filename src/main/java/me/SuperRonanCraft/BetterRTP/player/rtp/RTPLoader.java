package me.SuperRonanCraft.BetterRTP.player.rtp;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import me.SuperRonanCraft.BetterRTP.BetterRTP;
import me.SuperRonanCraft.BetterRTP.references.file.FileOther;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.PermissionGroup;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.RTPWorld;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WORLD_TYPE;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WorldCustom;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WorldDefault;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WorldLocation;

public class RTPLoader {

    static void loadWorlds(@NotNull WorldDefault defaultWorld, @NotNull Map<String, RTPWorld> customWorlds) {
        defaultWorld.load();
        customWorlds.clear();
        BetterRTP.debug("Loading Custom Worlds...");
        try {
            FileOther.FILETYPE config = FileOther.FILETYPE.CONFIG;
            List<Map<?, ?>> map = config.getMapList("CustomWorlds");
            for (Map<?, ?> m : map)
                for (Map.Entry<?, ?> entry : m.entrySet()) {
                    String world = entry.getKey().toString();
                    AtomicBoolean exists = new AtomicBoolean(false);
                    Bukkit.getWorlds().forEach(w -> {
                        if (w.getName().equals(world))
                            exists.set(true);
                    });
                    if (exists.get()) {
                        BetterRTP.debug("Custom World '" + world + "' registered:");
                        customWorlds.put(world, new WorldCustom(Bukkit.getWorld(world)));
                    } else
                        BetterRTP.debug("[WARN] - Custom World '" + world + "' was not registered because world does NOT exist");
                }
        } catch (RuntimeException exception) {
            getPl().getLogger().log(
                    java.util.logging.Level.SEVERE,
                    "Unable to load custom-world configuration", exception);
        }
    }

    static void loadOverrides(@NotNull Map<String, String> overriden) {
        BetterRTP.debug("Loading Overrides...");
        overriden.clear();
        try {
            FileOther.FILETYPE config = FileOther.FILETYPE.CONFIG;
            List<Map<?, ?>> override_map = config.getMapList("Overrides");
            for (Map<?, ?> m : override_map)
                for (Map.Entry<?, ?> entry : m.entrySet()) {
                    overriden.put(entry.getKey().toString(), entry.getValue().toString());
                    if (getPl().getSettings().isDebug())
                        getPl().getLogger().info("- Override '" + entry.getKey() + "' -> '" + entry.getValue() + "' added");
                    if (Bukkit.getWorld(entry.getValue().toString()) == null)
                        getPl().getLogger().warning("The world `" + entry.getValue() + "` doesn't seem to exist! Please update `" + entry.getKey() + "'s` override! Maybe there are capital letters?");
                }
        } catch (RuntimeException exception) {
            getPl().getLogger().log(
                    java.util.logging.Level.SEVERE,
                    "Unable to load world overrides", exception);
        }
    }

    static void loadWorldTypes(@NotNull Map<String, WORLD_TYPE> world_type) {
        BetterRTP.debug("Loading World Types...");
        world_type.clear();
        try {
            FileOther.FILETYPE config = FileOther.FILETYPE.CONFIG;
            //for (World world : Bukkit.getWorlds())
            //    world_type.put(world.getName(), WORLD_TYPE.NORMAL);
            List<Map<?, ?>> world_map = config.getMapList("WorldType");
            for (Map<?, ?> m : world_map)
                for (Map.Entry<?, ?> entry : m.entrySet()) {
                    //if (world_type.containsKey(entry.getKey())) {
                        try {
                            String world = entry.getKey().toString();
                            WORLD_TYPE type = WORLD_TYPE.valueOf(entry.getValue().toString().toUpperCase());
                            world_type.put(world, type);
                            BetterRTP.debug("- World Type for '" + world + "' set to '" + type + "'");
                        } catch(IllegalArgumentException e) {
                            StringBuilder valids = new StringBuilder();
                            for (WORLD_TYPE type : WORLD_TYPE.values())
                                valids.append(type.name()).append(", ");
                            valids.replace(valids.length() - 2, valids.length(), "");
                            getPl().getLogger().severe("World Type for '" + entry.getKey() + "' is INVALID '" + entry.getValue() +
                                    "'. Valid ID's are: " + valids);
                            //Wrong rtp world type
                        }
                    //}/* else {
                    //    if (getPl().getSettings().debug)
                    //        getPl().getLogger().info("- World Type failed for '" + entry.getKey() + "' is it loaded?");
                    //}*/
                }
        } catch (RuntimeException exception) {
            getPl().getLogger().log(
                    java.util.logging.Level.SEVERE,
                    "Unable to load configured world types", exception);
        }
    }

    static void loadLocations(@NotNull Map<String, RTPWorld> worlds) {
        worlds.clear();
        FileOther.FILETYPE config = FileOther.FILETYPE.LOCATIONS;
        if (!BetterRTP.getInstance().getSettings().isLocationEnabled())
            return;
        BetterRTP.debug("Loading Locations...");
        List<Map<?, ?>> map = config.getMapList("Locations");
        for (Map<?, ?> m : map)
            for (Map.Entry<?, ?> entry : m.entrySet()) {
                WorldLocation location = new WorldLocation(entry.getKey().toString());
                if (location.isValid()) {
                    worlds.put(entry.getKey().toString(), location);
                    BetterRTP.debug("- Location '" + entry.getKey() + "' registered");
                }
            }
    }

    static void loadPermissionGroups(@NotNull Map<String, PermissionGroup> permissionGroup) {
        permissionGroup.clear();
        FileOther.FILETYPE config = FileOther.FILETYPE.CONFIG;
        if (!getPl().getSettings().isPermissionGroupEnabled())
            return;
        BetterRTP.debug("Loading Permission Groups...");
        try {
            List<Map<?, ?>> map = config.getMapList("PermissionGroup.Groups");
            for (Map<?, ?> m : map)
                for (Map.Entry<?, ?> entry : m.entrySet()) {
                    String group = entry.getKey().toString();
                    permissionGroup.put(group, new PermissionGroup(entry));
                }
        } catch (RuntimeException exception) {
            getPl().getLogger().log(
                    java.util.logging.Level.SEVERE,
                    "Unable to load permission groups", exception);
        }
    }

    private static BetterRTP getPl() {
        return BetterRTP.getInstance();
    }
}
