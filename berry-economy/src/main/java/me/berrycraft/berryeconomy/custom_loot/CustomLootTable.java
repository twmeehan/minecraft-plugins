package me.berrycraft.berryeconomy.custom_loot;

import me.berrycraft.berryeconomy.Berry;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CustomLootTable {

    private static final File lootFolder = new File(Berry.getInstance().getDataFolder(), "loot");

    private static final HashMap<String, CustomLootTable> tables = new HashMap<>();

    private final String name;
    private final ArrayList<CustomLootTableEntry> entries;
    private final HashMap<Integer, Integer> weightLookupMap = new HashMap<>();
    private int totalWeight = 0;

    public CustomLootTable(String name, ArrayList<CustomLootTableEntry> entries) {
        this.name = name;
        this.entries = entries;
        populateWeightMap();
    }

    public String getName() {
        return name;
    }

    public ArrayList<CustomLootTableEntry> getEntries() {
        return entries;
    }

    public void populateWeightMap() {
        weightLookupMap.clear();
        int cursor = 0;
        for (int i = 0; i < entries.size(); i++) {
            int weight = entries.get(i).getWeight();
            for (int j = 0; j < weight; j++) {
                weightLookupMap.put(cursor++, i);
            }
        }
        totalWeight = cursor;
    }
    // gives all the items in the table
    public LinkedList<ItemStack> give() {
        LinkedList<ItemStack> dropList = new LinkedList<>();
        for (int i = 0; i < entries.size(); i++) {
            CustomLootTableEntry entry = entries.get(i);
            ItemStack item = entry.getItem();
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && meta.getDisplayName().endsWith(".yml")) {
                String linkedTable = meta.getDisplayName().replace(".yml", "");
                if (linkedTable.charAt(0)=='*') {
                    linkedTable = linkedTable.replace("*", "");

                    CustomLootTable linked = CustomLootTable.getTable(linkedTable);
                    if (linked != null) {
                        dropList.addAll(linked.give());

                        continue;
                    }
                }
                CustomLootTable linked = CustomLootTable.getTable(linkedTable);
                if (linked != null) {
                    dropList.addAll(linked.roll(new Random()));
                
                    continue;
                }
            }

            double x = Math.random();
            int amount;
            if (entry.getRandomness() == 0) {
                amount = item.getAmount();
            } else {
                amount = (int) Math.ceil((1 - Math.pow(x, 1.0 / entry.getRandomness())) * item.getAmount());
            }
            item.setAmount(Math.max(1, amount));
            dropList.add(item);
        }
        return dropList;

    }

    public LinkedList<ItemStack> roll(Random rng) {
        LinkedList<ItemStack> dropList = new LinkedList<>();
        if (totalWeight == 0 || weightLookupMap.isEmpty()) return dropList;

        int index = rng.nextInt(totalWeight);
        int entryIndex = weightLookupMap.getOrDefault(index, -1);
        CustomLootTableEntry entry = (entryIndex >= 0 && entryIndex < entries.size()) ? entries.get(entryIndex) : null;
        if (entry == null) return dropList;

        for (int i = 0; i < entry.getRolls(); i++) {
            ItemStack item = entry.getItem().clone();
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && meta.getDisplayName().endsWith(".yml")) {
                String linkedTable = meta.getDisplayName().replace(".yml", "");
                if (linkedTable.charAt(0)=='*') {
                    linkedTable = linkedTable.replace("*", "");
                    CustomLootTable linked = CustomLootTable.getTable(linkedTable);
                    if (linked != null) {
                        dropList.addAll(linked.give());

                        continue;
                    }
                }
                CustomLootTable linked = CustomLootTable.getTable(linkedTable);
                if (linked != null) {
                    dropList.addAll(linked.roll(rng));

                    continue;
                }
            }

            double x = Math.random();
            int amount;
            if (entry.getRandomness() == 0) {
                amount = item.getAmount();
            } else {
                amount = (int) Math.ceil((1 - Math.pow(x, 1.0 / entry.getRandomness())) * item.getAmount());
            }
            item.setAmount(Math.max(1, amount));
            dropList.add(item);
        }

        return dropList;
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
                Object rollsObj = entryData.get("rolls");
                Object randomnessObj = entryData.get("randomness");

                if (itemObj instanceof ItemStack && weightObj instanceof Number) {
                    ItemStack item = (ItemStack) itemObj;
                    int weight = ((Number) weightObj).intValue();
                    int rolls = (rollsObj instanceof Number) ? ((Number) rollsObj).intValue() : 1;
                    double randomness = (randomnessObj instanceof Number) ? ((Number) randomnessObj).doubleValue() : 0.0;
                    entryList.add(new CustomLootTableEntry(item, weight, rolls, randomness));
                }
            }

            String tableName = file.getName().replace(".yml", "");
            tables.put(tableName, new CustomLootTable(tableName, entryList));
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
            data.put("rolls", entry.getRolls());
            data.put("randomness", entry.getRandomness());
            serializedList.add(data);
        }

        config.set("entries", serializedList);

        try {
            config.save(file);
            ArrayList<CustomLootTableEntry> entryList = new ArrayList<>(Arrays.asList(entriesArray));
            tables.put(name, new CustomLootTable(name, entryList));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteTable(String tableName) {
        File file = new File(lootFolder, tableName + ".yml");
        if (file.exists()) file.delete();

        tables.remove(tableName);
    }

    public static HashMap<String, CustomLootTable> getTables() {
        return tables;
    }

    public static CustomLootTable getTable(String name) {
        return tables.get(name);
    }

    public static void saveOrReplaceTable(CustomLootTableEntry[] entriesArray, String name) {
        File file = new File(lootFolder, name + ".yml");

        YamlConfiguration config = new YamlConfiguration();
        List<Map<String, Object>> serializedList = new ArrayList<>();

        for (CustomLootTableEntry entry : entriesArray) {
            Map<String, Object> data = new HashMap<>();
            data.put("item", entry.getItem());
            data.put("weight", entry.getWeight());
            data.put("rolls", entry.getRolls());
            data.put("randomness", entry.getRandomness());
            serializedList.add(data);
        }

        config.set("entries", serializedList);

        try {
            config.save(file);
            ArrayList<CustomLootTableEntry> entryList = new ArrayList<>(Arrays.asList(entriesArray));
            tables.put(name.toLowerCase(), new CustomLootTable(name, entryList));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 
