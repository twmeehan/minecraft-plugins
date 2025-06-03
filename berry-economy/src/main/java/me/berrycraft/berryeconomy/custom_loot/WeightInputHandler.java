package me.berrycraft.berryeconomy.custom_loot;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import me.berrycraft.berryeconomy.Berry;

import java.util.HashMap;
import java.util.function.Consumer;

public class WeightInputHandler implements Listener {

    private static final HashMap<Player, Consumer<Double>> waiting = new HashMap<>();

    public static void awaitWeight(Player player, Consumer<Double> callback) {
        waiting.put(player, callback);
        player.addScoreboardTag("settingWeight");
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!waiting.containsKey(player)) return;

        event.setCancelled(true);

        try {
            double value = Double.parseDouble(event.getMessage());
            Bukkit.getScheduler().runTask(Berry.getInstance(), () -> {
                Consumer<Double> callback = waiting.remove(player);
                player.removeScoreboardTag("settingWeight");
                if (callback != null) {
                    callback.accept(value);
                    player.sendMessage("§aWeight set to " + value + ".");
                }
            });
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid number. Try again.");
        }
    }
}
