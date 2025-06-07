package me.berrycraft.berryeconomy.auction.windows;

import me.berrycraft.berryeconomy.Berry;
import me.berrycraft.berryeconomy.BerryUtility;
import me.berrycraft.berryeconomy.auction.AuctionEventHandler;
import me.berrycraft.berryeconomy.auction.MarketEntry;
import me.berrycraft.berryeconomy.items.Pinkberry;
import me.berrycraft.berryeconomy.items.Rainbowberry;
import me.berrycraft.berryeconomy.items.Raspberry;
import me.berrycraft.berryeconomy.logs.AuctionLogs;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ConfirmPurchaseWindow extends Window {

    AuctionWindow prevWindow;
    MarketEntry entry;

    public ConfirmPurchaseWindow(Player viewer, AuctionWindow prevWindow, MarketEntry entry) {

        this.viewer = viewer;
        this.prevWindow = prevWindow;
        this.entry = entry;
        size = 27;
        name = "Auction House";
        window = viewer.getServer().createInventory(viewer,size,name);

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta meta = back.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Back");
        back.setItemMeta(meta);
        window.setItem(11,back);

        ItemStack confirm = new ItemStack(Material.GREEN_TERRACOTTA);
        meta = confirm.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Purchase Item");
        confirm.setItemMeta(meta);
        window.setItem(15,confirm);

        window.setItem(13,entry.getDisplayIcon());

    }
    @Override
    public void click(int slot) {

        if (slot==11) {
            AuctionEventHandler.openWindow(viewer,prevWindow);
        } else if (slot==15) {
            if (LocalDateTime.now().until(entry.getExpirationDate(), ChronoUnit.MINUTES)<0) {
                viewer.sendMessage(ChatColor.RED+"This auction has expired");
                AuctionEventHandler.openWindow(viewer,prevWindow);
                return;
            }
            if (entry.getBuyer()!=null) {
                viewer.sendMessage(ChatColor.RED+""+entry.getBuyer().getName()+" has already purchased this item");
                AuctionEventHandler.openWindow(viewer,prevWindow);
                return;
            }
            if (Raspberry.getAmount(viewer)*0.01+ Pinkberry.getAmount(viewer)*0.1+ Rainbowberry.getAmount(viewer)>entry.getPrice()) {
                entry.setBuyer(viewer);

                Berry.getInstance().getAuctionConfig().set(entry.getID().toString() + ".buyer", viewer);
                Berry.getInstance().saveConfig();
                AuctionLogs.logAuctionAction(entry.getSeller(),viewer,entry.getItem().getItemMeta().getDisplayName(),entry.getItem().getAmount(),(int)(entry.getPrice()*100));
                try {
                    Berry.getInstance().getAuctionConfig().save(Berry.getInstance().getAuctionFile());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                BerryUtility.removeBerries(viewer,((int)(entry.getPrice()*100+0.5))/100.0);
                BerryUtility.give(viewer, entry.getItem());
                viewer.playSound(viewer, Sound.BLOCK_AMETHYST_CLUSTER_PLACE,2.0f,1.3f);
                prevWindow.openPage(0);
                AuctionEventHandler.openWindow(viewer,prevWindow);


            } else {
                viewer.sendMessage(ChatColor.RED + "You do not have enough money");
            }


        }

    }
}
