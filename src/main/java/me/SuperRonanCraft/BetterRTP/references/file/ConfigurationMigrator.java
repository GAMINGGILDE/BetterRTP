package me.SuperRonanCraft.BetterRTP.references.file;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class ConfigurationMigrator {

    public static final int CURRENT_VERSION = 4;

    private ConfigurationMigrator() {
    }

    public static MigrationResult migrate(String fileName, YamlConfiguration config) {
        int sourceVersion = config.contains("Config-Version") ? config.getInt("Config-Version") : 3;
        if (sourceVersion > CURRENT_VERSION) {
            throw new IllegalStateException(fileName + " uses unsupported Config-Version " + sourceVersion
                    + " (maximum supported: " + CURRENT_VERSION + ")");
        }
        if (sourceVersion == CURRENT_VERSION) {
            return new MigrationResult(false, sourceVersion, CURRENT_VERSION, List.of());
        }
        if (sourceVersion != 3) {
            throw new IllegalStateException(fileName + " cannot be migrated from Config-Version " + sourceVersion);
        }

        List<String> changes = new ArrayList<>();
        switch (fileName.replace('\\', '/')) {
            case "config.yml" -> migrateMainConfig(config, changes);
            case "economy.yml" -> migrateMapList(config, "CustomWorlds.Prices", changes);
            case "locations.yml" -> migrateMapList(config, "Locations", changes);
            default -> {
                // Other structural files only need the schema marker in 4.0.
            }
        }
        config.set("Config-Version", CURRENT_VERSION);
        changes.add("Config-Version -> " + CURRENT_VERSION);
        return new MigrationResult(true, sourceVersion, CURRENT_VERSION, List.copyOf(changes));
    }

    private static void migrateMainConfig(YamlConfiguration config, List<String> changes) {
        migrateMapList(config, "CustomWorlds", changes);
        migrateMapList(config, "Overrides", changes);
        migrateMapList(config, "WorldType", changes);
        migrateMapList(config, "PermissionGroup.Groups", changes);

        ConfigurationSection groups = config.getConfigurationSection("PermissionGroup.Groups");
        if (groups != null) {
            for (String groupName : groups.getKeys(false)) {
                Object groupValue = groups.get(groupName);
                if (groupValue instanceof List<?>) {
                    setMapping(config, "PermissionGroup.Groups." + groupName, listToMap(groupValue));
                }
            }
        }

        List<String> materials = config.getStringList("BlacklistedBlocks");
        if (!materials.isEmpty()) {
            Set<String> modern = new LinkedHashSet<>();
            for (String material : materials) {
                modern.addAll(modernMaterials(material));
            }
            config.set("BlacklistedBlocks", new ArrayList<>(modern));
            changes.add("BlacklistedBlocks auf moderne Materialnamen umgestellt");
        }
    }

    private static void migrateMapList(YamlConfiguration config, String path, List<String> changes) {
        Object value = config.get(path);
        if (!(value instanceof List<?>)) {
            return;
        }
        setMapping(config, path, listToMap(value));
        changes.add(path + " von Liste zu Mapping umgestellt");
    }

    private static void setMapping(YamlConfiguration config, String path, Map<String, Object> values) {
        config.set(path, null);
        values.forEach((key, value) -> setValue(config, path + "." + key, value));
    }

    private static void setValue(YamlConfiguration config, String path, Object value) {
        if (value instanceof Map<?, ?> map) {
            stringMap(map).forEach((key, nested) -> setValue(config, path + "." + key, nested));
            return;
        }
        config.set(path, value);
    }

    private static Map<String, Object> listToMap(Object value) {
        if (value instanceof Map<?, ?> existing) {
            return stringMap(existing);
        }
        if (value instanceof ConfigurationSection section) {
            return stringMap(section.getValues(false));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        if (value instanceof List<?> entries) {
            for (Object entry : entries) {
                if (entry instanceof Map<?, ?> map) {
                    map.forEach((key, nestedValue) ->
                            result.put(String.valueOf(key), normalizeValue(nestedValue)));
                }
            }
        }
        return result;
    }

    private static Map<String, Object> stringMap(Map<?, ?> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        source.forEach((key, value) -> result.put(String.valueOf(key), normalizeValue(value)));
        return result;
    }

    private static Object normalizeValue(Object value) {
        if (value instanceof ConfigurationSection section) {
            return stringMap(section.getValues(false));
        }
        if (value instanceof Map<?, ?> map) {
            return stringMap(map);
        }
        if (value instanceof List<?> list) {
            return list.stream().map(ConfigurationMigrator::normalizeValue).toList();
        }
        return value;
    }

    private static List<String> modernMaterials(String input) {
        String material = input.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
        return switch (material) {
            case "STATIONARY_WATER", "FLOWING_WATER" -> List.of("WATER");
            case "STATIONARY_LAVA", "FLOWING_LAVA" -> List.of("LAVA");
            case "LEAVES" -> List.of("OAK_LEAVES", "SPRUCE_LEAVES", "BIRCH_LEAVES", "JUNGLE_LEAVES");
            case "LEAVES_2" -> List.of("ACACIA_LEAVES", "DARK_OAK_LEAVES");
            default -> List.of(material);
        };
    }

    public record MigrationResult(
            boolean migrated, int sourceVersion, int targetVersion, List<String> changes) {
    }
}
