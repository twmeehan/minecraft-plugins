package me.berrycraft.dynamicspells.Spells;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.berrycraft.dynamicspells.DynamicSpells;
import me.berrycraft.dynamicspells.IExecutableSpell;
import me.berrycraft.dynamicspells.Spell;
import me.berrycraft.dynamicspells.SpellEngine;

public class FireAura extends Spell implements IExecutableSpell {

  public static final String NAME = "fireaura";
  public static final Material MATERIAL = Material.BLAZE_POWDER;
  public static YamlConfiguration config;

  private Player caster;
  private double damage;
  private double radius;
  private double duration;
  private double lastDamageTime;
  private int level;

  public static void init() {
    config = loadSpellConfig(NAME);
  }

  public static boolean cast(Player caster, int level) {
    FireAura aura = new FireAura();
    aura.caster = caster;
    aura.level = level;
    aura.damage = config.getDouble(level + ".damage");
    aura.radius = config.getDouble(level + ".radius");
    aura.duration = config.getDouble(level + ".duration");
    aura.lastDamageTime = 0;

    // Register the spell with the engine
    SpellEngine.register(aura, aura.duration);
    return true;
  }

  @Override
  public void start() {
    // Play sound and particle effects when the spell starts
    caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_BLAZE_BURN, 1.0f, 1.0f);
    caster.getWorld().spawnParticle(Particle.FLAME, caster.getLocation(), 50, 1, 1, 1, 0.1);
  }

  @Override
  public void update(double t) {
    // Create a ring of fire particles
    for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
      double x = Math.cos(angle) * radius;
      double z = Math.sin(angle) * radius;
      caster.getWorld().spawnParticle(
          Particle.FLAME,
          caster.getLocation().add(x, 0.1, z),
          1, 0, 0, 0, 0);
    }

    // Damage entities every 0.5 seconds
    if (t - lastDamageTime >= 0.5) {
      lastDamageTime = t;
      for (Entity entity : caster.getWorld().getNearbyEntities(caster.getLocation(), radius, radius, radius)) {
        if (entity instanceof LivingEntity && entity != caster) {
          LivingEntity target = (LivingEntity) entity;
          target.damage(damage * 2, caster);
          // target.setFireTicks(20); // Set on fire for 1 second
        }
      }
    }
  }

  @Override
  public void finish() {
    // Play sound and particle effects when the spell ends
    caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_BLAZE_DEATH, 0.5f, 1.0f);
    caster.getWorld().spawnParticle(Particle.SMOKE, caster.getLocation(), 20, 1, 1, 1, 0.1);
  }
}