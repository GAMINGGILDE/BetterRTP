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

import java.util.ArrayList;
import java.util.List;

public class QueueHandler implements Listener { //Randomly queues up some safe locations

    private final QueueGenerator generator = new QueueGenerator();

    public static boolean isEnabled() {
        return BetterRTP.getInstance().getSettings().isQueueEnabled();
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
        remove(e.getLocation());
    }

    public static QueueData getRandomAsync(RTPWorld rtpWorld, DatabaseQueue.QueueRangeData range) {
        List<QueueData> queueData = getApplicableAsync(rtpWorld, range);
        if (queueData.size() <= QueueGenerator.QUEUE_MIN) {
            BetterRTP.getInstance().getQueue().generator.generate(rtpWorld, range);
        }
        return QueueSelection.claimFirst(queueData, DatabaseHandler.getQueue()::claim);
    }

    public static List<QueueData> getApplicableAsync(
            RTPWorld rtpWorld, DatabaseQueue.QueueRangeData range) {
        List<QueueData> available = new ArrayList<>();
        //Is Enabled??
        if (!isEnabled()) return available;
        List<QueueData> queueData = DatabaseHandler.getQueue().getInRange(range);
        for (QueueData data : queueData) {
            switch (rtpWorld.getShape()) {
                case CIRCLE:
                    if (isInCircle(data.location, rtpWorld))
                        available.add(data);
                    break;
                case SQUARE:
                default:
                    if (isInSquare(data.location, rtpWorld))
                        available.add(data);
            }
        }

        //BetterRTP.getInstance().getLogger().info("Centerx " + rtpWorld.getCenterX());
        //BetterRTP.getInstance().getLogger().info("Available: " + available.size());
        return available;
    }

    public static void remove(Location loc) {
        if (!isEnabled()) return;
        String worldName = loc.getWorld().getName();
        int blockX = loc.getBlockX();
        int blockZ = loc.getBlockZ();
        AsyncHandler.async(() -> {
            //Delete all queue data async
            if (DatabaseHandler.getQueue().removeLocation(worldName, blockX, blockZ)) {
                //BetterRTP.getInstance().getQueue().queueList.remove(data);
                BetterRTP.debug("-Removed a queue world=" + worldName + ", x=" + blockX + ", z=" + blockZ);
            }
        });
    }

    public static boolean isInCircle(Location loc, RTPWorld rtpWorld) {
        return area(rtpWorld, RTP_SHAPE.CIRCLE).contains(loc.getBlockX(), loc.getBlockZ());
    }

    public static boolean isInSquare(Location loc, RTPWorld rtpWorld) {
        return area(rtpWorld, RTP_SHAPE.SQUARE).contains(loc.getBlockX(), loc.getBlockZ());
    }

    public static DatabaseQueue.QueueRangeData snapshot(RTPWorld world) {
        return new DatabaseQueue.QueueRangeData(world);
    }

    private static RtpArea area(RTPWorld world, RTP_SHAPE shape) {
        return new RtpArea(world.getCenterX(), world.getCenterZ(),
                world.getMinRadius(), world.getMaxRadius(), shape);
    }
}

