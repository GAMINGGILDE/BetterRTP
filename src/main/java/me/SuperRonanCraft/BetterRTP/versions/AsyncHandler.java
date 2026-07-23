package me.SuperRonanCraft.BetterRTP.versions;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.SuperRonanCraft.BetterRTP.BetterRTP;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Central scheduler boundary for Folia and Paper.
 *
 * <p>Callers must use an entity scheduler for entity state, a region scheduler
 * for world/chunk state, the global scheduler only for global server state, and
 * the async scheduler only for work that does not touch live Bukkit state.</p>
 */
public final class AsyncHandler {

    private AsyncHandler() {
    }

    public static void async(Runnable runnable) {
        Runnable checked = Objects.requireNonNull(runnable, "runnable");
        Bukkit.getAsyncScheduler().runNow(getPlugin(), task -> runSafely("asynchronous task", checked));
    }

    public static CompletableFuture<Void> asyncFuture(Runnable runnable) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Bukkit.getAsyncScheduler().runNow(getPlugin(), task -> {
            try {
                runnable.run();
                future.complete(null);
            } catch (Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        });
        return future;
    }

    public static void global(Runnable runnable) {
        Runnable checked = Objects.requireNonNull(runnable, "runnable");
        Bukkit.getGlobalRegionScheduler().run(getPlugin(), task -> runSafely("global task", checked));
    }

    /**
     * @deprecated The old name implied a traditional main thread. Folia has no
     * single main thread; use {@link #global(Runnable)} explicitly.
     */
    @Deprecated(forRemoval = false)
    public static void sync(Runnable runnable) {
        global(runnable);
    }

    public static void syncAtEntity(Entity entity, Runnable runnable) {
        Runnable checked = Objects.requireNonNull(runnable, "runnable");
        Objects.requireNonNull(entity, "entity").getScheduler().run(
                getPlugin(),
                task -> runSafely("entity task", checked),
                null);
    }

    public static void syncAtEntity(Entity entity, Runnable runnable, Runnable retired) {
        Runnable checked = Objects.requireNonNull(runnable, "runnable");
        Objects.requireNonNull(entity, "entity").getScheduler().run(
                getPlugin(),
                task -> runSafely("entity task", checked),
                retired == null ? null : () -> runSafely(
                        "retired entity task", retired));
    }

    public static CompletableFuture<Void> entityFuture(
            Entity entity, Runnable runnable, Runnable retired) {
        Objects.requireNonNull(entity, "entity");
        Runnable checked = Objects.requireNonNull(runnable, "runnable");
        CompletableFuture<Void> future = new CompletableFuture<>();
        AtomicBoolean claimed = new AtomicBoolean();
        Runnable retiredCallback = () -> {
            if (!claimed.compareAndSet(false, true)) {
                return;
            }
            try {
                if (retired != null) {
                    retired.run();
                }
                future.complete(null);
            } catch (Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        };
        ScheduledTask scheduled = entity.getScheduler().run(
                getPlugin(),
                task -> {
                    if (!claimed.compareAndSet(false, true)) {
                        return;
                    }
                    try {
                        checked.run();
                        future.complete(null);
                    } catch (Throwable throwable) {
                        future.completeExceptionally(throwable);
                    }
                },
                retiredCallback);
        if (scheduled == null && !future.isDone()) {
            global(retiredCallback);
        }
        return future;
    }

    public static void syncAtSender(CommandSender sender, Runnable runnable) {
        if (sender instanceof Entity entity) {
            syncAtEntity(entity, runnable);
        } else {
            global(runnable);
        }
    }

    public static void syncAtLocation(Location location, Runnable runnable) {
        Location checkedLocation = Objects.requireNonNull(location, "location").clone();
        Runnable checked = Objects.requireNonNull(runnable, "runnable");
        Bukkit.getRegionScheduler().run(
                getPlugin(), checkedLocation,
                task -> runSafely("region task at " + format(checkedLocation), checked));
    }

    public static CompletableFuture<Boolean> teleportAsync(Entity entity, Location location) {
        return entity.teleportAsync(location);
    }

    public static ScheduledTask asyncLater(Runnable runnable, long ticks) {
        Runnable checked = Objects.requireNonNull(runnable, "runnable");
        return Bukkit.getAsyncScheduler().runDelayed(
                getPlugin(), task -> runSafely("delayed asynchronous task", checked),
                ticks * 50L, TimeUnit.MILLISECONDS);
    }

    public static ScheduledTask globalLater(Runnable runnable, long ticks) {
        Runnable checked = Objects.requireNonNull(runnable, "runnable");
        return Bukkit.getGlobalRegionScheduler().runDelayed(
                getPlugin(), task -> runSafely("delayed global task", checked), ticks);
    }

    /**
     * @deprecated Use {@link #globalLater(Runnable, long)}.
     */
    @Deprecated(forRemoval = false)
    public static ScheduledTask syncLater(Runnable runnable, long ticks) {
        return globalLater(runnable, ticks);
    }

    public static ScheduledTask syncLaterAtEntity(Entity entity, Runnable runnable, long ticks) {
        return syncLaterAtEntity(entity, runnable, null, ticks);
    }

    public static ScheduledTask syncLaterAtEntity(
            Entity entity, Runnable runnable, Runnable retired, long ticks) {
        Runnable checked = Objects.requireNonNull(runnable, "runnable");
        return entity.getScheduler().runDelayed(
                getPlugin(),
                task -> runSafely("delayed entity task", checked),
                retired == null ? null : () -> runSafely("retired delayed entity task", retired),
                ticks);
    }

    private static BetterRTP getPlugin() {
        return BetterRTP.getInstance();
    }

    private static void runSafely(String description, Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable throwable) {
            getPlugin().getLogger().log(Level.SEVERE, "Unhandled exception in " + description, throwable);
        }
    }

    private static String format(Location location) {
        String world = location.getWorld() == null ? "<null>" : location.getWorld().getName();
        return world + "[" + location.getBlockX() + "," + location.getBlockZ() + "]";
    }
}
