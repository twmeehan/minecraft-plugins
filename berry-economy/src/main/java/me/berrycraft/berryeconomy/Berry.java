package me.berrycraft.berryeconomy;

import me.berrycraft.berryeconomy.auction.AuctionEventHandler;
import me.berrycraft.berryeconomy.auction.MarketEntry;
import me.berrycraft.berryeconomy.auction.windows.AuctionWindow;
import me.berrycraft.berryeconomy.auction.windows.elements.Price;
import me.berrycraft.berryeconomy.auction.windows.elements.Search;
import me.berrycraft.berryeconomy.commands.AuctionCommand;
import me.berrycraft.berryeconomy.commands.BerryCommand;
import me.berrycraft.berryeconomy.commands.CustomLootCommand;
import me.berrycraft.berryeconomy.commands.ExchangeCommand;
import me.berrycraft.berryeconomy.commands.GambleCommand;
import me.berrycraft.berryeconomy.commands.GiveCommand;
import me.berrycraft.berryeconomy.custom_loot.BerryLoot;
import me.berrycraft.berryeconomy.custom_loot.CustomLootEventHandler;
import me.berrycraft.berryeconomy.custom_loot.CustomLootTable;
import me.berrycraft.berryeconomy.custom_loot.RigLoot;
import me.berrycraft.berryeconomy.custom_loot.WeightInputHandler;
import me.berrycraft.berryeconomy.items.BuilderCrate;
import me.berrycraft.berryeconomy.items.CommonCrate;
import me.berrycraft.berryeconomy.items.CustomItemEventHandler;
import me.berrycraft.berryeconomy.items.RareCrate;
import me.berrycraft.berryeconomy.logs.AuctionLogs;
import me.berrycraft.berryeconomy.logs.LootLogs;
import me.berrycraft.berryeconomy.logs.PlayerActivityLogs;
import me.berrycraft.berryeconomy.logs.PurchaseLogs;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Scoreboard;
import org.checkerframework.checker.units.qual.A;

