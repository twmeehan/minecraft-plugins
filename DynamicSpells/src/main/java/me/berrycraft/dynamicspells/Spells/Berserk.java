package me.berrycraft.dynamicspells.Spells;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

// import org.bukkit.scheduler.BukkitRunnable;

import me.berrycraft.dynamicspells.DynamicSpells; 
import me.berrycraft.dynamicspells.IExecutableSpell;
import me.berrycraft.dynamicspells.Spell;

public class Berserk extends Spell implements IExecutableSpell {
    public static final String NAME = "berserk";
    public static final Material MATERIAL = Material.NETHER_WART;

    private static YamlConfiguration config;

    public static void init() {
        config = loadSpellConfig(NAME);
    }

    public static boolean cast(Player caster, int level) {
        int durationSeconds = config.getInt(level + ".duration");
        double healthDrain = config.getDouble(level + ".drainPerSecond");

        int potionDuration = durationSeconds * 20;
        caster.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, potionDuration, 1));
        caster.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, potionDuration, 1));

        return true;
    }

    @Override
    public void start() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'start'");
    }

    @Override
    public void update(double t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public void finish() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'finish'");
    }
}