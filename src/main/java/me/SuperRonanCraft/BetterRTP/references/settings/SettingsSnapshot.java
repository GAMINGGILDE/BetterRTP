package me.SuperRonanCraft.BetterRTP.references.settings;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

/**
 * Immutable view of every general BetterRTP setting used at runtime.
 *
 * <p>A complete instance is built before it replaces the active settings, so
 * readers can never observe a partially reloaded configuration.</p>
 */
public record SettingsSnapshot(
        boolean debug,
        boolean delayEnabled,
        int delayTime,
        boolean rtpOnFirstJoinEnabled,
        String rtpOnFirstJoinWorld,
        boolean rtpOnFirstJoinSetAsRespawn,
        boolean statusMessages,
        boolean locationEnabled,
        boolean useLocationIfAvailable,
        boolean locationNeedPermission,
        boolean useLocationsInSameWorld,
        boolean permissionGroupEnabled,
        boolean queueEnabled,
        int chunkLoadTimeoutSeconds,
        int teleportTimeoutSeconds,
        String placeholderTrue,
        String placeholderNoPermission,
        String placeholderCooldown,
        String placeholderBalance,
        String placeholderHunger,
        String placeholderTimeDays,
        String placeholderTimeHours,
        String placeholderTimeMinutes,
        String placeholderTimeSeconds,
        String placeholderTimeZero,
        String placeholderTimeInfinite,
        String placeholderTimeSeparatorMiddle,
        String placeholderTimeSeparatorLast) {

    public SettingsSnapshot {
        rtpOnFirstJoinWorld = Objects.requireNonNull(rtpOnFirstJoinWorld);
        chunkLoadTimeoutSeconds = Math.max(1, chunkLoadTimeoutSeconds);
        teleportTimeoutSeconds = Math.max(1, teleportTimeoutSeconds);
        placeholderTrue = Objects.requireNonNull(placeholderTrue);
        placeholderNoPermission = Objects.requireNonNull(placeholderNoPermission);
        placeholderCooldown = Objects.requireNonNull(placeholderCooldown);
        placeholderBalance = Objects.requireNonNull(placeholderBalance);
        placeholderHunger = Objects.requireNonNull(placeholderHunger);
        placeholderTimeDays = Objects.requireNonNull(placeholderTimeDays);
        placeholderTimeHours = Objects.requireNonNull(placeholderTimeHours);
        placeholderTimeMinutes = Objects.requireNonNull(placeholderTimeMinutes);
        placeholderTimeSeconds = Objects.requireNonNull(placeholderTimeSeconds);
        placeholderTimeZero = Objects.requireNonNull(placeholderTimeZero);
        placeholderTimeInfinite = Objects.requireNonNull(placeholderTimeInfinite);
        placeholderTimeSeparatorMiddle = Objects.requireNonNull(placeholderTimeSeparatorMiddle);
        placeholderTimeSeparatorLast = Objects.requireNonNull(placeholderTimeSeparatorLast);
    }

    public static SettingsSnapshot from(
            ConfigurationSection config,
            ConfigurationSection locations,
            ConfigurationSection placeholders) {
        return new SettingsSnapshot(
                config.getBoolean("Settings.Debugger"),
                config.getBoolean("Settings.Delay.Enabled"),
                config.getInt("Settings.Delay.Time"),
                config.getBoolean("Settings.RtpOnFirstJoin.Enabled"),
                requiredString(config, "Settings.RtpOnFirstJoin.World"),
                config.getBoolean("Settings.RtpOnFirstJoin.SetAsRespawn"),
                config.getBoolean("Settings.StatusMessages"),
                locations.getBoolean("Enabled"),
                locations.getBoolean("UseLocationIfAvailable"),
                locations.getBoolean("RequirePermission"),
                locations.getBoolean("UseLocationsInSameWorld"),
                config.getBoolean("PermissionGroup.Enabled"),
                config.getBoolean("Settings.Queue.Enabled"),
                config.getInt("Settings.Timeouts.ChunkLoadSeconds"),
                config.getInt("Settings.Timeouts.TeleportSeconds"),
                requiredString(placeholders, "Config.CanRTP.Success"),
                requiredString(placeholders, "Config.CanRTP.NoPermission"),
                requiredString(placeholders, "Config.CanRTP.Cooldown"),
                requiredString(placeholders, "Config.CanRTP.Price"),
                requiredString(placeholders, "Config.CanRTP.Hunger"),
                requiredString(placeholders, "Config.TimeFormat.Days"),
                requiredString(placeholders, "Config.TimeFormat.Hours"),
                requiredString(placeholders, "Config.TimeFormat.Minutes"),
                requiredString(placeholders, "Config.TimeFormat.Seconds"),
                requiredString(placeholders, "Config.TimeFormat.ZeroAll"),
                requiredString(placeholders, "Config.TimeFormat.Infinite"),
                requiredString(placeholders, "Config.TimeFormat.Separator.Middle"),
                requiredString(placeholders, "Config.TimeFormat.Separator.Last"));
    }

    static SettingsSnapshot empty() {
        return new SettingsSnapshot(
                false, false, 0, false, "", false, false,
                false, false, false, false, false, false, 1, 1,
                "", "", "", "", "", "", "", "", "", "", "", "", "");
    }

    private static String requiredString(ConfigurationSection section, String path) {
        String value = section.getString(path);
        if (value == null) {
            throw new IllegalArgumentException("Missing required configuration value: " + path);
        }
        return value;
    }
}
