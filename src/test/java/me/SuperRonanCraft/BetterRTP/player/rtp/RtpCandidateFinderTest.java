package me.SuperRonanCraft.BetterRTP.player.rtp;

import me.SuperRonanCraft.BetterRTP.references.rtpinfo.QueueData;
import org.bukkit.Location;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RtpCandidateFinderTest {

    @Test
    void suppliedLocationBypassesQueueAndRandomGeneration() {
        AtomicBoolean sourceUsed = new AtomicBoolean();
        RtpWorldSnapshot world = RtpWorldSnapshotTest.snapshot(List.of());
        Location supplied = new Location(world.world(), 1, 2, 3);
        RtpCandidateFinder finder = finder(
                (settings, range) -> {
                    sourceUsed.set(true);
                    return null;
                },
                settings -> {
                    sourceUsed.set(true);
                    return null;
                });

        assertSame(supplied, finder.select(supplied, world, null));
        assertFalse(sourceUsed.get());
    }

    @Test
    void queuedCandidateWinsAndRandomLocationIsTheFallback() {
        RtpWorldSnapshot world = RtpWorldSnapshotTest.snapshot(List.of());
        Location queued = new Location(world.world(), 10, 20, 30);
        Location generated = new Location(world.world(), 40, 50, 60);

        RtpCandidateFinder withQueue = finder(
                (settings, range) -> new QueueData(queued, 1L, 7),
                settings -> generated);
        RtpCandidateFinder withoutQueue = finder(
                (settings, range) -> null,
                settings -> generated);

        assertSame(queued, withQueue.select(null, world, null));
        assertSame(generated, withoutQueue.select(null, world, null));
    }

    @Test
    void rejectedSafeLocationDiscardsTheOriginalCandidate() {
        AtomicBoolean discarded = new AtomicBoolean();
        RtpWorldSnapshot world = RtpWorldSnapshotTest.snapshot(List.of());
        Location candidate = new Location(world.world(), 10, 20, 30);
        Location safe = new Location(world.world(), 10, 21, 30);
        RtpCandidateFinder finder = new RtpCandidateFinder(
                (settings, range) -> null,
                settings -> null,
                (location, settings, blocked) -> safe,
                location -> false,
                location -> discarded.set(location == candidate));

        assertNull(finder.validate(candidate, world, List.of("LAVA")));
        assertTrue(discarded.get());
    }

    private static RtpCandidateFinder finder(
            RtpCandidateFinder.QueueSource queue,
            RtpCandidateFinder.LocationGenerator generator) {
        return new RtpCandidateFinder(
                queue,
                generator,
                (candidate, world, blocked) -> candidate,
                location -> true,
                location -> { });
    }
}
