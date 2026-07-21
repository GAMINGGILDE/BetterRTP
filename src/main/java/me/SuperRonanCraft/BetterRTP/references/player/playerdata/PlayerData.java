package me.SuperRonanCraft.BetterRTP.references.player.playerdata;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import lombok.Getter;
import lombok.Setter;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.CooldownData;

public class PlayerData {

    public boolean loading; //Is this players data loading?
    @Getter private final UUID uuid;
    //Menus
    @Getter final PlayerData_Menus menu = new PlayerData_Menus();
    //Player Data
    @Getter final Map<String, CooldownData> cooldowns = new ConcurrentHashMap<>();
    //@Getter @Setter CooldownData globalCooldown;
    @Getter @Setter boolean rtping;
    @Getter @Setter int rtpCount;
    @Getter @Setter long globalCooldown;
    @Getter @Setter long invincibleEndTime;

    PlayerData(Player player) {
        this.uuid = player.getUniqueId();
    }

    public void load(boolean joined) {
        //Setup Defaults
        //new TaskDownloadPlayerData(this, joined).start();
    }
}
