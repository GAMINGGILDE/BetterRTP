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
import java.util.Objects;

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

    public static QueueData getRandomAsync(RTPWorld rtpWorld) {
        List<QueueData> queueData = getApplicableAsync(rtpWorld);
        if (queueData.size() <= QueueGenerator.QUEUE_MIN) {
            BetterRTP.getInstance().getQueue().generator.generate(rtpWorld);
        }
        for (QueueData candidate : queueData) {
            if (DatabaseHandler.getQueue().claim(candidate.getDatabaseId())) {
                return candidate;
            }
        }
        return null;
    }

    public static List<QueueData> getApplicableAsync(RTPWorld rtpWorld) {
        List<QueueData> available = new ArrayList<>();
        //Is Enabled??
        if (!isEnabled()) return available;
        List<QueueData> queueData = DatabaseHandler.getQueue().getInRange(new DatabaseQueue.QueueRangeData(rtpWorld));
        for (QueueData data : queueData) {
            if (!Objects.equals(data.getLocation().getWorld().getName(), rtpWorld.getWorld().getName()))
                continue;
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
        AsyncHandler.async(() -> {
            //Delete all queue data async
            if (DatabaseHandler.getQueue().removeLocation(loc)) {
                //BetterRTP.getInstance().getQueue().queueList.remove(data);
                BetterRTP.debug("-Removed a queue " + loc);
            }
        });
    }

    public static boolean isInCircle(Location loc, RTPWorld rtpWorld) {
        return area(rtpWorld, RTP_SHAPE.CIRCLE).contains(loc.getBlockX(), loc.getBlockZ());
    }

    public static boolean isInSquare(Location loc, RTPWorld rtpWorld) {
        return area(rtpWorld, RTP_SHAPE.SQUARE).contains(loc.getBlockX(), loc.getBlockZ());
    }

    private static RtpArea area(RTPWorld world, RTP_SHAPE shape) {
        return new RtpArea(world.getCenterX(), world.getCenterZ(),
                world.getMinRadius(), world.getMaxRadius(), shape);
    }
}

