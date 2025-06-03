package me.berrycraft.berryeconomy2.auction;

import me.berrycraft.berryeconomy.BerryUtility;
import me.berrycraft.berryeconomy.auction.windows.AuctionWindow;
import me.berrycraft.berryeconomy.auction.windows.CreateListingWindow;
import me.berrycraft.berryeconomy.auction.windows.MyListingsWindow;
import me.berrycraft.berryeconomy.auction.windows.Window;
import me.berrycraft.berryeconomy.items.Pinkberry;
import me.berrycraft.berryeconomy.items.Rainbowberry;
import me.berrycraft.berryeconomy.items.Raspberry;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;

/*
 * This is the controlling class of the auction system. It
 * keeps track of which player is looking at what window and
 * handles GUI events for that player.
 */
public class AuctionEventHandler implements Listener {

    // maps each player to the window they have opened
    private static HashMap<Player, Window> openedWindow = new HashMap<Player,Window>();

    // opens a new auction window for Player p
    public static void openAuctionWindow(Player p) {
        p.addScoreboardTag("updatingWindow");
        openedWindow.put(p,new AuctionWindow(p));
        openedWindow.get(p).display();
        p.removeScoreboardTag("updatingWindow");
    }

    public static void openWindow(Player p,Window w) {
        p.addScoreboardTag("updatingWindow");
        openedWindow.put(p,w);
        openedWindow.get(p).display();
        p.removeScoreboardTag("updatingWindow");
    }

    public static void openMyListingsWindow(Player p) {
        p.addScoreboardTag("updatingWindow");
        openedWindow.put(p,new MyListingsWindow(p));
        openedWindow.get(p).display();
        p.removeScoreboardTag("updatingWindow");

    }

    public static void openCreateListingWindow(Player p) {
        p.addScoreboardTag("updatingWindow");
        openedWindow.put(p,new CreateListingWindow(p));
        openedWindow.get(p).display();
        p.removeScoreboardTag("updatingWindow");

    }

    // tells the system that Player p is no longer looking at a window
    // note that this doesn't close the GUI
    public static void closeWindow(Player p) {
        openedWindow.remove(p);
    }

    // checks which Window player p is looking at
    public static Window getOpenedWindow(Player p) {
        return openedWindow.get(p);
    }

    // if the clicking player has an auction GUI up, call that windows click() function
    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (!(e.getWhoClicked() instanceof Player)) return;

        if (openedWindow.containsKey((Player)e.getWhoClicked())) {

            // players that have been prompted for a input in chat are still considered to be looking
            // at the auction house but are able to interact with their inventories as normal
            if (e.getWhoClicked().getScoreboardTags().contains("searching")) return;
            if (e.getWhoClicked().getScoreboardTags().contains("settingPrice")) return;

            e.setCancelled(true);


            // if player is creating a listing and clicks a item in their inventory
            if (openedWindow.get((Player)e.getWhoClicked()) instanceof CreateListingWindow && e.getClickedInventory() != null && e.getCurrentItem() != null &&
                    e.getClickedInventory().getType()==InventoryType.PLAYER) {

                CreateListingWindow c = (CreateListingWindow) openedWindow.get((Player) e.getWhoClicked());
                if (Raspberry.getAmount(e.getCurrentItem())+ Pinkberry.getAmount(e.getCurrentItem())+ Rainbowberry.getAmount(e.getCurrentItem()) > 0) {
                    e.getWhoClicked().sendMessage(ChatColor.RED + "You can't sell that item");
                    return;
                }
                c.setItem(e.getCurrentItem());
                e.getClickedInventory().setItem(e.getSlot(),null);

                return;

            }

            openedWindow.get((Player)e.getWhoClicked()).click(e.getSlot());

        }
    }

    // if the player closes the auction house tell the system that this player is no longer
    // looking at a window
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        // when the player searches for something the inventory is closed but the system should
        // still keep track of that player's auction window
        if (e.getPlayer().getScoreboardTags().contains("searching")) return;
        if (e.getPlayer().getScoreboardTags().contains("settingPrice")) return;
        if (e.getPlayer().getScoreboardTags().contains("updatingWindow")) return;

        if (openedWindow.get(e.getPlayer()) instanceof CreateListingWindow) {
            CreateListingWindow c = (CreateListingWindow) openedWindow.get(e.getPlayer());
            BerryUtility.give((Player)e.getPlayer(),c.getItem());
        }
        closeWindow((Player)e.getPlayer());

    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        if (e.getPlayer().getScoreboardTags().contains("settingPrice")) {
            CreateListingWindow c = (CreateListingWindow) openedWindow.get(e.getPlayer());
            BerryUtility.give((Player)e.getPlayer(),c.getItem());
        }
        closeWindow(e.getPlayer());
        e.getPlayer().removeScoreboardTag("searching");
        e.getPlayer().removeScoreboardTag("exchanging");
        e.getPlayer().removeScoreboardTag("settingPrice");
        e.getPlayer().removeScoreboardTag("updatingWindow");


    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.getPlayer().removeScoreboardTag("searching");
        e.getPlayer().removeScoreboardTag("exchanging");
        e.getPlayer().removeScoreboardTag("settingPrice");
        e.getPlayer().removeScoreboardTag("updatingWindow");

    }


}
