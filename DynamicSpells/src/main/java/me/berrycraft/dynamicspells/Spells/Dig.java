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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;

import me.berrycraft.dynamicspells.DynamicSpells;
import me.berrycraft.dynamicspells.Spell;
import de.tr7zw.nbtapi.NBTItem;

import java.util.*;

public class Dig extends Spell implements Listener {
    public static final String NAME = "dig";
    public static final Material MATERIAL = Material.DIAMOND_SHOVEL;
    public static YamlConfiguration config;

    public static void init() {
        config = loadSpellConfig(NAME);
        Bukkit.getPluginManager().registerEvents(new Dig(), DynamicSpells.getInstance());
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
               block.getType() == Material.NETHER_PORTAL ||
               block.getType() == Material.OBSIDIAN;
    }

    private static boolean hasContainer(Block block) {
        BlockState state = block.getState();
        return state instanceof Container;
    }

    public static boolean cast(Player caster, int level) {
        // Get the block the player is looking at
        Block targetBlock = caster.getTargetBlock(null, 5);
        if (targetBlock == null || targetBlock.getType().isAir()) {
            return false;
        }

        Location center = targetBlock.getLocation();
        int blocksRemoved = 0;

        // Dig out a 3x3x3 area
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Location loc = center.clone().add(x, y, z);
                    Block block = loc.getBlock();
                    
                    if (!block.getType().isAir() && !isUnbreakable(block) && !hasContainer(block)) {
                        // Drop the block's items without silk touch
                        Material blockType = block.getType();
                        block.breakNaturally();
                        blocksRemoved++;
                        
                        // Play block break effect
                        caster.spawnParticle(Particle.BLOCK, loc.add(0.5, 0.5, 0.5), 5, 0.2, 0.2, 0.2, 0, blockType.createBlockData());
                    }
                }
            }
        }

        if (blocksRemoved > 0) {
            // Play effects
            caster.playSound(caster.getLocation(), Sound.BLOCK_STONE_BREAK, 1.0f, 1.0f);
            caster.spawnParticle(Particle.EXPLOSION, center, 20, 0.5, 0.5, 0.5, 0);
            return true;
        }

        return false;
    }
} 