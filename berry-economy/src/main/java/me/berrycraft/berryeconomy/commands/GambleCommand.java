package me.berrycraft.berryeconomy.commands;

import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.berrycraft.berryeconomy.BerryUtility;
import me.berrycraft.berryeconomy.items.CommonCrate;
import me.berrycraft.berryeconomy.items.Pinkberry;
import me.berrycraft.berryeconomy.items.Rainbowberry;
import me.berrycraft.berryeconomy.items.RareCrate;
import me.berrycraft.berryeconomy.items.Raspberry;
import net.md_5.bungee.api.ChatColor;

public class GambleCommand implements CommandExecutor, Listener {

    LinkedList<String> playersGambling = new LinkedList<>();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        
        // only works for players
        if (!(commandSender instanceof Player)) return false;
        Player p = (Player)commandSender;

        // add this player to the list of players with the GUI open
        playersGambling.add(p.getName());
        Inventory GUI = p.getServer().createInventory(p,27,ChatColor.DARK_PURPLE +"Purchase Lootboxes");

        ItemStack commonCrate = new Raspberry();
        ItemMeta meta = commonCrate.getItemMeta();
        meta.setDisplayName(ChatColor.GRAY + "Purchase 1 " + ChatColor.YELLOW + " Common Crate");
        commonCrate.setItemMeta(meta);

        ItemStack rareCrate = new Rainbowberry();
        meta = rareCrate.getItemMeta();
        meta.setDisplayName(ChatColor.GRAY + "Purchase 1 " + ChatColor.LIGHT_PURPLE + " Rare Crate");
        rareCrate.setItemMeta(meta);

        // place items in their correct slots
        GUI.setItem(12,commonCrate);
        GUI.setItem(14,rareCrate);

        p.openInventory(GUI);
        return true;
    }

    // when the player clicks something in the GUI
    @EventHandler
    public void onItemClick(InventoryClickEvent e) {

        // if the player doesnt have the exchange GUI open do nothing
        if (!playersGambling.contains(e.getWhoClicked().getName())) return;
        Player p = (Player)e.getWhoClicked();

        // if player clicks their own inventory do nothing
        if (e.getClickedInventory()!=p.getInventory()) {
            if (e.getSlot()==12) {
                if (Raspberry.getAmount(p)*0.01+ Pinkberry.getAmount(p)*0.1+ Rainbowberry.getAmount(p)>= 1) {
                    p.sendMessage(ChatColor.GREEN + "Purchased!");
                    BerryUtility.give(p,new CommonCrate());
                    BerryUtility.removeBerries(p, 1);


                } else {
                    p.sendMessage(ChatColor.RED + "You do not have enough money");
                }
            } else if (e.getSlot()==14) {
                if (Raspberry.getAmount(p)*0.01+ Pinkberry.getAmount(p)*0.1+ Rainbowberry.getAmount(p)>= 8) {
                    p.sendMessage(ChatColor.GREEN + "Purchased!");
                    BerryUtility.give(p,new RareCrate());
                    BerryUtility.removeBerries(p, 8);


                } else {
                    p.sendMessage(ChatColor.RED + "You do not have enough money");
                }
            }
        }
        e.setCancelled(true);

    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {

        // if the player is not in the list of players viewing this GUI do nothing
        if (!playersGambling.contains(e.getPlayer().getName())) return;

        playersGambling.remove(e.getPlayer().getName());

    }
}
