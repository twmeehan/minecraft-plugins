package me.berrycraft.berryeconomy2.auction.windows;

import me.berrycraft.berryeconomy.Berry;
import me.berrycraft.berryeconomy.BerryUtility;
import me.berrycraft.berryeconomy.auction.AuctionEventHandler;
import me.berrycraft.berryeconomy.auction.MarketEntry;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CancelListingWindow extends Window {

    MarketEntry entry;

    public CancelListingWindow(Player viewer, MarketEntry entry) {

        this.viewer = viewer;
        this.entry = entry;
        size = 27;
        name = "Auction House";
        window = viewer.getServer().createInventory(viewer,size,name);

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta meta = back.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Back");
        back.setItemMeta(meta);
        window.setItem(11,back);

        ItemStack confirm = new ItemStack(Material.RED_TERRACOTTA);
        meta = confirm.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Cancel Auction");
        confirm.setItemMeta(meta);
        window.setItem(15,confirm);

        window.setItem(13,entry.getDisplayIcon());

    }
    @Override
    public void click(int slot) {

        if (slot==11) {
            AuctionEventHandler.openMyListingsWindow(viewer);
        } else if (slot==15) {
            entry.setBuyer(viewer);
            AuctionWindow.marketEntries.remove(entry);
            BerryUtility.give(viewer,entry.getItem());
            Berry.getInstance().getConfig().set(entry.getID().toString(), null);

            Berry.getInstance().saveConfig();
            AuctionEventHandler.openMyListingsWindow(viewer);
        }

    }
}
