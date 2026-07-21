package me.SuperRonanCraft.BetterRTP.versions;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.SuperRonanCraft.BetterRTP.BetterRTP;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class AsyncHandler {

    public static void async(Runnable runnable) {
        Bukkit.getAsyncScheduler().runNow(getPlugin(), task -> runnable.run());
    }

    public static void sync(Runnable runnable) {
        Bukkit.getGlobalRegionScheduler().run(getPlugin(), task -> runnable.run());
    }

    public static void syncAtEntity(Entity entity, Runnable runnable) {
        entity.getScheduler().run(getPlugin(), task -> runnable.run(), null);
    }

    public static void syncAtEntity(Entity entity, Runnable runnable, Runnable retired) {
        entity.getScheduler().run(getPlugin(), task -> runnable.run(), retired);
    }

    public static void syncAtSender(CommandSender sender, Runnable runnable) {
        if (sender instanceof Entity entity) {
            syncAtEntity(entity, runnable);
        } else {
            sync(runnable);
        }
    }

    public static void syncAtLocation(Location location, Runnable runnable) {
        Bukkit.getRegionScheduler().run(getPlugin(), location, task -> runnable.run());
    }

    public static CompletableFuture<Boolean> teleportAsync(Entity entity, Location location) {
        return entity.teleportAsync(location);
    }

    public static ScheduledTask asyncLater(Runnable runnable, long ticks) {
        return Bukkit.getAsyncScheduler().runDelayed(
                getPlugin(), task -> runnable.run(), ticks * 50L, TimeUnit.MILLISECONDS);
    }

    public static ScheduledTask syncLater(Runnable runnable, long ticks) {
        return Bukkit.getGlobalRegionScheduler().runDelayed(getPlugin(), task -> runnable.run(), ticks);
    }

    public static ScheduledTask syncLaterAtEntity(Entity entity, Runnable runnable, long ticks) {
        return entity.getScheduler().runDelayed(
                getPlugin(), task -> runnable.run(), null, ticks);
    }

    private static BetterRTP getPlugin() {
        return BetterRTP.getInstance();
    }
}
