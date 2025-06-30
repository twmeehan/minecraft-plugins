package me.berrycraft.berryeconomy.npcs;

import me.berrycraft.berryeconomy.auction.AuctionEventHandler;
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

public class AuctionNPC implements Listener {
    private final JavaPlugin plugin;
    private static final NamespacedKey NPC_TYPE_KEY = new NamespacedKey(Berry.getInstance(), "npc_type");

    public AuctionNPC(JavaPlugin plugin) {
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
        
        if ("auction".equals(npcType)) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            AuctionEventHandler.openAuctionWindow(player);
        }
    }

    public static void markAsAuctionNPC(Villager villager) {
        villager.getPersistentDataContainer().set(NPC_TYPE_KEY, PersistentDataType.STRING, "auction");
        villager.setCustomName("§6§lAuction Master");
        villager.setCustomNameVisible(true);
        villager.setProfession(Villager.Profession.LIBRARIAN);
        villager.setAI(false);
        villager.setInvulnerable(true);
    }

    public static boolean isAuctionNPC(Villager villager) {
        String npcType = villager.getPersistentDataContainer().get(NPC_TYPE_KEY, PersistentDataType.STRING);
        return "auction".equals(npcType);
    }
} 