package me.berrycraft.dynamicspells.Spells;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import me.berrycraft.dynamicspells.Spell;
import me.berrycraft.dynamicspells.IExecutableSpell;
import me.berrycraft.dynamicspells.SpellEngine;

public class Laser extends Spell implements IExecutableSpell {

    public static final String NAME = "laser";
    public static final Material MATERIAL = Material.BLAZE_ROD;

    private static YamlConfiguration config;

    private Player caster;
    private int level;
    private double damage;

    private static final double CHARGE_TIME = 0.5; // seconds

    private double timer = 0.0;
    private boolean fired = false;

    public static void init() {
        config = loadSpellConfig(NAME);
    }

    public static boolean cast(Player caster, int level) {
        double damage = config.getDouble(level + ".damage", 5.0);
        SpellEngine.register(new Laser(caster, level, damage), (int) (CHARGE_TIME * 20) + 1);
        return true;
    }

    public Laser(Player caster, int level, double damage) {
        this.caster = caster;
        this.level = level;
        this.damage = damage;
    }

    @Override
    public void start() {
        // Apply slowness effect manually
        caster.setWalkSpeed(0.05f); // very slow
    }

    @Override
    public void update(double t) {
        timer += 1.0 / 20.0;
        caster.getWorld().spawnParticle(Particle.SMALL_FLAME, caster.getLocation().add(0, 1.5, 0), 5, 0.2, 0.2, 0.2, 0);

        if (timer >= CHARGE_TIME && !fired) {
            fireLaser();
            fired = true;
            cancel(); // ends the spell
        }
    }

    @Override
    public void finish() {
        caster.setWalkSpeed(0.2f); // reset to default speed
    }

    private void fireLaser() {
        Location start = caster.getEyeLocation();
        Vector direction = start.getDirection().normalize();

        for (double distance = 0; distance < 1000; distance += 1.0) { // effectively infinite range
            Location point = start.clone().add(direction.clone().multiply(distance));

            // Show particle trail
            caster.getWorld().spawnParticle(Particle.FLAME, point, 1, 0, 0, 0, 0, null);

            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target.equals(caster))
                    continue;
                if (!target.getWorld().equals(point.getWorld()))
                    continue;
                if (target.getLocation().distance(point) < 1.0) {
                    double newHealth = target.getHealth() - damage;
                    target.setHealth(Math.max(0.0, newHealth));
                }
            }
        }
    }
}
