package me.SuperRonanCraft.BetterRTP.references.rtpinfo;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class QueueSelectionTest {

    @Test
    void skipsCandidatesClaimedByAnotherRequest() {
        QueueData first = new QueueData(null, 100L, 1);
        QueueData second = new QueueData(null, 200L, 2);
        List<Integer> attempts = new ArrayList<>();

        QueueData selected = QueueSelection.claimFirst(List.of(first, second), id -> {
            attempts.add(id);
            return id == 2;
        });

        assertSame(second, selected);
        assertEquals(List.of(1, 2), attempts);
    }

    @Test
    void returnsNullWhenNoCandidateCanBeClaimed() {
        QueueData candidate = new QueueData(null, 100L, 1);

        assertNull(QueueSelection.claimFirst(List.of(candidate), ignored -> false));
    }

    @Test
    void doesNotInvokeClaimWithoutCandidates() {
        assertNull(QueueSelection.claimFirst(List.of(), ignored -> {
            throw new AssertionError("claim must not be invoked");
        }));
    }
}
