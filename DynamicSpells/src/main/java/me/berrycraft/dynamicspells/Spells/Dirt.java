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

public class Dirt extends Spell implements Listener {
    public static final String NAME = "dirt";
    public static final Material MATERIAL = Material.BROWN_DYE;
    public static YamlConfiguration config;
    private static final Map<UUID, Inventory> openGuis = new HashMap<>();

    private static final Material[] DIRT_TYPES = {
        Material.DIRT,
        Material.COARSE_DIRT,
        Material.ROOTED_DIRT,
        Material.MUD,
        Material.PODZOL,
        Material.MYCELIUM,
        Material.SOUL_SOIL,
        Material.SOUL_SAND,
        Material.SAND,
        Material.RED_SAND,
        Material.GRAVEL,
        Material.DIRT_PATH,
        Material.MOSS_BLOCK
    };

    public static void init() {
        config = loadSpellConfig(NAME);
        Bukkit.getPluginManager().registerEvents(new Dirt(), DynamicSpells.getInstance());
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
        
        // Give the selected dirt to the player
        ItemStack dirt = event.getCurrentItem().clone();
        dirt.setAmount(64); // Give a full stack
        player.getInventory().addItem(dirt);
        player.playSound(player.getLocation(), Sound.BLOCK_GRAVEL_PLACE, 1.0f, 1.0f);
        
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
        Inventory gui = Bukkit.createInventory(null, 27, "Select Dirt Type");
        
        // Get 3 random dirt types
        List<Material> availableDirts = new ArrayList<>(Arrays.asList(DIRT_TYPES));
        Collections.shuffle(availableDirts);
        
        // Add the dirt types to the GUI, spaced out
        for (int i = 0; i < 3; i++) {
            Material dirtType = availableDirts.get(i);
            ItemStack dirt = new ItemStack(dirtType);
            ItemMeta meta = dirt.getItemMeta();
            dirt.setItemMeta(meta);
            
            // Space items out: slots 11, 13, and 15
            gui.setItem(11 + (i * 2), dirt);
        }
        
        // Open the GUI
        caster.openInventory(gui);
        openGuis.put(caster.getUniqueId(), gui);
        
        return true;
    }
} 