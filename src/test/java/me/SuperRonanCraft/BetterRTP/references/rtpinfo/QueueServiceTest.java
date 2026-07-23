package me.SuperRonanCraft.BetterRTP.references.rtpinfo;

import me.SuperRonanCraft.BetterRTP.player.rtp.RTP_SHAPE;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.RTPWorld;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

class QueueServiceTest {

    @Test
    void filtersRepositoryRowsByTheRequestedArea() {
        World world = world();
        RTPWorld settings = settings(world);
        QueueData inside = data(world, 20, 0, 1);
        QueueData innerHole = data(world, 5, 5, 2);
        QueueData outside = data(world, 101, 0, 3);
        FakeRepository repository = new FakeRepository(List.of(inside, innerHole, outside));
        QueueService service = new QueueService(repository, () -> true);

        assertEquals(List.of(inside), service.applicable(settings, QueueRange.from(settings)));
    }

    @Test
    void claimsCandidatesAtomicallyUntilOneSucceeds() {
        World world = world();
        RTPWorld settings = settings(world);
        QueueData first = data(world, 20, 0, 1);
        QueueData second = data(world, 30, 0, 2);
        FakeRepository repository = new FakeRepository(List.of(first, second));
        repository.claimableId = 2;
        QueueService service = new QueueService(repository, () -> true);

        assertSame(second, service.claimFirst(settings, QueueRange.from(settings)));
        assertEquals(List.of(1, 2), repository.claimAttempts);
    }

    @Test
    void disabledQueueDoesNotTouchTheRepository() {
        FakeRepository repository = new FakeRepository(List.of());
        QueueService service = new QueueService(repository, () -> false);

        assertEquals(List.of(), service.applicable(settings(world()), QueueRange.from(settings(world()))));
        assertFalse(repository.queried);
    }

    @Test
    void capturesQueuePersistenceCoordinatesBeforeAsyncWork() {
        World world = world();
        QueuePosition position = QueuePosition.capture(new Location(world, 12.9, 70, -4.1));

        assertSame(world, position.world());
        assertEquals("world", position.worldName());
        assertEquals(12, position.blockX());
        assertEquals(-5, position.blockZ());
        assertEquals(12, position.toLocation().getBlockX());
        assertEquals(-5, position.toLocation().getBlockZ());
    }

    private static QueueData data(World world, int x, int z, int id) {
        return new QueueData(new Location(world, x, 64, z), 1L, id);
    }

    private static RTPWorld settings(World world) {
        return new RTPWorld() {
            @Override public boolean getUseWorldborder() { return false; }
            @Override public int getCenterX() { return 0; }
            @Override public int getCenterZ() { return 0; }
            @Override public int getMaxRadius() { return 100; }
            @Override public int getMinRadius() { return 10; }
            @Override public int getPrice() { return 0; }
            @Override public List<String> getBiomes() { return List.of(); }
            @Override public World getWorld() { return world; }
            @Override public RTP_SHAPE getShape() { return RTP_SHAPE.SQUARE; }
            @Override public int getMinY() { return -64; }
            @Override public int getMaxY() { return 320; }
            @Override public long getCooldown() { return 0; }
            @Override public boolean getRTPOnDeath() { return false; }
        };
    }

    private static World world() {
        return (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, arguments) -> {
                    if (method.getName().equals("getName")) return "world";
                    if (method.getReturnType().isPrimitive()) return primitiveDefault(method.getReturnType());
                    return null;
                });
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

    private static final class FakeRepository implements QueueRepository {
        private final List<QueueData> data;
        private final List<Integer> claimAttempts = new ArrayList<>();
        private int claimableId = -1;
        private boolean queried;

        private FakeRepository(List<QueueData> data) {
            this.data = data;
        }

        @Override public boolean isLoaded() { return true; }

        @Override public List<QueueData> findInRange(QueueRange range) {
            queried = true;
            return data;
        }

        @Override public boolean claim(int databaseId) {
            claimAttempts.add(databaseId);
            return databaseId == claimableId;
        }

        @Override public QueueData save(QueuePosition position) { return null; }

        @Override public boolean remove(QueuePosition position) { return false; }
    }
}
