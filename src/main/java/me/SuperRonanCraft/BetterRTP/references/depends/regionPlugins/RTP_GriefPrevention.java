package me.SuperRonanCraft.BetterRTP.references.depends.regionPlugins;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;

public class RTP_GriefPrevention implements RegionPluginCheck {

    // TESTED (v2.13.0)
    // GriefPrevention (v16.15.0)
    // https://www.spigotmc.org/resources/griefprevention.1884/
    public boolean check(Location loc) {
        if (REGIONPLUGINS.GRIEFPREVENTION.isEnabled()) {
            try {
                return GriefPrevention.instance.dataStore.getClaimAt(loc, true, null) == null;
            } catch (RuntimeException exception) {
                return RegionPluginFailureHandler.reject("GriefPrevention", loc, exception);
            }
        }
        return true;
    }
}
