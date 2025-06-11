package me.berrycraft.dynamicspells.Spells;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Color;

import me.berrycraft.dynamicspells.DynamicSpells;
import me.berrycraft.dynamicspells.IExecutableSpell;
import me.berrycraft.dynamicspells.Spell;
import me.berrycraft.dynamicspells.SpellEngine;

public class BodySlam extends Spell implements IExecutableSpell, Listener {

  public static final String NAME = "bodyslam";
  public static final Material MATERIAL = Material.ANVIL;
  public static YamlConfiguration config;

  private Player caster;
  private int level;
  private double verticalBoost;
  private double victimBoost;
  private double damage;
  private double radius;
  private boolean hasLanded;
  private double originalY;

  public static void init() {
    config = loadSpellConfig(NAME);
  }

  public static boolean cast(Player caster, int level) {
    BodySlam slam = new BodySlam();
    slam.caster = caster;
    slam.level = level;
    slam.verticalBoost = config.getDouble(level + ".vertical_boost", 2.0);
    slam.victimBoost = config.getDouble(level + ".victim_boost", 1.0);
    slam.damage = config.getDouble(level + ".damage", 10.0);
    slam.radius = config.getDouble(level + ".radius", 5.0);
    slam.hasLanded = false;
    slam.originalY = caster.getLocation().getY();

    // Mark the player as using Body Slam
    caster.setMetadata("BODY_SLAM_ACTIVE", new FixedMetadataValue(DynamicSpells.getInstance(), true));

    SpellEngine.register(slam, 5.0); // 5 second max duration
    return true;
  }

  @Override
  public void start() {
    // Apply vertical boost
    Vector velocity = caster.getVelocity();
    velocity.setY(verticalBoost);
    caster.setVelocity(velocity);

    // Play sound and particles for the initial boost
    caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1.0f, 0.5f);

    // Create iron block particles for the initial boost
    for (int i = 0; i < 20; i++) {
      caster.getWorld().spawnParticle(
          Particle.BLOCK,
          caster.getLocation().add(0, 1, 0),
          1, 0.3, 0.3, 0.3, 0,
          Material.IRON_BLOCK.createBlockData());
    }
  }

  @Override
  public void update(double t) {
    if (!hasLanded) {
      // Check if player has started falling
      if (caster.getVelocity().getY() < 0) {
        // Create falling particles
        caster.getWorld().spawnParticle(Particle.SMOKE,
            caster.getLocation().add(0, 1, 0),
            5, 0.2, 0.2, 0.2, 0.05);

        // Check if player has landed
        if (caster.isOnGround()) {
          hasLanded = true;
          performLandingEffect();
          caster.removeMetadata("BODY_SLAM_ACTIVE", DynamicSpells.getInstance());
        }
      }
    }
  }

  private void performLandingEffect() {
    // Play impact sound
    caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);

    // Create impact particles
    caster.getWorld().spawnParticle(Particle.SMOKE, caster.getLocation(), 1, 0, 0, 0, 0);

    // Create circular explosion effect
    Location center = caster.getLocation();
    for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
      for (double r = 0; r <= radius; r += 0.5) {
        double x = Math.cos(angle) * r;
        double z = Math.sin(angle) * r;
        Location particleLoc = center.clone().add(x, 0.1, z);

        // Main explosion particles
        caster.getWorld().spawnParticle(Particle.CLOUD, particleLoc, 1, 0, 0, 0, 0);
        // Main explosion particles (iron blocks)
        caster.getWorld().spawnParticle(
            Particle.BLOCK,
            particleLoc,
            1, 0, 0, 0, 0,
            Material.IRON_BLOCK.createBlockData());

        // Add some anvil particles for effect
        if (r > radius * 0.7) { // Only on the outer ring
          caster.getWorld().spawnParticle(
              Particle.BLOCK,
              particleLoc.clone().add(0, 0.5, 0),
              1, 0.1, 0.1, 0.1, 0,
              Material.ANVIL.createBlockData());
        }
      }
    }

    // Damage nearby entities
    for (Entity entity : caster.getWorld().getNearbyEntities(caster.getLocation(), radius, radius, radius)) {
      if (entity instanceof LivingEntity && entity != caster) {
        LivingEntity target = (LivingEntity) entity;
        double distance = target.getLocation().distance(caster.getLocation());
        if (distance <= radius) {
          // Damage falls off with distance
          double damageMultiplier = 1.0 - (distance / radius);
          target.damage(damage * damageMultiplier, caster);

          // Knockback effect
          Vector knockback = target.getLocation().subtract(caster.getLocation()).toVector();
          knockback.setY(victimBoost); // Strong upward force
          target.setVelocity(knockback);
        }
      }
    }
  }

  @Override
  public void finish() {
    if (!hasLanded) {
      // If the spell ends before landing, perform a smaller impact
      performLandingEffect();
    }
    // Remove the metadata when the spell ends
    caster.removeMetadata("BODY_SLAM_ACTIVE", DynamicSpells.getInstance());
  }

  @EventHandler
  public void onEntityDamage(EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player)) {
      return;
    }

    Player player = (Player) event.getEntity();

    // Check if the player is using Body Slam and the damage is from falling
    if (player.hasMetadata("BODY_SLAM_ACTIVE") && event.getCause() == DamageCause.FALL) {
      event.setCancelled(true);
    }
  }
}