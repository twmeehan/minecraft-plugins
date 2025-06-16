package me.berrycraft.dynamicspells.database;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.io.Serializable;
import java.util.*;

public class SpellAction implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final UUID actionId;
    private final UUID playerUUID;
    private final String playerName;
    private final String spellName;
    private final long timestamp;
    private final List<BlockChange> blockChanges;
    private final List<ItemStack> itemsUsed;
    
    public SpellAction(UUID playerUUID, String playerName, String spellName) {
        this.actionId = UUID.randomUUID();
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.spellName = spellName;
        this.timestamp = System.currentTimeMillis();
        this.blockChanges = new ArrayList<>();
        this.itemsUsed = new ArrayList<>();
    }
    
    public void addBlockChange(int x, int y, int z, Material originalMaterial, Material newMaterial) {
        blockChanges.add(new BlockChange(x, y, z, originalMaterial, newMaterial));
    }
    
    public void addItemUsed(ItemStack item) {
        if (item != null) {
            itemsUsed.add(item.clone());
        }
    }
    
    public UUID getActionId() {
        return actionId;
    }
    
    public UUID getPlayerUUID() {
        return playerUUID;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public String getSpellName() {
        return spellName;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public List<BlockChange> getBlockChanges() {
        return Collections.unmodifiableList(blockChanges);
    }
    
    public List<ItemStack> getItemsUsed() {
        return Collections.unmodifiableList(itemsUsed);
    }
    
    public static class BlockChange implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private final int x;
        private final int y;
        private final int z;
        private final Material originalMaterial;
        private final Material newMaterial;
        
        public BlockChange(int x, int y, int z, Material originalMaterial, Material newMaterial) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.originalMaterial = originalMaterial;
            this.newMaterial = newMaterial;
        }
        
        public int getX() {
            return x;
        }
        
        public int getY() {
            return y;
        }
        
        public int getZ() {
            return z;
        }
        
        public Material getOriginalMaterial() {
            return originalMaterial;
        }
        
        public Material getNewMaterial() {
            return newMaterial;
        }
    }
} 