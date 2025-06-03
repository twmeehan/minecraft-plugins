package me.berrycraft.berryeconomy.custom_loot;

import org.bukkit.inventory.ItemStack;

public class CustomLootTableEntry {

    private final ItemStack item;
    private final double weight;

    public CustomLootTableEntry(ItemStack item, double weight) {
        this.item = item;
        this.weight = weight;
    }

    public ItemStack getItem() {
        return item;
    }

    public double getWeight() {
        return weight;
    }
}