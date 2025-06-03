package me.berrycraft.bugfixes;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EventListener;

public class VillagerTradeListener implements Listener {

    @EventHandler
    public void onInteract(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player) || e.getClickedInventory() == null || e.getCursor() == null)  {
            return;
        }
        Player p = (Player) e.getWhoClicked();

        if (e.getClickedInventory().getType()== InventoryType.MERCHANT && e.getSlot()==2 && e.getCursor().getType()==Material.ENCHANTED_BOOK) {

            if (e.getView().getTopInventory().getItem(1).getType()== Material.BOOK) {

                ItemStack books = e.getView().getTopInventory().getItem(1);
                if (books.getAmount() > 1) {
                    books.setAmount(books.getAmount() - 1);
                    if (p.getInventory().firstEmpty()!= -1) {
                        p.getInventory().addItem(books);
                    } else {
                        p.getWorld().dropItem(p.getLocation(),books);
                    }
                    books.setAmount(1);
                    e.getView().getTopInventory().setItem(1,books);
                    //e.setCancelled(true);
                }
            } else if (e.getView().getTopInventory().getItem(0).getType()== Material.BOOK && e.getCursor().getType()==Material.ENCHANTED_BOOK) {

                ItemStack books = e.getView().getTopInventory().getItem(0);
                if (books.getAmount() > 1) {
                    books.setAmount(books.getAmount() - 1);
                    if (p.getInventory().firstEmpty()!= -1) {
                        p.getInventory().addItem(books);
                    } else {
                        p.getWorld().dropItem(p.getLocation(),books);
                    }
                    books.setAmount(1);
                    e.getView().getTopInventory().setItem(0,books);
                    //e.setCancelled(true);

                }
            }
        }
    }
}
