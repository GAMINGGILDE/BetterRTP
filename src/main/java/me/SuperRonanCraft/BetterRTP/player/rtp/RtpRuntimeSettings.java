package me.SuperRonanCraft.BetterRTP.player.rtp;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

/** Immutable settings consumed directly by the RTP state machine. */
public record RtpRuntimeSettings(
        List<String> disabledWorlds,
        List<String> blockedBlocks,
        int maxAttempts,
        int delayTime,
        boolean cancelOnMove,
        boolean cancelOnDamage) {

    public RtpRuntimeSettings {
        disabledWorlds = List.copyOf(disabledWorlds);
        blockedBlocks = List.copyOf(blockedBlocks);
        maxAttempts = Math.max(1, maxAttempts);
        delayTime = Math.max(0, delayTime);
    }

    public static RtpRuntimeSettings from(ConfigurationSection config) {
        return new RtpRuntimeSettings(
                config.getStringList("DisabledWorlds"),
                config.getStringList("BlacklistedBlocks"),
                config.getInt("Settings.MaxAttempts"),
                config.getInt("Settings.Delay.Time"),
                config.getBoolean("Settings.Delay.CancelOnMove"),
                config.getBoolean("Settings.Delay.CancelOnDamage"));
    }

    static RtpRuntimeSettings empty() {
        return new RtpRuntimeSettings(List.of(), List.of(), 1, 0, false, false);
    }
}
