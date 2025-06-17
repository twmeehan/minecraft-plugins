package me.berrycraft.dynamicspells.Spells;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
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

public class Wood extends Spell implements Listener {
    public static final String NAME = "wood";
    public static final Material MATERIAL = Material.STICK;
    public static YamlConfiguration config;
    private static final Map<UUID, Inventory> openGuis = new HashMap<>();

    private static final Material[] WOOD_TYPES = {
        Material.OAK_LOG,
        Material.BIRCH_LOG,
        Material.SPRUCE_LOG,
        Material.JUNGLE_LOG,
        Material.ACACIA_LOG,
        Material.DARK_OAK_LOG,
        Material.MANGROVE_LOG,
        Material.CHERRY_LOG,
        Material.CRIMSON_STEM,
        Material.WARPED_STEM,
        Material.PALE_OAK_LOG
    };

    public static void init() {
        config = loadSpellConfig(NAME);
        Bukkit.getPluginManager().registerEvents(new Wood(), DynamicSpells.getInstance());
    }
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player)event.getPlayer();
        if (openGuis.containsKey(player.getUniqueId())) {
            openGuis.remove(player.getUniqueId());
        }
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
        
        // Give the selected wood to the player
        ItemStack wood = event.getCurrentItem().clone();
        wood.setAmount(64); // Give a full stack
        player.getInventory().addItem(wood);
        player.playSound(player.getLocation(), Sound.BLOCK_WOOD_PLACE, 1.0f, 1.0f);
        
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
        Inventory gui = Bukkit.createInventory(null, 27, "Select Wood Type");
        
        // Get 3 random wood types
        List<Material> availableWoods = new ArrayList<>(Arrays.asList(WOOD_TYPES));
        Collections.shuffle(availableWoods);
        
        // Add the wood types to the GUI, spaced out
        for (int i = 0; i < 3; i++) {
            Material woodType = availableWoods.get(i);
            ItemStack wood = new ItemStack(woodType);
            ItemMeta meta = wood.getItemMeta();
            wood.setItemMeta(meta);
            
            // Space items out: slots 11, 13, and 15
            gui.setItem(11 + (i * 2), wood);
        }
        
        // Open the GUI
        caster.openInventory(gui);
        openGuis.put(caster.getUniqueId(), gui);
        
        return true;
    }
} 