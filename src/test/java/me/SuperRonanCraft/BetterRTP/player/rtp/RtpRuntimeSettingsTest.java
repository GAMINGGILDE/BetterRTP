package me.SuperRonanCraft.BetterRTP.player.rtp;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RtpRuntimeSettingsTest {

    @Test
    void readsAndNormalizesRuntimeConfiguration() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("DisabledWorlds", List.of("spawn"));
        config.set("BlacklistedBlocks", List.of("LAVA"));
        config.set("Settings.MaxAttempts", 0);
        config.set("Settings.Delay.Time", -1);
        config.set("Settings.Delay.CancelOnMove", true);

        RtpRuntimeSettings settings = RtpRuntimeSettings.from(config);

        assertEquals(List.of("spawn"), settings.disabledWorlds());
        assertEquals(List.of("LAVA"), settings.blockedBlocks());
        assertEquals(1, settings.maxAttempts());
        assertEquals(0, settings.delayTime());
        assertTrue(settings.cancelOnMove());
        assertFalse(settings.cancelOnDamage());
    }

    @Test
    void ownsImmutableCopiesOfConfiguredLists() {
        java.util.ArrayList<String> worlds = new java.util.ArrayList<>(List.of("world"));
        RtpRuntimeSettings settings =
                new RtpRuntimeSettings(worlds, List.of(), 5, 0, false, false);

        worlds.add("later");

        assertEquals(List.of("world"), settings.disabledWorlds());
        assertThrows(
                UnsupportedOperationException.class,
                () -> settings.disabledWorlds().add("blocked"));
    }
}
