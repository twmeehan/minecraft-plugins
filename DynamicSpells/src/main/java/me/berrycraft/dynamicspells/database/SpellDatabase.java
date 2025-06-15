package me.berrycraft.dynamicspells.database;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.*;

public class SpellDatabase {
    private static final Map<UUID, SpellAction> actions = new HashMap<>();
    
    public static void recordSpellAction(SpellAction action) {
        actions.put(action.getActionId(), action);
    }
    
    public static SpellAction getSpellAction(UUID actionId) {
        return actions.get(actionId);
    }
    
    public static List<SpellAction> getPlayerSpellActions(UUID playerUUID, int limit) {
        List<SpellAction> playerActions = new ArrayList<>();
        for (SpellAction action : actions.values()) {
            if (action.getPlayerUUID().equals(playerUUID)) {
                playerActions.add(action);
                if (playerActions.size() >= limit) {
                    break;
                }
            }
        }
        return playerActions;
    }
    
    public static void clearOldActions(long expiryTime) {
        long currentTime = System.currentTimeMillis();
        actions.entrySet().removeIf(entry -> 
            currentTime - entry.getValue().getTimestamp() > expiryTime
        );
    }
} 