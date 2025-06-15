package me.berrycraft.dynamicspells;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.berrycraft.dynamicspells.Spells.FireAura;

public class SpellEngine {

    public static HashMap<UUID, IExecutableSpell> activeSpells = new HashMap<UUID, IExecutableSpell>();
    public static HashMap<UUID, FireAura> activeFireAuraCasters = new HashMap<>();

    public static void register(Spell spell, double duration) {

        if (!(spell instanceof IExecutableSpell))
            return;
        IExecutableSpell executableSpell = (IExecutableSpell) spell;
        executableSpell.start();

        if (spell instanceof FireAura) {
            activeFireAuraCasters.put(((FireAura) spell).caster.getUniqueId(), (FireAura) spell);
        }

        if (duration > 0) {

            activeSpells.put(spell.id, executableSpell);

            new BukkitRunnable() {
                Long start_time = System.currentTimeMillis();
                double time = 0;

                @Override
                public void run() {
                    time = (System.currentTimeMillis() - start_time) / 1000.0;
                    executableSpell.update(time);
                    if (time > duration) {
                        activeSpells.remove(spell.id);
                        if (spell instanceof FireAura) {
                            activeFireAuraCasters.remove(((FireAura) spell).caster.getUniqueId());
                        }
                        executableSpell.finish();
                        this.cancel();
                    }
                }
            }.runTaskTimer(DynamicSpells.getInstance(), 0, 1);
        }
    }

    public static void kill(Spell spell) {
        if (!(spell instanceof IExecutableSpell))
            return;
        activeSpells.remove(spell.id);
        if (spell instanceof FireAura) {
            activeFireAuraCasters.remove(((FireAura) spell).caster.getUniqueId());
        }
    }

    public static void damage(Entity damager, Entity victim, double damage, SpellDamageType damageType) {
        if (victim instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) victim;
            double currentHealth = target.getHealth();
            double finalDamage = damage;

            switch (damageType) {
                case NORMAL:
                    // Vanilla damage system (armor + enchantments apply)
                    target.damage(damage, damager);
                    return;

                case MAGIC:
                    // Armor applies, but NOT enchantments (manual reduction)
                    AttributeInstance armorAttr = target.getAttribute(Attribute.ARMOR);
                    double armor = armorAttr != null ? armorAttr.getValue() : 0;
                    double reduction = 1.0 - Math.min(20.0, armor) / 25.0;
                    finalDamage = damage * reduction;
                    break;

                case TRUE:
                    // Ignore all armor and enchants – raw damage
                    break;
            }

            double newHealth = currentHealth - finalDamage;

            if (newHealth <= 0) {
                // Kill cleanly with damager context
                target.damage(100000, damager);
            } else {
                // Bypass Bukkit damage modifiers and enchantments
                target.damage(0.01); // triggers animation/sound
                target.setHealth(newHealth);
                target.setLastDamageCause(new EntityDamageByEntityEvent(
                    damager, victim, EntityDamageEvent.DamageCause.CUSTOM, finalDamage
                ));
            }
        }
    }


}
