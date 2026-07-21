package me.SuperRonanCraft.BetterRTP.player.events;

import me.SuperRonanCraft.BetterRTP.BetterRTP;
import me.SuperRonanCraft.BetterRTP.references.file.FileOther;
import me.SuperRonanCraft.BetterRTP.references.player.HelperPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

public class Damage {
    private static Set<EntityDamageEvent.DamageCause> ignoredDamageCauses = Set.of();

    static void load() {
        List<String> configuredCauses = FileOther.FILETYPE.EFFECTS
                .getStringList("Invincible.IgnoredDamageCauses");
        ignoredDamageCauses = Set.copyOf(parseIgnoredDamageCauses(configuredCauses,
                message -> BetterRTP.getInstance().getLogger().warning(message)));
    }

    static boolean canCancel(EntityDamageEvent.DamageCause damageCause) {
        return !ignoredDamageCauses.contains(damageCause);
    }

    static Set<EntityDamageEvent.DamageCause> parseIgnoredDamageCauses(
            List<String> configuredCauses, Consumer<String> warningLogger) {
        EnumSet<EntityDamageEvent.DamageCause> causes = EnumSet.noneOf(EntityDamageEvent.DamageCause.class);
        for (String configuredCause : configuredCauses) {
            try {
                String normalized = configuredCause.trim()
                        .replace('-', '_')
                        .replace(' ', '_')
                        .toUpperCase(Locale.ROOT);
                causes.add(EntityDamageEvent.DamageCause.valueOf(normalized));
            } catch (IllegalArgumentException e) {
                warningLogger.accept("Unknown damage cause '" + configuredCause
                        + "' in effects.yml at Invincible.IgnoredDamageCauses; entry ignored.");
            }
        }
        return causes;
    }

    static boolean isInInvincibleMode(Player player) {
        return HelperPlayer.getData(player).getInvincibleEndTime() > System.currentTimeMillis();
    }

    static void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) return;
        Player player = (Player) entity;

        if (!canCancel(event.getCause())) return;
        if (!isInInvincibleMode(player)) return;

        event.setCancelled(true);
    }
}
