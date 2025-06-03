package me.berrycraft.bugfixes;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Fixes extends JavaPlugin {

    @Override
    public void onEnable() {

        this.getServer().getPluginManager().registerEvents(new VillagerTradeListener(),this);
        this.getServer().getPluginManager().registerEvents(new PreventSuffocationGlitch(),this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
