package me.SuperRonanCraft.BetterRTP.player.rtp;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RTPTransactionTest {

    @Test
    void commitsExactlyOnce() {
        AtomicInteger commits = new AtomicInteger();
        AtomicInteger rollbacks = new AtomicInteger();
        RTPTransaction transaction = new RTPTransaction(commits::incrementAndGet, rollbacks::incrementAndGet);

        assertTrue(transaction.commit());
        assertFalse(transaction.commit());
        assertFalse(transaction.rollback());
        assertEquals(1, commits.get());
        assertEquals(0, rollbacks.get());
        assertEquals(RTPTransaction.State.COMMITTED, transaction.state());
    }

    @Test
    void rollsBackExactlyOnce() {
        AtomicInteger commits = new AtomicInteger();
        AtomicInteger rollbacks = new AtomicInteger();
        RTPTransaction transaction = new RTPTransaction(commits::incrementAndGet, rollbacks::incrementAndGet);

        assertTrue(transaction.rollback());
        assertFalse(transaction.rollback());
        assertFalse(transaction.commit());
        assertEquals(0, commits.get());
        assertEquals(1, rollbacks.get());
        assertEquals(RTPTransaction.State.ROLLED_BACK, transaction.state());
    }

    @Test
    void concurrentCompletionHasOnlyOneWinner() throws Exception {
        AtomicInteger commits = new AtomicInteger();
        AtomicInteger rollbacks = new AtomicInteger();
        RTPTransaction transaction = new RTPTransaction(commits::incrementAndGet, rollbacks::incrementAndGet);
        ExecutorService executor = Executors.newFixedThreadPool(8);
        CountDownLatch start = new CountDownLatch(1);

        try {
            List<Future<Boolean>> results = new ArrayList<>();
            for (int index = 0; index < 100; index++) {
                boolean commit = index % 2 == 0;
                results.add(executor.submit(() -> {
                    start.await();
                    return commit ? transaction.commit() : transaction.rollback();
                }));
            }
            start.countDown();

            int successfulCompletions = 0;
            for (Future<Boolean> result : results) {
                if (result.get()) {
                    successfulCompletions++;
                }
            }

            assertEquals(1, successfulCompletions);
            assertEquals(1, commits.get() + rollbacks.get());
            assertNotEquals(RTPTransaction.State.RESERVED, transaction.state());
        } finally {
            executor.shutdownNow();
        }
    }
}
