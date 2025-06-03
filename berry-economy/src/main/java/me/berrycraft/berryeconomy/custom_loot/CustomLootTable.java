package me.berrycraft.berryeconomy.custom_loot;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.berrycraft.berryeconomy.Berry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CustomLootTable {

    private static final File lootFolder = new File(Berry.getInstance().getDataFolder(), "loot");
    private static final LinkedList<CustomLootTable> tables = new LinkedList<>();

    private String name;
    private ArrayList<CustomLootTableEntry> entries;

    public CustomLootTable(String name, ArrayList<CustomLootTableEntry> entries) {
        this.name = name;
        this.entries = entries;
    }

    public String getName() {
        return name;
    }

    public ArrayList<CustomLootTableEntry> getEntries() {
        return entries;
    }

    public static void init() {
        tables.clear();
        if (!lootFolder.exists()) lootFolder.mkdirs();
    
        File[] files = lootFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;
    
        for (File file : files) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            List<Map<?, ?>> rawList = config.getMapList("entries");
    
            ArrayList<CustomLootTableEntry> entryList = new ArrayList<>();
            for (Map<?, ?> entryData : rawList) {
                Object itemObj = entryData.get("item");
                Object weightObj = entryData.get("weight");
    
                if (itemObj instanceof ItemStack && weightObj instanceof Number) {
                    ItemStack item = (ItemStack) itemObj;
                    double weight = ((Number) weightObj).doubleValue();
                    entryList.add(new CustomLootTableEntry(item, weight));
                }
            }
    
            String tableName = file.getName().replace(".yml", "");
            tables.add(new CustomLootTable(tableName, entryList));
        }
    }

    public static void createNewTable(CustomLootTableEntry[] entriesArray, String name) {
        File file = new File(lootFolder, name + ".yml");
        if (file.exists()) {
            System.out.println("Loot table already exists: " + name);
            return;
        }

        YamlConfiguration config = new YamlConfiguration();
        List<Map<String, Object>> serializedList = new ArrayList<>();

        for (CustomLootTableEntry entry : entriesArray) {
            Map<String, Object> data = new HashMap<>();
            data.put("item", entry.getItem());
            data.put("weight", entry.getWeight());
            serializedList.add(data);
        }

        config.set("entries", serializedList);

        try {
            config.save(file);
            ArrayList<CustomLootTableEntry> entryList = new ArrayList<>(Arrays.asList(entriesArray));
            tables.add(new CustomLootTable(name, entryList));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteTable(String tableName) {
        File file = new File(lootFolder, tableName + ".yml");
        if (file.exists()) file.delete();

        tables.removeIf(table -> table.getName().equalsIgnoreCase(tableName));
    }

    public static LinkedList<CustomLootTable> getTables() {
        return tables;
    }

    public static CustomLootTable getTable(String name) {
        for (CustomLootTable table : tables) {
            if (table.getName().equalsIgnoreCase(name)) {
                return table;
            }
        }
        return null; // not found
    }
}