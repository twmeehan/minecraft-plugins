package me.berrycraft.berryeconomy2.items;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/*
 * Raspberry is a custom item that is a part of
 * the currency of berrycraft. It is worth 0.1 of a pinkberry
 */
public class Raspberry extends CustomItem{

    // use new Raspberry() to get an item stack containing 1 raspberry
    public Raspberry() {

        super(Material.COPPER_INGOT);

        ItemMeta meta = this.getItemMeta();
        meta.setCustomModelData(1);
        meta.setDisplayName(ChatColor.YELLOW + "Copper Coin");

        this.setItemMeta(meta);
        NBTItem nbti = new NBTItem(this);
        nbti.setString("CustomItem","Raspberry");
        nbti.applyNBT(this);
        this.setAmount(1);

    }

    // use new Raspberry(amount) to get an item stack containing x amount of raspberry
    public Raspberry(int amount) {

        super(Material.COPPER_INGOT);

        ItemMeta meta = this.getItemMeta();
        meta.setCustomModelData(1);
        meta.setDisplayName(ChatColor.YELLOW + "Copper Coin");

        this.setItemMeta(meta);
        NBTItem nbti = new NBTItem(this);
        nbti.setString("CustomItem","Raspberry");
        nbti.applyNBT(this);
        this.setAmount(amount);

    }
    public static int getAmount(ItemStack stack) {
        if (stack.getType()!=Material.COPPER_INGOT) return 0;
        NBTItem nbti = new NBTItem(stack);
        return nbti.getString("CustomItem").equals("Raspberry") ? stack.getAmount() : 0;
    }
    public static int getAmount(Player p) {
        int total = 0;
        for (ItemStack stack : p.getInventory().getContents()) {
            if (stack== null || stack.getType()!=Material.COPPER_INGOT) continue;
            else {
                NBTItem nbti = new NBTItem(stack);
                total += nbti.getString("CustomItem").equals("Raspberry") ? stack.getAmount() : 0;
            }
        }
        return total;

    }


}
