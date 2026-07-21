package me.SuperRonanCraft.BetterRTP.player.rtp;

import lombok.Getter;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.SuperRonanCraft.BetterRTP.references.customEvents.RTP_FailedEvent;
import me.SuperRonanCraft.BetterRTP.references.customEvents.RTP_FindLocationEvent;
import me.SuperRonanCraft.BetterRTP.references.depends.DepEconomy;
import me.SuperRonanCraft.BetterRTP.references.database.DatabaseQueue;
import me.SuperRonanCraft.BetterRTP.references.helpers.HelperRTP_Check;
import me.SuperRonanCraft.BetterRTP.references.messages.MessagesCore;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.QueueHandler;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WorldPlayer;
import me.SuperRonanCraft.BetterRTP.versions.AsyncHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * Holds the state of one teleport request and advances it across the Folia
 * entity, region and async schedulers.
 */
public class RTPPlayer {

    @Getter private final Player player;
    @Getter private final UUID playerId;
    private final RTP settings;
    @Getter private final WorldPlayer worldPlayer;
    @Getter private final RTP_TYPE type;
    @Getter private final RtpRequest request;
    private final AtomicInteger attempts = new AtomicInteger();
    private final AtomicBoolean finished = new AtomicBoolean();
    private volatile RTPTransaction transaction;
    private final Set<CompletableFuture<?>> pendingFutures = ConcurrentHashMap.newKeySet();
    private final Set<ScheduledTask> pendingTasks = ConcurrentHashMap.newKeySet();
    private final DatabaseQueue.QueueRangeData queueRange;
    private final List<String> blockedBlocks;
    private final RtpCandidateFinder candidateFinder;

    RTPPlayer(Player player, RTP settings, WorldPlayer worldPlayer, RTP_TYPE type) {
        this(player, settings, worldPlayer, type, new RtpCandidateFinder());
    }

    RTPPlayer(
            Player player, RTP settings, WorldPlayer worldPlayer, RTP_TYPE type,
            RtpCandidateFinder candidateFinder) {
        this.player = player;
        this.playerId = player.getUniqueId();
        this.settings = settings;
        this.worldPlayer = worldPlayer;
        this.type = type;
        this.request = worldPlayer == null ? null : RtpRequest.from(worldPlayer);
        this.queueRange = request == null ? null : QueueHandler.snapshot(request.world());
        this.blockedBlocks = settings.getBlockList() == null
                ? List.of() : List.copyOf(settings.getBlockList());
        this.candidateFinder = candidateFinder;
    }

    public int getAttempts() {
        return attempts.get();
    }

    void randomlyTeleport(CommandSender sender) {
        if (!isActive()) {
            return;
        }
        if (attempts.get() >= settings.maxAttempts) {
            fail(sender);
            return;
        }

        attempts.incrementAndGet();
        RTP_FindLocationEvent event = new RTP_FindLocationEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            finish();
            return;
        }

