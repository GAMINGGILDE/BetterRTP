package me.SuperRonanCraft.BetterRTP.references.rtpinfo;

import java.util.List;
import java.util.Locale;

/** Pure material, biome and body-space decisions for RTP destinations. */
final class LocationSafetyPolicy {

    private LocationSafetyPolicy() {
    }

    static boolean isAllowedSurface(
            String material, String biome, List<String> blacklistedMaterials, List<String> allowedBiomes) {
        if (blacklistedMaterials.stream().anyMatch(entry -> entry.equalsIgnoreCase(material))) {
            return false;
        }
        if (allowedBiomes == null || allowedBiomes.isEmpty()) {
            return true;
        }
        if (biome == null) {
            return false;
        }
        String normalizedBiome = biome.toLowerCase(Locale.ROOT);
        return allowedBiomes.stream()
                .map(entry -> entry.toLowerCase(Locale.ROOT))
                .anyMatch(normalizedBiome::contains);
    }

    static boolean hasBodySpace(boolean feetSolid, boolean feetAir, boolean headAir) {
        return (feetAir || !feetSolid) && headAir;
    }
}
