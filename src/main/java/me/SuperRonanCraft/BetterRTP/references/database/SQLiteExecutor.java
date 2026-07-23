package me.SuperRonanCraft.BetterRTP.references.database;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.RejectedExecutionException;

public class SQLiteExecutor {
    private static ExecutorService executor = newExecutor();

    public static synchronized void start() {
        if (executor.isShutdown()) {
            executor = newExecutor();
        }
    }

    public static synchronized ExecutorService executor() {
        if (executor.isShutdown()) {
            throw new RejectedExecutionException("BetterRTP database executor is shut down");
        }
        return executor;
    }

    public static synchronized void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException exception) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static ExecutorService newExecutor() {
        return Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "BetterRTP-SQLite");
            thread.setDaemon(true);
            return thread;
        });
    }
}
