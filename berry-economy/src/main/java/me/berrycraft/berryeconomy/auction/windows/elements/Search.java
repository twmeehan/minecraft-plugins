package me.berrycraft.berryeconomy.auction.windows.elements;

import me.berrycraft.berryeconomy.Berry;
import me.berrycraft.berryeconomy.auction.AuctionEventHandler;
import me.berrycraft.berryeconomy.auction.windows.AuctionWindow;
import me.berrycraft.berryeconomy.auction.windows.Window;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/*
 * Search is an element of AuctionWindow that switches
 * between a spyglass and a brush dependent on whether
 * the player is currently searching for a keyword. Pressing
 * the spyglass allows the player to enter a keyword to search
 * for and clicking on the brush clears the current search
 */
public class Search extends Element implements Listener {

    // the keyword that the user is searching for
    public String searchCriteria;

    // links players back to the search object that is asking for a keyword
    private static HashMap<Player,Search> openSearches = new HashMap<>();

    // this is a runnable function that is called 10s after the player has been prompted for a keyword
    private BukkitRunnable searchTimeout;

    public Search(Window window, int slot) {
        this.window = window;
        this.slot = slot;
        createSpyglass();
        setIcon();
    }
    public Search() {
        // this is just for registering events
    }

    // if the player is supposed to be entering a keyword dont let them open inventories
    @EventHandler
    public void onOpenInventory(InventoryOpenEvent e) {
        // the searching tag is added to a player which has been prompted for a keyword
        if (e.getPlayer().getScoreboardTags().contains("searching")) e.setCancelled(true);
    }

    // if the player types a message
    @EventHandler
    public void onPlayerType(AsyncPlayerChatEvent e) {

        // check if the player was prompted for a keyword
        if (e.getPlayer().getScoreboardTags().contains("searching")) {

            /*
             * Events are handled by a empty Search object that is
             * not part of any Window. So we use openSearches to get
             * a reference to the Search object associated with the
             * player.
             */
            Search s = openSearches.get(e.getPlayer());

            s.searchCriteria = e.getMessage();
            e.getPlayer().removeScoreboardTag("searching");
            e.setCancelled(true);

            // can't call sync methods in a async function, so we use BukkitRunnable
            new BukkitRunnable() {
                public void run() {

                    e.getPlayer().removePotionEffect(PotionEffectType.SLOWNESS);
                    s.createBrush();
                    s.setIcon();
                    s.searchTimeout.cancel();
                    s.window.display();
                    ((AuctionWindow)s.window).openPage(0);
                    openSearches.remove(e.getPlayer());

                }
            }.runTask(Berry.getInstance());


        }
    }

    // called when this element is clicked
    public void click() {

        Player p = window.getViewer();

        if (searchCriteria==null) {

            // prompt this player for a keyword to look for in chat
            p.sendMessage(ChatColor.GREEN+"Please enter the item you would like to search for:");
            // the searching tag is added to a player which has been prompted for a keyword
            p.addScoreboardTag("searching");

            // make sure that the system knows that Player p is prompted to enter a keyword by THIS Search object
            openSearches.put(p,this);

            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,200,2,false, false, true));
            p.closeInventory();

            // this will run in 10s when the search times out
            searchTimeout = new BukkitRunnable() {
                public void run() {
                    if (p.getScoreboardTags().contains("searching")) {
                        p.removeScoreboardTag("searching");
                        openSearches.remove(p);
                        p.sendMessage(ChatColor.RED+"Search cancelled");
                    }
                }
            };
            searchTimeout.runTaskLater(Berry.getInstance(),200);

        } else {

            searchCriteria = null;
            createSpyglass();
            setIcon();

        }
        ((AuctionWindow)window).openPage(0);
    }

    private void createSpyglass() {

        icon = new ItemStack(Material.SPYGLASS);
        ItemMeta spyglassMeta = icon.getItemMeta();
        spyglassMeta.setDisplayName(ChatColor.AQUA + "Search");
        icon.setItemMeta(spyglassMeta);

    }

    private void createBrush() {

        icon = new ItemStack(Material.BRUSH);
        ItemMeta spyglassMeta = icon.getItemMeta();
        spyglassMeta.setDisplayName(ChatColor.AQUA + "Clear Search");
        spyglassMeta.setLore(getLore());
        icon.setItemMeta(spyglassMeta);

    }

    private ArrayList<String> getLore() {

        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "");
        lore.add(ChatColor.DARK_GRAY + "Searching for \"" +searchCriteria+"\"");
        return lore;

    }
}
