package me.berrycraft.dynamicspells;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.berrycraft.dynamicspells.Commands.CastCommand;
import me.berrycraft.dynamicspells.Commands.SpellBookCommand;
import me.berrycraft.dynamicspells.Spells.FireAura;
import me.berrycraft.dynamicspells.Spells.Heal;
import me.berrycraft.dynamicspells.Spells.Laser;

public final class DynamicSpells extends JavaPlugin {

    List<Class<? extends Spell>> SPELLS = new ArrayList<>();
    public HashMap<String, Class<? extends Spell>> stringToClass = new HashMap<String, Class<? extends Spell>>();

    private static DynamicSpells instance;

    @Override
    public void onEnable() {

        SPELLS.add(Heal.class);
        SPELLS.add(FireAura.class);
        SPELLS.add(Laser.class);

        instance = this;

        CastCommand castCommand = new CastCommand(this);
        getCommand("cast").setExecutor(castCommand);
        getCommand("cast").setTabCompleter(castCommand);

        SpellBookCommand spellbookCommand = new SpellBookCommand(this);
        getCommand("spellbook").setExecutor(spellbookCommand);
        getCommand("spellbook").setTabCompleter(spellbookCommand);

        for (Player p : Bukkit.getOnlinePlayers()) {
            SpellBookHandler.cooldowns.put(p, new HashMap<String, Long>());
        }

        getServer().getPluginManager().registerEvents(new SpellBookHandler(this), this);

        populateStringToClassMap();
        initSpells();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static DynamicSpells getInstance() {
        return instance;
    }

    public static void cast(String spellName) {

    }

    public void populateStringToClassMap() {
        for (Class<? extends Spell> spellClass : SPELLS) {
            try {
                Field field = spellClass.getField("NAME");
                stringToClass.put((String) field.get(null), spellClass);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }

        }
    }

    public void initSpells() {
        for (Class<? extends Spell> spellClass : SPELLS) {
            try {
                // Look for a static method named "init" with no parameters
                java.lang.reflect.Method initMethod = spellClass.getMethod("init");

                // Call the static method (pass null for static)
                initMethod.invoke(null);
            } catch (NoSuchMethodException e) {
                getLogger().warning("No init() method found in " + spellClass.getSimpleName());
            } catch (Exception e) {
                getLogger().severe("Failed to initialize spell: " + spellClass.getSimpleName());
                e.printStackTrace();
            }
        }
    }
}
