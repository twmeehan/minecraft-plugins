package me.berrycraft.berryeconomy.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBT;

public class RareCrate extends CustomItem{

    public RareCrate() {
        super(Material.PLAYER_HEAD);
        super.setSkull("http://textures.minecraft.net/texture/64095ac2f42b9b649d00de7d761b56396d4e4dac9382ac02ed15e6ddf901aa4b");

        ItemMeta meta = this.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Rare Crate");

        this.setItemMeta(meta);
        NBT.modify(this, nbt -> {
            nbt.setString("CustomItem","RareCrate");
        });
        this.setAmount(1);
    }
    
}
