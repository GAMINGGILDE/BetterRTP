package me.SuperRonanCraft.BetterRTP.references.file;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationMigratorTest {

    @Test
    void migratesMainConfigurationFrom37To40() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString("""
                BlacklistedBlocks:
                  - stationary_water
                  - flowing_lava
                  - leaves
                  - leaves_2
                CustomWorlds:
                  - survival:
                      MaxRadius: 5000
                      MinRadius: 100
                Overrides:
                  - lobby: survival
                WorldType:
                  - survival: NORMAL
                PermissionGroup:
                  Groups:
                    - vip:
                        - survival:
                            Priority: 10
                            MaxRadius: 3000
                """);

        ConfigurationMigrator.MigrationResult result =
                ConfigurationMigrator.migrate("config.yml", config);

        assertTrue(result.migrated());
        assertEquals(3, result.sourceVersion());
        assertEquals(4, config.getInt("Config-Version"));
        assertEquals(5000, config.getInt("CustomWorlds.survival.MaxRadius"));
        assertEquals("survival", config.getString("Overrides.lobby"));
        assertEquals("NORMAL", config.getString("WorldType.survival"));
        assertEquals(10, config.getInt("PermissionGroup.Groups.vip.survival.Priority"));
        assertEquals(List.of(
                        "WATER", "LAVA", "OAK_LEAVES", "SPRUCE_LEAVES", "BIRCH_LEAVES",
                        "JUNGLE_LEAVES", "ACACIA_LEAVES", "DARK_OAK_LEAVES"),
                config.getStringList("BlacklistedBlocks"));

        YamlConfiguration reloaded = new YamlConfiguration();
        reloaded.loadFromString(config.saveToString());
        assertEquals(5000, reloaded.getInt("CustomWorlds.survival.MaxRadius"));
        assertEquals(10, reloaded.getInt("PermissionGroup.Groups.vip.survival.Priority"));
    }

    @Test
    void leavesCurrentConfigurationUntouched() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("Config-Version", 4);
        config.set("CustomWorlds.survival.MaxRadius", 1000);

        ConfigurationMigrator.MigrationResult result =
                ConfigurationMigrator.migrate("config.yml", config);

        assertFalse(result.migrated());
        assertEquals(1000, config.getInt("CustomWorlds.survival.MaxRadius"));
    }

    @Test
    void bundledConfigurationsUseVersionFourAndMappings() throws Exception {
        for (String fileName : List.of(
                "config.yml", "economy.yml", "effects.yml", "locations.yml",
                "placeholders.yml", "signs.yml")) {
            YamlConfiguration config = loadResource(fileName);
            assertEquals(4, config.getInt("Config-Version"), fileName);
        }

        YamlConfiguration main = loadResource("config.yml");
        assertSection(main, "CustomWorlds");
        assertSection(main, "Overrides");
        assertSection(main, "WorldType");
        assertSection(main, "PermissionGroup.Groups");
        assertTrue(main.isBoolean("Settings.Delay.CancelOnDamage"));
        assertTrue(main.isBoolean("Settings.StatusMessages"));
        assertTrue(main.getBoolean("Settings.StatusMessages"));
        assertFalse(main.contains("Settings.PreloadRadius"));
        assertSection(loadResource("economy.yml"), "CustomWorlds.Prices");
        assertSection(loadResource("locations.yml"), "Locations");
    }

    @Test
    void exposesMappingEntriesToExistingWorldLoaders() throws Exception {
        YamlConfiguration config = loadResource("config.yml");
        FileData data = new FileData() {
            @Override public YamlConfiguration getConfig() { return config; }
            @Override public File getFile() { return null; }
            @Override public String fileName() { return "config.yml"; }
            @Override public Plugin plugin() { return null; }
        };

        Object customWorld = data.getMapList("CustomWorlds").getFirst().get("custom_world_1");
        assertTrue(customWorld instanceof java.util.Map<?, ?>);
        assertEquals(1000, ((java.util.Map<?, ?>) customWorld).get("MaxRadius"));
    }

    private YamlConfiguration loadResource(String fileName) throws Exception {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            assertNotNull(input, fileName);
            YamlConfiguration config = new YamlConfiguration();
            config.load(new InputStreamReader(input, StandardCharsets.UTF_8));
            return config;
        }
    }

    private void assertSection(YamlConfiguration config, String path) {
        ConfigurationSection section = config.getConfigurationSection(path);
        assertNotNull(section, path);
        assertFalse(config.isList(path), path);
    }
}
