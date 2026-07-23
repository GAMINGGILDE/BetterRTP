package me.SuperRonanCraft.BetterRTP.references.file;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import me.SuperRonanCraft.BetterRTP.versions.AsyncHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface FileData {

    YamlConfiguration getConfig();

    File getFile();

    String fileName();

    Plugin plugin();

    default String getString(String path) {
        if (getConfig().isString(path))
            return getConfig().getString(path);
        return "SOMETHING WENT WRONG";
    }

    default boolean exists(String path) {
        return getConfig().contains(path);
    }

    default boolean getBoolean(String path) {
        return getConfig().getBoolean(path);
    }

    default int getInt(String path) {
        return getConfig().getInt(path);
    }

    default long getLong(String path) {
        return getConfig().getLong(path);
    }

    default List<String> getStringList(String path) {
        if (getConfig().isList(path))
            return getConfig().getStringList(path);
        return new ArrayList<>();
    }

    //Can be configured as a String OR List
    default List<String> getList(String path) {
        List<String> list = new ArrayList<>();
        if (getConfig().isList(path)) list.addAll(getStringList(path));
        else if (getConfig().isString(path)) list.add(getString(path));
        else return new ArrayList<>(Collections.singleton("&7The path &e" + path + " &7was not configured correctly!"));
        return list;
    }

    default ConfigurationSection getConfigurationSection(String path) {
        return getConfig().getConfigurationSection(path);
    }

    default boolean isString(String path) {
        return getConfig().isString(path);
    }

    default boolean isList(String path) {
        return getConfig().isList(path);
    }

    default List<Map<?, ?>> getMapList(String path) {
        if (getConfig().isList(path)) {
            return getConfig().getMapList(path);
        }
        ConfigurationSection section = getConfig().getConfigurationSection(path);
        if (section == null) {
            return List.of();
        }
        List<Map<?, ?>> entries = new ArrayList<>();
        for (String key : section.getKeys(false)) {
            Object value = section.get(key);
            if (value instanceof ConfigurationSection child) {
                value = child.getValues(false);
            }
            entries.add(Map.of(key, value));
        }
        return entries;
    }

    default void setValue(String path, Object value) {
        getConfig().set(path, value);
    }

    //PROCCESSING
    default void load() {
        YamlConfiguration config = getConfig();
        File file = getFile();
        boolean existingUserFile = file.exists();
        if (!existingUserFile) {
            plugin().saveResource(fileName(), false);
        }

        try {
            config.load(file);
            YamlConfiguration defaults = new YamlConfiguration();
            try (InputStream input = plugin().getResource(fileName().replace(File.separator, "/"))) {
                if (input != null) {
                    defaults = YamlConfiguration.loadConfiguration(
                            new InputStreamReader(input, StandardCharsets.UTF_8));
                }
            }

            if (existingUserFile) {
                ConfigurationMigrator.MigrationResult result =
                        ConfigurationMigrator.migrate(fileName(), config);
                if (result.migrated()) {
                    java.nio.file.Path backup = AtomicConfigWriter.backup(file, result.sourceVersion());
                    AtomicConfigWriter.write(file, config.saveToString());
                    plugin().getLogger().info("Migrated " + fileName() + " from Config-Version "
                            + result.sourceVersion() + " to " + result.targetVersion()
                            + " (backup: " + backup.getFileName() + ")");
                    result.changes().forEach(change -> plugin().getLogger().info(" - " + change));
                }
            }

            config.setDefaults(defaults);
            config.options().copyDefaults(false);
        } catch (Exception exception) {
            plugin().getLogger().log(java.util.logging.Level.SEVERE,
                    "Unable to load or migrate " + fileName(), exception);
            throw new IllegalStateException("Invalid BetterRTP configuration: " + fileName(), exception);
        }
    }

    default void save() {
        String contents = getConfig().saveToString();
        java.util.logging.Logger logger = plugin().getLogger();
        AsyncHandler.async(() -> {
            try {
                AtomicConfigWriter.write(getFile(), contents);
            } catch (IOException exception) {
                logger.log(
                        java.util.logging.Level.SEVERE, "Unable to save " + fileName(), exception);
            }
        });
    }
}
