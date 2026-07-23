package me.SuperRonanCraft.BetterRTP.references.rtpinfo;

import java.util.List;
import java.util.function.IntPredicate;

/** Selects and atomically claims the first still-available queue candidate. */
final class QueueSelection {

    private QueueSelection() {
    }

    static QueueData claimFirst(List<QueueData> candidates, IntPredicate claim) {
        for (QueueData candidate : candidates) {
            if (claim.test(candidate.getDatabaseId())) {
                return candidate;
            }
        }
        return null;
    }
}
