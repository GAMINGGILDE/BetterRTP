package me.SuperRonanCraft.BetterRTP.references.rtpinfo;

import lombok.Getter;
import me.SuperRonanCraft.BetterRTP.BetterRTP;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WorldPermissionGroup;
import org.bukkit.Bukkit;
import org.bukkit.World;

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
        if (!(fields.getValue() instanceof List<?> worldLists)) {
            return;
        }
        for (Object worldList : worldLists) {
            if (!(worldList instanceof Map<?, ?> worldsByName)) {
                continue;
            }
            for (Map.Entry<?, ?> worldFields : worldsByName.entrySet()) {
                BetterRTP.debug("- -- World: " + worldFields.getKey());
                World world = Bukkit.getWorld(worldFields.getKey().toString());
                if (world != null) {
                    WorldPermissionGroup permissionGroup = new WorldPermissionGroup(groupName, world, worldFields);
                    this.worlds.put(worldFields.getKey().toString(), permissionGroup);
                } else
                    BetterRTP.debug("- - The Permission Group '" + groupName + "'s world '" + worldFields.getKey() + "' does not exist! Permission Group not loaded...");
            }
        }
    }

}
