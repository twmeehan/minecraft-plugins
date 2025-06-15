package me.berrycraft.dynamicspells.Spells;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Bukkit;

import me.berrycraft.dynamicspells.DynamicSpells;
import me.berrycraft.dynamicspells.Spell;
import de.tr7zw.nbtapi.NBTItem;

import java.util.*;

public class Hollow extends Spell implements Listener {
    public static final String NAME = "hollow";
    public static final Material MATERIAL = Material.SHEARS;
    public static YamlConfiguration config;

    private static final Map<Player, Location[]> selectedPositions = new HashMap<>();
    private static final Map<Player, Integer> positionIndex = new HashMap<>();

    public static void init() {
        config = loadSpellConfig(NAME);
        Bukkit.getPluginManager().registerEvents(new Hollow(), DynamicSpells.getInstance());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        
        // Check main hand
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (isSpellBook(mainHand)) {
            event.setCancelled(true);
            return;
        }
        
        // Check offhand
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (isSpellBook(offHand)) {
            event.setCancelled(true);
            return;
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

    private static boolean isUnbreakable(Block block) {
        return block.getType().getHardness() < 0 || 
               block.getType() == Material.BARRIER ||
               block.getType() == Material.BEDROCK ||
               block.getType() == Material.END_PORTAL_FRAME ||
               block.getType() == Material.END_PORTAL ||
               block.getType() == Material.NETHER_PORTAL;
    }

    private static boolean hasContainer(Block block) {
        BlockState state = block.getState();
        return state instanceof Container;
    }

    public static boolean cast(Player caster, int level) {
        // Initialize position tracking for this player if not exists
        if (!selectedPositions.containsKey(caster)) {
            selectedPositions.put(caster, new Location[2]);
            positionIndex.put(caster, 0);
        }

        // Handle shift + right click (set positions)
        if (caster.isSneaking()) {
            // Toggle between position 1 and 2
            int currentIndex = positionIndex.get(caster);
            Location[] positions = selectedPositions.get(caster);
            
            // Get the block the player is looking at
            Block targetBlock = caster.getTargetBlock(null, 5);
            if (targetBlock != null && !targetBlock.getType().isAir()) {
                positions[currentIndex] = targetBlock.getLocation();
                positionIndex.put(caster, (currentIndex + 1) % 2);
                
                // Play sound and particle effects
                caster.playSound(caster.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                caster.spawnParticle(Particle.END_ROD, targetBlock.getLocation().add(0.5, 0.5, 0.5), 10, 0.2, 0.2, 0.2, 0);
                
                caster.sendMessage("Position " + (currentIndex + 1) + " set!");
                
                // If both positions are set, show the region size
                if (positions[0] != null && positions[1] != null) {
                    int minX = Math.min(positions[0].getBlockX(), positions[1].getBlockX());
                    int maxX = Math.max(positions[0].getBlockX(), positions[1].getBlockX());
                    int minY = Math.min(positions[0].getBlockY(), positions[1].getBlockY());
                    int maxY = Math.max(positions[0].getBlockY(), positions[1].getBlockY());
                    int minZ = Math.min(positions[0].getBlockZ(), positions[1].getBlockZ());
                    int maxZ = Math.max(positions[0].getBlockZ(), positions[1].getBlockZ());
                    
                    int volume = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
                    caster.sendMessage("Selected region size: " + volume + " blocks");
                }
            }
            return false; // Don't consume spell usage
        }
        
        // Handle right click (hollow)
        Location[] positions = selectedPositions.get(caster);
        if (positions[0] == null || positions[1] == null) {
            caster.sendMessage("Please select both positions first!");
            return false;
        }

        // Calculate volume and check if it's within limits
        int minX = Math.min(positions[0].getBlockX(), positions[1].getBlockX());
        int maxX = Math.max(positions[0].getBlockX(), positions[1].getBlockX());
        int minY = Math.min(positions[0].getBlockY(), positions[1].getBlockY());
        int maxY = Math.max(positions[0].getBlockY(), positions[1].getBlockY());
        int minZ = Math.min(positions[0].getBlockZ(), positions[1].getBlockZ());
        int maxZ = Math.max(positions[0].getBlockZ(), positions[1].getBlockZ());

        int volume = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
        int maxBlocks = config.getInt(level + ".max_blocks", 32);

        if (volume > maxBlocks) {
            caster.sendMessage("Selected area is too large! Maximum size: " + maxBlocks + " blocks");
            return false;
        }

        // Hollow out the area
        int blocksRemoved = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Location loc = new Location(positions[0].getWorld(), x, y, z);
                    Block block = loc.getBlock();
                    
                    if (!block.getType().isAir() && !isUnbreakable(block) && !hasContainer(block)) {
                        block.setType(Material.AIR);
                        blocksRemoved++;
                        
                        // Play block break effect
                        caster.spawnParticle(Particle.BLOCK, loc.add(0.5, 0.5, 0.5), 5, 0.2, 0.2, 0.2, 0, block.getType().createBlockData());
                    }
                }
            }
        }

        // Play effects
        caster.playSound(caster.getLocation(), Sound.BLOCK_STONE_BREAK, 1.0f, 1.0f);
        caster.spawnParticle(Particle.EXPLOSION, positions[0], 20, 0.5, 0.5, 0.5, 0);

        // Clear positions after successful hollow
        selectedPositions.remove(caster);
        positionIndex.remove(caster);

        return true;
    }
} 