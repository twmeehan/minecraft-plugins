package me.berrycraft.berryeconomy.custom_loot;

import me.berrycraft.berryeconomy.auction.windows.Window;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CustomLootTableInspectionWindow extends Window {

    public final CustomLootTableWindow parent;
    private final int slot;
    private final CustomLootTableEntry entry;

    public CustomLootTableInspectionWindow(Player viewer, CustomLootTableWindow parent, int slot, CustomLootTableEntry entry) {
        this.viewer = viewer;
        this.parent = parent;
        this.slot = slot;
        this.entry = entry;

        this.size = 27;
        this.name = "Edit Entry";
        this.window = viewer.getServer().createInventory(viewer, size, name);

        ItemStack weight = new ItemStack(Material.ANVIL);
        ItemMeta weightMeta = weight.getItemMeta();
        weightMeta.setDisplayName(ChatColor.YELLOW + "Adjust Weight");
        weight.setItemMeta(weightMeta);

        ItemStack remove = new ItemStack(Material.BARRIER);
        ItemMeta removeMeta = remove.getItemMeta();
        removeMeta.setDisplayName(ChatColor.RED + "Remove Item");
        remove.setItemMeta(removeMeta);

        window.setItem(11, weight);
        window.setItem(13, entry.getItem().clone());
        window.setItem(15, remove);
    }

    @Override
    public void click(int clickedSlot) {
        if (clickedSlot == 11) {
            viewer.closeInventory();
            viewer.sendMessage(ChatColor.YELLOW + "Type the new weight in chat:");

            WeightInputHandler.awaitWeight(viewer, newWeight -> {
                parent.updateEntry(slot, new CustomLootTableEntry(entry.getItem(), newWeight));
                CustomLootEventHandler.openWindow(viewer, parent);
            });

        } else if (clickedSlot == 15) {
            parent.removeEntry(slot);
            CustomLootEventHandler.openWindow(viewer, parent);
        }
    }
}
