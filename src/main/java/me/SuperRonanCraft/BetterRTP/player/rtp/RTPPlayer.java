package me.SuperRonanCraft.BetterRTP.player.rtp;

import lombok.Getter;
import me.SuperRonanCraft.BetterRTP.BetterRTP;
import me.SuperRonanCraft.BetterRTP.references.customEvents.RTP_FailedEvent;
import me.SuperRonanCraft.BetterRTP.references.customEvents.RTP_FindLocationEvent;
import me.SuperRonanCraft.BetterRTP.references.helpers.HelperRTP_Check;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.QueueData;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.QueueHandler;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.RandomLocation;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WorldPlayer;
import me.SuperRonanCraft.BetterRTP.versions.AsyncHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * Holds the state of one teleport request and advances it across the Folia
 * entity, region and async schedulers.
 */
public class RTPPlayer {

    @Getter private final Player player;
    private final RTP settings;
    @Getter private final WorldPlayer worldPlayer;
    @Getter private final RTP_TYPE type;
    private final AtomicInteger attempts = new AtomicInteger();
    private final AtomicBoolean finished = new AtomicBoolean();

    RTPPlayer(Player player, RTP settings, WorldPlayer worldPlayer, RTP_TYPE type) {
        this.player = player;
        this.settings = settings;
        this.worldPlayer = worldPlayer;
        this.type = type;
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
            Location candidate = suppliedLocation;
            if (candidate == null) {
                QueueData queueData = QueueHandler.getRandomAsync(worldPlayer);
                candidate = queueData != null
                        ? queueData.getLocation()
                        : RandomLocation.generateLocation(worldPlayer);
            }

            if (candidate == null || candidate.getWorld() == null) {
                retry(sender);
                return;
            }
            loadCandidateChunk(sender, candidate);
        } catch (Throwable throwable) {
            getPl().getLogger().log(Level.WARNING, "Unable to generate an RTP candidate", throwable);
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
                getPl().getLogger().log(Level.WARNING,
                        "Unable to start loading an RTP chunk at " + candidate, throwable);
                retry(sender);
                return;
            }

            chunkFuture.whenComplete((chunk, throwable) -> {
                if (throwable != null) {
                    getPl().getLogger().log(Level.WARNING,
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
            Location safeLocation = RandomLocation.getSafeLocation(
                    worldPlayer.getWorldtype(), worldPlayer.getWorld(), candidate,
                    worldPlayer.getMinY(), worldPlayer.getMaxY(), worldPlayer.getBiomes());

            if (safeLocation == null || !RTPPluginValidation.checkLocation(safeLocation)) {
                QueueHandler.remove(candidate);
                retry(sender);
                return;
            }

            safeLocation.add(0.5, 0, 0.5);
            AsyncHandler.syncAtEntity(player, () -> completeTeleport(sender, safeLocation));
        } catch (Throwable throwable) {
            getPl().getLogger().log(Level.WARNING,
                    "Unable to validate an RTP candidate at " + candidate, throwable);
            retry(sender);
        }
    }

    private void completeTeleport(CommandSender sender, Location location) {
        if (!isActive()) {
            return;
        }

        if (!getPl().getEco().charge(player, worldPlayer)) {
            if (worldPlayer.getPlayerInfo().isApplyCooldown()) {
                getPl().getCooldowns().removeCooldown(player, worldPlayer.getWorld());
            }
            finish();
            return;
        }

        if (worldPlayer.getPlayerInfo().isApplyCooldown() && HelperRTP_Check.applyCooldown(player)) {
            getPl().getCooldowns().add(player, worldPlayer.getWorld());
        }
        location.setYaw(player.getLocation().getYaw());
        location.setPitch(player.getLocation().getPitch());
        settings.teleport.sendPlayer(sender, this, location);
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
            settings.teleport.failedTeleport(player, sender);
            getPl().getCooldowns().removeCooldown(player, worldPlayer.getWorld());
            Bukkit.getPluginManager().callEvent(new RTP_FailedEvent(this));
        } finally {
            getPl().getPInfo().endTeleport(player);
        }
    }

    void cancel() {
        if (finished.compareAndSet(false, true)) {
            getPl().getCooldowns().removeCooldown(player, worldPlayer.getWorld());
            getPl().getPInfo().endTeleport(player);
        }
    }

    void finish() {
        if (finished.compareAndSet(false, true)) {
            getPl().getPInfo().endTeleport(player);
        }
    }

    boolean isActive() {
        return !finished.get() && getPl().getPInfo().isTeleporting(player);
    }

    private BetterRTP getPl() {
        return BetterRTP.getInstance();
    }
}
