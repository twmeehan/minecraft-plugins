package me.berrycraft.berryeconomy.custom_loot;

import org.bukkit.inventory.ItemStack;

public class CustomLootTableEntry {
    private final ItemStack item;
    private final int weight;
    private final int rolls;
    private final double randomness;

    public CustomLootTableEntry(ItemStack item, int weight, int rolls, double randomness) {
        this.item = item;
        this.weight = weight;
        this.rolls = rolls;
        this.randomness = randomness;
    }

    public ItemStack getItem() { return item; }
    public int getWeight() { return weight; }
    public int getRolls() { return rolls; }
    public double getRandomness() { return randomness; }
}
