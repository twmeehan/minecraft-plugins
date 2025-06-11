package me.berrycraft.dynamicspells.Spells;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.berrycraft.dynamicspells.IExecutableSpell;
import me.berrycraft.dynamicspells.Spell;
import me.berrycraft.dynamicspells.SpellEngine;
import me.berrycraft.dynamicspells.SpellDamageType;

public class Berserk extends Spell implements IExecutableSpell, Listener {
    public static final String NAME = "berserk";
    public static final Material MATERIAL = Material.NETHER_WART;

    private static YamlConfiguration config;
    private Player caster;
    private double healthDrain;
    private int durationSeconds;
    private double lastDamageTime = 0;
    private int level;

    public static void init() {
        config = loadSpellConfig(NAME);
    }

    public static boolean cast(Player caster, int level) {
        Berserk berserk = new Berserk();
        berserk.caster = caster;
        berserk.level = level;
        berserk.durationSeconds = 10;
        berserk.healthDrain = 0.5;

        SpellEngine.register(berserk, berserk.durationSeconds);
        return true;
    }

    @Override
    public void start() {
        int potionDuration = durationSeconds * 20;
        // Potion level is level - 1 (level 1 = effect level 0, level 2 = effect level
        // 1, etc)
        int potionLevel = Math.max(0, level - 1);
        caster.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, potionDuration, potionLevel));
        caster.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, potionDuration, potionLevel));

        // Visual and sound effects
        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1.0f, 0.8f);
        caster.getWorld().spawnParticle(Particle.CRIT, caster.getLocation(), 30, 1, 1, 1, 0.2);
        caster.getWorld().spawnParticle(Particle.SMOKE, caster.getLocation(), 20, 1, 1, 1, 0.1);
    }

    @Override
    public void update(double t) {
        if (t - lastDamageTime >= 1) {
            SpellEngine.damage(caster, caster, healthDrain * 2, SpellDamageType.TRUE);
            lastDamageTime = t;

            // Ongoing particle effects
            caster.getWorld().spawnParticle(Particle.CRIT, caster.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0.1);
            if (Math.random() < 0.3) {
                caster.getWorld().spawnParticle(Particle.SMOKE, caster.getLocation().add(0, 1, 0), 2, 0.2, 0.2,
                        0.2, 0);
            }
        }
    }

    @Override
    public void finish() {
        // End effects
        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_RAVAGER_STUNNED, 0.5f, 1.0f);
        caster.getWorld().spawnParticle(Particle.SMOKE, caster.getLocation(), 15, 0.5, 0.5, 0.5, 0.1);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        // Check if the player has an active Berserk spell
        for (IExecutableSpell spell : SpellEngine.activeSpells.values()) {
            if (spell instanceof Berserk && ((Berserk) spell).caster.equals(player)) {
                // Check if the damage is from the Berserk spell
                if (event.getCause() == DamageCause.CUSTOM &&
                        event.getDamage() == healthDrain * 2) {
                    // Prevent death from Berserk damage
                    if (player.getHealth() - event.getDamage() <= 0) {
                        event.setCancelled(true);
                        player.setHealth(1.0);
                    }
                }
                break;
            }
        }
    }
}