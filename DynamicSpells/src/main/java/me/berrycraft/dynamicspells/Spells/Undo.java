package me.berrycraft.dynamicspells.Spells;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.berrycraft.dynamicspells.DynamicSpells;
import me.berrycraft.dynamicspells.Spell;
import de.tr7zw.nbtapi.NBTItem;

import java.util.*;

public class Undo extends Spell implements Listener {
    public static final String NAME = "undo";
    public static final Material MATERIAL = Material.ARROW;
    public static YamlConfiguration config;
    
    private static final Map<UUID, SpellAction> lastSpellActions = new HashMap<>();
    
    public static void init() {
        config = loadSpellConfig(NAME);
        Bukkit.getPluginManager().registerEvents(new Undo(), DynamicSpells.getInstance());
        
        // Start task to clear old actions
        long expiryTime = config.getLong("undo_expiry_time", 300) * 1000; // Convert to milliseconds
        new BukkitRunnable() {
            @Override
            public void run() {
                clearOldActions(expiryTime);
            }
        }.runTaskTimer(DynamicSpells.getInstance(), 20L * 60, 20L * 60); // Run every minute
    }
    
    public static void trackSpellCast(Player player, String spellName) {
        SpellAction action = new SpellAction(player.getUniqueId(), player.getName(), spellName);
        lastSpellActions.put(player.getUniqueId(), action);
    }
    
    public static void trackBlockPlace(Player player, int x, int y, int z, Material originalMaterial, Material newMaterial) {
        SpellAction action = lastSpellActions.get(player.getUniqueId());
        if (action != null) {
            action.addBlockChange(x, y, z, originalMaterial, newMaterial);
        }
    }
    
    public static void trackItemUse(Player player, ItemStack item) {
        SpellAction action = lastSpellActions.get(player.getUniqueId());
        if (action != null) {
            action.addItemUsed(item);
        }
    }
    
    private boolean isSpellBook(ItemStack item) {
        if (item == null || item.getType() != MATERIAL) {
            return false;
        }
        
        try {
            NBTItem nbti = new NBTItem(item);
            return "spell_book".equals(nbti.getString("CustomItem")) && 
                   NAME.equals(nbti.getString("Spell"));
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean cast(Player caster, int level) {
        SpellAction action = lastSpellActions.get(caster.getUniqueId());
        if (action == null) {
            caster.sendMessage("§cYou have no spell actions to undo!");
            return false;
        }
        
        // Check if the action has expired
        long expiryTime = config.getLong("undo_expiry_time", 300) * 1000; // Convert to milliseconds
        if (System.currentTimeMillis() - action.getTimestamp() > expiryTime) {
            caster.sendMessage("§cYour last action has expired and cannot be undone.");
            lastSpellActions.remove(caster.getUniqueId());
            return false;
        }
        
        // Check if undo is disabled in this world
        List<String> disabledWorlds = config.getStringList("disabled_worlds");
        if (disabledWorlds.contains(caster.getWorld().getName())) {
            caster.sendMessage("§cUndo is disabled in this world!");
            return false;
        }
        
        // Start undo process
        new BukkitRunnable() {
            int blocksUndone = 0;
            final int totalBlocks = action.getBlockChanges().size();
            
            @Override
            public void run() {
                if (blocksUndone >= totalBlocks) {
                    
                    caster.sendMessage("§eUndoing blocks: " + totalBlocks + "/" + totalBlocks);
                    caster.sendMessage("§aSuccessfully undone your last " + action.getSpellName() + " spell!");
                    caster.playSound(caster.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                    lastSpellActions.remove(caster.getUniqueId());
                    this.cancel();
                    return;
                }
                
                // Undo next block
                SpellAction.BlockChange change = action.getBlockChanges().get(blocksUndone);
                World world = caster.getWorld();
                Block block = world.getBlockAt(change.getX(), change.getY(), change.getZ());
                
                // Only restore if the current block matches what we changed it to
                if (block.getType() == change.getNewMaterial()) {
                    if (change.getOriginalMaterial() == Material.AIR) {
                        caster.getInventory().addItem(new ItemStack(change.getNewMaterial()));
                    }
                    block.setType(change.getOriginalMaterial());
                    
                    // Play effects
                    if (config.getBoolean("show_particles", true)) {
                        caster.spawnParticle(
                            org.bukkit.Particle.valueOf(config.getString("undo_particle", "SMOKE")),
                            block.getLocation().add(0.5, 0.5, 0.5),
                            config.getInt("undo_particle_count", 5),
                            0.2, 0.2, 0.2,
                            0
                        );
                    }
                    
                    if (config.getBoolean("play_sounds", true)) {
                        caster.playSound(
                            block.getLocation(),
                            Sound.valueOf(config.getString("undo_sound", "BLOCK_NOTE_BLOCK_PLING")),
                            (float) config.getDouble("undo_sound_volume", 1.0),
                            (float) config.getDouble("undo_sound_pitch", 2.0)
                        );
                    }
                }
                
                blocksUndone++;
                if (blocksUndone % 10 == 0) {
                    caster.sendMessage("§eUndoing blocks: " + blocksUndone + "/" + totalBlocks);
                }
            }
        }.runTaskTimer(DynamicSpells.getInstance(), 0L, 2L);
        
        return true;
    }
    
    private static void clearOldActions(long expiryTime) {
        long currentTime = System.currentTimeMillis();
        lastSpellActions.entrySet().removeIf(entry -> 
            currentTime - entry.getValue().getTimestamp() > expiryTime
        );
    }
    
    public static class SpellAction {
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
        
        public static class BlockChange {
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
} 