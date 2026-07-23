package me.SuperRonanCraft.BetterRTP.player.events;

import org.bukkit.event.entity.EntityDamageEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DamageTest {

    @Test
    void emptyConfigurationPreservesProtectionForEveryCause() {
        Set<EntityDamageEvent.DamageCause> ignored =
                Damage.parseIgnoredDamageCauses(List.of(), message -> {});

        assertTrue(ignored.isEmpty());
    }

    @Test
    void parsesDamageCausesCaseInsensitively() {
        Set<EntityDamageEvent.DamageCause> ignored =
                Damage.parseIgnoredDamageCauses(List.of("kill", "fire-tick", "world border"), message -> {});

        assertEquals(Set.of(
                EntityDamageEvent.DamageCause.KILL,
                EntityDamageEvent.DamageCause.FIRE_TICK,
                EntityDamageEvent.DamageCause.WORLD_BORDER), ignored);
    }

    @Test
    void ignoresInvalidDamageCausesAndLogsWarning() {
        List<String> warnings = new ArrayList<>();

        Set<EntityDamageEvent.DamageCause> ignored =
                Damage.parseIgnoredDamageCauses(List.of("FALL", "not-a-cause"), warnings::add);

        assertEquals(Set.of(EntityDamageEvent.DamageCause.FALL), ignored);
        assertEquals(1, warnings.size());
        assertTrue(warnings.getFirst().contains("not-a-cause"));
    }
}
