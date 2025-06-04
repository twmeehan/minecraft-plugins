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

        ItemStack itemDisplay = entry.getItem().clone();

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.GRAY + "Go Back");
        back.setItemMeta(backMeta);

        ItemStack weight = new ItemStack(Material.ANVIL);
        ItemMeta weightMeta = weight.getItemMeta();
        weightMeta.setDisplayName(ChatColor.YELLOW + "Adjust Weight");
        weight.setItemMeta(weightMeta);

        ItemStack rolls = new ItemStack(Material.REPEATER);
        ItemMeta rollsMeta = rolls.getItemMeta();
        rollsMeta.setDisplayName(ChatColor.AQUA + "Adjust Rolls");
        rolls.setItemMeta(rollsMeta);

        ItemStack randomness = new ItemStack(Material.ENDER_PEARL);
        ItemMeta randMeta = randomness.getItemMeta();
        randMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Adjust Randomness");
        randomness.setItemMeta(randMeta);

        ItemStack remove = new ItemStack(Material.BARRIER);
        ItemMeta removeMeta = remove.getItemMeta();
        removeMeta.setDisplayName(ChatColor.RED + "Remove Item");
        remove.setItemMeta(removeMeta);

        window.setItem(10, itemDisplay);
        window.setItem(12, back);
        window.setItem(13, weight);
        window.setItem(14, rolls);
        window.setItem(15, randomness);
        window.setItem(16, remove);
    }

    @Override
    public void click(int clickedSlot) {
        if (clickedSlot == 13) {
            viewer.closeInventory();
            viewer.sendMessage(ChatColor.YELLOW + "Type the new weight in chat:");

            WeightInputHandler.awaitWeight(viewer, newWeight -> {
                parent.updateEntry(slot, new CustomLootTableEntry(entry.getItem(), (int) Math.floor(newWeight), entry.getRolls(), entry.getRandomness()));
                CustomLootEventHandler.openWindow(viewer, parent);
            });

        } else if (clickedSlot == 14) {
            viewer.closeInventory();
            viewer.sendMessage(ChatColor.AQUA + "Type the new rolls (whole number):");

            WeightInputHandler.awaitWeight(viewer, newRolls -> {
                parent.updateEntry(slot, new CustomLootTableEntry(entry.getItem(), entry.getWeight(), (int)Math.floor(newRolls), entry.getRandomness()));
                CustomLootEventHandler.openWindow(viewer, parent);
            });

        } else if (clickedSlot == 15) {
            viewer.closeInventory();
            viewer.sendMessage(ChatColor.LIGHT_PURPLE + "Type the new randomness value:");

            WeightInputHandler.awaitWeight(viewer, newRandomness -> {
                parent.updateEntry(slot, new CustomLootTableEntry(entry.getItem(), entry.getWeight(), entry.getRolls(), newRandomness));
                CustomLootEventHandler.openWindow(viewer, parent);
            });

        } else if (clickedSlot == 16) {
            parent.removeEntry(slot);
            CustomLootEventHandler.openWindow(viewer, parent);

        } else if (clickedSlot == 12) {
            CustomLootEventHandler.openWindow(viewer, parent);
        }
    }
} 
