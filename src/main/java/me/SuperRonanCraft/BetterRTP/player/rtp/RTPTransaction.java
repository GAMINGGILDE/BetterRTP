package me.SuperRonanCraft.BetterRTP.player.rtp;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/** Executes exactly one terminal action for an RTP reservation. */
public final class RTPTransaction {

    private final Runnable commitAction;
    private final Runnable rollbackAction;
    private final AtomicReference<State> state = new AtomicReference<>(State.RESERVED);

    public RTPTransaction(Runnable commitAction, Runnable rollbackAction) {
        this.commitAction = Objects.requireNonNull(commitAction, "commitAction");
        this.rollbackAction = Objects.requireNonNull(rollbackAction, "rollbackAction");
    }

    public boolean commit() {
        if (!state.compareAndSet(State.RESERVED, State.COMMITTED)) {
            return false;
        }
        commitAction.run();
        return true;
    }

    public boolean rollback() {
        if (!state.compareAndSet(State.RESERVED, State.ROLLED_BACK)) {
            return false;
        }
        rollbackAction.run();
        return true;
    }

    public State state() {
        return state.get();
    }

    public enum State {
        RESERVED,
        COMMITTED,
        ROLLED_BACK
    }
}
