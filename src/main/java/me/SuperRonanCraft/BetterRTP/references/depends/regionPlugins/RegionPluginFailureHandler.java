package me.SuperRonanCraft.BetterRTP.references.depends.regionPlugins;

import me.SuperRonanCraft.BetterRTP.BetterRTP;
import org.bukkit.Location;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/** Logs integration failures at a bounded rate and rejects the unsafe target. */
final class RegionPluginFailureHandler {

    private static final long LOG_INTERVAL_MILLIS = 60_000L;
    private static final Map<String, Long> LAST_LOGGED = new ConcurrentHashMap<>();

    private RegionPluginFailureHandler() {
    }

    static boolean reject(String integration, Location location, RuntimeException exception) {
        long now = System.currentTimeMillis();
        Long previous = LAST_LOGGED.get(integration);
        if (previous == null || now - previous >= LOG_INTERVAL_MILLIS) {
            LAST_LOGGED.put(integration, now);
            String worldName = location.getWorld() == null
                    ? "<unknown>" : location.getWorld().getName();
            BetterRTP.getInstance().getLogger().log(
                    Level.WARNING,
                    integration + " could not validate RTP target "
                            + worldName + "[" + location.getBlockX() + ","
                            + location.getBlockY() + "," + location.getBlockZ()
                            + "]; rejecting the target for safety",
                    exception);
        }
        return false;
    }
}
