package me.berrycraft.berryeconomy.commands;

import me.berrycraft.berryeconomy.custom_loot.*;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;

public class CustomLootCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /customloot <create|edit|delete> <name>");
            return true;
        }

        String action = args[0].toLowerCase();
        String tableName = args[1];

        switch (action) {
            case "create":
                CustomLootTableWindow createWindow = new CustomLootTableWindow(player, tableName);
                CustomLootEventHandler.openWindow(player, createWindow);
                player.sendMessage(ChatColor.GREEN + "Click items in your inventory to add them to the loot table.");
                break;

            case "edit":
                CustomLootTable existing = CustomLootTable.getTable(tableName);
                if (existing == null) {
                    player.sendMessage(ChatColor.RED + "Loot table not found: " + tableName);
                    return true;
                }
                CustomLootTableWindow editWindow = new CustomLootTableWindow(player, tableName, existing.getEntries());
                CustomLootEventHandler.openWindow(player, editWindow);
                player.sendMessage(ChatColor.YELLOW + "Editing loot table: " + tableName);
                break;

            case "delete":
                CustomLootTable.deleteTable(tableName);
                sender.sendMessage(ChatColor.GREEN + "Deleted loot table: " + tableName);
                break;

            case "roll":
                CustomLootTable table = CustomLootTable.getTable(tableName);
                if (table == null) {
                    player.sendMessage(ChatColor.RED + "Loot table not found: " + tableName);
                    return true;
                }

                LinkedList<ItemStack> drops = table.roll(new Random());
                Inventory inv = Bukkit.createInventory(null, 27, "Rolled Loot: " + tableName); // Adjust size if needed

                int i = 0;
                for (ItemStack item : drops) {
                    inv.setItem(i, item);
                    i++;
                    if (i >= inv.getSize()) break;
                }

                player.openInventory(inv);
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Invalid action. Use 'create', 'edit', or 'delete'.");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "edit", "delete","roll").stream()
                    .filter(option -> option.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            return CustomLootTable.getTables().keySet().stream()
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
} 
