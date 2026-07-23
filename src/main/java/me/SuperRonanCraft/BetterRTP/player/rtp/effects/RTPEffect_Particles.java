package me.SuperRonanCraft.BetterRTP.player.rtp.effects;

import me.SuperRonanCraft.BetterRTP.BetterRTP;
import me.SuperRonanCraft.BetterRTP.references.file.FileOther;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

//---
public class RTPEffect_Particles {

    private boolean enabled;
    private final List<Particle> effects = new ArrayList<>();
    private String shape;
    private final int precision = 16;

    private static final Map<String, String> LEGACY_PARTICLE_NAMES = Map.ofEntries(
            Map.entry("EXPLOSION_NORMAL", "POOF"),
            Map.entry("EXPLOSION_LARGE", "EXPLOSION"),
            Map.entry("EXPLOSION_HUGE", "EXPLOSION_EMITTER"),
            Map.entry("FIREWORKS_SPARK", "FIREWORK"),
            Map.entry("WATER_BUBBLE", "BUBBLE"),
            Map.entry("WATER_SPLASH", "SPLASH"),
            Map.entry("TOWN_AURA", "MYCELIUM"),
            Map.entry("SPELL_WITCH", "WITCH"),
            Map.entry("VILLAGER_ANGRY", "ANGRY_VILLAGER"),
            Map.entry("VILLAGER_HAPPY", "HAPPY_VILLAGER"));

    public static String[] shapeTypes = {
            "SCAN", //Body scan
            "EXPLODE", //Make an explosive entrance
            "TELEPORT" //Startrek type of portal
            };

    void load() {
        FileOther.FILETYPE config = getPl().getFiles().getType(FileOther.FILETYPE.EFFECTS);
        enabled = config.getBoolean("Particles.Enabled");
        effects.clear();
        if (!enabled) return;
        //Enabled? Load all this junk
        List<String> types;
        if (config.isList("Particles.Type"))
            types = config.getStringList("Particles.Type");
        else {
            types = new ArrayList<>();
            types.add(config.getString("Particles.Type"));
        }
        for (String configuredType : types) {
            Particle particle = resolve(configuredType);
            if (particle == null) {
                getPl().getLogger().warning(
                        "Unknown or data-dependent particle '" + configuredType
                                + "' in effects.yml; entry ignored. Use '/rtp info particles'.");
            } else {
                effects.add(particle);
            }
        }
        if (effects.isEmpty()) {
            effects.add(Particle.ASH);
            getPl().getLogger().warning(
                    "No usable particles were configured; using ASH as a safe default.");
        }
        shape = config.getString("Particles.Shape").toUpperCase();
        if (!Arrays.asList(shapeTypes).contains(shape)) {
            getPl().getLogger().severe("The particle shape '" + shape + "' doesn't exist! Default particle shape enabled...");
            getPl().getLogger().severe("Try using '/rtp info shapes' to get a list of shapes, or: " + Arrays.asList(shapeTypes));
            shape = shapeTypes[0];
        }
    }

    public void display(Player p) {
        if (!enabled) return;
        try {
            switch (shape) {
                case "TELEPORT":
                    partTeleport(p);
                    break;
                case "EXPLODE":
                    partExplosion(p);
                    break;
                default:
                case "SCAN":
                    partScan(p);
                    break;
            }
        } catch (Exception e) {
            getPl().getLogger().log(java.util.logging.Level.WARNING,
                    "Unable to display RTP particles for " + p.getName(), e);
        }
    }

    private void partScan(Player p) { //Particles with negative velocity
        Location loc = p.getLocation().add(new Vector(0, 1.75, 0));
        for (int index = 1; index < precision; index++) {
            Vector vec = getVecCircle(index);
            for (Particle effect : effects) {
                spawnDirectional(p, effect, loc.clone().add(vec), new Vector(0, -0.125, 0), .15);
            }
        }
    }

    private void partTeleport(Player p) { //Static particles in a shape
        Location loc = p.getLocation();
        for (float y = 2.5f; y > 0; y -= .25f)
            for (int index = 1; index < precision; index++) {
                //double yran = ran.nextGaussian() * pHeight;
                Vector vec = getVecCircle(index).add(new Vector(0, y, 0));
                for (Particle effect : effects) {
                    p.spawnParticle(effect, loc.clone().add(vec), 1);
                }
            }
    }

    private void partExplosion(Player p) { //Particles with a shape and forward velocity
        Location loc = p.getLocation().add(new Vector(0, 1, 0));
        for (int index = 1; index < precision; index++) {
            Vector vec = getVecCircle(index);
            for (Particle effect : effects) {
                spawnDirectional(p, effect, loc.clone().add(vec), vec, 1.5);
            }
        }
    }

    static Particle resolve(String configuredName) {
        if (configuredName == null || configuredName.isBlank()) {
            return null;
        }
        String normalized = configuredName.toUpperCase(Locale.ROOT);
        normalized = LEGACY_PARTICLE_NAMES.getOrDefault(normalized, normalized);
        try {
            Particle particle = Particle.valueOf(normalized);
            return particle.getDataType() == Void.class ? particle : null;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private void spawnDirectional(
            Player player, Particle particle, Location location,
            Vector direction, double speed) {
        player.spawnParticle(
                particle, location, 0,
                direction.getX(), direction.getY(), direction.getZ(), speed);
    }

    private Vector getVecCircle(int index) {
        double p1 = (index * Math.PI) / (precision / 2);
        double p2 = (index - 1) * Math.PI / (precision / 2);
        //Positions
        int radius = 3;
        double x1 = Math.cos(p1) * radius;
        double x2 = Math.cos(p2) * radius;
        double z1 = Math.sin(p1) * radius;
        double z2 = Math.sin(p2) * radius;
        return new Vector(x2 - x1, 0, z2 - z1);
    }

    private BetterRTP getPl() {
        return BetterRTP.getInstance();
    }
}
