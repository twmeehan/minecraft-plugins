package me.berrycraft.berryeconomy2.items;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/*
 * Rainbowberry is a custom item that is a part of
 * the currency of berrycraft. It is worth 10 pinkberries
 */
public class Rainbowberry extends CustomItem {

    // use new Rainbowberry() to get an item stack containing 1 rainbowberry

    public Rainbowberry() {

        super(Material.GOLD_INGOT);

        ItemMeta meta = this.getItemMeta();
        meta.setCustomModelData(1);
        meta.setDisplayName(ChatColor.YELLOW + "Gold Coin");
        this.setItemMeta(meta);
        NBTItem nbti = new NBTItem(this);
        nbti.setString("CustomItem","Rainbowberry");
        nbti.applyNBT(this);
        this.setAmount(1);

    }

    // use new Rainbowberry(amount) to get an item stack containing x amount of rainbowberries

    public Rainbowberry(int amount) {

        super(Material.GOLD_INGOT);

        ItemMeta meta = this.getItemMeta();
        meta.setCustomModelData(1);
        meta.setDisplayName(ChatColor.YELLOW + "Gold Coin");
        this.setItemMeta(meta);
        NBTItem nbti = new NBTItem(this);
        nbti.setString("CustomItem","Rainbowberry");
        nbti.applyNBT(this);
        this.setAmount(amount);

    }
    public static int getAmount(ItemStack stack) {
        if (stack.getType()!=Material.GOLD_INGOT) return 0;
        NBTItem nbti = new NBTItem(stack);
        return nbti.getString("CustomItem").equals("Rainbowberry") ? stack.getAmount() : 0;
    }
    public static int getAmount(Player p) {
        int total = 0;
        for (ItemStack stack : p.getInventory().getContents()) {
            if (stack== null || stack.getType()!=Material.GOLD_INGOT) continue;
            else {
                NBTItem nbti = new NBTItem(stack);
                total += nbti.getString("CustomItem").equals("Rainbowberry") ? stack.getAmount() : 0;
            }
        }
        return total;

    }
}
