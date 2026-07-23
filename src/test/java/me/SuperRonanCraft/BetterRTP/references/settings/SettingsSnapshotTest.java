package me.SuperRonanCraft.BetterRTP.references.settings;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsSnapshotTest {

    @Test
    void buildsCompleteSnapshotAndNormalizesTimeouts() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("Settings.RtpOnFirstJoin.World", "world");
        config.set("Settings.Debugger", true);
        config.set("Settings.Timeouts.ChunkLoadSeconds", 0);
        config.set("Settings.Timeouts.TeleportSeconds", -4);
        YamlConfiguration locations = new YamlConfiguration();
        locations.set("Enabled", true);
        YamlConfiguration placeholders = placeholders();

        SettingsSnapshot snapshot = SettingsSnapshot.from(config, locations, placeholders);

        assertTrue(snapshot.debug());
        assertTrue(snapshot.locationEnabled());
        assertEquals("world", snapshot.rtpOnFirstJoinWorld());
        assertEquals(1, snapshot.chunkLoadTimeoutSeconds());
        assertEquals(1, snapshot.teleportTimeoutSeconds());
    }

    @Test
    void rejectsMissingRequiredValuesBeforeReplacingRuntimeState() {
        YamlConfiguration config = new YamlConfiguration();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> SettingsSnapshot.from(
                        config, new YamlConfiguration(), new YamlConfiguration()));

        assertTrue(exception.getMessage().contains("Settings.RtpOnFirstJoin.World"));
    }

    private static YamlConfiguration placeholders() {
        YamlConfiguration placeholders = new YamlConfiguration();
        placeholders.set("Config.CanRTP.Success", "yes");
        placeholders.set("Config.CanRTP.NoPermission", "no");
        placeholders.set("Config.CanRTP.Cooldown", "cooldown");
        placeholders.set("Config.CanRTP.Price", "price");
        placeholders.set("Config.CanRTP.Hunger", "hunger");
        placeholders.set("Config.TimeFormat.Days", "days");
        placeholders.set("Config.TimeFormat.Hours", "hours");
        placeholders.set("Config.TimeFormat.Minutes", "minutes");
        placeholders.set("Config.TimeFormat.Seconds", "seconds");
        placeholders.set("Config.TimeFormat.ZeroAll", "zero");
        placeholders.set("Config.TimeFormat.Infinite", "infinite");
        placeholders.set("Config.TimeFormat.Separator.Middle", ", ");
        placeholders.set("Config.TimeFormat.Separator.Last", " and ");
        return placeholders;
    }
}
