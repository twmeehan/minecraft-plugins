package me.berrycraft.berryeconomy.auction.windows.elements;

import me.berrycraft.berryeconomy.auction.windows.Window;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/*
 * Filter represents the totem icon in the Auction Window
 * that allows the user to browse items by rarity. Clicking
 * the filter toggles through a list of rarities
 */
public class Filter extends Element{

    private static final String[] filters = {ChatColor.YELLOW + "All",ChatColor.WHITE + "Vanilla",
            ChatColor.DARK_GRAY + "Common",ChatColor.DARK_AQUA + "Uncommon",ChatColor.BLUE + "Rare",
            ChatColor.DARK_PURPLE + "Epic",ChatColor.GOLD + "Legendary"};

    public int selection = 0;

    public Filter(Window window, int slot) {
        this.window = window;
        this.slot = slot;

        // set up the icon
        icon = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta totemMeta = icon.getItemMeta();
        totemMeta.setDisplayName(ChatColor.YELLOW + "Filter");
        totemMeta.setLore(getLore());
        icon.setItemMeta(totemMeta);
        setIcon();

    }

    // updates the totems lore
    public void update() {
        ItemMeta totemMeta = icon.getItemMeta();
        totemMeta.setLore(getLore());
        icon.setItemMeta(totemMeta);
    }

    // the item's lore changes based off what filter is selected
    private ArrayList<String> getLore() {
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "");
        for (int i = 0; i < filters.length; i++) {
            if (i==selection) {
                lore.add(ChatColor.GOLD + "‣ "+filters[i]);
            } else {
                lore.add(ChatColor.GRAY + "⁃ "+filters[i]);
            }
        }
        return lore;

    }

    // jump to the next filter and update the icon's appearance
    @Override
    public void click() {
        selection++;
        selection %= filters.length;
        update();
    }
}