        Location suppliedLocation = event.getLocation();
        AsyncHandler.async(() -> findCandidate(sender, suppliedLocation));
    }

    private void findCandidate(CommandSender sender, Location suppliedLocation) {
        if (!isActive()) {
            return;
        }

        try {
            Location candidate = candidateFinder.select(
                    suppliedLocation, request.world(), queueRange);

            if (candidate == null || candidate.getWorld() == null) {
                retry(sender);
                return;
            }
            loadCandidateChunk(sender, candidate);
        } catch (Throwable throwable) {
            settings.logger().log(Level.WARNING, "Unable to generate an RTP candidate", throwable);
            retry(sender);
        }
    }

    private void loadCandidateChunk(CommandSender sender, Location candidate) {
        AsyncHandler.syncAtLocation(candidate, () -> {
            if (!isActive()) {
                return;
            }

            CompletableFuture<?> chunkFuture;
            try {
                chunkFuture = candidate.getWorld().getChunkAtAsync(candidate);
            } catch (Throwable throwable) {
                settings.logger().log(Level.WARNING,
                        "Unable to start loading an RTP chunk at " + candidate, throwable);
                retry(sender);
                return;
            }

            track(chunkFuture.orTimeout(
                    settings.pluginSettings().getChunkLoadTimeoutSeconds(), TimeUnit.SECONDS))
                    .whenComplete((chunk, throwable) -> {
                if (throwable != null) {
                    settings.logger().log(Level.WARNING,
                            "Unable to load an RTP chunk at " + candidate, throwable);
                    retry(sender);
                    return;
                }
                AsyncHandler.syncAtLocation(candidate, () -> validateCandidate(sender, candidate));
            });
        });
    }

    private void validateCandidate(CommandSender sender, Location candidate) {
        if (!isActive()) {
            return;
        }

        try {
            Location safeLocation = candidateFinder.validate(
                    candidate, request.world(), blockedBlocks);

            if (safeLocation == null) {
                retry(sender);
                return;
            }

            safeLocation.add(0.5, 0, 0.5);
            AsyncHandler.syncAtEntity(player, () -> completeTeleport(sender, safeLocation));
        } catch (Throwable throwable) {
            settings.logger().log(Level.WARNING,
                    "Unable to validate an RTP candidate at " + candidate, throwable);
            retry(sender);
        }
    }

    private void completeTeleport(CommandSender sender, Location location) {
        if (!isActive()) {
            return;
        }

        DepEconomy.Reservation reservation = settings.economy().reserve(request);
        if (!reservation.successful()) {
            notifyReservationFailure(sender, reservation.failure());
            finish();
            return;
        }

        boolean applyCooldown = request.options().applyCooldown()
                && HelperRTP_Check.applyCooldown(player);
        transaction = new RTPTransaction(
                () -> {
                    try {
                        reservation.commitHunger();
                    } finally {
                        if (applyCooldown) {
                            settings.cooldowns().add(player, request.world().world());
                        }
                    }
                },
                reservation::rollback);

        location.setYaw(player.getLocation().getYaw());
        location.setPitch(player.getLocation().getPitch());
        settings.getTeleport().sendPlayer(sender, this, location);
    }

    private void notifyReservationFailure(CommandSender sender, DepEconomy.Failure failure) {
        MessagesCore message = failure == DepEconomy.Failure.HUNGER
                ? MessagesCore.FAILED_HUNGER
                : MessagesCore.FAILED_PRICE;
        message.send(player, worldPlayer);
        if (sender != player) {
            AsyncHandler.syncAtSender(sender, () -> message.send(sender, worldPlayer));
        }
    }

    private void retry(CommandSender sender) {
        if (isActive()) {
            AsyncHandler.syncAtEntity(player, () -> randomlyTeleport(sender));
        }
    }

    private void fail(CommandSender sender) {
        if (!finished.compareAndSet(false, true)) {
            return;
        }
        try {
            settings.getTeleport().failedTeleport(player, sender);
            Bukkit.getPluginManager().callEvent(new RTP_FailedEvent(this));
        } finally {
            closeSession();
        }
    }

    void cancel() {
        finish();
    }

    <T> CompletableFuture<T> track(CompletableFuture<T> future) {
        pendingFutures.add(future);
        future.whenComplete((result, throwable) -> pendingFutures.remove(future));
        return future;
    }

    void track(ScheduledTask task) {
        if (task != null) {
            pendingTasks.add(task);
        }
    }

    void finish() {
        if (finished.compareAndSet(false, true)) {
            try {
                RTPTransaction currentTransaction = transaction;
                if (currentTransaction != null) {
                    currentTransaction.rollback();
                }
            } finally {
                closeSession();
            }
        }
    }

    void completeSuccessfully() {
        if (finished.compareAndSet(false, true)) {
            try {
                RTPTransaction currentTransaction = transaction;
                if (currentTransaction != null) {
                    currentTransaction.commit();
                }
            } finally {
                closeSession();
            }
        }
    }

    boolean isActive() {
        return !finished.get() && settings.getSessions().isActive(this);
    }

    RTP runtime() {
        return settings;
    }

    private void closeSession() {
        pendingFutures.forEach(future -> future.cancel(true));
        pendingFutures.clear();
        pendingTasks.forEach(ScheduledTask::cancel);
        pendingTasks.clear();
        settings.getSessions().finished(this);
    }
}
