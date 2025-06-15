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

public class Wood extends Spell implements Listener {
    public static final String NAME = "wood";
    public static final Material MATERIAL = Material.OAK_LOG;
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
        Material.CHERRY_LOG
    };

    public static void init() {
        config = loadSpellConfig(NAME);
        Bukkit.getPluginManager().registerEvents(new Wood(), DynamicSpells.getInstance());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        if (!openGuis.containsKey(player.getUniqueId())) return;
        
        event.setCancelled(true);
        
        if (event.getCurrentItem() == null) return;
        
        // Give the selected wood to the player
        player.getInventory().addItem(event.getCurrentItem());
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
        // Create a new inventory
        Inventory gui = Bukkit.createInventory(null, 9, "Select Wood Type");
        
        // Get 3 random wood types
        List<Material> availableWoods = new ArrayList<>(Arrays.asList(WOOD_TYPES));
        Collections.shuffle(availableWoods);
        
        // Add the wood types to the GUI
        for (int i = 0; i < 3; i++) {
            Material woodType = availableWoods.get(i);
            ItemStack wood = new ItemStack(woodType);
            ItemMeta meta = wood.getItemMeta();
            meta.setDisplayName("§f" + woodType.name().replace("_", " ").toLowerCase());
            wood.setItemMeta(meta);
            
            gui.setItem(i + 3, wood);
        }
        
        // Open the GUI
        caster.openInventory(gui);
        openGuis.put(caster.getUniqueId(), gui);
        
        return true;
    }
} 