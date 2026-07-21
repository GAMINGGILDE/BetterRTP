package me.SuperRonanCraft.BetterRTP.player.rtp;

import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Owns every active RTP request so lifecycle events can cancel it reliably. */
public final class RTPSessionManager {

    private final ConcurrentMap<UUID, RTPPlayer> sessions = new ConcurrentHashMap<>();

    boolean register(RTPPlayer session) {
        return sessions.putIfAbsent(session.getPlayerId(), session) == null;
    }

    boolean isActive(RTPPlayer session) {
        return sessions.get(session.getPlayerId()) == session;
    }

    public boolean isActive(Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    void finished(RTPPlayer session) {
        sessions.remove(session.getPlayerId(), session);
    }

    public void cancel(Player player) {
        RTPPlayer session = sessions.get(player.getUniqueId());
        if (session != null) {
            session.cancel();
        }
    }

    public void cancelAll() {
        sessions.values().forEach(RTPPlayer::cancel);
        sessions.clear();
    }
}
