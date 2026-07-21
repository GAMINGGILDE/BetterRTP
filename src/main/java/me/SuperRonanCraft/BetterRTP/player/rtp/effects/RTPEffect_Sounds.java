package me.SuperRonanCraft.BetterRTP.player.rtp.effects;

import me.SuperRonanCraft.BetterRTP.BetterRTP;
import me.SuperRonanCraft.BetterRTP.references.file.FileOther;
import org.bukkit.Location;
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
        String soundKey = configuredSound.contains(":")
                ? configuredSound.toLowerCase(Locale.ROOT)
                : "minecraft:" + configuredSound.toLowerCase(Locale.ROOT);
        try {
            player.playSound(location, soundKey, 1.0F, 1.0F);
        } catch (IllegalArgumentException exception) {
            BetterRTP.getInstance().getLogger().warning("The sound '" + configuredSound + "' is invalid.");
        }
    }
}
