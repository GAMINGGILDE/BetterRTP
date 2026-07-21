package me.SuperRonanCraft.BetterRTP.references.helpers;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;

import java.util.Locale;
import java.util.stream.Stream;

/** Registry-based access to biomes without relying on the legacy enum API. */
public final class BiomeHelper {

    private BiomeHelper() {
    }

    public static Biome find(String input) {
        String value = input.replace(",", "").trim().toLowerCase(Locale.ROOT);
        NamespacedKey key = NamespacedKey.fromString(value);
        return key == null ? null : registry().get(key);
    }

    public static Stream<Biome> stream() {
        return registry().stream();
    }

    /** Keeps the historic uppercase names used in BetterRTP configuration and placeholders. */
    public static String name(Biome biome) {
        NamespacedKey key = biome.getKey();
        String value = NamespacedKey.MINECRAFT.equals(key.getNamespace()) ? key.getKey() : key.toString();
        return value.toUpperCase(Locale.ROOT);
    }

    private static Registry<Biome> registry() {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME);
    }
}
