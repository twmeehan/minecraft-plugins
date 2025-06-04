package me.berrycraft.berryeconomy.custom_loot;

import me.berrycraft.berryeconomy.auction.windows.Window;
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

        if (player.getScoreboardTags().contains("settingWeight")) return;

        Window window = openedLootWindows.get(player);
        e.setCancelled(true);

        // Add from player inventory
        if (window instanceof CustomLootTableWindow &&
            e.getClickedInventory() != null &&
            e.getClickedInventory().getType() == InventoryType.PLAYER &&
            e.getCurrentItem() != null &&
            e.getCurrentItem().getType() != Material.AIR) {

            ((CustomLootTableWindow) window).addEntry(e.getCurrentItem().clone());
            return;
        }

        // GUI slot interaction
        if (e.getClickedInventory() != null &&
            e.getClickedInventory().equals(window.getInventory())) {
            window.click(e.getSlot());
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        Player player = (Player) e.getPlayer();

        if (player.getScoreboardTags().contains("updatingWindow") ||
            player.getScoreboardTags().contains("settingWeight")) return;

        Window window = openedLootWindows.get(player);
        if (window instanceof CustomLootTableWindow) {
            CustomLootTableWindow lootWindow = (CustomLootTableWindow) window;
            CustomLootTable.saveOrReplaceTable(lootWindow.getEntries(), lootWindow.getName());
        } else if (window instanceof CustomLootTableInspectionWindow) {
            CustomLootTableInspectionWindow inspectWindow = (CustomLootTableInspectionWindow) window;
            CustomLootTable.saveOrReplaceTable(inspectWindow.parent.getEntries(), inspectWindow.parent.getName());
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
