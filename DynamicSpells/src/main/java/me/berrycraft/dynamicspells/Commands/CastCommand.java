package me.berrycraft.dynamicspells.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import me.berrycraft.dynamicspells.DynamicSpells;
import me.berrycraft.dynamicspells.Spell;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CastCommand implements CommandExecutor, TabCompleter {

    private final DynamicSpells plugin;

    public CastCommand(DynamicSpells plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage("Usage: /cast <spell_name> <level>");
            return true;
        }

        String spellName = args[0].toLowerCase();
        int level = 1;

        if (args.length >= 2) {
            try {
                level = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage("Invalid level: " + args[1]);
                return true;
            }
        }

        Map<String, Class<? extends Spell>> spellMap = plugin.stringToClass;
        Class<? extends Spell> clazz = spellMap.get(spellName);

        if (clazz == null) {
            player.sendMessage("Spell not found: " + spellName);
            return true;
        }

        try {
            Method method = clazz.getMethod("cast", Player.class, int.class);
            boolean success = (boolean) method.invoke(null, player, level);
            if (!success) {
                player.sendMessage("Failed to cast spell");
            }
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage("Failed to cast spell: " + e.getMessage());
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // Suggest spell names
            return new ArrayList<>(plugin.stringToClass.keySet());
        }

        if (args.length == 2) {
            // Suggest levels 1-5
            List<String> levels = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                levels.add(String.valueOf(i));
            }
            return levels;
        }

        return null;
    }
}
