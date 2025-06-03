package me.berrycraft.berryeconomy2.items;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

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
            e.setCancelled(nbti.hasTag("CustomItem"));

        }
    }
}
