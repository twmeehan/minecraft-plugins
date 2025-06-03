package me.berrycraft.berryeconomy;

import me.berrycraft.berryeconomy.auction.AuctionEventHandler;
import me.berrycraft.berryeconomy.auction.MarketEntry;
import me.berrycraft.berryeconomy.auction.windows.AuctionWindow;
import me.berrycraft.berryeconomy.auction.windows.elements.Price;
import me.berrycraft.berryeconomy.auction.windows.elements.Search;
import me.berrycraft.berryeconomy.commands.AuctionCommand;
import me.berrycraft.berryeconomy.commands.CustomLootCommand;
import me.berrycraft.berryeconomy.commands.ExchangeCommand;
import me.berrycraft.berryeconomy.commands.GambleCommand;
import me.berrycraft.berryeconomy.commands.GiveCommand;
import me.berrycraft.berryeconomy.custom_loot.CustomLootEventHandler;
import me.berrycraft.berryeconomy.custom_loot.CustomLootTable;
import me.berrycraft.berryeconomy.custom_loot.WeightInputHandler;
import me.berrycraft.berryeconomy.items.CommonCrate;
import me.berrycraft.berryeconomy.items.CustomItemEventHandler;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.units.qual.A;

import java.io.File;
import java.time.LocalDateTime;
import java.util.UUID;

/*
 * Main class of Berry Economy plugin
 *
 * Berry Economy is one of Berrycraft's main plugins that
 * handles everything about custom items and the in-game economy
 * such as the auction house and trading. (*Spell books may be handled
 * by another plugin)
 */
public final class Berry extends JavaPlugin {

    private static Berry instance;
    @Override
    public void onEnable() {

        instance = this;
        getServer().getPluginManager().registerEvents(new CustomItemEventHandler(), this);
        getServer().getPluginManager().registerEvents(new GiveCommand(), this);
        getServer().getPluginManager().registerEvents(new AuctionEventHandler(), this);
        getServer().getPluginManager().registerEvents(new Search(), this);
        getServer().getPluginManager().registerEvents(new Price(), this);
        getServer().getPluginManager().registerEvents(new BerryLoot(), this);
        getServer().getPluginManager().registerEvents(new CommonCrate(), this);
        getServer().getPluginManager().registerEvents(new CustomLootEventHandler(), this);
        getServer().getPluginManager().registerEvents(new WeightInputHandler(), this);


        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            File file = new File(getDataFolder(), "config.yml");
            if (!file.exists()) {
                getLogger().info("Config.yml not found, creating!");
                saveDefaultConfig();
            } else {
                getLogger().info("Config.yml found, loading!");
            }
            CustomLootTable.init();
        } catch (Exception e) {
            e.printStackTrace();

        }
        for (String s : getConfig().getKeys(true) ) {
            if (s.endsWith(".item")) {
                String UUID = s.split(".item")[0];
                AuctionWindow.marketEntries.add(new MarketEntry(java.util.UUID.fromString(UUID),
                        getConfig().getItemStack(UUID+".item"),
                        getConfig().getDouble(UUID+".price"),
                        (OfflinePlayer)getConfig().get(UUID+".seller"),
                        (OfflinePlayer)getConfig().get(UUID+".buyer"),
                        LocalDateTime.parse(getConfig().getString(UUID+".expiration-date"))));
            }
        }


        ExchangeCommand exchangeCommand = new ExchangeCommand();
        getServer().getPluginManager().registerEvents(exchangeCommand, this);
        this.getCommand("exchange").setExecutor(exchangeCommand);
        
        this.getCommand("auction").setExecutor(new AuctionCommand());

        this.getCommand("customloot").setExecutor(new CustomLootCommand());

        GambleCommand gambleCommand = new GambleCommand();
        getServer().getPluginManager().registerEvents(gambleCommand, this);
        this.getCommand("gamble").setExecutor(gambleCommand);

    }

    @Override
    public void onDisable() {


    }

    public static Berry getInstance() {
        return instance;
    }

}
