package me.berrycraft.dynamicspells.Spells;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
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

public class Texture extends Spell implements Listener {
    public static final String NAME = "texture";
    public static final Material MATERIAL = Material.NETHER_BRICK;
    public static YamlConfiguration config;

    private static final Map<Player, Location[]> selectedPositions = new HashMap<>();
    private static final Map<Player, Integer> positionIndex = new HashMap<>();

    public static void init() {
        config = loadSpellConfig(NAME);
        Bukkit.getPluginManager().registerEvents(new Texture(), DynamicSpells.getInstance());
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
        
        // Handle right click (fill)
        Location[] positions = selectedPositions.get(caster);
        if (positions[0] == null || positions[1] == null) {
            caster.sendMessage("Please select both positions first!");
            return false;
        }

        // Track the spell cast
        Undo.trackSpellCast(caster, NAME);
        
        // Track the item used (the spell book)
        ItemStack spellBook = caster.getInventory().getItemInMainHand();
        if (spellBook != null && spellBook.getType() == MATERIAL) {
            Undo.trackItemUse(caster, spellBook);
        }

        // Get valid blocks from hotbar and count total available blocks
        List<Material> validBlocks = new ArrayList<>();
        Map<Material, Integer> availableBlocks = new HashMap<>();
        Map<Material, Integer> hotbarFrequency = new HashMap<>();
        
        // First pass: collect valid blocks from hotbar and count frequency
        for (int i = 0; i < 9; i++) {
            ItemStack item = caster.getInventory().getItem(i);
            if (item != null && item.getType().isBlock()) {
                validBlocks.add(item.getType());
                hotbarFrequency.put(item.getType(), hotbarFrequency.getOrDefault(item.getType(), 0) + 1);
            }
        }

        if (validBlocks.isEmpty()) {
            caster.sendMessage("Please have at least one block in your hotbar!");
            return false;
        }

        // Count total blocks of valid types in inventory
        for (ItemStack item : caster.getInventory().getContents()) {
            if (item != null && item.getType().isBlock() && validBlocks.contains(item.getType())) {
                availableBlocks.put(item.getType(), availableBlocks.getOrDefault(item.getType(), 0) + item.getAmount());
            }
        }

        // Calculate volume and check if player has enough blocks
        int minX = Math.min(positions[0].getBlockX(), positions[1].getBlockX());
        int maxX = Math.max(positions[0].getBlockX(), positions[1].getBlockX());
        int minY = Math.min(positions[0].getBlockY(), positions[1].getBlockY());
        int maxY = Math.max(positions[0].getBlockY(), positions[1].getBlockY());
        int minZ = Math.min(positions[0].getBlockZ(), positions[1].getBlockZ());
        int maxZ = Math.max(positions[0].getBlockZ(), positions[1].getBlockZ());

        int volume = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
        int maxBlocks = config.getInt("1.max_blocks", 1024);

        if (volume > maxBlocks) {
            caster.sendMessage("Selected area is too large! Maximum size: " + maxBlocks + " blocks");
            return false;
        }

        // Check if we have enough blocks
        int totalAvailable = 0;
        for (int count : availableBlocks.values()) {
            totalAvailable += count;
        }

        if (totalAvailable < volume) {
            caster.sendMessage("§cNot enough blocks! Need: " + volume + ", Have: " + totalAvailable);
            return false;
        }

        // Fill the area with random blocks from hotbar
        Random random = new Random();
        int blocksPlaced = 0;
        Map<Material, Integer> blocksToRemove = new HashMap<>();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Location loc = new Location(positions[0].getWorld(), x, y, z);
                    if (loc.getBlock().getType().isAir()) {
                        // Get a random block type that we still have available, weighted by hotbar frequency
                        List<Material> availableTypes = new ArrayList<>();
                        for (Map.Entry<Material, Integer> entry : availableBlocks.entrySet()) {
                            if (entry.getValue() > 0) {
                                // Add the block type multiple times based on its hotbar frequency
                                int frequency = hotbarFrequency.getOrDefault(entry.getKey(), 1);
                                for (int i = 0; i < frequency; i++) {
                                    availableTypes.add(entry.getKey());
                                }
                            }
                        }
                        
                        if (availableTypes.isEmpty()) {
                            caster.sendMessage("§cRan out of blocks while placing!");
                            return false;
                        }
                        
                        Material randomBlock = availableTypes.get(random.nextInt(availableTypes.size()));
                        
                        // Track the block change
                        Undo.trackBlockPlace(caster, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), Material.AIR, randomBlock);
                        
                        loc.getBlock().setType(randomBlock);
                        availableBlocks.put(randomBlock, availableBlocks.get(randomBlock) - 1);
                        blocksToRemove.put(randomBlock, blocksToRemove.getOrDefault(randomBlock, 0) + 1);
                        blocksPlaced++;
                    }
                }
            }
        }

        // Remove used blocks from inventory
        for (Map.Entry<Material, Integer> entry : blocksToRemove.entrySet()) {
            Material material = entry.getKey();
            int toRemove = entry.getValue();
            
            // Remove blocks from inventory
            for (int i = 0; i < caster.getInventory().getSize() && toRemove > 0; i++) {
                ItemStack item = caster.getInventory().getItem(i);
                if (item != null && item.getType() == material) {
                    if (item.getAmount() <= toRemove) {
                        toRemove -= item.getAmount();
                        caster.getInventory().setItem(i, null);
                    } else {
                        item.setAmount(item.getAmount() - toRemove);
                        toRemove = 0;
                    }
                }
            }
        }

        // Play effects
        caster.playSound(caster.getLocation(), Sound.BLOCK_STONE_PLACE, 1.0f, 1.0f);
        caster.spawnParticle(Particle.BLOCK, positions[0], 50, 0.5, 0.5, 0.5, 0, validBlocks.get(0).createBlockData());

        // Clear positions after successful fill
        selectedPositions.remove(caster);
        positionIndex.remove(caster);

        return true;
    }
} 