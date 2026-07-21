package me.SuperRonanCraft.BetterRTP.player.rtp;

import me.SuperRonanCraft.BetterRTP.references.database.DatabaseQueue;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.QueueData;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.QueueHandler;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.RandomLocation;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.RTPWorld;
import org.bukkit.Location;

import java.util.List;
import java.util.Objects;

/** Selects and validates RTP candidates without owning session scheduling. */
final class RtpCandidateFinder {

    private final QueueSource queueSource;
    private final LocationGenerator generator;
    private final SafetyValidator safetyValidator;
    private final RegionValidator regionValidator;
    private final CandidateDiscarder discarder;

    RtpCandidateFinder() {
        this(
                QueueHandler::getRandomAsync,
                RandomLocation::generateLocation,
                RandomLocation::getSafeLocation,
                RTPPluginValidation::checkLocation,
                QueueHandler::remove);
    }

    RtpCandidateFinder(
            QueueSource queueSource,
            LocationGenerator generator,
            SafetyValidator safetyValidator,
            RegionValidator regionValidator,
            CandidateDiscarder discarder) {
        this.queueSource = Objects.requireNonNull(queueSource, "queueSource");
        this.generator = Objects.requireNonNull(generator, "generator");
        this.safetyValidator = Objects.requireNonNull(safetyValidator, "safetyValidator");
        this.regionValidator = Objects.requireNonNull(regionValidator, "regionValidator");
        this.discarder = Objects.requireNonNull(discarder, "discarder");
    }

    Location select(
            Location suppliedLocation,
            RtpWorldSnapshot world,
            DatabaseQueue.QueueRangeData queueRange) {
        if (suppliedLocation != null) {
            return suppliedLocation;
        }
        QueueData queued = queueSource.find(world, queueRange);
        return queued == null ? generator.generate(world) : queued.getLocation();
    }

    Location validate(Location candidate, RtpWorldSnapshot world, List<String> blockedBlocks) {
        Location safe = safetyValidator.findSafe(candidate, world, blockedBlocks);
        if (safe == null || !regionValidator.isAllowed(safe)) {
            discarder.discard(candidate);
            return null;
        }
        return safe;
    }

    @FunctionalInterface
    interface QueueSource {
        QueueData find(RTPWorld world, DatabaseQueue.QueueRangeData range);
    }

    @FunctionalInterface
    interface LocationGenerator {
        Location generate(RTPWorld world);
    }

    @FunctionalInterface
    interface SafetyValidator {
        Location findSafe(Location candidate, RtpWorldSnapshot world, List<String> blockedBlocks);
    }

    @FunctionalInterface
    interface RegionValidator {
        boolean isAllowed(Location location);
    }

    @FunctionalInterface
    interface CandidateDiscarder {
        void discard(Location location);
    }
}
