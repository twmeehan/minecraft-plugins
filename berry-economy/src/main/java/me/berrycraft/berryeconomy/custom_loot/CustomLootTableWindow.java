package me.berrycraft.berryeconomy.custom_loot;

import me.berrycraft.berryeconomy.auction.windows.Window;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class CustomLootTableWindow extends Window {

    private final Map<Integer, CustomLootTableEntry> entries = new HashMap<>();

    public CustomLootTableWindow(Player viewer,String name) {
        this.viewer = viewer;
        this.size = 54;
        this.name = name;
        this.window = viewer.getServer().createInventory(viewer, size, name);
    }

    @Override
    public void click(int slot) {
        if (!entries.containsKey(slot)) return;

        CustomLootTableEntry entry = entries.get(slot);
        CustomLootTableInspectionWindow inspectionWindow = new CustomLootTableInspectionWindow(viewer, this, slot, entry);
        CustomLootEventHandler.openWindow(viewer, inspectionWindow);
    }

    public void addEntry(ItemStack item) {
        for (int i = 0; i < size; i++) {
            if (!entries.containsKey(i)) {
                CustomLootTableEntry entry = new CustomLootTableEntry(item.clone(), 1.0);
                entries.put(i, entry);
                window.setItem(i, formatEntryItem(entry));
                break;
            }
        }
    }

    public void updateEntry(int slot, CustomLootTableEntry newEntry) {
        entries.put(slot, newEntry);
        window.setItem(slot, formatEntryItem(newEntry));
    }

    public void removeEntry(int slot) {
        entries.remove(slot);
        window.setItem(slot, null);
    }

    public CustomLootTableEntry[] getEntries() {
        return entries.values().toArray(new CustomLootTableEntry[0]);
    }

    private ItemStack formatEntryItem(CustomLootTableEntry entry) {
        ItemStack item = entry.getItem().clone();
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        lore.add(ChatColor.GRAY + "Weight: " + entry.getWeight());
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
