package me.SuperRonanCraft.BetterRTP.player;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import lombok.Getter;
import me.SuperRonanCraft.BetterRTP.references.invs.RTP_INV_SETTINGS;

public class PlayerInfo {

    private final HashMap<Player, Inventory> invs = new HashMap<>();
    //private final HashMap<Player, RTP_INV_SETTINGS> invType = new HashMap<>();
    @Getter private final HashMap<Player, World> invWorld = new HashMap<>();
    @Getter private final HashMap<Player, RTP_INV_SETTINGS> invNextInv = new HashMap<>();
    //private final HashMap<Player, CooldownData> cooldown = new HashMap<>();
    private final Set<UUID> activeTeleports = ConcurrentHashMap.newKeySet();
    //private final HashMap<Player, List<Location>> previousLocations = new HashMap<>();
    //private final HashMap<Player, RTP_TYPE> rtpType = new HashMap<>();

    /*private void setInv(Player p, Inventory inv) {
        invs.put(p, inv);
    }*/

    /*private void setInvType(Player p, RTP_INV_SETTINGS type) {
        invType.put(p, type);
    }*/

    public void setInvWorld(Player p, World type) {
        invWorld.put(p, type);
    }

    public void setNextInv(Player p, RTP_INV_SETTINGS type) {
        invNextInv.put(p, type);
    }

    //--Logic--

    public Boolean playerExists(Player p) {
        return invs.containsKey(p);
    }

    public void clearInvs(Player p) {
        invs.remove(p);
        //invType.remove(p);
        invWorld.remove(p);
        invNextInv.remove(p);
    }

    public boolean beginTeleport(Player player) {
        return activeTeleports.add(player.getUniqueId());
    }

    public boolean isTeleporting(Player player) {
        return activeTeleports.contains(player.getUniqueId());
    }

    public void endTeleport(Player player) {
        activeTeleports.remove(player.getUniqueId());
    }
}
