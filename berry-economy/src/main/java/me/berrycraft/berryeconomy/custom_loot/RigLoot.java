package me.berrycraft.berryeconomy.custom_loot;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.berrycraft.berryeconomy.Berry;

import java.io.File;
import java.io.IOException;

public class RigLoot {

    private static File file;
    private static YamlConfiguration config;

    public static void init() {
        JavaPlugin plugin = Berry.getInstance();
        file = new File(plugin.getDataFolder(), "secret.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public static Double check(Player player, String crate) {
        String key = player.getName() + "." + crate;
        Double value = config.contains(key) ? config.getDouble(key) : -1.0;
        config.set(key, null); // remove after reading
        save();
        return value;
    }

    public static void set(Player player, String crate, double value) {
        config.set(player.getName() + "." + crate, value);
        save();
    }

    private static void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
