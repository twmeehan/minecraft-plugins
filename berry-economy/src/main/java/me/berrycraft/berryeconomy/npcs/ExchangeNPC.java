package me.berrycraft.berryeconomy.npcs;

import me.berrycraft.berryeconomy.commands.ExchangeCommand;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import me.berrycraft.berryeconomy.Berry;

public class ExchangeNPC implements Listener {
    private final JavaPlugin plugin;
    private static final NamespacedKey NPC_TYPE_KEY = new NamespacedKey(Berry.getInstance(), "npc_type");

    public ExchangeNPC(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        
        if (!(entity instanceof Villager)) {
            return;
        }

        Villager villager = (Villager) entity;
        String npcType = villager.getPersistentDataContainer().get(NPC_TYPE_KEY, PersistentDataType.STRING);
        
        if ("exchange".equals(npcType)) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            ExchangeCommand.openExchangeWindow(player);
        }
    }

    public static void markAsExchangeNPC(Villager villager) {
        villager.getPersistentDataContainer().set(NPC_TYPE_KEY, PersistentDataType.STRING, "exchange");
        villager.setCustomName("§a§lMoney Changer");
        villager.setCustomNameVisible(true);
        villager.setProfession(Villager.Profession.CLERIC);
        villager.setAI(false);
        villager.setInvulnerable(true);
    }

    public static boolean isExchangeNPC(Villager villager) {
        String npcType = villager.getPersistentDataContainer().get(NPC_TYPE_KEY, PersistentDataType.STRING);
        return "exchange".equals(npcType);
    }
} 