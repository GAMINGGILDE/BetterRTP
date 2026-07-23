package me.SuperRonanCraft.BetterRTP.references.rtpinfo;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.SuperRonanCraft.BetterRTP.BetterRTP;
import me.SuperRonanCraft.BetterRTP.player.commands.RTP_SETUP_TYPE;
import me.SuperRonanCraft.BetterRTP.player.rtp.RTP;
import me.SuperRonanCraft.BetterRTP.references.database.DatabaseHandler;
import me.SuperRonanCraft.BetterRTP.references.helpers.HelperRTP;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.RTPWorld;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WORLD_TYPE;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WorldCustom;
import me.SuperRonanCraft.BetterRTP.versions.AsyncHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QueueGenerator {

    public static final int QUEUE_MAX = QueueLimits.MAX_ENTRIES;
    public static final int QUEUE_MIN = QueueLimits.REFILL_THRESHOLD;
    private static final int MAX_ATTEMPTS_PER_TARGET = 50;

    private final QueueService queueService;
    private final Supplier<RTP> rtp;
    private final IntSupplier chunkLoadTimeoutSeconds;
    private final Supplier<Logger> logger;
    private final Function<World, WORLD_TYPE> worldType;
    private final Consumer<String> debug;

    private final AtomicBoolean running = new AtomicBoolean();
    private final AtomicBoolean stopped = new AtomicBoolean(true);
    private final AtomicBoolean rerunRequested = new AtomicBoolean();
    private final AtomicLong generation = new AtomicLong();
    private volatile QueueRequest pendingRequest;
    private volatile ScheduledTask task;
    private final Set<CompletableFuture<?>> pendingChunks = ConcurrentHashMap.newKeySet();

    @Deprecated(forRemoval = false)
    public QueueGenerator() {
        this(new QueueService(DatabaseHandler.getQueue(), QueueHandler::isEnabled));
    }

    QueueGenerator(QueueService queueService) {
        this(
                queueService,
                () -> BetterRTP.getInstance().getRTP(),
                () -> BetterRTP.getInstance().getSettings().getChunkLoadTimeoutSeconds(),
                () -> BetterRTP.getInstance().getLogger(),
                HelperRTP::getWorldType,
                BetterRTP::debug);
    }

    QueueGenerator(
            QueueService queueService,
            Supplier<RTP> rtp,
            IntSupplier chunkLoadTimeoutSeconds,
            Supplier<Logger> logger,
            Function<World, WORLD_TYPE> worldType,
            Consumer<String> debug) {
        this.queueService = queueService;
        this.rtp = rtp;
        this.chunkLoadTimeoutSeconds = chunkLoadTimeoutSeconds;
        this.logger = logger;
        this.worldType = worldType;
        this.debug = debug;
    }

    public void unload() {
        generation.incrementAndGet();
        stopped.set(true);
        rerunRequested.set(false);
        pendingRequest = null;
        ScheduledTask currentTask = task;
        task = null;
        if (currentTask != null) {
            currentTask.cancel();
        }
        pendingChunks.forEach(future -> future.cancel(true));
        pendingChunks.clear();
        running.set(false);
    }

    public void load() {
        unload();
        stopped.set(false);
        generate(null, null);
    }

    void generate(@Nullable RTPWorld rtpWorld, @Nullable QueueRange range) {
        if (!queueService.isEnabled() || stopped.get()) {
            return;
        }
        if (!running.compareAndSet(false, true)) {
            pendingRequest = rtpWorld == null ? null : new QueueRequest(rtpWorld, range);
            rerunRequested.set(true);
            return;
        }
        waitForDatabase(rtpWorld, range, generation.get());
    }

    private void waitForDatabase(@Nullable RTPWorld requestedWorld,
                                 @Nullable QueueRange range, long runId) {
        task = AsyncHandler.asyncLater(() -> {
            if (isObsolete(runId)) {
                return;
            }
            if (!queueService.isReady()) {
                waitForDatabase(requestedWorld, range, runId);
                return;
            }

            debug.accept("Checking RTP location queues...");
            if (requestedWorld != null) {
                processTargets(List.of(new QueueTarget(
                        requestedWorld, range, targetId(requestedWorld, range))), 0, 0, runId);
            } else {
                collectTargets(runId);
            }
        }, 10L);
    }

    private void collectTargets(long runId) {
        AsyncHandler.global(() -> {
            if (isObsolete(runId)) {
                return;
            }

            List<QueueTarget> targets = new ArrayList<>();
            RTP rtpSettings = rtp.get();
            addConfiguredTargets(targets, RTP_SETUP_TYPE.LOCATION, rtpSettings.getRTPworldLocations());
            addConfiguredTargets(targets, RTP_SETUP_TYPE.CUSTOM_WORLD, rtpSettings.getRTPcustomWorld());
            for (World world : Bukkit.getWorlds()) {
                if (!rtpSettings.getDisabledWorlds().contains(world.getName())
                        && !rtpSettings.getRTPcustomWorld().containsKey(world.getName())) {
                    RTPWorld targetWorld = new WorldCustom(world, rtpSettings.getRTPdefaultWorld());
                    targets.add(new QueueTarget(targetWorld, QueueRange.from(targetWorld),
                            "default_" + world.getName()));
                }
            }
            AsyncHandler.async(() -> processTargets(List.copyOf(targets), 0, 0, runId));
        });
    }

    private void addConfiguredTargets(List<QueueTarget> targets, RTP_SETUP_TYPE type,
                                      Map<String, RTPWorld> worlds) {
        for (Map.Entry<String, RTPWorld> entry : worlds.entrySet()) {
            String prefix = type == RTP_SETUP_TYPE.LOCATION ? "location_" : "custom_";
            targets.add(new QueueTarget(
                    entry.getValue(), QueueRange.from(entry.getValue()), prefix + entry.getKey()));
        }
    }

    private void processTargets(List<QueueTarget> targets, int index, int attempts, long runId) {
        if (isObsolete(runId)) {
            return;
        }
        if (index >= targets.size()) {
            debug.accept("RTP location queues are ready.");
            finishRun(runId);
            return;
        }

        QueueTarget target = targets.get(index);
        try {
            int available = queueService.applicable(target.world(), target.range()).size();
            if (available >= QUEUE_MIN) {
                processTargets(targets, index + 1, 0, runId);
                return;
            }
            if (attempts >= MAX_ATTEMPTS_PER_TARGET) {
                debug.accept("Unable to fill queue " + target.id() + " after " + attempts
                        + " attempts (amount: " + available + ")");
                processTargets(targets, index + 1, 0, runId);
                return;
            }
            generateCandidate(targets, index, attempts, target, runId);
        } catch (Throwable throwable) {
            logger.get().log(
                    Level.WARNING, "Unable to inspect RTP queue " + target.id(), throwable);
            processTargets(targets, index + 1, 0, runId);
        }
    }

    private void generateCandidate(List<QueueTarget> targets, int index, int attempts,
                                   QueueTarget target, long runId) {
        Location candidate = RandomLocation.generateLocation(target.world());
        if (candidate == null || candidate.getWorld() == null) {
            processTargets(targets, index, attempts + 1, runId);
            return;
        }

        AsyncHandler.syncAtLocation(candidate, () -> {
            if (isObsolete(runId)) {
                return;
            }
            try {
                CompletableFuture<?> chunkFuture = candidate.getWorld().getChunkAtAsync(candidate)
                        .orTimeout(chunkLoadTimeoutSeconds.getAsInt(), TimeUnit.SECONDS);
                pendingChunks.add(chunkFuture);
                chunkFuture.whenComplete((chunk, throwable) -> {
                    pendingChunks.remove(chunkFuture);
                    if (isObsolete(runId)) {
                        return;
                    }
                    if (throwable != null) {
                        logger.get().log(Level.WARNING,
                                "Unable to load a queued RTP chunk at " + candidate, throwable);
                        continueAsync(targets, index, attempts + 1, runId);
                        return;
                    }
                    AsyncHandler.syncAtLocation(candidate,
                            () -> validateCandidate(targets, index, attempts, target, candidate, runId));
                });
            } catch (Throwable throwable) {
                logger.get().log(Level.WARNING,
                        "Unable to start loading a queued RTP chunk at " + candidate, throwable);
                continueAsync(targets, index, attempts + 1, runId);
            }
        });
    }

    private void validateCandidate(List<QueueTarget> targets, int index, int attempts,
                                   QueueTarget target, Location candidate, long runId) {
        if (isObsolete(runId)) {
            return;
        }

        final Location safeLocation;
        try {
            safeLocation = RandomLocation.getSafeLocation(
                    worldType.apply(target.world().getWorld()), candidate.getWorld(), candidate,
                    target.world().getMinY(), target.world().getMaxY(), target.world().getBiomes());
        } catch (Throwable throwable) {
            logger.get().log(Level.WARNING,
                    "Unable to validate a queued RTP location at " + candidate, throwable);
            continueAsync(targets, index, attempts + 1, runId);
            return;
        }
        if (safeLocation == null) {
            continueAsync(targets, index, attempts + 1, runId);
            return;
        }

        String worldName = safeLocation.getWorld().getName();
        int blockX = safeLocation.getBlockX();
        int blockZ = safeLocation.getBlockZ();
        QueuePosition position = QueuePosition.capture(safeLocation);
        AsyncHandler.async(() -> {
            QueueData data = queueService.save(position);
            if (data != null) {
                debug.accept("Queue position generated: id=" + target.id()
                        + ", databaseId=" + data.getDatabaseId()
                        + ", world=" + worldName + ", x=" + blockX + ", z=" + blockZ);
            }
            processTargets(targets, index, attempts + 1, runId);
        });
    }

    private void continueAsync(List<QueueTarget> targets, int index, int attempts, long runId) {
        AsyncHandler.async(() -> processTargets(targets, index, attempts, runId));
    }

    private void finishRun(long runId) {
        if (generation.get() != runId) {
            return;
        }
        task = null;
        running.set(false);
        if (!stopped.get() && rerunRequested.compareAndSet(true, false)) {
            QueueRequest request = pendingRequest;
            pendingRequest = null;
            generate(request == null ? null : request.world(), request == null ? null : request.range());
        }
    }

    private boolean isObsolete(long runId) {
        return stopped.get() || generation.get() != runId;
    }

    private static String targetId(RTPWorld world, QueueRange range) {
        return "rtp_" + (world.getID() != null ? world.getID() : range.worldName());
    }

    private record QueueTarget(RTPWorld world, QueueRange range, String id) {
    }

    private record QueueRequest(RTPWorld world, QueueRange range) {
    }
}
