package me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds;

import me.SuperRonanCraft.BetterRTP.player.commands.RTP_SETUP_TYPE;
import me.SuperRonanCraft.BetterRTP.player.rtp.RTP_SHAPE;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RtpWorldResolverTest {

    @Test
    void normalizesInvalidMinimumRadiusAndCopiesBiomeOverride() {
        World world = world("world");
        RTPWorld configured = configuredWorld(world, 100, 150);
        List<String> override = new ArrayList<>(List.of("DESERT"));

        ResolvedRtpWorld resolved = RtpWorldResolver.resolve(
                configured, world, override, 25, WORLD_TYPE.NORMAL,
                null, null);
        override.add("FOREST");

        assertEquals(25, resolved.settings().minRadius());
        assertEquals(List.of("DESERT"), resolved.settings().biomes());
        assertEquals(RTP_SETUP_TYPE.DEFAULT, resolved.setupType());
        assertThrows(UnsupportedOperationException.class,
                () -> resolved.settings().biomes().add("PLAINS"));
    }

    @Test
    void validityCheckUsesTheLocationsZCoordinate() {
        World world = world("world");
        RTPWorld configured = configuredWorld(world, 100, 10);

        assertFalse(WorldPlayer.checkIsValid(new Location(world, 20, 64, 200), configured));
    }

    private static RTPWorld configuredWorld(World world, int maxRadius, int minRadius) {
        return new RTPWorld() {
            @Override public boolean getUseWorldborder() { return false; }
            @Override public int getCenterX() { return 0; }
            @Override public int getCenterZ() { return 0; }
            @Override public int getMaxRadius() { return maxRadius; }
            @Override public int getMinRadius() { return minRadius; }
            @Override public int getPrice() { return 10; }
            @Override public List<String> getBiomes() { return List.of("PLAINS"); }
            @Override public World getWorld() { return world; }
            @Override public RTP_SHAPE getShape() { return RTP_SHAPE.SQUARE; }
            @Override public int getMinY() { return -64; }
            @Override public int getMaxY() { return 320; }
            @Override public long getCooldown() { return 60L; }
            @Override public boolean getRTPOnDeath() { return false; }
        };
    }

    private static World world(String name) {
        return (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, arguments) -> {
                    if (method.getName().equals("getName")) return name;
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
}
