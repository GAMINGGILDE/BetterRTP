package me.SuperRonanCraft.BetterRTP.player.events;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.SuperRonanCraft.BetterRTP.BetterRTP;
import me.SuperRonanCraft.BetterRTP.versions.AsyncHandler;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldLoad {

    ScheduledTask loader;

    void load(WorldLoadEvent e) {
        String worldName = e.getWorld().getName();
        //BetterRTP.getInstance().getLogger().info("NEW WORLD!");
        if (loader != null)
            loader.cancel();
        loader = AsyncHandler.globalLater(() -> {
            BetterRTP.debug("New world `" + worldName + "` detected! Reloaded Databases!");
            BetterRTP.getInstance().getDatabaseHandler().refreshWorlds();
        }, 20L * 5);
    }
}
