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
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.bukkit.Bukkit;

import me.berrycraft.dynamicspells.DynamicSpells;
import me.berrycraft.dynamicspells.IExecutableSpell;
import me.berrycraft.dynamicspells.Spell;
import me.berrycraft.dynamicspells.SpellEngine;
import de.tr7zw.nbtapi.NBTItem;

import java.util.HashMap;
import java.util.Map;

public class Place extends Spell implements Listener {
    public static final String NAME = "place";
    public static final Material MATERIAL = Material.BRICK;
    public static YamlConfiguration config;

    private static final Map<Player, Location[]> selectedPositions = new HashMap<>();
    private static final Map<Player, Integer> positionIndex = new HashMap<>();

    public static void init() {
        config = loadSpellConfig(NAME);
        Bukkit.getServer().getPluginManager().registerEvents(new Place(), DynamicSpells.getInstance());

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

        // Get the block from offhand
        ItemStack offhandItem = caster.getInventory().getItemInOffHand();
        if (offhandItem == null || offhandItem.getType().isAir() || !offhandItem.getType().isBlock()) {
            caster.sendMessage("Please hold a valid block in your offhand!");
            return false;
        }

        // Track the spell cast
        Undo.trackSpellCast(caster, NAME);
        
        // Track the item used (the spell book)
        ItemStack spellBook = caster.getInventory().getItemInMainHand();
        if (spellBook != null && spellBook.getType() == MATERIAL) {
            Undo.trackItemUse(caster, spellBook);
        }

        // Calculate volume and check if player has enough blocks
        int minX = Math.min(positions[0].getBlockX(), positions[1].getBlockX());
        int maxX = Math.max(positions[0].getBlockX(), positions[1].getBlockX());
        int minY = Math.min(positions[0].getBlockY(), positions[1].getBlockY());
        int maxY = Math.max(positions[0].getBlockY(), positions[1].getBlockY());
        int minZ = Math.min(positions[0].getBlockZ(), positions[1].getBlockZ());
        int maxZ = Math.max(positions[0].getBlockZ(), positions[1].getBlockZ());

        int volume = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
        int maxBlocks = config.getInt(level + ".max_blocks", 256);

        if (volume > maxBlocks) {
            caster.sendMessage("Selected area is too large! Maximum size: " + maxBlocks + " blocks");
            return false;
        }

        // Check if player has enough blocks
        int totalBlocks = 0;
        for (ItemStack item : caster.getInventory().getContents()) {
            if (item != null && item.getType() == offhandItem.getType()) {
                totalBlocks += item.getAmount();
            }
        }

        if (totalBlocks < volume) {
            caster.sendMessage("§cNot enough blocks!");
            return false;
        }

        // Fill the area
        int blocksPlaced = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Location loc = new Location(positions[0].getWorld(), x, y, z);
                    if (loc.getBlock().getType().isAir()) {
                        // Track the block change
                        Undo.trackBlockPlace(caster, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), Material.AIR, offhandItem.getType());
                        
                        loc.getBlock().setType(offhandItem.getType());
                        blocksPlaced++;
                    }
                }
            }
        }

        // Remove blocks from inventory
        int remainingToRemove = blocksPlaced;
        for (int i = 0; i < caster.getInventory().getSize() && remainingToRemove > 0; i++) {
            ItemStack item = caster.getInventory().getItem(i);
            if (item != null && item.getType() == offhandItem.getType()) {
                if (item.getAmount() <= remainingToRemove) {
                    remainingToRemove -= item.getAmount();
                    caster.getInventory().setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - remainingToRemove);
                    remainingToRemove = 0;
                }
            }
        }

        // Play effects
        caster.playSound(caster.getLocation(), Sound.BLOCK_STONE_PLACE, 1.0f, 1.0f);
        caster.spawnParticle(Particle.BLOCK, positions[0], 50, 0.5, 0.5, 0.5, 0, offhandItem.getType().createBlockData());

        // Clear positions after successful fill
        selectedPositions.remove(caster);
        positionIndex.remove(caster);

        return true;
    }

} 