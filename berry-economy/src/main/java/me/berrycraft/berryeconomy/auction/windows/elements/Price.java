package me.berrycraft.berryeconomy.auction.windows.elements;

import me.berrycraft.berryeconomy.Berry;
import me.berrycraft.berryeconomy.BerryUtility;
import me.berrycraft.berryeconomy.auction.AuctionEventHandler;
import me.berrycraft.berryeconomy.auction.windows.CreateListingWindow;
import me.berrycraft.berryeconomy.auction.windows.Window;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;

/*
 * Price contains the functionality for the gold nugget in the
 * CreateListingWindow that allows the player to set a price
 * for the item they are selling
 */
public class Price extends Element implements Listener {

    public double price = -1;

    private static HashMap<Player,Price> playersEnteringPrice = new HashMap<>();

    // this is a runnable function that is called 10s after the player has been prompted for the price
    private BukkitRunnable searchTimeout;

    public Price(Window window, int slot) {

        this.window = window;
        this.slot = slot;
        icon = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD+ "Price");
        meta.setLore(getLore());
        icon.setItemMeta(meta);
        setIcon();

    }
    public Price() {
        // used for calling events
    }


    // if the player types a message
    @EventHandler
    public void onPlayerType(AsyncPlayerChatEvent e) {

        // check if the player was prompted for a price
        if (e.getPlayer().getScoreboardTags().contains("settingPrice")) {

            /*
             * Events are handled by a empty Price object that is
             * not part of any Window. So we use playersEnteringPrice to get
             * a reference to the Price object associated with the
             * player.
             */
            Price s = playersEnteringPrice.get(e.getPlayer());

            try {
                s.price = Double.parseDouble(e.getMessage());
            } catch (NumberFormatException exception) {
                return;
            }
            e.getPlayer().removeScoreboardTag("settingPrice");
            e.setCancelled(true);

            // can't call sync methods in a async function, so we use BukkitRunnable
            new BukkitRunnable() {
                public void run() {

                    e.getPlayer().removePotionEffect(PotionEffectType.SLOWNESS);
                    s.searchTimeout.cancel();
                    ItemMeta meta = s.icon.getItemMeta();
                    meta.setLore(s.getLore());
                    s.icon.setItemMeta(meta);

                    s.window.display();
                    playersEnteringPrice.remove(e.getPlayer());

                }
            }.runTask(Berry.getInstance());


        }
    }

    // determines what the lore of this element will be
    public ArrayList<String> getLore() {
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "");
        if (price <= 0) {
            lore.add(ChatColor.GRAY + "Cost: " + ChatColor.RED + "0.00");
        } else {
            lore.add(ChatColor.GRAY + "Cost: " + ChatColor.GOLD + Math.round(price*100)*0.01);

        }
        lore.add(ChatColor.GRAY + "");
        lore.add(ChatColor.DARK_GRAY + "Click to set price");
        return lore;

    }
    @Override
    public void click() {

        Player p = window.getViewer();

        p.sendMessage(ChatColor.GREEN+"Please enter the price of the item:");
        // the price tag is added to a player which has been prompted for a price
        p.addScoreboardTag("settingPrice");

        // make sure that the system knows that Player p is prompted to enter a keyword by THIS Price object
        playersEnteringPrice.put(p,this);

        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,200,2,false, false, true));
        p.closeInventory();

        // this will run in 10s when the price request times out
        searchTimeout = new BukkitRunnable() {
            public void run() {
                if (p.getScoreboardTags().contains("settingPrice")) {

                    // give back the item that was being sold
                    BerryUtility.give(window.getViewer(),((CreateListingWindow) window).getItem());

                    // tell the system that this player no longer is looking at the auction house or setting a price
                    p.removeScoreboardTag("settingPrice");
                    AuctionEventHandler.closeWindow(p);

                    playersEnteringPrice.remove(p);

                    p.sendMessage(ChatColor.RED+"Auction creation cancelled");

                }
            }
        };
        searchTimeout.runTaskLater(Berry.getInstance(),200);

    }
}
