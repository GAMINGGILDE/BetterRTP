package me.SuperRonanCraft.BetterRTP.player.rtp.effects;

import me.SuperRonanCraft.BetterRTP.BetterRTP;
import me.SuperRonanCraft.BetterRTP.references.file.FileOther;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Locale;

public class RTPEffect_Sounds {

    private boolean enabled;
    private String soundTeleport, soundDelay;

    void load() {
        FileOther.FILETYPE config = FileOther.FILETYPE.EFFECTS;
        enabled = config.getBoolean("Sounds.Enabled");
        if (enabled) {
            soundTeleport = config.getString("Sounds.Success");
            soundDelay = config.getString("Sounds.Delay");
        }
    }

    public void playTeleport(Player player) {
        playConfiguredSound(player, soundTeleport);
    }

    public void playDelay(Player player) {
        playConfiguredSound(player, soundDelay);
    }

    private void playConfiguredSound(Player player, String configuredSound) {
        if (!enabled || configuredSound == null || configuredSound.isBlank()) {
            return;
        }

        Location location = player.getLocation();
        Sound sound = resolveSound(configuredSound);
        try {
            if (sound != null) {
                player.playSound(location, sound, 1.0F, 1.0F);
                return;
            }

            NamespacedKey customSound = NamespacedKey.fromString(configuredSound.toLowerCase(Locale.ROOT));
            if (customSound != null) {
                player.playSound(location, customSound.asString(), 1.0F, 1.0F);
                return;
            }
            logInvalidSound(configuredSound);
        } catch (IllegalArgumentException exception) {
            logInvalidSound(configuredSound);
        }
    }

    private Sound resolveSound(String configuredSound) {
        String normalized = configuredSound.toLowerCase(Locale.ROOT);
        NamespacedKey directKey = NamespacedKey.fromString(normalized);
        Sound directMatch = directKey == null ? null : Registry.SOUND_EVENT.get(directKey);
        if (directMatch != null) {
            return directMatch;
        }

        String legacyName = normalized.startsWith("minecraft:")
                ? normalized.substring("minecraft:".length())
                : normalized;
        return Registry.SOUND_EVENT.keyStream()
                .filter(key -> NamespacedKey.MINECRAFT.equals(key.getNamespace()))
                .filter(key -> key.getKey().replace('.', '_').equals(legacyName))
                .findFirst()
                .map(Registry.SOUND_EVENT::get)
                .orElse(null);
    }

    private void logInvalidSound(String configuredSound) {
        BetterRTP.getInstance().getLogger().warning("The sound '" + configuredSound + "' is invalid.");
    }
}
