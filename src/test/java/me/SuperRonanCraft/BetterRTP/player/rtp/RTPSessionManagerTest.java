package me.SuperRonanCraft.BetterRTP.player.rtp;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RTPSessionManagerTest {

    @Test
    void allowsOnlyOneSessionPerPlayer() {
        UUID playerId = UUID.randomUUID();
        RTP rtp = new RTP();
        RTPSessionManager manager = rtp.getSessions();
        RTPPlayer first = session(rtp, playerId);
        RTPPlayer second = session(rtp, playerId);

        assertTrue(manager.register(first));
        assertFalse(manager.register(second));
        assertTrue(manager.isActive(first));

        manager.finished(first);
        assertTrue(manager.register(second));
    }

    @Test
    void cancelAllClosesEverySession() {
        RTP rtp = new RTP();
        RTPSessionManager manager = rtp.getSessions();
        RTPPlayer first = session(rtp, UUID.randomUUID());
        RTPPlayer second = session(rtp, UUID.randomUUID());
        manager.register(first);
        manager.register(second);

        manager.cancelAll();

        assertFalse(manager.isActive(first));
        assertFalse(manager.isActive(second));
    }

    @Test
    void cancellationStopsTrackedFutures() {
        RTP rtp = new RTP();
        RTPSessionManager manager = rtp.getSessions();
        RTPPlayer session = session(rtp, UUID.randomUUID());
        CompletableFuture<Void> pending = new CompletableFuture<>();
        manager.register(session);
        session.track(pending);

        manager.cancelAll();

        assertTrue(pending.isCancelled());
        assertFalse(manager.isActive(session));
    }

    private static RTPPlayer session(RTP rtp, UUID playerId) {
        Player player = (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, arguments) -> {
                    if (method.getName().equals("getUniqueId")) {
                        return playerId;
                    }
                    if (method.getReturnType().isPrimitive()) {
                        return primitiveDefault(method.getReturnType());
                    }
                    return null;
                });
        RtpRequest request = new RtpRequest(
                player,
                playerId,
                player,
                RTP_TYPE.COMMAND,
                RtpWorldSnapshotTest.snapshot(List.of()),
                new RtpPlayerOptions(true, true, true, true, true));
        RtpCandidateFinder candidateFinder = new RtpCandidateFinder(
                (world, range) -> null,
                world -> null,
                (candidate, world, blocked) -> null,
                location -> true,
                location -> { });
        return new RTPPlayer(rtp, request, null, candidateFinder);
    }

    private static Object primitiveDefault(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0.0F;
        if (type == double.class) return 0.0D;
        if (type == char.class) return '\0';
        return null;
    }
}
