package me.berrycraft.berryeconomy.auction;

import de.tr7zw.nbtapi.NBT;
import me.berrycraft.berryeconomy.BerryUtility;
import me.berrycraft.berryeconomy.items.Pinkberry;
import me.berrycraft.berryeconomy.items.Rainbowberry;
import me.berrycraft.berryeconomy.items.Raspberry;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MarketEntry {

    UUID id;
    ItemStack item;
    double price;
    OfflinePlayer seller;
    OfflinePlayer buyer;
    LocalDateTime expirationDate;

    public MarketEntry(ItemStack item, double price, Player seller, LocalDateTime expirationDate) {
        this.id = UUID.randomUUID();
        this.item = item;
        this.price = price;
        this.seller = seller;
        this.expirationDate = expirationDate;
    }
    public MarketEntry(UUID id, ItemStack item, double price, OfflinePlayer seller, OfflinePlayer buyer, LocalDateTime expirationDate) {
        this.id = id;
        this.item = item;
        this.price = price;
        this.seller = seller;
        this.buyer = buyer;
        this.expirationDate = expirationDate;
    }

    public ItemStack getItem() {
        return item;
    }
    public UUID getID() {
        return id;
    }

    public double getPrice() {
        return price;
    }

    public OfflinePlayer getSeller() {
        return seller;
    }

    public void setBuyer(Player p) {
        buyer = p;
    }
    public void setBuyer(OfflinePlayer p) {
        buyer = p;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public OfflinePlayer getBuyer() {
        return buyer;
    }

    public ItemStack getDisplayIcon() {
        if (buyer != null) {
            return setupSoldItem();
        }
        int min = (int)LocalDateTime.now().until(expirationDate, ChronoUnit.MINUTES);

        if (min < 0) {
            return setupExpiredItem();
        }
        return setupPurchasableItem();
    }

    private ItemStack setupPurchasableItem() {

        ItemStack icon = item.clone();
        List<String> itemLore = icon.getItemMeta().getLore();
        ArrayList<String> iconLore = new ArrayList<>();
        iconLore.add(ChatColor.GRAY + "");
        iconLore.add(ChatColor.GRAY + "Price: " + ChatColor.GOLD + ((int)(price*100)) +"$");
        iconLore.add(ChatColor.GRAY + "Seller: " + ChatColor.GREEN + seller.getName());
        int min = (int)LocalDateTime.now().until(expirationDate, ChronoUnit.MINUTES);
        if (min < 0) {
            iconLore.add(ChatColor.GRAY + "Time Remaining: " +ChatColor.RED + "Expired");

        } else {
            iconLore.add(ChatColor.GRAY + "Time Remaining: " + min / 1440 + "d " + (min % 1440) / 60 + "h " + (min % 60) + "m");
        }
        if (itemLore != null) {
            iconLore.add(ChatColor.GRAY + "");
            iconLore.add(ChatColor.GOLD + "-----------------------------");
            for (String s : itemLore) {
                iconLore.add(s);
            }
        } else {
            iconLore.add(ChatColor.GRAY + "");
            iconLore.add(ChatColor.WHITE +""+ChatColor.BOLD +"Vanilla");
        }
        ItemMeta meta = icon.getItemMeta();
        meta.setLore(iconLore);
        icon.setItemMeta(meta);
        NBT.modify(icon, nbt -> {
            nbt.setString("ID",UUID.randomUUID().toString());
        });
        return icon;
    }
    private ItemStack setupExpiredItem() {

        ItemStack icon = item.clone();
        List<String> itemLore = icon.getItemMeta().getLore();
        ArrayList<String> iconLore = new ArrayList<>();
        iconLore.add(ChatColor.GRAY + "");
        iconLore.add(ChatColor.GRAY + "Price: " + ChatColor.GOLD + ((int)(price*100)) +"$");
        int min = (int)LocalDateTime.now().until(expirationDate, ChronoUnit.MINUTES);
        if (min < 0) {
            iconLore.add(ChatColor.GRAY + "Time Remaining: " +ChatColor.RED + "Expired");

        } else {
            iconLore.add(ChatColor.GRAY + "Time Remaining: " + min / 1440 + "d " + (min % 1440) / 60 + "h " + (min % 60) + "m");
        }
        if (itemLore != null) {
            iconLore.add(ChatColor.GRAY + "");
            iconLore.add(ChatColor.GOLD + "-----------------------------");
            for (String s : itemLore) {
                iconLore.add(s);
            }
        } else {
            iconLore.add(ChatColor.GRAY + "");
            iconLore.add(ChatColor.WHITE +""+ChatColor.BOLD +"Vanilla");
        }
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "[EXPIRED] " + item.getItemMeta().getLocalizedName());
        meta.setLore(iconLore);
        icon.setItemMeta(meta);
        NBT.modify(icon, nbt -> {
            nbt.setString("ID",UUID.randomUUID().toString());
        });
        return icon;
    }
    private ItemStack setupSoldItem() {

        ItemStack icon;

        if (price < 0.1) {
            icon = new Raspberry();
            // +0.5 helps round
            icon.setAmount((int) (price * 100 +0.5));
        } else if (price < 1) {
            icon = new Pinkberry();
            icon.setAmount((int) (price * 10 +0.5));
        } else {
            icon = new Rainbowberry();
            if (price > 64) {
                icon.setAmount(64);

            } else {
                icon.setAmount((int) (price +0.5));

            }
        }

        BerryUtility.addRandUUID(icon);

        List<String> itemLore = item.getItemMeta().getLore();
        ArrayList<String> iconLore = new ArrayList<>();
        iconLore.add(ChatColor.GRAY + "");
        iconLore.add(ChatColor.GRAY + "Profits: " + ChatColor.GOLD + ((int)(price*100)) +"$");
        iconLore.add(ChatColor.GRAY + "Buyer: " + ChatColor.GREEN + buyer.getName());
        if (itemLore != null) {
            iconLore.add(ChatColor.GRAY + "");
            iconLore.add(ChatColor.GOLD + "-----------------------------");
            for (String s : itemLore) {
                iconLore.add(s);
            }
        } else {
            iconLore.add(ChatColor.GRAY + "");
            iconLore.add(ChatColor.WHITE +""+ChatColor.BOLD +"Vanilla");
        }
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "[SOLD] " +item.getItemMeta().getDisplayName());
        meta.setLore(iconLore);
        icon.setItemMeta(meta);
        NBT.modify(icon, nbt -> {
            nbt.setString("ID",UUID.randomUUID().toString());
        });

        return icon;
    }
}
