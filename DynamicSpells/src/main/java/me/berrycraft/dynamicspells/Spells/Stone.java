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

public class Stone extends Spell implements Listener {
    public static final String NAME = "stone";
    public static final Material MATERIAL = Material.STONE;
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
        
        event.setCancelled(true);
        
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