package me.berrycraft.berryeconomy.auction;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.berrycraft.berryeconomy.Berry;
import me.berrycraft.berryeconomy.auction.windows.AuctionWindow;
import me.berrycraft.berryeconomy.logs.AuctionLogs;

public class AuctionBot {
    private final JavaPlugin plugin;
    private final String botName;
    private final YamlConfiguration config;
    private OfflinePlayer bot;

    private int auctionTurnoverGoal;
    //private int sellFrequency;
    private int allowanceFrequency;
    private int allowance;
    private int maxBalance;



    public AuctionBot(JavaPlugin plugin, String botName) {
        this.bot = Bukkit.getOfflinePlayer(UUID.fromString("1ac99706-340a-42f7-aa6f-0fadf074119c"));
        this.plugin = plugin;
        this.botName = botName;

        File file = new File(plugin.getDataFolder(), "bots/" + botName + ".yml");
        this.config = YamlConfiguration.loadConfiguration(file);

        // the amount of time that the bot aims to purchase all items in the auction in
        // eg buy every item in the auction house in 1 week
        this.auctionTurnoverGoal = config.getInt("auction_turnover_goal", 600);

        this.allowanceFrequency = config.getInt("allowance_frequency", 1200); // default 1min
        this.allowance = config.getInt("allowance", 30);
        this.maxBalance = config.getInt("max_balance", 60);
        init();
    }

    public void init() {

        new BukkitRunnable() {

            @Override
            public void run() {
                int currentBalance = config.getInt("balance", 0);
                if (currentBalance < maxBalance) {
                    int newBalance = Math.min(currentBalance + allowance, maxBalance);
                    config.set("balance", newBalance);
                    saveConfig();
                    //Bukkit.broadcastMessage("Bot balance updated to " + newBalance);
                }
            }
        }.runTaskTimer(plugin, allowanceFrequency * 20L, allowanceFrequency * 20L);
        // Schedule buy loop to run every auctionTurnoverGoal ticks

        startBuyLoop();
        
        // Schedule wants reset
        int resetFrequency = config.getInt("reset_frequency", 36000); // default 1 hour
        new BukkitRunnable() {
            @Override
            public void run() {
                if (config.contains("items")) {
                    Set<String> keys = config.getConfigurationSection("items").getKeys(false);
                    for (String key : keys) {
                        int def = config.getInt("items." + key + ".default", 64);
                        config.set("items." + key + ".wants", def);
                    }
                    saveConfig();
                }
            }
        }.runTaskTimer(plugin, 0, resetFrequency * 20L);
    }

    public void startBuyLoop() {
        new BukkitRunnable() {
            @Override
            public void run() {
                buy();
    
                // Reschedule the next run
                startBuyLoop();
            }
        }.runTaskLater(plugin, auctionTurnoverGoal * 20L / Math.max(14, AuctionWindow.marketEntries.size()));
    }

    private void buy() {

        HashMap<MarketEntry,Double> values = new HashMap<MarketEntry,Double>();

        // calculate the cost/benefit of each item
        for (MarketEntry entry : AuctionWindow.marketEntries) {
            if (entry.getBuyer()!=null) continue;
            if (!config.contains("items."+ entry.getItem().getType().toString().toLowerCase())) continue;

            double fairPrice = config.getDouble( "items." + entry.getItem().getType().toString().toLowerCase() + ".fair_price", 600) * entry.getItem().getAmount();
            //int avgPrice = config.getInt( "items/" + entry.getItem().getType() + "/average_price", 0);
            int wants = config.getInt( "items." + entry.getItem().getType().toString().toLowerCase() + ".wants", 0);
            int defaultWants = config.getInt( "items." + entry.getItem().getType().toString().toLowerCase() + ".default", 0);
            double entryPrice = (entry.getPrice()*100);
            double priceValue = 1.0/((entryPrice/fairPrice)+0.1);
            double value = ((double)wants/(double)defaultWants)*priceValue;
            if (priceValue < 0.5) continue;
            values.put(entry,value);
        }

        // sort by most valued item
        List<Map.Entry<MarketEntry, Double>> sortedEntries = values.entrySet()
            .stream()
            .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
            .collect(Collectors.toList());

        int index = 0;
            
        // buys the top item
        int balance = config.getInt( "balance", 0);
        while (index < sortedEntries.size()) {

            MarketEntry entry = sortedEntries.get(index).getKey();
            int price = (int)(entry.getPrice()*100);

            // check if we have enough balance
            if (balance < price) {
                index++;
                continue;
            }
            // double check expired
            if (LocalDateTime.now().until(entry.getExpirationDate(), ChronoUnit.MINUTES)<0) {
                index++;
                continue;
            }
            // check if bought
            if (entry.getBuyer()!=null) {
                index++;
                continue;
            }
            
            entry.setBuyer(bot);
            String name = entry.getItem().getType().toString().toLowerCase();

            Berry.getInstance().getAuctionConfig().set(entry.getID().toString() + ".buyer", bot);    
            AuctionLogs.logAuctionAction(entry.getSeller(),bot,name,entry.getItem().getAmount(),(int)(entry.getPrice()*100));
            try {
                Berry.getInstance().getAuctionConfig().save(Berry.getInstance().getAuctionFile());
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Bukkit.broadcastMessage("bought " + name + " from "+ entry.getSeller().getName());

            balance -= price;

            plugin.getLogger().info("Bought " + name + "[" + sortedEntries.get(index).getValue() + "]" + " from "+ entry.getSeller().getName() + " for " + price + " (Current balance: " + balance + " Next Purchase in: " + (auctionTurnoverGoal / Math.max(1, AuctionWindow.marketEntries.size())) + "s)");

            config.set( "items."+name+".inventory", config.getInt( "items."+name+".inventory", 0)+entry.getItem().getAmount());
            int oldWants = config.getInt( "items."+name+".wants", 0);
            config.set( "items."+name+".wants", oldWants-entry.getItem().getAmount());
            
            Set<String> keys = config.getConfigurationSection("items").getKeys(false);
            List<String> itemKeys = new ArrayList<>(keys);

            if (!itemKeys.isEmpty()) {
                double normalizedAmountPurchased = (double)entry.getItem().getAmount()/((double)entry.getItem().getMaxStackSize());
                
                // Randomly choose 2-3 items to distribute wants to
                int numItemsToUpdate = (int)(Math.random() * 3) + 1; // 2 or 3 items
                numItemsToUpdate = Math.min(numItemsToUpdate, itemKeys.size()); // Don't exceed available items
                
                // Shuffle the item keys to randomize selection
                List<String> shuffledItems = new ArrayList<>(itemKeys);
                java.util.Collections.shuffle(shuffledItems);
                
                // Distribute the wants among the selected items
                for (int i = 0; i < numItemsToUpdate; i++) {
                    String randomItem = shuffledItems.get(i);
                    int addAmount = (int)(normalizedAmountPurchased * Material.getMaterial(randomItem.toUpperCase()).getMaxStackSize() / numItemsToUpdate);
                    int current = config.getInt("items." + randomItem + ".wants", 0);
                    config.set("items." + randomItem + ".wants", current + addAmount);
                }
            }

            saveConfig();
            index++;
            config.set( "balance", balance);
            return;
        }
        config.set( "balance", balance);

       
        
    }
    private void saveConfig() {
        try {
            config.save(new File(plugin.getDataFolder(), "bots/" + botName + ".yml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
