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

import me.berrycraft.dynamicspells.DynamicSpells;
import me.berrycraft.dynamicspells.Spell;
import de.tr7zw.nbtapi.NBTItem;

import java.util.*;

public class Leaves extends Spell implements Listener {
    public static final String NAME = "leaves";
    public static final Material MATERIAL = Material.OAK_LEAVES;
    public static YamlConfiguration config;
    private static final Map<UUID, Inventory> openGuis = new HashMap<>();

    private static final Material[] LEAF_TYPES = {
        Material.OAK_LEAVES,
        Material.BIRCH_LEAVES,
        Material.SPRUCE_LEAVES,
        Material.JUNGLE_LEAVES,
        Material.ACACIA_LEAVES,
        Material.DARK_OAK_LEAVES,
        Material.MANGROVE_LEAVES,
        Material.CHERRY_LEAVES,
        Material.AZALEA_LEAVES,
        Material.FLOWERING_AZALEA_LEAVES,

    };

    public static void init() {
        config = loadSpellConfig(NAME);
        Bukkit.getPluginManager().registerEvents(new Leaves(), DynamicSpells.getInstance());
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
        
        // Give the selected leaves to the player
        ItemStack leaves = event.getCurrentItem().clone();
        leaves.setAmount(64); // Give a full stack
        player.getInventory().addItem(leaves);
        player.playSound(player.getLocation(), Sound.BLOCK_GRASS_PLACE, 1.0f, 1.0f);
        
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
        // Create a new inventory
        Inventory gui = Bukkit.createInventory(null, 27, "Select Leaf Type");
        
        // Get 3 random leaf types
        List<Material> availableLeaves = new ArrayList<>(Arrays.asList(LEAF_TYPES));
        Collections.shuffle(availableLeaves);
        
        // Add the leaf types to the GUI, spaced out
        for (int i = 0; i < 3; i++) {
            Material leafType = availableLeaves.get(i);
            ItemStack leaves = new ItemStack(leafType);
            ItemMeta meta = leaves.getItemMeta();
            leaves.setItemMeta(meta);
            
            // Space items out: slots 11, 13, and 15
            gui.setItem(11 + (i * 2), leaves);
        }
        
        // Open the GUI
        caster.openInventory(gui);
        openGuis.put(caster.getUniqueId(), gui);
        
        return true;
    }
} 