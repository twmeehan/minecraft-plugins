package me.berrycraft.dynamicspells;

public enum SpellDamageType {
    TRUE,   // Ignores armor and enchantments
    NORMAL, // Vanilla-style damage (affected by armor and enchantments)
    MAGIC   // Ignores enchantments
}