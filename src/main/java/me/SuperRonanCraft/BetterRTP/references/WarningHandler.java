package me.SuperRonanCraft.BetterRTP.references;

import me.SuperRonanCraft.BetterRTP.BetterRTP;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class WarningHandler {

    private static final long WARNING_INTERVAL_MILLIS = 30L * 60L * 1000L;
    private final ConcurrentMap<WARNING, Long> lastWarning = new ConcurrentHashMap<>();

    public static void warn(WARNING type, String str) {
        warn(type, str, true);
    }

    public static void warn(WARNING type, String str, boolean auto_ignore) {
        WarningHandler handler = BetterRTP.getInstance().getWarningHandler();
        if (auto_ignore) { //Ignored automatically every 30 minutes
            long now = System.currentTimeMillis();
            Long lastTime = handler.lastWarning.getOrDefault(type, 0L);
            if (lastTime <= now) {
                BetterRTP.getInstance().getLogger().info(str);
                handler.lastWarning.put(type, now + WARNING_INTERVAL_MILLIS);
            }
        } else
            BetterRTP.getInstance().getLogger().warning(str);
    }

    public enum WARNING {
        USELOCATION_ENABLED_NO_LOCATION_AVAILABLE,
        NO_WORLD_TYPE_DECLARED
    }
}
