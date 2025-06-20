package me.berrycraft.dynamicspells.Spells;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.berrycraft.dynamicspells.Spell;

public class Heal extends Spell {

    public static final String NAME = "heal";

    public static final Material MATERIAL = Material.GLISTERING_MELON_SLICE;

    public static YamlConfiguration config;

    public static void init() {
        config = loadSpellConfig(NAME);
    }

    public static boolean cast(Player caster, int level) {
        double healAmount = config.getDouble(level + ".heal");
        caster.setHealth(Math.min(caster.getAttribute(Attribute.MAX_HEALTH).getBaseValue(),
                caster.getHealth() + healAmount * 2));

        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

        return true;
    }

}
