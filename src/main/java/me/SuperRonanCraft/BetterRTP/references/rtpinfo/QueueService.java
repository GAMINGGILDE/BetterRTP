package me.SuperRonanCraft.BetterRTP.references.rtpinfo;

import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.RTPWorld;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;

/** Coordinates queue selection independently of Bukkit lifecycle and SQLite. */
public final class QueueService {

    private final QueueRepository repository;
    private final BooleanSupplier enabled;

    public QueueService(QueueRepository repository, BooleanSupplier enabled) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.enabled = Objects.requireNonNull(enabled, "enabled");
    }

    public boolean isEnabled() {
        return enabled.getAsBoolean();
    }

    public boolean isReady() {
        return isEnabled() && repository.isLoaded();
    }

    public List<QueueData> applicable(RTPWorld world, QueueRange range) {
        if (!isEnabled()) {
            return List.of();
        }
        RtpArea area = new RtpArea(
                world.getCenterX(), world.getCenterZ(),
                world.getMinRadius(), world.getMaxRadius(), world.getShape());
        return repository.findInRange(range).stream()
                .filter(data -> area.contains(
                        data.getLocation().getBlockX(), data.getLocation().getBlockZ()))
                .toList();
    }

    public QueueData claimFirst(RTPWorld world, QueueRange range) {
        return QueueSelection.claimFirst(applicable(world, range), repository::claim);
    }

    public boolean claim(int databaseId) {
        return repository.claim(databaseId);
    }

    public QueueData save(QueuePosition position) {
        return repository.save(position);
    }

    public boolean remove(QueuePosition position) {
        return isEnabled() && repository.remove(position);
    }
}
