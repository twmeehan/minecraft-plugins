package me.berrycraft.berryeconomy.commands;

import me.berrycraft.berryeconomy.custom_loot.CustomLootEventHandler;
import me.berrycraft.berryeconomy.custom_loot.CustomLootTable;
import me.berrycraft.berryeconomy.custom_loot.CustomLootTableWindow;
import me.berrycraft.berryeconomy.custom_loot.RigLoot;
import me.berrycraft.berryeconomy.items.CommonCrate;
import me.berrycraft.berryeconomy.items.Pinkberry;
import me.berrycraft.berryeconomy.items.Rainbowberry;
import me.berrycraft.berryeconomy.items.RareCrate;
import me.berrycraft.berryeconomy.items.Raspberry;
import me.berrycraft.berryeconomy.items.SpellBook;
import me.berrycraft.berryeconomy.npcs.AuctionNPC;
import me.berrycraft.berryeconomy.npcs.ExchangeNPC;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.stream.Collectors;


import java.util.*;

public class BerryCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!sender.hasPermission("berry-economy.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /berry <give|table|npc>");
            return true;
        }

        String subcommand = args[0].toLowerCase();
        switch (subcommand) {
            case "give":
                handleGive(player, args);
                break;
            case "table":
                handleTable(player, args);
                break;
            case "rig":
                handleRig(player, args);
                break;
            case "npc":
                handleNPC(player, args);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Invalid subcommand: " + subcommand);
        }

        return true;
    }

    private void handleRig(Player sender, String[] args) {
        if (args.length != 4) {
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }

        String crate = args[2];
        double value;
        try {
            value = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Value must be an integer between 1 and 5.");
            return;
        }

        RigLoot.set(target, crate, value);
        sender.sendMessage(ChatColor.GREEN + "Rigged loot for " + target.getName() + " on crate '" + crate + "' to rarity " + value + ".");
    }

    private void handleGive(Player sender, String[] args) {

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /berry give <player|@p|@a> <item> [amount]");
            return;
        }

        String itemName = args[2].toLowerCase();
        ItemStack stack = null;
        if (itemName.contains("rainbowberry")) stack = new Rainbowberry();
        else if (itemName.contains("pinkberry")) stack = new Pinkberry();
        else if (itemName.contains("raspberry")) stack = new Raspberry();
        else if (itemName.equals("spell_book")) stack = new SpellBook();
        else if (itemName.equals("common_crate")) stack = new CommonCrate();
        else if (itemName.equals("rare_crate")) stack = new RareCrate();


        if (stack == null) {
            sender.sendMessage(ChatColor.RED + "Unknown item: " + itemName);
            return;
        }

        // Optional amount
        if (args.length > 3) {
            try {
                int amount = Integer.parseInt(args[3]);
                if (amount > 0 && amount <= 64) {
                    stack.setAmount(amount);
                }
            } catch (NumberFormatException ignored) {}
        }

        List<Player> targets = new ArrayList<>();
        String targetArg = args[1];

        if (targetArg.equalsIgnoreCase("@p") || targetArg.equalsIgnoreCase("@s")) {
            targets.add(sender);
        } else if (targetArg.equalsIgnoreCase("@a")) {
            targets.addAll(Bukkit.getOnlinePlayers());
        } else {
            Player target = Bukkit.getPlayer(targetArg);
            if (target != null) {
                targets.add(target);
            }
        }

        if (targets.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No valid targets found.");
            return;
        }

        for (Player target : targets) {
            target.getInventory().addItem(stack.clone());
        }

        sender.sendMessage(ChatColor.GREEN + "Gave " + stack.getAmount() + " of " + itemName + " to " + targetArg);
    }

    private void handleTable(Player player, String[] args) {
        if (args.length != 3) {
            player.sendMessage(ChatColor.RED + "Usage: /berry table <create|edit|delete|roll|give> <table>");
            return;
        }

        String action = args[1].toLowerCase();
        String tableName = args[2];

        CustomLootTable table;
        Inventory inv;
        List<ItemStack> drops;

        switch (action) {
            case "create":
                CustomLootEventHandler.openWindow(player, new CustomLootTableWindow(player, tableName));
                player.sendMessage(ChatColor.GREEN + "Created loot table: " + tableName);
                break;
            case "edit":
                table = CustomLootTable.getTable(tableName);
                if (table == null) {
                    player.sendMessage(ChatColor.RED + "Loot table not found: " + tableName);
                    return;
                }
                CustomLootEventHandler.openWindow(player, new CustomLootTableWindow(player, tableName, table.getEntries()));
                player.sendMessage(ChatColor.YELLOW + "Editing loot table: " + tableName);
                break;
            case "delete":
                CustomLootTable.deleteTable(tableName);
                player.sendMessage(ChatColor.GREEN + "Deleted loot table: " + tableName);
                break;
            case "roll":
                table = CustomLootTable.getTable(tableName);
                if (table == null) {
                    player.sendMessage(ChatColor.RED + "Loot table not found: " + tableName);
                    return;
                }
                drops = table.roll(new Random());
                inv = Bukkit.createInventory(null, 27, "Rolled Loot: " + tableName);
                for (int i = 0; i < drops.size() && i < inv.getSize(); i++) {
                    inv.setItem(i, drops.get(i));
                }
                player.openInventory(inv);
                break;
            case "give":
                table = CustomLootTable.getTable(tableName);
                if (table == null) {
                    player.sendMessage(ChatColor.RED + "Loot table not found: " + tableName);
                    return;
                }
                drops = table.give();
                inv = Bukkit.createInventory(null, 27, "Loot: " + tableName);
                for (int i = 0; i < drops.size() && i < inv.getSize(); i++) {
                    inv.setItem(i, drops.get(i));
                }
                player.openInventory(inv);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown action: " + action);
        }
    }

    private void handleNPC(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Usage: /berry npc <auction|exchange>");
            return;
        }

        String npcType = args[1].toLowerCase();
        
        // Spawn a villager at the player's location
        Villager villager = player.getWorld().spawn(player.getLocation(), Villager.class);
        
        switch (npcType) {
            case "auction":
                AuctionNPC.markAsAuctionNPC(villager);
                player.sendMessage(ChatColor.GREEN + "Created Auction Master NPC!");
                break;
            case "exchange":
                ExchangeNPC.markAsExchangeNPC(villager);
                player.sendMessage(ChatColor.GREEN + "Created Exchange Master NPC!");
                break;
            default:
                player.sendMessage(ChatColor.RED + "Invalid NPC type. Use 'auction' or 'exchange'.");
                villager.remove();
                return;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("give", "table", "npc").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("table")) {
            return Arrays.asList("create", "edit", "delete", "roll", "give").stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("table")) {
            return new ArrayList<>(CustomLootTable.getTables().keySet()).stream()
                    .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return Arrays.asList("rainbowberry", "pinkberry", "raspberry", "spell_book","common_crate","rare_crate").stream()
                    .filter(s -> s.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            List<String> suggestions = new ArrayList<>();

            suggestions.addAll(Arrays.asList("@a", "@p", "@s"));
            for (Player player : Bukkit.getOnlinePlayers()) {
                suggestions.add(player.getName());
            }
            return suggestions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
