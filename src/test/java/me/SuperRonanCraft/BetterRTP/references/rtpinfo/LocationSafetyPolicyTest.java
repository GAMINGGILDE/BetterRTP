package me.SuperRonanCraft.BetterRTP.references.rtpinfo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocationSafetyPolicyTest {

    @Test
    void rejectsBlacklistedMaterialsCaseInsensitively() {
        assertFalse(LocationSafetyPolicy.isAllowedSurface(
                "LAVA", "NETHER_WASTES", List.of("lava", "cactus"), List.of()));
        assertTrue(LocationSafetyPolicy.isAllowedSurface(
                "STONE", "PLAINS", List.of("LAVA"), List.of()));
    }

    @Test
    void requiresAnExplicitlyAllowedBiomeWhenConfigured() {
        assertTrue(LocationSafetyPolicy.isAllowedSurface(
                "GRASS_BLOCK", "PLAINS", List.of(), List.of("plains", "forest")));
        assertTrue(LocationSafetyPolicy.isAllowedSurface(
                "GRASS_BLOCK", "FLOWER_FOREST", List.of(), List.of("forest")));
        assertFalse(LocationSafetyPolicy.isAllowedSurface(
                "GRASS_BLOCK", "DESERT", List.of(), List.of("PLAINS")));
        assertFalse(LocationSafetyPolicy.isAllowedSurface(
                "GRASS_BLOCK", null, List.of(), List.of("PLAINS")));
    }

    @Test
    void requiresClearFeetAndHeadSpace() {
        assertTrue(LocationSafetyPolicy.hasBodySpace(false, true, true));
        assertTrue(LocationSafetyPolicy.hasBodySpace(false, false, true));
        assertFalse(LocationSafetyPolicy.hasBodySpace(true, false, true));
        assertFalse(LocationSafetyPolicy.hasBodySpace(false, true, false));
    }
}
