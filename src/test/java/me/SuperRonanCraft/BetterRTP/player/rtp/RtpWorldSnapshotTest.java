package me.SuperRonanCraft.BetterRTP.player.rtp;

import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WORLD_TYPE;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RtpWorldSnapshotTest {

    @Test
    void defensivelyCopiesBiomeConfiguration() {
        List<String> biomes = new ArrayList<>(List.of("PLAINS"));
        RtpWorldSnapshot snapshot = snapshot(biomes);

        biomes.add("DESERT");

        assertEquals(List.of("PLAINS"), snapshot.biomes());
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.biomes().add("FOREST"));
    }

    @Test
    void playerOptionsDoNotChangeWithTheirMutableSource() {
        RTP_PlayerInfo source = new RTP_PlayerInfo(true, true, true, true, true);
        RtpPlayerOptions options = RtpPlayerOptions.from(source);

        source.setApplyDelay(false);
        source.setTakeMoney(false);

        assertEquals(new RtpPlayerOptions(true, true, true, true, true), options);
    }

    @Test
    void setupRequestDoesNotRetainMutableSetupCollectionsOrOptions() {
        List<String> biomes = new ArrayList<>(List.of("PLAINS"));
        Player player = player();
        RTPSetupInformation source = new RTPSetupInformation(
                world(), player, player, true, biomes, true,
                RTP_TYPE.COMMAND, null);

        RtpSetupRequest request = RtpSetupRequest.from(source);
        biomes.add("DESERT");
        source.getPlayerInfo().setApplyDelay(false);

        assertEquals(List.of("PLAINS"), request.biomes());
        assertTrue(request.playerOptions().applyDelay());
    }

    static RtpWorldSnapshot snapshot(List<String> biomes) {
        return new RtpWorldSnapshot(
                false, 10, -20, 1_000, 25, 50, biomes, world(),
                RTP_SHAPE.CIRCLE, -64, 320, "test", 60L, false, WORLD_TYPE.NORMAL);
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

    private static Player player() {
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, arguments) -> {
                    if (method.getName().equals("getUniqueId")) return java.util.UUID.randomUUID();
                    if (method.getName().equals("getName")) return "player";
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
