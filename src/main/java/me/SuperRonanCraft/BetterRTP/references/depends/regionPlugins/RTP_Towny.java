package me.SuperRonanCraft.BetterRTP.references.depends.regionPlugins;

import org.bukkit.Location;

import com.palmergames.bukkit.towny.TownyAPI;

public class RTP_Towny implements RegionPluginCheck {

    // NOT TESTED (2.13.0)
    // Towny (v0.96.1.11)
    // https://www.spigotmc.org/resources/towny.72694/
    public boolean check(Location loc) {
        if (REGIONPLUGINS.TOWNY.isEnabled()) {
            try {
                return TownyAPI.getInstance().isWilderness(loc);
            } catch (RuntimeException exception) {
                return RegionPluginFailureHandler.reject("Towny", loc, exception);
            }
        }
        return true;
    }
}
