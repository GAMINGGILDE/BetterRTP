package me.SuperRonanCraft.BetterRTP.references.rtpinfo;

/** Pure cooldown calculations, independent of Bukkit and wall-clock access. */
final class CooldownPolicy {

    private CooldownPolicy() {
    }

    static long remainingMillis(long startedAtMillis, long durationSeconds, long nowMillis) {
        return startedAtMillis + durationSeconds * 1_000L - nowMillis;
    }

    static boolean isLocked(int rtpCount, int lockedAfter) {
        return lockedAfter > 0 && rtpCount >= lockedAfter;
    }
}
