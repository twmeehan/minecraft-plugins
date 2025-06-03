package me.berrycraft.berryeconomy2.auction.windows.elements;

import me.berrycraft.berryeconomy.auction.windows.Window;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/*
 * icon that displays lifetime statistics about the users
 * auction house usage
 */
public class Stats extends Element{

    public Stats(Window window, int slot) {

        this.window = window;
        this.slot = slot;
        icon = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.UNDERLINE +"Statistics");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "");

        // get stats from scoreboard
        lore.add(ChatColor.DARK_GRAY + "Listings Created: " + window.getViewer().getScoreboard().getObjective("lifetimeListings").getScore(window.getViewer().getName()).getScore());
        lore.add(ChatColor.DARK_GRAY + "Items Sold: " + window.getViewer().getScoreboard().getObjective("itemsSold").getScore(window.getViewer().getName()).getScore());
        lore.add(ChatColor.DARK_GRAY + "Total Profits: " + window.getViewer().getScoreboard().getObjective("lifetimeProfits").getScore(window.getViewer().getName()).getScore());
        lore.add(ChatColor.DARK_GRAY + "Items Purchased: "+ window.getViewer().getScoreboard().getObjective("itemsPurchased").getScore(window.getViewer().getName()).getScore());
        lore.add(ChatColor.DARK_GRAY + "Total Spending: "+ window.getViewer().getScoreboard().getObjective("lifetimeSpending").getScore(window.getViewer().getName()).getScore());
        meta.setLore(lore);
        icon.setItemMeta(meta);
        setIcon();

    }
    @Override
    public void click() {

    }
}
