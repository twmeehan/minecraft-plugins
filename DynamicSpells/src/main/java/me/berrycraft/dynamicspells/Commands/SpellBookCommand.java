package me.berrycraft.dynamicspells.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.berrycraft.dynamicspells.DynamicSpells;
import me.berrycraft.dynamicspells.Spell;
import me.berrycraft.dynamicspells.SpellBookHandler;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpellBookCommand implements TabExecutor {

    private final DynamicSpells plugin;

    public SpellBookCommand(DynamicSpells plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage("Usage: /spellbook <spell_name> <level>");
            return true;
        }

        String spellName = args[0].toLowerCase();
        int level;

        try {
            level = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid level: " + args[1]);
            return true;
        }

        Map<String, Class<? extends Spell>> spellMap = plugin.stringToClass;
        Class<? extends Spell> spellClass = spellMap.get(spellName);

        if (spellClass == null) {
            player.sendMessage("Spell not found: " + spellName);
            return true;
        }

        try {
            ItemStack book = SpellBookHandler.getSpellBook(spellClass, level);
            player.getInventory().addItem(book);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return new ArrayList<>(plugin.stringToClass.keySet());
        }

        if (args.length == 2) {
            List<String> levels = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                levels.add(String.valueOf(i));
            }
            return levels;
        }

        return null;
    }
}
