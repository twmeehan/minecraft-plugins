package me.berrycraft.berryeconomy.items;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

// Handles events for all custom items
public class CustomItemEventHandler implements Listener {


    /*
     * When the player places a custom item like a
     * berry the event is cancelled
     */
    @EventHandler
    public void onBerryPlaced(BlockPlaceEvent e) {

        if (e.getItemInHand().getType()== Material.PLAYER_HEAD) {
            
            NBTItem nbti = new NBTItem(e.getItemInHand());
            //if (nbti.getString("CustomItem").equals("CommonCrate") || nbti.getString("CustomItem").equals("RareCrate")) return;
            e.setCancelled(nbti.hasTag("CustomItem"));

        }
    }
}
