package me.berrycraft.berryeconomy.npcs;

import me.berrycraft.berryeconomy.Berry;
import me.berrycraft.berryeconomy.windows.RepairWindow;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class RepairNPC implements Listener {
    private final JavaPlugin plugin;
    private static final NamespacedKey NPC_TYPE_KEY = new NamespacedKey(Berry.getInstance(), "npc_type");

    public RepairNPC(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (!(entity instanceof Villager)) return;
        Villager villager = (Villager) entity;
        String npcType = villager.getPersistentDataContainer().get(NPC_TYPE_KEY, PersistentDataType.STRING);
        if ("repair".equals(npcType)) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            RepairWindow.open(player);
        }
    }

    public static void markAsRepairNPC(Villager villager) {
        villager.getPersistentDataContainer().set(NPC_TYPE_KEY, PersistentDataType.STRING, "repair");
        villager.setCustomName("§b§lSpellbook Repair Specialist");
        villager.setCustomNameVisible(true);
        villager.setProfession(Villager.Profession.TOOLSMITH);
        villager.setAI(false);
        villager.setInvulnerable(true);
    }

    public static boolean isRepairNPC(Villager villager) {
        String npcType = villager.getPersistentDataContainer().get(NPC_TYPE_KEY, PersistentDataType.STRING);
        return "repair".equals(npcType);
    }
}