import java.io.File;
import java.sql.SQLException;
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
    File auctionFile;
    FileConfiguration auctionConfig;
    private PlayerActivityLogs playerActivityLogs ;
    private AuctionLogs auctionLogs;
    private PurchaseLogs purchaseLogs;
    private LootLogs lootLogs;
    @Override
    public void onEnable() {

        instance = this;
        getServer().getPluginManager().registerEvents(new CustomItemEventHandler(), this);
        getServer().getPluginManager().registerEvents(new AuctionEventHandler(), this);
        getServer().getPluginManager().registerEvents(new Search(), this);
        getServer().getPluginManager().registerEvents(new Price(), this);
        getServer().getPluginManager().registerEvents(new BerryLoot(), this);
        getServer().getPluginManager().registerEvents(new CommonCrate(), this);
        getServer().getPluginManager().registerEvents(new RareCrate(), this);
        getServer().getPluginManager().registerEvents(new BuilderCrate(), this);

        getServer().getPluginManager().registerEvents(new CustomLootEventHandler(), this);
        getServer().getPluginManager().registerEvents(new WeightInputHandler(), this);

        ensureScoreboardsExist();
        this.initAuction();

        ExchangeCommand exchangeCommand = new ExchangeCommand();
        getServer().getPluginManager().registerEvents(exchangeCommand, this);
        this.getCommand("exchange").setExecutor(exchangeCommand);
        
        this.getCommand("auction").setExecutor(new AuctionCommand());

        BerryCommand berryCommand = new BerryCommand();
        this.getCommand("berry").setExecutor(berryCommand);
        this.getCommand("berry").setTabCompleter(berryCommand);

        GambleCommand gambleCommand = new GambleCommand();
        getServer().getPluginManager().registerEvents(gambleCommand, this);
        this.getCommand("gamble").setExecutor(gambleCommand);

        BerryLoot.init();
        RigLoot.init();

        // Player join and leave logs
        tryRegisterActivityLogs("jdbc:mysql://db-buf-04.sparkedhost.us:3306/s176279_berry", "u176279_AzqIUqrWkU", "aIJ9YG9eY!nrLpu6GL+CnaMZ");
        tryRegisterAuctionLogs("jdbc:mysql://db-buf-04.sparkedhost.us:3306/s176279_berry", "u176279_AzqIUqrWkU", "aIJ9YG9eY!nrLpu6GL+CnaMZ");
        tryRegisterPurchaseLogs("jdbc:mysql://db-buf-04.sparkedhost.us:3306/s176279_berry", "u176279_AzqIUqrWkU", "aIJ9YG9eY!nrLpu6GL+CnaMZ");
        tryRegisterLootLogs("jdbc:mysql://db-buf-04.sparkedhost.us:3306/s176279_berry", "u176279_AzqIUqrWkU", "aIJ9YG9eY!nrLpu6GL+CnaMZ");

    }

    public void tryRegisterActivityLogs(String url, String user, String password) {
        try {
            playerActivityLogs = new PlayerActivityLogs(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (playerActivityLogs != null) {

            getServer().getPluginManager().registerEvents(playerActivityLogs, this);
            // Undo any false QUIT logs due to reload
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerActivityLogs.removeLastQuitLog(player.getUniqueId());
            }
        }
    }

    public void tryRegisterAuctionLogs(String url, String user, String password) {
        try {
            auctionLogs = new AuctionLogs(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void tryRegisterPurchaseLogs(String url, String user, String password) {
        try {
            purchaseLogs = new PurchaseLogs(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void tryRegisterLootLogs(String url, String user, String password) {
        try {
            lootLogs = new LootLogs(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {

        if (playerActivityLogs != null) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerActivityLogs.logEvent(player.getUniqueId(), player.getName(), "QUIT");
        }
        playerActivityLogs.close();
    }

        if (playerActivityLogs != null) playerActivityLogs.close();
        if (auctionLogs != null) auctionLogs.close();
        if (purchaseLogs != null) purchaseLogs.close();
        if (lootLogs != null) lootLogs.close();



    }

    public static Berry getInstance() {
        return instance;
    }

    public FileConfiguration getAuctionConfig() {
        return auctionConfig;
    }

    public File getAuctionFile() {
        return auctionFile;
    }

    public void initAuction() {
    try {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

       auctionFile = new File(getDataFolder(), "auction.yml");

        if (!auctionFile.exists()) {
            getLogger().info("auction.yml not found, creating!");
            auctionFile.createNewFile(); // creates empty file
        } else {
            getLogger().info("auction.yml found, loading!");
        }

        // Load the custom config
        auctionConfig = YamlConfiguration.loadConfiguration(auctionFile);

        // Load auction data
        for (String s : auctionConfig.getKeys(true)) {
            if (s.endsWith(".item")) {
                String uuidKey = s.substring(0, s.length() - 5); // strip ".item"
                AuctionWindow.marketEntries.add(new MarketEntry(
                        java.util.UUID.fromString(uuidKey),
                        auctionConfig.getItemStack(uuidKey + ".item"),
                        auctionConfig.getDouble(uuidKey + ".price"),
                        (OfflinePlayer) auctionConfig.get(uuidKey + ".seller"),
                        (OfflinePlayer) auctionConfig.get(uuidKey + ".buyer"),
                        LocalDateTime.parse(auctionConfig.getString(uuidKey + ".expiration-date"))
                ));
            }
        }

        CustomLootTable.init(); // if needed elsewhere
    } catch (Exception e) {
        e.printStackTrace();
    }
}


    public static void ensureScoreboardsExist() {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        String[] objectives = {
            "lifetimeListings",
            "itemsSold",
            "lifetimeProfits",
            "itemsPurchased",
            "lifetimeSpending"
        };

        for (String name : objectives) {
            if (board.getObjective(name) == null) {
                board.registerNewObjective(name, Criteria.DUMMY, name);
            }
        }
    }

}
