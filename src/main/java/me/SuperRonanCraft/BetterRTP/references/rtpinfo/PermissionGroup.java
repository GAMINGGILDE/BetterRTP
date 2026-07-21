package me.SuperRonanCraft.BetterRTP.references.rtpinfo;

import lombok.Getter;
import me.SuperRonanCraft.BetterRTP.BetterRTP;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WorldPermissionGroup;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionGroup {

    String groupName;
    @Getter private final HashMap<String, WorldPermissionGroup> worlds = new HashMap<>();

    public PermissionGroup(Map.Entry<?, ?> fields) {
        this.groupName = fields.getKey().toString();

        BetterRTP.debug("- Permission Group: " + groupName);
        //Find Location and cache its values
        Object configuredWorlds = fields.getValue();
        if (configuredWorlds instanceof ConfigurationSection section) {
            configuredWorlds = section.getValues(false);
        }
        if (configuredWorlds instanceof Map<?, ?> worldsByName) {
            loadWorlds(worldsByName);
        } else if (configuredWorlds instanceof List<?> worldLists) {
            for (Object worldList : worldLists) {
                if (worldList instanceof Map<?, ?> worldsByName) {
                    loadWorlds(worldsByName);
                }
            }
        }
    }

    private void loadWorlds(Map<?, ?> worldsByName) {
        for (Map.Entry<?, ?> configuredWorld : worldsByName.entrySet()) {
            Object values = configuredWorld.getValue();
            if (values instanceof ConfigurationSection section) {
                values = section.getValues(false);
            }
            BetterRTP.debug("- -- World: " + configuredWorld.getKey());
            World world = Bukkit.getWorld(configuredWorld.getKey().toString());
            if (world != null) {
                Map.Entry<?, ?> worldFields = new java.util.AbstractMap.SimpleImmutableEntry<>(
                        configuredWorld.getKey(), values);
                WorldPermissionGroup permissionGroup = new WorldPermissionGroup(groupName, world, worldFields);
                this.worlds.put(configuredWorld.getKey().toString(), permissionGroup);
            } else {
                BetterRTP.debug("- - The Permission Group '" + groupName + "'s world '"
                        + configuredWorld.getKey() + "' does not exist! Permission Group not loaded...");
            }
        }
    }

}
