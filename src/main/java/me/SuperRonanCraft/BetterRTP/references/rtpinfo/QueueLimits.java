package me.SuperRonanCraft.BetterRTP.references.rtpinfo;

/** Shared queue capacity policy, independent of persistence and generation. */
public final class QueueLimits {

    public static final int MAX_ENTRIES = 32;
    public static final int REFILL_THRESHOLD = 2;
    public static final int DATABASE_FETCH_LIMIT = MAX_ENTRIES + 1;

    private QueueLimits() {
    }
}
