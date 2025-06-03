package me.berrycraft.berryeconomy.auction.windows;

import me.berrycraft.berryeconomy.Berry;
import me.berrycraft.berryeconomy.auction.AuctionEventHandler;
import me.berrycraft.berryeconomy.auction.MarketEntry;
import me.berrycraft.berryeconomy.auction.windows.elements.Filter;
import me.berrycraft.berryeconomy.auction.windows.elements.Search;
import me.berrycraft.berryeconomy.items.CustomItemEventHandler;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.LinkedList;

/*
 * This is the GUI for browsing items that
 * other players are selling. It's the first window
 * that pops up when you run /auction
 */
public class AuctionWindow extends Window {

    // all auction windows share a list of items that are for sale
    public static LinkedList<MarketEntry> marketEntries = new LinkedList<MarketEntry>();

    private MarketEntry[][] pages = new MarketEntry[marketEntries.size()/28+1][28];
    int currentPage = 0;
    Filter filter;
    Search search;

    public AuctionWindow(Player viewer) {
        size = 54;
        name = "Auction House";
        this.viewer = viewer;
        window = viewer.getServer().createInventory(viewer,size,name);
        setupEmtpyPage();
    }

    // sets up the items in the GUI
    private void setupEmtpyPage() {

        ItemStack border = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        border.addUnsafeEnchantment(Enchantment.SHARPNESS,1);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        borderMeta.setDisplayName(" ");
        border.setItemMeta(borderMeta);
        addBorder(border);

        ItemStack emerald = new ItemStack(Material.EMERALD);
        ItemMeta emeraldMeta = emerald.getItemMeta();
        emeraldMeta.setDisplayName(ChatColor.GREEN + ""+ChatColor.BOLD + "My Listings");
        emerald.setItemMeta(emeraldMeta);
        window.setItem(49,emerald);

        filter = new Filter(this,50);

        search = new Search(this,48);

        openPage(currentPage);
    }

    // runs when anything in the GUI is clicked
    @Override
    public void click(int slot) {
        if (slot==50) {
            filter.click();
        } else if (slot==48) {
            search.click();
        } else if (slot==49) {
            AuctionEventHandler.openMyListingsWindow(viewer);
        } else if (slot==53 && currentPage + 1 < pages.length) {
            openPage(currentPage + 1);

        } else if (slot==45 && currentPage !=0) {
            openPage(currentPage - 1);
        } else if (!(slot%9 == 0 || slot%9 == 8 || slot < 9 || slot>44)) {
            MarketEntry entry =getMarketEntry(slot);
            if (entry !=null) {
                AuctionEventHandler.openWindow(viewer,new ConfirmPurchaseWindow(viewer, this,entry));

            }
        }
    }

    private MarketEntry getMarketEntry(int slot) {
        return pages[currentPage][(slot - 10 - ((slot-9)/9)*2)];
    }


    public void openPage(int page) {

        updatePages();
        currentPage = page;

        ItemStack border = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        border.addUnsafeEnchantment(Enchantment.SHARPNESS,1);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        borderMeta.setDisplayName(" ");
        border.setItemMeta(borderMeta);
        window.setItem(53,border);
        window.setItem(45,border);

        if (page+1 < pages.length) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta meta = next.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "Next page");
            next.setItemMeta(meta);
            window.setItem(53,next);
        }

        if (page!=0) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta meta = next.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "Previous page");
            next.setItemMeta(meta);
            window.setItem(45,next);
        }

        clearCenter();
        for (int i =0;i<28;i++) {
            if (pages[page][i]!=null) {
                add(pages[page][i].getDisplayIcon());
            }
        }
    }

    public void updatePages() {
        pages = new MarketEntry[marketEntries.size()/28+1][28];
        int index = 0;
        int page = 0;
        for (int i = marketEntries.size()-1; i >= 0; i--) {

            MarketEntry entry = marketEntries.get(i);
            if (entry.getBuyer()!=null) continue;
            if (LocalDateTime.now().until(entry.getExpirationDate(), ChronoUnit.MINUTES)<0) continue;
            if (search.searchCriteria != null) {
                if (entry.getItem().getType().name().toUpperCase().contains(search.searchCriteria.replace(' ','_').toUpperCase())) {

                } else if (entry.getItem().getItemMeta()!= null && entry.getItem().getItemMeta().getDisplayName().replace(' ','_').toUpperCase().contains(search.searchCriteria.replace(' ','_').toUpperCase())) {

                } else{

                    continue;
                }
            }


            pages[page][index] = marketEntries.get(i);
            index++;
            if (index > 27) {
                index=0;
                page++;
            }

        }
    }
}
