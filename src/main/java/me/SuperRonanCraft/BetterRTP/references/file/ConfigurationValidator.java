package me.SuperRonanCraft.BetterRTP.references.file;

import me.SuperRonanCraft.BetterRTP.BetterRTP;
import me.SuperRonanCraft.BetterRTP.player.rtp.RTP_SHAPE;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.WORLD_TYPE;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Locale;
import java.util.logging.Logger;

public final class ConfigurationValidator {

    private final Logger logger;
    private int errors;

    private ConfigurationValidator(Logger logger) {
        this.logger = logger;
    }

    public static boolean validateAll(BetterRTP plugin) {
        ConfigurationValidator validator = new ConfigurationValidator(plugin.getLogger());
        validator.validateMain(FileOther.FILETYPE.CONFIG.getConfig());
        validator.validateLocations(FileOther.FILETYPE.LOCATIONS.getConfig());
        validator.validateEconomy(FileOther.FILETYPE.ECO.getConfig());
        if (validator.errors == 0) {
            plugin.getLogger().info("Configuration validation completed without errors.");
            return true;
        }
        plugin.getLogger().severe("BetterRTP found " + validator.errors
                + " configuration error(s). Invalid entries will be skipped or use safe defaults.");
        return false;
    }

    private void validateMain(YamlConfiguration config) {
        requireSection(config, "config.yml", "CustomWorlds");
        requireSection(config, "config.yml", "Overrides");
        requireSection(config, "config.yml", "WorldType");
        requireSection(config, "config.yml", "PermissionGroup.Groups");
        requirePositive(config, "config.yml", "Settings.MaxAttempts");
        requirePositive(config, "config.yml", "Settings.Timeouts.ChunkLoadSeconds");
        requirePositive(config, "config.yml", "Settings.Timeouts.TeleportSeconds");
        validateWorldSettings(config, "config.yml", "Default");

        ConfigurationSection worlds = config.getConfigurationSection("CustomWorlds");
        if (worlds != null) {
            for (String worldName : worlds.getKeys(false)) {
                validateWorldSettings(config, "config.yml", "CustomWorlds." + worldName);
            }
        }

        ConfigurationSection worldTypes = config.getConfigurationSection("WorldType");
        if (worldTypes != null) {
            for (String worldName : worldTypes.getKeys(false)) {
                String value = worldTypes.getString(worldName, "");
                try {
                    WORLD_TYPE.valueOf(value.toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException exception) {
                    error("config.yml", "WorldType." + worldName,
                            "unknown world type '" + value + "'; expected NORMAL or NETHER");
                }
            }
        }

        for (String configuredMaterial : config.getStringList("BlacklistedBlocks")) {
            Material material = Material.matchMaterial(configuredMaterial);
            if (material == null || !material.isBlock()) {
                error("config.yml", "BlacklistedBlocks",
                        "unknown block material '" + configuredMaterial + "'");
            }
        }
    }

    private void validateLocations(YamlConfiguration config) {
        requireSection(config, "locations.yml", "Locations");
        ConfigurationSection locations = config.getConfigurationSection("Locations");
        if (locations == null) {
            return;
        }
        for (String name : locations.getKeys(false)) {
            String path = "Locations." + name;
            if (!config.isString(path + ".World") || config.getString(path + ".World", "").isBlank()) {
                error("locations.yml", path + ".World", "a world name is required");
            }
            validateWorldSettings(config, "locations.yml", path);
        }
    }

    private void validateEconomy(YamlConfiguration config) {
        requireSection(config, "economy.yml", "CustomWorlds.Prices");
        requireNonNegative(config, "economy.yml", "Economy.Price");
        requireNonNegative(config, "economy.yml", "Hunger.Honches");
        ConfigurationSection prices = config.getConfigurationSection("CustomWorlds.Prices");
        if (prices != null) {
            for (String worldName : prices.getKeys(false)) {
                requireNonNegative(config, "economy.yml", "CustomWorlds.Prices." + worldName);
            }
        }
    }

    private void validateWorldSettings(YamlConfiguration config, String fileName, String path) {
        boolean hasMaxRadius = config.contains(path + ".MaxRadius");
        boolean hasMinRadius = config.contains(path + ".MinRadius");
        int maxRadius = config.getInt(path + ".MaxRadius");
        int minRadius = config.getInt(path + ".MinRadius");
        if (hasMaxRadius && maxRadius <= 0) {
            error(fileName, path + ".MaxRadius", "must be greater than zero");
        }
        if (hasMinRadius && minRadius < 0) {
            error(fileName, path + ".MinRadius", "must be non-negative");
        }
        if (hasMaxRadius && hasMinRadius && minRadius >= maxRadius) {
            error(fileName, path + ".MinRadius", "must be non-negative and smaller than MaxRadius");
        }
        boolean hasMinY = config.contains(path + ".MinY");
        boolean hasMaxY = config.contains(path + ".MaxY");
        int minY = config.getInt(path + ".MinY");
        int maxY = config.getInt(path + ".MaxY");
        if (hasMinY && hasMaxY && minY >= maxY) {
            error(fileName, path + ".MinY", "must be smaller than MaxY");
        }
        if (config.contains(path + ".Shape")) {
            String shape = config.getString(path + ".Shape", "SQUARE");
            try {
                RTP_SHAPE.valueOf(shape.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                error(fileName, path + ".Shape", "unknown shape '" + shape + "'; expected SQUARE or CIRCLE");
            }
        }
    }

    private void requirePositive(YamlConfiguration config, String fileName, String path) {
        if (!config.isInt(path) || config.getInt(path) <= 0) {
            error(fileName, path, "must be a positive integer");
        }
    }

    private void requireNonNegative(YamlConfiguration config, String fileName, String path) {
        if (!config.isInt(path) || config.getInt(path) < 0) {
            error(fileName, path, "must be a non-negative integer");
        }
    }

    private void requireSection(YamlConfiguration config, String fileName, String path) {
        if (config.contains(path) && !config.isConfigurationSection(path)) {
            error(fileName, path, "must be a YAML mapping in Config-Version 4");
        }
    }

    private void error(String fileName, String path, String reason) {
        errors++;
        logger.severe("[Configuration] " + fileName + " -> " + path + ": " + reason);
    }
}
