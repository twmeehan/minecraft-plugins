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

import me.berrycraft.berryeconomy.Berry;
import me.berrycraft.berryeconomy.auction.windows.AuctionWindow;
import me.berrycraft.berryeconomy.logs.AuctionLogs;

public class AuctionBot {
    private final JavaPlugin plugin;
    private final String botName;
    private final YamlConfiguration config;
    private OfflinePlayer bot;

    private int buyFrequency;
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

        this.buyFrequency = config.getInt("buy_frequency", 600);  // default 30s
        this.allowanceFrequency = config.getInt("allowance_frequency", 1200); // default 1min
        this.allowance = config.getInt("allowance", 30);
        this.maxBalance = config.getInt("max_balance", 60);
        init();
    }

    public void init() {

        // Schedule allowance loop to run every allowanceFrequency ticks
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            int currentBalance = config.getInt("balance", 0);
            if (currentBalance < maxBalance) {
                int newBalance = Math.min(currentBalance + allowance, maxBalance);
                config.set("balance", newBalance);
                saveConfig();
                Bukkit.broadcastMessage("Bot balance updated to " + newBalance);
            }
        }, 120L, allowanceFrequency * 20L); // Initial delay 6 seconds, then repeat every allowanceFrequency seconds

        // Schedule buy loop to run every buyFrequency ticks
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            buy();
        }, 120L, buyFrequency * 20L); // Initial delay 6 seconds, then repeat every buyFrequency seconds

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
            double entryPrice = (entry.getPrice()*100);
            double priceValue = 1.0/((entryPrice/fairPrice)+0.1);
            double value = ((double)wants/(double)entry.getItem().getType().getMaxStackSize())*priceValue;
            if (value < 0.4) continue;
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
                continue;
            }
            // double check expired
            if (LocalDateTime.now().until(entry.getExpirationDate(), ChronoUnit.MINUTES)<0) {
                continue;
            }
            // check if bought
            if (entry.getBuyer()!=null) {
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

            Bukkit.broadcastMessage("bought " + name + " from "+ entry.getSeller().getName());

            balance -= price;
            config.set( "items."+name+".inventory", config.getInt( "items."+name+".inventory", 0)+entry.getItem().getAmount());
            config.set( "items."+name+".wants", config.getInt( "items."+name+".wants", 0)-entry.getItem().getAmount());
            
            Set<String> keys = config.getConfigurationSection("items").getKeys(false);
            List<String> itemKeys = new ArrayList<>(keys);

            if (!itemKeys.isEmpty()) {
                double normalizedAmountPurchased = (double)entry.getItem().getAmount()/((double)entry.getItem().getMaxStackSize());
                String randomItem = itemKeys.get((int)(Math.random() * itemKeys.size()));
                int addAmount = (int)(normalizedAmountPurchased*Material.getMaterial(randomItem.toUpperCase()).getMaxStackSize());
                int current = config.getInt("items." + randomItem + ".wants", 0);
                config.set("items." + randomItem + ".wants", current + addAmount);
            }

            saveConfig();
            index++;
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
