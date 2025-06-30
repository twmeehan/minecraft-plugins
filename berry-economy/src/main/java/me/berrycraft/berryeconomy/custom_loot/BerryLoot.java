package me.berrycraft.berryeconomy.custom_loot;

import me.berrycraft.berryeconomy.Berry;
import me.berrycraft.berryeconomy.items.Pinkberry;
import me.berrycraft.berryeconomy.items.Rainbowberry;
import me.berrycraft.berryeconomy.items.Raspberry;
import me.berrycraft.berryeconomy.logs.LootLogs;
import net.md_5.bungee.api.chat.ClickEvent;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BerryLoot implements Listener {

    private static File file;
    private static FileConfiguration config;

    // Multiplayer looting system
    private static class LootRecord {
        Set<UUID> playersLooted = new HashSet<>();
        long firstOpenedTimestamp;
        String lootTableName;
        int tier;
        LootRecord(UUID player, long time, String lootTableName, int tier) {
            this.playersLooted.add(player);
            this.firstOpenedTimestamp = time;
            this.lootTableName = lootTableName;
            this.tier = tier;
        }
    }
    private static final Map<Location, LootRecord> lootedContainers = new HashMap<>();
    private static final long EXPIRATION_MS = 3600_000; // 1 hour

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        Random rand = new Random();
        Player player = e.getPlayer();
        Block block = e.getClickedBlock();
        if (block != null && (block.getType() == Material.CHEST || block.getType() == Material.BARREL)) {
            cleanupExpiredLootRecords();
            Location loc = block.getLocation().clone();
            LootTable table = null;
            Inventory inventory = null;
            if (block.getType() == Material.CHEST) {
                table = ((Chest) block.getState()).getLootTable();
                inventory = ((Chest) block.getState()).getBlockInventory();
            } else if (block.getType() == Material.BARREL) {
                table = ((Barrel) block.getState()).getLootTable();
                inventory = ((Barrel) block.getState()).getInventory();
            }
            if (table != null) {
                int tier = getTierForLootTable(table.toString());
                LootRecord record = new LootRecord(player.getUniqueId(), System.currentTimeMillis(), table.toString(), tier);
                lootedContainers.put(loc, record);

                LinkedList<ItemStack> berries = BerryLoot.generateLoot(table.toString());
                LootLogs.logLoot(player, table.toString(), Rainbowberry.getAmount(berries)*100+Pinkberry.getAmount(berries)*10+Raspberry.getAmount(berries));
                placeItemsRandomly(inventory, berries);

                // else: player already looted, do nothing
            } else if (lootedContainers.get(loc) != null) {
                LootRecord record = lootedContainers.get(loc);
                if (!record.playersLooted.contains(player.getUniqueId())) {
                    record.playersLooted.add(player.getUniqueId());
                    LinkedList<ItemStack> berries = BerryLoot.generateLootForTier(record.tier);
                    
                    LootLogs.logLoot(player, record.lootTableName, Rainbowberry.getAmount(berries)*100+Pinkberry.getAmount(berries)*10+Raspberry.getAmount(berries));
                    placeItemsRandomly(inventory, berries);
                }
            }
        }
    }

    private static void cleanupExpiredLootRecords() {
        long now = System.currentTimeMillis();
        lootedContainers.entrySet().removeIf(entry -> now - entry.getValue().firstOpenedTimestamp > EXPIRATION_MS);
    }

    private static int getTierForLootTable(String lootTableName) {
        config = YamlConfiguration.loadConfiguration(file);
        if (!config.contains(lootTableName)) return 0;
        return config.getInt(lootTableName);
    }

    public static LinkedList<ItemStack> generateLootForTier(int tier) {
        switch (tier) {
            case 1:
                return BerryLoot.getCommon();
            case 2:
                return BerryLoot.getUncommon();
            case 3:
                return BerryLoot.getRare();
            case 4:
                return BerryLoot.getLegendary();
            default:
                return new LinkedList<>();
        }
    }

    public static LinkedList<ItemStack> getCommon() {
        Random rand = new Random();
        int berries = 0;
        if (Math.random() > 0.5)
            berries = rand.nextInt(5)+4;
        LinkedList<ItemStack> loot = distributeBerries(berries,rand);
        return loot;

    }

    public static LinkedList<ItemStack> getUncommon() {
        Random rand = new Random();
        int berries = rand.nextInt(20)+10;
        LinkedList<ItemStack> loot = distributeBerries(berries,rand);
        return loot;

    }

    public static LinkedList<ItemStack> getRare() {
        Random rand = new Random();
        int berries = (rand.nextInt(2)+3)*10;
        if (Math.random()>0.6) {
            berries += rand.nextInt(6);
        }
        LinkedList<ItemStack> loot = distributeBerries(berries,rand);
        return loot;

    }
    public static LinkedList<ItemStack> getLegendary() {
        Random rand = new Random();
        int berries = (rand.nextInt(7)+5)*10;
        LinkedList<ItemStack> loot = distributeBerries(berries,rand);
        return loot;

    }

    // breaks stacks into small randomized amounts
    public static LinkedList<ItemStack> distributeBerries(int count, Random rand) {
        
        //////////////////////////////////////////////////////////////
        /// REMOVE THIS AFTER UPDATE 1.7
        //////////////////////////////////////////////////////////////
        // temp boost to berries 
        count = (int)(count * 1.25);
        LinkedList<ItemStack> loot = new LinkedList<>();
        while (count - 100 > 0) {
            loot.add(new Rainbowberry());
            count-=100;
        }
        while (count - 10 > 0) {
            int max = count / 20 + 1;
            int amount = rand.nextInt(max)+1;
            loot.add(new Pinkberry(amount));
            count-=10*amount;
        }
        while (count > 0) {
            int max = count / 2 + 1;
            int amount = rand.nextInt(max)+1;
            loot.add(new Raspberry(amount));
            count-=amount;
        }
        return loot;
    }

    public static void init() {

        file = new File(Berry.getInstance().getDataFolder(), "loot_generation.yml");

        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                Bukkit.getLogger().info("Created loot_generation.yml");
            } catch (IOException e) {
                Bukkit.getLogger().severe("Could not create loot_generation.yml");
                e.printStackTrace();
            }
        }
        
    }

    public static LinkedList<ItemStack> generateLoot(String lootTableName) {
        
        config = YamlConfiguration.loadConfiguration(file);

        if (!config.contains(lootTableName)) {
            Bukkit.getLogger().warning("Loot table '" + lootTableName + "' not found in loot_generation.yml");
            return new LinkedList<ItemStack>();
        }

        int tier = config.getInt(lootTableName);
        LinkedList<ItemStack> output;
        switch (tier) {
            case 1:
             output= BerryLoot.getCommon();
             break;
            case 2 :
             output=BerryLoot.getUncommon();
             break;
            case 3 :
             output=BerryLoot.getRare();
             break;
            case 4 : 
             output=BerryLoot.getLegendary();
             break;
            default :
             return null;
            
        }
        return output;

    }

    public static void placeItemsRandomly(Inventory inventory, List<ItemStack> items) {
        // Get all empty slot indices
        List<Integer> emptySlots = new ArrayList<>();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType().isAir()) {
                emptySlots.add(i);
            }
        }

        // Shuffle available slots for randomness
        Collections.shuffle(emptySlots);

        Iterator<Integer> slotIterator = emptySlots.iterator();
        for (ItemStack item : items) {
            if (!slotIterator.hasNext()) break;
            int slot = slotIterator.next();
            inventory.setItem(slot, item);
        }
    }


}
