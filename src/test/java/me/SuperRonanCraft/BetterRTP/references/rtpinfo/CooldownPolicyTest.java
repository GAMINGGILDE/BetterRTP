package me.SuperRonanCraft.BetterRTP.references.rtpinfo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CooldownPolicyTest {

    @Test
    void calculatesRemainingTimeWithoutLosingMilliseconds() {
        assertEquals(4_750L, CooldownPolicy.remainingMillis(10_250L, 5L, 10_500L));
        assertEquals(0L, CooldownPolicy.remainingMillis(10_000L, 5L, 15_000L));
        assertEquals(-1L, CooldownPolicy.remainingMillis(10_000L, 5L, 15_001L));
    }

    @Test
    void locksOnlyAtConfiguredPositiveLimit() {
        assertFalse(CooldownPolicy.isLocked(100, 0));
        assertFalse(CooldownPolicy.isLocked(4, 5));
        assertTrue(CooldownPolicy.isLocked(5, 5));
        assertTrue(CooldownPolicy.isLocked(6, 5));
    }
}
