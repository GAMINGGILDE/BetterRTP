package me.SuperRonanCraft.BetterRTP.player.rtp.effects;

import org.bukkit.Particle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RTPEffectParticlesTest {

    @Test
    void resolvesCurrentAndLegacyParticleNames() {
        assertEquals(Particle.POOF, RTPEffect_Particles.resolve("poof"));
        assertEquals(Particle.POOF, RTPEffect_Particles.resolve("EXPLOSION_NORMAL"));
        assertEquals(Particle.FIREWORK, RTPEffect_Particles.resolve("fireworks_spark"));
    }

    @Test
    void rejectsUnknownAndDataDependentParticles() {
        assertNull(RTPEffect_Particles.resolve("does_not_exist"));
        assertNull(RTPEffect_Particles.resolve("DUST"));
    }
}
