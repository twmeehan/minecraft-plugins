package me.berrycraft.berryeconomy.custom_loot;

import me.berrycraft.berryeconomy.Berry;
import me.berrycraft.berryeconomy.items.Pinkberry;
import me.berrycraft.berryeconomy.items.Rainbowberry;
import me.berrycraft.berryeconomy.items.Raspberry;
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

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BerryLoot implements Listener {

    private static File file;
    private static FileConfiguration config;


    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        Random rand = new Random();
        if (e.getClickedBlock()!= null && (e.getClickedBlock().getType()==Material.CHEST)) {
            LootTable table = ((Chest)e.getClickedBlock().getState()).getLootTable();
            if (table!=null) {
                Inventory inventory = ((Chest)e.getClickedBlock().getState()).getBlockInventory();
                placeItemsRandomly(inventory, BerryLoot.generateLoot(table.toString()));

            }
        } else if (e.getClickedBlock()!= null && (e.getClickedBlock().getType()==Material.BARREL)) {
            LootTable table = ((Barrel)e.getClickedBlock().getState()).getLootTable();

            if (table!=null) {
                Inventory inventory = ((Barrel)e.getClickedBlock().getState()).getInventory();
                placeItemsRandomly(inventory, BerryLoot.generateLoot(table.toString()));


            }
        }
    }

    public static LinkedList<ItemStack> getCommon() {
        Random rand = new Random();
        int berries = 0;
        if (Math.random() > 0.5)
            berries = rand.nextInt(5)+2;
        LinkedList<ItemStack> loot = distributeBerries(berries,rand);
        return loot;

    }

    public static LinkedList<ItemStack> getUncommon() {
        Random rand = new Random();
        int berries = rand.nextInt(10)+10;
        LinkedList<ItemStack> loot = distributeBerries(berries,rand);
        return loot;

    }

    public static LinkedList<ItemStack> getRare() {
        Random rand = new Random();
        int berries = (rand.nextInt(2)+2)*10;
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
        
        config = YamlConfiguration.loadConfiguration(file);

    }

    public static LinkedList<ItemStack> generateLoot(String lootTableName) {
        
        Bukkit.broadcastMessage(lootTableName);

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
