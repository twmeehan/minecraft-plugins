package me.berrycraft.berryeconomy.custom_loot;

import me.berrycraft.berryeconomy.BerryUtility;
import me.berrycraft.berryeconomy.auction.windows.CreateListingWindow;
import me.berrycraft.berryeconomy.auction.windows.Window;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class CustomLootEventHandler implements Listener {

    // maps each player to the custom loot window they have open
    private static final HashMap<Player, Window> openedLootWindows = new HashMap<>();

    public static void openWindow(Player p, Window window) {
        p.addScoreboardTag("updatingWindow");
        openedLootWindows.put(p, window);
        window.display();
        p.removeScoreboardTag("updatingWindow");
    }

    public static void closeWindow(Player p) {
        openedLootWindows.remove(p);
    }

    public static Window getOpenedWindow(Player p) {
        return openedLootWindows.get(p);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();

        if (!openedLootWindows.containsKey(player)) return;

        // Let chat-based input work normally
        if (player.getScoreboardTags().contains("settingWeight")) return;

        Window window = openedLootWindows.get(player);
        e.setCancelled(true);

        // If clicking in player inventory to add items to CustomLootTableWindow
        if (window instanceof CustomLootTableWindow &&
                e.getClickedInventory() != null &&
                e.getClickedInventory().getType() == InventoryType.PLAYER &&
                e.getCurrentItem() != null &&
                e.getCurrentItem().getType() != Material.AIR) {

            
            ((CustomLootTableWindow)window).addEntry(e.getCurrentItem().clone());
            return;
        }

        // Clicked inside the GUI — route to the window's click method
        if (e.getClickedInventory() != null &&
            e.getClickedInventory().equals(window.getInventory())) {
            window.click(e.getSlot());
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        Player player = (Player)e.getPlayer();

        if (player.getScoreboardTags().contains("updatingWindow")) return;
        if (player.getScoreboardTags().contains("settingWeight")) return;

        if (openedLootWindows.get(player) instanceof CustomLootTableWindow) {
            CustomLootTableWindow window = (CustomLootTableWindow) openedLootWindows.get(player);
            CustomLootTable.createNewTable(window.getEntries(), window.getName());

        } else if (openedLootWindows.get(player) instanceof CustomLootTableInspectionWindow) {
            CustomLootTableInspectionWindow window = (CustomLootTableInspectionWindow) openedLootWindows.get(player);
            CustomLootTable.createNewTable(window.parent.getEntries(), window.getName());

        }
        closeWindow(player);


    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        closeWindow(e.getPlayer());
        e.getPlayer().removeScoreboardTag("updatingWindow");
        e.getPlayer().removeScoreboardTag("settingWeight");


    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.getPlayer().removeScoreboardTag("updatingWindow");
        e.getPlayer().removeScoreboardTag("settingWeight");

    }
}
