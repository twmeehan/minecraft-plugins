package me.berrycraft.dynamicspells.Spells;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.ChatColor;

import me.berrycraft.dynamicspells.DynamicSpells;
import me.berrycraft.dynamicspells.Spell;
import de.tr7zw.nbtapi.NBTItem;

import java.util.*;

public class Stone extends Spell implements Listener {
    public static final String NAME = "stone";
    public static final Material MATERIAL = Material.CLAY_BALL;
    public static YamlConfiguration config;
    private static final Map<UUID, Inventory> openGuis = new HashMap<>();

    private static final Material[] STONE_TYPES = {
        Material.STONE,
        Material.GRANITE,
        Material.DIORITE,
        Material.ANDESITE,
        Material.DEEPSLATE,
        Material.TUFF,
        Material.CALCITE,
        Material.DRIPSTONE_BLOCK,
        Material.NETHERRACK,
        Material.BASALT,
        Material.BLACKSTONE,
        Material.END_STONE,
        Material.WHITE_TERRACOTTA
    };

    public static void init() {
        config = loadSpellConfig(NAME);
        Bukkit.getPluginManager().registerEvents(new Stone(), DynamicSpells.getInstance());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        if (!openGuis.containsKey(player.getUniqueId())) return;
        
        // Cancel the event to prevent any inventory interaction
        event.setCancelled(true);
        
        // Only process clicks in the GUI inventory
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;
        
        if (event.getCurrentItem() == null) return;
        
        // Give the selected stone to the player
        ItemStack stone = event.getCurrentItem().clone();
        stone.setAmount(64); // Give a full stack
        player.getInventory().addItem(stone);
        player.playSound(player.getLocation(), Sound.BLOCK_STONE_PLACE, 1.0f, 1.0f);
        
        // Close the GUI
        player.closeInventory();
        openGuis.remove(player.getUniqueId());
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
        // Check if player is looking at a container block
        Block targetBlock = caster.getTargetBlock(null, 5);
        if (targetBlock != null && targetBlock.getState() instanceof Container) {
            caster.sendMessage(ChatColor.RED + "You cannot use this spell while looking at a container!");
            return false;
        }

        // Create a new inventory
        Inventory gui = Bukkit.createInventory(null, 27, "Select Stone Type");
        
        // Get 3 random stone types
        List<Material> availableStones = new ArrayList<>(Arrays.asList(STONE_TYPES));
        Collections.shuffle(availableStones);
        
        // Add the stone types to the GUI, spaced out
        for (int i = 0; i < 3; i++) {
            Material stoneType = availableStones.get(i);
            ItemStack stone = new ItemStack(stoneType);
            ItemMeta meta = stone.getItemMeta();
            stone.setItemMeta(meta);
            
            // Space items out: slots 11, 13, and 15
            gui.setItem(11 + (i * 2), stone);
        }
        
        // Open the GUI
        caster.openInventory(gui);
        openGuis.put(caster.getUniqueId(), gui);
        
        return true;
    }
} 