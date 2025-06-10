package me.berrycraft.dynamicspells;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import de.tr7zw.nbtapi.NBTItem;
import net.md_5.bungee.api.ChatColor;

public abstract class Spell {
    
    public UUID id = UUID.randomUUID();

    protected static YamlConfiguration loadSpellConfig(String id) {

        DynamicSpells plugin = DynamicSpells.getInstance();

        File file = new File(plugin.getDataFolder(), id.toLowerCase() + ".yml");
        if (!file.exists()) {
            plugin.getLogger().warning("Spell config not found: " + file.getName());
            return new YamlConfiguration();
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public void cancel() {
        SpellEngine.kill(this);
    }

}
