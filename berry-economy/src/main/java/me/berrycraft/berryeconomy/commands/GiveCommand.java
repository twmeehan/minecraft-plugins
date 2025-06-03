package me.berrycraft.berryeconomy.commands;

import me.berrycraft.berryeconomy.items.Pinkberry;
import me.berrycraft.berryeconomy.items.Rainbowberry;
import me.berrycraft.berryeconomy.items.Raspberry;
import me.berrycraft.berryeconomy.items.SpellBook;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBTItem;
import java.util.LinkedList;
import java.util.List;

/*
 * Listens for the /give command and if one of
 * the custom items is specified, runs the code
 * to give that item
 */
public class GiveCommand implements Listener {
    @EventHandler
    public boolean onCommand(PlayerCommandPreprocessEvent e) {

        if (!e.getPlayer().hasPermission("berry-economy.give")) return false;

        if (e.getMessage().substring(0,5).equals("/give")) {

            String[] args = e.getMessage().split(" ");
            if (args.length < 2) {
                return false;
            }

            ItemStack stack = null;

            // first check if the item is a custom item since this is possible to be false
            switch (args[2]) {
                case "berrycraft:rainbowberry" :
                    stack = new Rainbowberry();
                    break;
                case "minecraft:rainbowberry" :
                    stack = new Rainbowberry();
                    break;
                case "rainbowberry" :
                    stack = new Rainbowberry();
                    break;
                case "berrycraft:pinkberry" :
                    stack = new Pinkberry();
                    break;
                case "minecraft:pinkberry" :
                    stack = new Pinkberry();
                    break;
                case "pinkberry" :
                    stack = new Pinkberry();
                    break;
                case "berrycraft:raspberry" :
                    stack = new Raspberry();
                    break;
                case "minecraft:raspberry" :
                    stack = new Raspberry();
                    break;
                case "raspberry" :
                    stack = new Raspberry();
                    break;
                case "spell_book" :
                    stack = new SpellBook();
                    break;

            }

            // if the item is not a custom item then return
            if (stack == null) return false;

            // handles things like /give @p
            LinkedList<Player> targets = new LinkedList<Player>();
            switch (args[1]) {
                case "@p":
                case "@s":
                    targets.add(e.getPlayer());
                    break;
                case "@a":
                    targets.addAll(e.getPlayer().getServer().getOnlinePlayers());
                    break;
            }
            if (e.getPlayer().getServer().getPlayer(args[1]) != null) {
                targets.add(e.getPlayer().getServer().getPlayer(args[1]));
            }
            if (targets.isEmpty()) return false;

            // if amount is specified
            if (args.length > 3 && Integer.parseInt(args[3]) > 0 && Integer.parseInt(args[3]) <=64)
                stack.setAmount(Integer.parseInt(args[3]));

            for (Player p : targets) {
                p.getInventory().addItem(stack);
                e.setCancelled(true);
            }

        }
        return false;
    }

}
