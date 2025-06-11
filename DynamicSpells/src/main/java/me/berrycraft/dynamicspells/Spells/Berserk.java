package me.berrycraft.dynamicspells.Spells;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.berrycraft.dynamicspells.IExecutableSpell;
import me.berrycraft.dynamicspells.Spell;
import me.berrycraft.dynamicspells.SpellEngine;
import me.berrycraft.dynamicspells.SpellDamageType;

public class Berserk extends Spell implements IExecutableSpell {
    public static final String NAME = "berserk";
    public static final Material MATERIAL = Material.NETHER_WART;

    private static YamlConfiguration config;

    public Player caster;
    private double healthDrain;
    private int durationSeconds;

    public static void init() {
        config = loadSpellConfig(NAME);
    }

    public static boolean cast(Player caster, int level) {
        Berserk berserk = new Berserk();
        berserk.durationSeconds = config.getInt(level + ".duration");
        berserk.healthDrain = config.getDouble(level + ".drainPerSecond");

        int potionDuration = berserk.durationSeconds * 20;
        caster.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, potionDuration, 1));
        caster.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, potionDuration, 1));

        return true;
    }

    @Override
    public void start() {
        throw new UnsupportedOperationException("Unimplemented method 'start'");
    }

    @Override
    public void update(double t) {
        SpellEngine.damage(caster, caster, healthDrain, SpellDamageType.TRUE);
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public void finish() {
        throw new UnsupportedOperationException("Unimplemented method 'finish'");
    }
}