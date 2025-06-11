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

public class SpellEngine {

    public static HashMap<UUID, IExecutableSpell> activeSpells = new HashMap<UUID, IExecutableSpell>();

    public static void register(Spell spell, double duration) {

        if (!(spell instanceof IExecutableSpell))
            return;
        IExecutableSpell executableSpell = (IExecutableSpell) spell;
        executableSpell.start();
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
    }

    public static void damage(Entity damager, Entity victim, double damage) {
    if (victim instanceof LivingEntity) {
        LivingEntity target = (LivingEntity) victim;
        double currentHealth = target.getHealth();
        
        AttributeInstance attr = target.getAttribute(Attribute.GENERIC_ARMOR);
        double armor = attr != null ? attr.getValue() : 0;

        // Calculate reduction
        double reduction = 1.0 - Math.min(20.0, armor) / 25.0;
        double finalDamage = damage * reduction;
        double newHealth = currentHealth - finalDamage;

        if (newHealth <= 0) {
            target.setHealth(0);
            target.setLastDamageCause(new EntityDamageByEntityEvent(
                damager, victim, EntityDamageEvent.DamageCause.CUSTOM, damage
            ));
        } else {
            target.setHealth(newHealth);
            target.setLastDamageCause(new EntityDamageByEntityEvent(
                damager, victim, EntityDamageEvent.DamageCause.CUSTOM, damage
            ));
        }
    }
}

}
