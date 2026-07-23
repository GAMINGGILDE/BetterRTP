package me.SuperRonanCraft.BetterRTP.references.rtpinfo;

import org.bukkit.Location;

import lombok.Getter;
import lombok.Setter;

public class QueueData {

    @Getter final int databaseId;
    @Getter @Setter Location location;
    @Getter final long generated;

    public QueueData(Location location, long generated, int databaseId) {
        this.location = location;
        this.generated = generated;
        this.databaseId = databaseId;
    }

}
