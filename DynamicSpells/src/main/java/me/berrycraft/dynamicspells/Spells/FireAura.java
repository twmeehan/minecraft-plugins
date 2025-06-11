package me.berrycraft.dynamicspells.Spells;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import me.berrycraft.dynamicspells.DynamicSpells;
import me.berrycraft.dynamicspells.IExecutableSpell;
import me.berrycraft.dynamicspells.Spell;
import me.berrycraft.dynamicspells.SpellEngine;
import me.berrycraft.dynamicspells.SpellDamageType;;

public class FireAura extends Spell implements IExecutableSpell, Listener {

  public static final String NAME = "fireaura";
  public static final Material MATERIAL = Material.BLAZE_POWDER;
  public static YamlConfiguration config;

  public Player caster;
  private double damage;
  private double radius;
  private double duration;
  private double lastDamageTime;
  private double lastParticleTime;
  private double damage_cooldown;
  private Location loc;
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
    aura.damage_cooldown = config.getDouble(level + ".damage_cooldown");
    aura.lastDamageTime = 0;
    aura.lastParticleTime = 0;

    // Register the spell with the engine
    SpellEngine.register(aura, aura.duration);
    return true;
  }

  @Override
  public void start() {
    this.loc = caster.getLocation();
    // Set caster on fire
    caster.setFireTicks((int) (duration * 20));

    // Play sound and particle effects when the spell starts
    caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_BLAZE_BURN, 1.0f, 1.0f);
    // Reduced initial burst of particles
    caster.getWorld().spawnParticle(Particle.FLAME, caster.getLocation(), 40, 1.2, 1.2, 1.2, 0.15);
    caster.getWorld().spawnParticle(Particle.LAVA, caster.getLocation(), 15, 1, 1, 1, 0.1);
    caster.getWorld().spawnParticle(Particle.SMOKE, caster.getLocation(), 10, 1, 1, 1, 0.1);
  }

  @Override
  public void update(double t) {
    if (t - lastParticleTime > damage_cooldown / 3) {
      lastParticleTime = t;

      // Create multiple rings of particles at different heights
      for (double height = 0.1; height <= 1.5; height += 0.7) { // Increased height spacing
        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) { // Reduced density of particles in ring
          double x = Math.cos(angle) * radius;
          double z = Math.sin(angle) * radius;
          Location particleLoc = caster.getLocation().add(x, height, z);

          // Main flame particles
          caster.getWorld().spawnParticle(
              Particle.FLAME,
              particleLoc,
              1, 0.1, 0.1, 0.1, 0.05);

          // Add some lava particles for extra effect (reduced frequency)
          if (Math.random() < 0.15) { // Reduced chance
            caster.getWorld().spawnParticle(
                Particle.LAVA,
                particleLoc,
                1, 0.1, 0.1, 0.1, 0);
          }

          // Add smoke particles occasionally (reduced frequency)
          if (Math.random() < 0.1) { // Reduced chance
            caster.getWorld().spawnParticle(
                Particle.SMOKE,
                particleLoc,
                1, 0.1, 0.1, 0.1, 0);
          }
        }
      }

      // Add upward flame particles in the center (reduced quantity)
      caster.getWorld().spawnParticle(
          Particle.FLAME,
          caster.getLocation().add(0, 0.1, 0),
          3, 0.3, 0.5, 0.3, 0.1);
    }

    if (t - lastDamageTime >= damage_cooldown) {
      caster.setFireTicks((int) (duration * 20));
      // Damage entities every 0.5 seconds
      lastDamageTime = t;
      // Keep caster on fire
      for (Entity entity : caster.getWorld().getNearbyEntities(caster.getLocation(), radius, radius, radius)) {
        if (entity instanceof LivingEntity && entity != caster) {
          LivingEntity target = (LivingEntity) entity;
          SpellEngine.damage(caster, target, damage * 2, SpellDamageType.MAGIC);
        }
      }
    }
  }

  @Override
  public void finish() {
    // Extinguish caster
    caster.setFireTicks(0);

    // Play sound and particle effects when the spell ends
    caster.getWorld().playSound(this.loc, Sound.ENTITY_BLAZE_DEATH, 0.5f, 1.0f);
    caster.getWorld().spawnParticle(Particle.SMOKE, this.loc, 20, 1, 1, 1, 0.1);
  }

  @EventHandler
  public void onEntityDamage(EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player)) {
      return;
    }

    Player player = (Player) event.getEntity();

    // Check if the player is an active FireAura caster
    if (SpellEngine.activeFireAuraCasters.containsKey(player.getUniqueId())) {
      // Check if the damage cause is fire-related
      if (event.getCause() == DamageCause.FIRE ||
          event.getCause() == DamageCause.FIRE_TICK ||
          event.getCause() == DamageCause.LAVA) {

        event.setCancelled(true);
      }
    }
  }
}