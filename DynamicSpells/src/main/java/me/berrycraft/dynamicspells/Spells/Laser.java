package me.berrycraft.dynamicspells.Spells;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.berrycraft.dynamicspells.DynamicSpells;
import me.berrycraft.dynamicspells.IExecutableSpell;
import me.berrycraft.dynamicspells.Spell;
import me.berrycraft.dynamicspells.SpellEngine;

public class Laser extends Spell implements IExecutableSpell {

    public static final String NAME = "laser";
    public static final Material MATERIAL = Material.BLAZE_ROD;
    public static YamlConfiguration config;

    private Player caster;
    private int level;
    private double damage;
    private double chargeTime;
    private boolean fired;

    public static void init() {
        config = loadSpellConfig(NAME);
    }

    public static boolean cast(Player caster, int level) {
        Laser laser = new Laser();
        laser.caster = caster;
        laser.level = level;
        laser.damage = config.getDouble(level + ".damage", 5.0);
        laser.chargeTime = config.getDouble(level + ".charge_time", 1.0); // default to 0.5 seconds
        laser.fired = false;

        SpellEngine.register(laser, laser.chargeTime);
        return true;
    }

    @Override
    public void start() {
        // Slow down caster during charge
        caster.setWalkSpeed(0.05f);
        caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 0.8f);
    }

    @Override
    public void update(double t) {
        // Show charging particles
        caster.getWorld().spawnParticle(Particle.SMALL_FLAME, caster.getLocation().add(0, 1.5, 0), 5, 0.2, 0.2, 0.2, 0);

        if (!fired && t >= chargeTime) {
            fireLaser();
            fired = true;
            cancel(); // Finish after firing
        }
    }

    @Override
    public void finish() {
        caster.setWalkSpeed(0.2f); // reset speed
        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 0.5f, 1.0f);
        caster.getWorld().spawnParticle(Particle.SMOKE, caster.getLocation(), 20, 1, 1, 1, 0.1);
    }

    private void fireLaser() {
        Location start = caster.getEyeLocation();
        Vector direction = start.getDirection().normalize();

        caster.getWorld().playSound(start, Sound.ENTITY_GHAST_SHOOT, 1.0f, 1.0f);

        // Fire the laser beam
        for (double distance = 0; distance < 1000; distance += 1.0) {
            Location point = start.clone().add(direction.clone().multiply(distance));
            caster.getWorld().spawnParticle(Particle.FLAME, point, 1, 0, 0, 0, 0, null);

            // Check nearby entities at this point
            for (Entity entity : caster.getWorld().getNearbyEntities(point, 0.5, 0.5, 0.5)) {
                if (entity.equals(caster))
                    continue;
                if (!(entity instanceof LivingEntity))
                    continue;

                LivingEntity target = (LivingEntity) entity;
                SpellEngine.damage(caster, target, damage);
            }
        }
    }
}
