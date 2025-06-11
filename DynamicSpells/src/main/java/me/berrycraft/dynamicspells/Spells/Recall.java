package me.berrycraft.dynamicspells.Spells;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Location;

import me.berrycraft.dynamicspells.DynamicSpells;
import me.berrycraft.dynamicspells.IExecutableSpell;
import me.berrycraft.dynamicspells.Spell;

public class Recall extends Spell implements IExecutableSpell {

  public static final String NAME = "recall";
  public static final Material MATERIAL = Material.ECHO_SHARD;
  public static YamlConfiguration config;

  private Player caster;
  private int level;
  private double channelTime;
  private boolean isChanneling;
  private Location startLocation;
  private long startTime;
  private BossBar bossBar;
  private double lastHealth;

  public static void init() {
    config = loadSpellConfig(NAME);
  }

  public static boolean cast(Player caster, int level) {
    Recall recall = new Recall();
    recall.caster = caster;
    recall.level = level;
    recall.channelTime = config.getDouble(level + ".channel_time", 8.0);
    recall.isChanneling = true;
    recall.startLocation = caster.getLocation();
    recall.startTime = System.currentTimeMillis();
    recall.lastHealth = caster.getHealth();

    // Create boss bar
    recall.bossBar = Bukkit.createBossBar(
        "§b§lChanneling Recall...",
        BarColor.BLUE,
        BarStyle.SOLID);
    recall.bossBar.addPlayer(caster);

    // Mark the player as channeling
    caster.setMetadata("RECALL_CHANNELING", new FixedMetadataValue(DynamicSpells.getInstance(), true));

    // Start channeling effect
    new BukkitRunnable() {
      double time = 0;

      @Override
      public void run() {
        if (!recall.isChanneling || !caster.isOnline()) {
          recall.bossBar.removeAll();
          this.cancel();
          return;
        }

        // Check for movement after grace period
        if (System.currentTimeMillis() - recall.startTime > 500) {
          double distance = caster.getLocation().distance(recall.startLocation);
          if (distance > 0.1) {
            recall.cancelChannel("§c§lRecall Failed!", "§eDon't move while channeling");
            this.cancel();
            return;
          }

          // Check for damage
          double currentHealth = caster.getHealth();
          if (currentHealth < recall.lastHealth) {
            recall.cancelChannel("§c§lRecall Failed!", "§eDon't take damage while channeling");
            this.cancel();
            return;
          }
          recall.lastHealth = currentHealth;
        }

        time += 0.1;
        if (time >= recall.channelTime) {
          // Channel complete, teleport to spawn
          caster.teleport(caster.getWorld().getSpawnLocation());
          caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);
          caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
          caster.getWorld().spawnParticle(Particle.PORTAL, caster.getLocation(), 100, 0.5, 0.5, 0.5, 0.2);
          caster.getWorld().spawnParticle(Particle.END_ROD, caster.getLocation(), 30, 0.5, 0.5, 0.5, 0.1);
          recall.bossBar.setTitle("§b§lRecall Complete!");
          recall.bossBar.setProgress(1.0);
          new BukkitRunnable() {
            @Override
            public void run() {
              recall.bossBar.removeAll();
            }
          }.runTaskLater(DynamicSpells.getInstance(), 40L);
          recall.isChanneling = false;
          caster.removeMetadata("RECALL_CHANNELING", DynamicSpells.getInstance());
          this.cancel();
          return;
        }

        // Channeling particles
        double progress = time / recall.channelTime;
        Location particleLoc = caster.getLocation().add(0, 1, 0);
        caster.getWorld().spawnParticle(
            Particle.PORTAL,
            particleLoc,
            3, 0.3, 0.3, 0.3, 0);
        caster.getWorld().spawnParticle(
            Particle.END_ROD,
            particleLoc,
            1, 0.2, 0.2, 0.2, 0);

        // Update boss bar progress
        recall.bossBar.setProgress(progress);

        // Play sound at certain intervals
        if (Math.floor(time) != Math.floor(time - 0.1)) {
          caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.7f,
              0.5f + (float) progress);
          caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 0.5f, 0.5f + (float) progress);
        }
      }
    }.runTaskTimer(DynamicSpells.getInstance(), 0L, 2L); // Run every 0.1 seconds

    return true;
  }

  private void cancelChannel(String title, String subtitle) {
    if (!isChanneling) {
      return;
    }

    isChanneling = false;
    caster.removeMetadata("RECALL_CHANNELING", DynamicSpells.getInstance());

    // Play failure sound and particles
    caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_ENRMAN_TELEPORT, 1.0f, 0.5f);
    caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.0f);
    caster.getWorld().spawnParticle(Particle.SMOKE, caster.getLocation(), 30, 0.5, 0.5, 0.5, 0.1);
    caster.getWorld().spawnParticle(Particle.END_ROD, caster.getLocation(), 15, 0.5, 0.5, 0.5, 0.1);

    // Update boss bar for failure
    bossBar.setTitle(title);
    bossBar.setColor(BarColor.BLUE);
    bossBar.setProgress(0.0);
    new BukkitRunnable() {
      @Override
      public void run() {
        bossBar.removeAll();
      }
    }.runTaskLater(DynamicSpells.getInstance(), 40L);

    // Cancel the spell in the engine
    cancel();
  }

  @Override
  public void start() {
    // Initial cast effects
    caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);
    caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
    caster.getWorld().spawnParticle(Particle.PORTAL, caster.getLocation(), 40, 0.5, 0.5, 0.5, 0.2);
    caster.getWorld().spawnParticle(Particle.END_ROD, caster.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
  }

  @Override
  public void update(double t) {
    // Channeling is handled by the BukkitRunnable
  }

  @Override
  public void finish() {
    if (isChanneling) {
      cancelChannel("§c§lRecall Failed!", "§eDon't move while channeling");
    }
  }
}
