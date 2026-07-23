package me.SuperRonanCraft.BetterRTP.player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import lombok.Getter;
import me.SuperRonanCraft.BetterRTP.references.invs.RTP_INV_SETTINGS;

public class PlayerInfo {

    private final Map<UUID, Inventory> invs = new ConcurrentHashMap<>();
    @Getter private final Map<UUID, World> invWorld = new ConcurrentHashMap<>();
    @Getter private final Map<UUID, RTP_INV_SETTINGS> invNextInv = new ConcurrentHashMap<>();

    public void setInvWorld(Player p, World type) {
        invWorld.put(p.getUniqueId(), type);
    }

    public void setNextInv(Player p, RTP_INV_SETTINGS type) {
        invNextInv.put(p.getUniqueId(), type);
    }

    //--Logic--

    public Boolean playerExists(Player p) {
        return invs.containsKey(p.getUniqueId());
    }

    public void clearInvs(Player p) {
        invs.remove(p.getUniqueId());
        //invType.remove(p);
        invWorld.remove(p.getUniqueId());
        invNextInv.remove(p.getUniqueId());
    }

    public void clearInvs() {
        invs.clear();
        invWorld.clear();
        invNextInv.clear();
    }
}
