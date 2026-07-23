package me.SuperRonanCraft.BetterRTP.references.database;

import lombok.Getter;
import me.SuperRonanCraft.BetterRTP.BetterRTP;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.QueueHandler;
import me.SuperRonanCraft.BetterRTP.versions.AsyncHandler;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class DatabaseHandler {

    private final AtomicBoolean started = new AtomicBoolean();

    @Getter private final DatabasePlayers databasePlayers = new DatabasePlayers();
    @Getter private final DatabaseCooldowns databaseCooldowns = new DatabaseCooldowns();
    @Getter private final DatabaseQueue databaseQueue;
    @Getter private final DatabaseChunkData databaseChunks = new DatabaseChunkData();

    @Deprecated(forRemoval = false)
    public DatabaseHandler() {
        this(QueueHandler::isEnabled);
    }

    public DatabaseHandler(BooleanSupplier queueEnabled) {
        databaseQueue = new DatabaseQueue(queueEnabled);
    }

    public void load() {
        if (!started.compareAndSet(false, true)) {
            refreshWorlds();
            return;
        }
        SQLiteExecutor.start();
        AsyncHandler.global(() -> {
            databaseCooldowns.setWorldNames(Bukkit.getWorlds().stream().map(World::getName).toList());
            databasePlayers.load();
            databaseCooldowns.load();
            databaseQueue.load();
            databaseChunks.load();
        });
    }

    public void shutdown() {
        SQLiteExecutor.shutdown();
        started.set(false);
    }

    public void refreshWorlds() {
        AsyncHandler.global(() -> {
            databaseCooldowns.setWorldNames(Bukkit.getWorlds().stream().map(World::getName).toList());
            databaseCooldowns.load();
        });
    }

    public static DatabasePlayers getPlayers() {
        return BetterRTP.getInstance().getDatabaseHandler().getDatabasePlayers();
    }

    public static DatabaseCooldowns getCooldowns() {
        return BetterRTP.getInstance().getDatabaseHandler().getDatabaseCooldowns();
    }

    public static DatabaseQueue getQueue() {
        return BetterRTP.getInstance().getDatabaseHandler().getDatabaseQueue();
    }

    //public static DatabaseChunkData getChunks() {
    //    return BetterRTP.getInstance().getDatabaseHandler().getDatabaseChunks();
    //}

}
