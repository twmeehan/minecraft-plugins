package me.berrycraft.berryeconomy.commands;

import me.berrycraft.berryeconomy.custom_loot.*;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CustomLootCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player)sender;

        if (args.length != 2) {
            sender.sendMessage("Usage: /customloot <create|delete> <name>");
            return true;
        }

        String action = args[0].toLowerCase();
        String tableName = args[1];

        switch (action) {
            case "create" :
                CustomLootTableWindow window = new CustomLootTableWindow(player,tableName );
                CustomLootEventHandler.openWindow(player, window);
                player.sendMessage(ChatColor.GREEN + "Click items in your inventory to add them to the loot table.");
                break;

            case "delete" :
                CustomLootTable.deleteTable(tableName);
                sender.sendMessage("Deleted loot table: " + tableName);
    
                break;
            default : sender.sendMessage("Invalid action. Use 'create' or 'delete'.");
        }

        return true;
    }
}

