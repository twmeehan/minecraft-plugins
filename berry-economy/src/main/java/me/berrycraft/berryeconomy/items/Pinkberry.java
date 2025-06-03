package me.berrycraft.berryeconomy.items;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/*
 * Pinkberry is a custom item that is a part of
 * the currency of berrycraft. It is worth 10 raspberries
 * and 0.1 of a rainbowberry
 */
public class Pinkberry extends CustomItem{

    // use new Pinkberry() to get an item stack containing 1 pinkberry
    public Pinkberry() {

        super(Material.PLAYER_HEAD);
        super.setSkull("http://textures.minecraft.net/texture/778308fe4dda1bb8a96f5b226b32542f49fd65bc55b44bbc31343eb400cf5e2");

        ItemMeta meta = this.getItemMeta();
        meta.setCustomModelData(1);
        meta.setDisplayName(ChatColor.AQUA + "Pinkberry");

        this.setItemMeta(meta);
        NBT.modify(this, nbt -> {
            nbt.setString("CustomItem","Pinkberry");
        });
        this.setAmount(1);

    }

    // use new Pinkberry(amount) to get an item stack containing said amount of pinkberry
    public Pinkberry(int amount) {

        super(Material.PLAYER_HEAD);
        super.setSkull("http://textures.minecraft.net/texture/778308fe4dda1bb8a96f5b226b32542f49fd65bc55b44bbc31343eb400cf5e2");

        ItemMeta meta = this.getItemMeta();
        meta.setCustomModelData(1);
        meta.setDisplayName(ChatColor.AQUA + "Pinkberry");

        this.setItemMeta(meta);
        NBT.modify(this, nbt -> {
            nbt.setString("CustomItem","Pinkberry");
        });
        this.setAmount(amount);

    }
    public static int getAmount(ItemStack stack) {
        if (stack.getType()!=Material.PLAYER_HEAD) return 0;
        NBTItem nbti = new NBTItem(stack);
        return nbti.getString("CustomItem").equals("Pinkberry") ? stack.getAmount() : 0;
    }
    public static int getAmount(Player p) {
        int total = 0;
        for (ItemStack stack : p.getInventory().getContents()) {
            if (stack== null || stack.getType()!=Material.PLAYER_HEAD) continue;
            else {
                NBTItem nbti = new NBTItem(stack);
                total += nbti.getString("CustomItem").equals("Pinkberry") ? stack.getAmount() : 0;
            }
        }
        return total;

    }
}
