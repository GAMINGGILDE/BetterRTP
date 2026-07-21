package me.SuperRonanCraft.BetterRTP.references.rtpinfo;

import me.SuperRonanCraft.BetterRTP.BetterRTP;
import me.SuperRonanCraft.BetterRTP.player.rtp.RTP_SHAPE;
import me.SuperRonanCraft.BetterRTP.references.customEvents.RTP_TeleportPostEvent;
import me.SuperRonanCraft.BetterRTP.references.database.DatabaseHandler;
import me.SuperRonanCraft.BetterRTP.references.database.DatabaseQueue;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.RTPWorld;
import me.SuperRonanCraft.BetterRTP.versions.AsyncHandler;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.logging.Logger;
import me.SuperRonanCraft.BetterRTP.player.rtp.RTP;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WORLD_TYPE;
import org.bukkit.World;

public class QueueHandler implements Listener { //Randomly queues up some safe locations

    private final QueueService service;
    private final QueueGenerator generator;
    private final Consumer<String> debug;

    @Deprecated(forRemoval = false)
    public QueueHandler() {
        this(
                () -> BetterRTP.getInstance().getSettings().isQueueEnabled(),
                DatabaseHandler.getQueue());
    }

    public QueueHandler(BooleanSupplier enabled, QueueRepository repository) {
        service = new QueueService(repository, enabled);
        generator = new QueueGenerator(service);
        debug = BetterRTP::debug;
    }

    public QueueHandler(
            BooleanSupplier enabled,
            QueueRepository repository,
            Supplier<RTP> rtp,
            IntSupplier chunkLoadTimeoutSeconds,
            Supplier<Logger> logger,
            Function<World, WORLD_TYPE> worldType,
            Consumer<String> debug) {
        service = new QueueService(repository, enabled);
        generator = new QueueGenerator(
                service, rtp, chunkLoadTimeoutSeconds, logger, worldType, debug);
        this.debug = debug;
    }

    /** @deprecated Use the plugin's {@link QueueHandler} instance. */
    @Deprecated(forRemoval = false)
    public static boolean isEnabled() {
        return BetterRTP.getInstance().getQueue().service.isEnabled();
    }

    public boolean enabled() {
        return service.isEnabled();
    }

    public void registerEvents(BetterRTP pl) {
        PluginManager pm = pl.getServer().getPluginManager();
        pm.registerEvents(this, pl);
    }

    public void unload() {
        generator.unload();
    }

    public void load() {
        generator.load();
    }

    @EventHandler
    public void onRTP(RTP_TeleportPostEvent e) {
        //Delete previously used location
        removeQueued(e.getLocation());
    }

    public QueueData claimRandom(RTPWorld world, QueueRange range) {
        List<QueueData> candidates = service.applicable(world, range);
        if (candidates.size() <= QueueLimits.REFILL_THRESHOLD) {
            generator.generate(world, range);
        }
        return QueueSelection.claimFirst(candidates, service::claim);
    }

    public List<QueueData> applicable(RTPWorld world, QueueRange range) {
        return service.applicable(world, range);
    }

    public void removeQueued(Location location) {
        if (!service.isEnabled()) return;
        AsyncHandler.async(() -> {
            if (service.remove(location)) {
                debug.accept("-Removed a queue world=" + location.getWorld().getName()
                        + ", x=" + location.getBlockX() + ", z=" + location.getBlockZ());
            }
        });
    }

    public QueueRange range(RTPWorld world) {
        return QueueRange.from(world);
    }

    /** @deprecated Use {@link #claimRandom(RTPWorld, QueueRange)}. */
    @Deprecated(forRemoval = false)
    public static QueueData getRandomAsync(RTPWorld rtpWorld, DatabaseQueue.QueueRangeData range) {
        return BetterRTP.getInstance().getQueue().claimRandom(rtpWorld, range.toRange());
    }

    public static QueueData getRandomAsync(RTPWorld rtpWorld, QueueRange range) {
        return BetterRTP.getInstance().getQueue().claimRandom(rtpWorld, range);
    }

    /** @deprecated Use {@link #applicable(RTPWorld, QueueRange)}. */
    @Deprecated(forRemoval = false)
    public static List<QueueData> getApplicableAsync(
            RTPWorld rtpWorld, DatabaseQueue.QueueRangeData range) {
        return BetterRTP.getInstance().getQueue().applicable(rtpWorld, range.toRange());
    }

    /** @deprecated Use {@link #removeQueued(Location)}. */
    @Deprecated(forRemoval = false)
    public static void remove(Location loc) {
        BetterRTP.getInstance().getQueue().removeQueued(loc);
    }

    public static boolean isInCircle(Location loc, RTPWorld rtpWorld) {
        return area(rtpWorld, RTP_SHAPE.CIRCLE).contains(loc.getBlockX(), loc.getBlockZ());
    }

    public static boolean isInSquare(Location loc, RTPWorld rtpWorld) {
        return area(rtpWorld, RTP_SHAPE.SQUARE).contains(loc.getBlockX(), loc.getBlockZ());
    }

    /** @deprecated Use {@link QueueRange#from(RTPWorld)}. */
    @Deprecated(forRemoval = false)
    public static DatabaseQueue.QueueRangeData snapshot(RTPWorld world) {
        return new DatabaseQueue.QueueRangeData(world);
    }

    private static RtpArea area(RTPWorld world, RTP_SHAPE shape) {
        return new RtpArea(world.getCenterX(), world.getCenterZ(),
                world.getMinRadius(), world.getMaxRadius(), shape);
    }
}

