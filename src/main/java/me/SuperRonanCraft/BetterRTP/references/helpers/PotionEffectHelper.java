package me.SuperRonanCraft.BetterRTP.references.helpers;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffectType;

import java.util.Locale;
import java.util.stream.Stream;

/** Registry-based access to potion effects without the legacy static lookup API. */
public final class PotionEffectHelper {

    private PotionEffectHelper() {
    }

    public static PotionEffectType find(String input) {
        NamespacedKey key = NamespacedKey.fromString(input.trim().toLowerCase(Locale.ROOT));
        return key == null ? null : registry().get(key);
    }

    public static Stream<PotionEffectType> stream() {
        return registry().stream();
    }

    /** Keeps the historic uppercase names shown by BetterRTP commands. */
    public static String name(PotionEffectType effect) {
        NamespacedKey key = effect.getKey();
        String value = NamespacedKey.MINECRAFT.equals(key.getNamespace()) ? key.getKey() : key.toString();
        return value.toUpperCase(Locale.ROOT);
    }

    private static Registry<PotionEffectType> registry() {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.MOB_EFFECT);
    }
}
