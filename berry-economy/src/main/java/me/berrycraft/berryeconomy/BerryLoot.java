package me.berrycraft.berryeconomy;

import me.berrycraft.berryeconomy.items.Pinkberry;
import me.berrycraft.berryeconomy.items.Rainbowberry;
import me.berrycraft.berryeconomy.items.Raspberry;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.Lootable;

import java.util.*;

public class BerryLoot implements Listener {


    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        Random rand = new Random();
        if (e.getClickedBlock()!= null && (e.getClickedBlock().getType()==Material.CHEST)) {
            if (((Chest)e.getClickedBlock().getState()).getLootTable()!=null) {
                Inventory inventory = ((Chest)e.getClickedBlock().getState()).getBlockInventory();
                if (Math.random() < 0.5) {
                    inventory.setItem(rand.nextInt(inventory.getSize()),new Raspberry(rand.nextInt(3)+1));
                    inventory.setItem(rand.nextInt(inventory.getSize()),new Raspberry(rand.nextInt(3)+1));
                    inventory.setItem(rand.nextInt(inventory.getSize()),new Raspberry(rand.nextInt(3)+1));

                } else if (Math.random() < 0.5) {
                    inventory.setItem(rand.nextInt(inventory.getSize()),new Raspberry(rand.nextInt(5)+3));
                    inventory.setItem(rand.nextInt(inventory.getSize()),new Raspberry(rand.nextInt(5)+3));

                } else if (Math.random() < 0.5) {
                    inventory.setItem(rand.nextInt(inventory.getSize()),new Raspberry(rand.nextInt(4)+1));
                    inventory.setItem(rand.nextInt(inventory.getSize()),new Pinkberry(1));

                } else {
                    inventory.setItem(rand.nextInt(inventory.getSize()),new Pinkberry(rand.nextInt(2)+1));

                }

            }
        } else if (e.getClickedBlock()!= null && (e.getClickedBlock().getType()==Material.BARREL)) {
            if (((Barrel)e.getClickedBlock().getState()).getLootTable()!=null) {
                Inventory inventory = ((Barrel)e.getClickedBlock().getState()).getInventory();
                if (Math.random() < 0.5) {
                    inventory.setItem(rand.nextInt(inventory.getSize()),new Raspberry(rand.nextInt(3)+1));
                    inventory.setItem(rand.nextInt(inventory.getSize()),new Raspberry(rand.nextInt(3)+1));
                    inventory.setItem(rand.nextInt(inventory.getSize()),new Raspberry(rand.nextInt(3)+1));

                } else if (Math.random() < 0.5) {
                    inventory.setItem(rand.nextInt(inventory.getSize()),new Raspberry(rand.nextInt(5)+3));
                    inventory.setItem(rand.nextInt(inventory.getSize()),new Raspberry(rand.nextInt(5)+3));

                } else if (Math.random() < 0.5) {
                    inventory.setItem(rand.nextInt(inventory.getSize()),new Raspberry(rand.nextInt(4)+1));
                    inventory.setItem(rand.nextInt(inventory.getSize()),new Pinkberry(1));

                } else {
                    inventory.setItem(rand.nextInt(inventory.getSize()),new Pinkberry(rand.nextInt(2)+1));

                }

            }
        }
    }

}
