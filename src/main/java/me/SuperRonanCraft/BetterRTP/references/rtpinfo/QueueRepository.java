package me.SuperRonanCraft.BetterRTP.references.rtpinfo;

import org.bukkit.Location;

import java.util.List;

/** Persistence boundary for queued RTP locations. Calls are blocking. */
public interface QueueRepository {

    boolean isLoaded();

    List<QueueData> findInRange(QueueRange range);

    boolean claim(int databaseId);

    QueueData save(Location location);

    boolean remove(Location location);
}
