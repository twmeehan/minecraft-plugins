package me.berrycraft.dynamicspells;

import java.util.HashMap;
import java.util.UUID;

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
}
