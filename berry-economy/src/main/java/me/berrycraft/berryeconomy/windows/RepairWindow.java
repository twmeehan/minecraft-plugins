package me.berrycraft.berryeconomy.windows;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTItem;
import me.berrycraft.berryeconomy.BerryUtility;
import me.berrycraft.berryeconomy.items.Rainbowberry;
import me.berrycraft.dynamicspells.DynamicSpells;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class RepairWindow implements Listener {
    private static final int SIZE = 45; // 5x9
    private static final int REPAIR_SLOT = 20; // Center left
    private static final int ANVIL_SLOT = 24; // Center right
    private static final int[] GLASS_SLOTS = {10, 11, 12, 21, 30, 29, 28, 19}; // Circle around repair slot
    private static final Material[] GLASS_COLORS = {
            Material.RED_STAINED_GLASS_PANE,
            Material.ORANGE_STAINED_GLASS_PANE,
            Material.YELLOW_STAINED_GLASS_PANE,
            Material.LIME_STAINED_GLASS_PANE,
            Material.LIGHT_BLUE_STAINED_GLASS_PANE,
            Material.BLUE_STAINED_GLASS_PANE,
            Material.PURPLE_STAINED_GLASS_PANE,
            Material.PINK_STAINED_GLASS_PANE
    };
    private static final String GUI_TITLE = ChatColor.DARK_AQUA + "Spellbook Repair";

    // Track open windows and items
    private static final Map<UUID, ItemStack> repairSlotMap = new HashMap<>();
    private static final Map<UUID, BukkitRunnable> animationTasks = new HashMap<>();
    private static final Set<UUID> openWindows = new HashSet<>();

    // Rarity cost table
    private static final Map<String, Integer> rarityCost = new HashMap<>();
    static {
        rarityCost.put("COMMON", 2);
        rarityCost.put("UNCOMMON", 4);
        rarityCost.put("RARE", 6);
        rarityCost.put("LEGENDARY", 8);
        rarityCost.put("MYTHIC", 10);
    }

    public static void init(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(new RepairWindow(), plugin);
    }

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(player, SIZE, GUI_TITLE);
        // Fill with gray panes
        ItemStack gray = glassPane(Material.GRAY_STAINED_GLASS_PANE, "");
        for (int i = 0; i < SIZE; i++) {
            inv.setItem(i, gray);
        }
        inv.setItem(REPAIR_SLOT, null);
        // Place colored glass
        for (int i = 0; i < GLASS_SLOTS.length; i++) {
            inv.setItem(GLASS_SLOTS[i], glassPane(GLASS_COLORS[i], ""));
        }
        // Place anvil
        inv.setItem(ANVIL_SLOT, anvilItem());
        player.openInventory(inv);
        openWindows.add(player.getUniqueId());
        startAnimation(player);
    }

    private static void startAnimation(Player player) {
        UUID uuid = player.getUniqueId();
        BukkitRunnable task = new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                if (!openWindows.contains(uuid)) {
                    this.cancel();
                    return;
                }
                Inventory inv = player.getOpenInventory().getTopInventory();
                ItemStack item = repairSlotMap.get(uuid);
                if (item == null) {
                    // Show static glass
                    for (int i = 0; i < GLASS_SLOTS.length; i++) {
                        inv.setItem(GLASS_SLOTS[i], glassPane(GLASS_COLORS[i], ""));
                    }
                } else {
                    // Rotate glass
                    for (int i = 0; i < GLASS_SLOTS.length; i++) {
                        int colorIdx = (i + tick) % GLASS_COLORS.length;
                        inv.setItem(GLASS_SLOTS[i], glassPane(GLASS_COLORS[colorIdx], ""));
                    }
                    tick = (tick + 1) % GLASS_COLORS.length;
                }
            }
        };
        task.runTaskTimer(Bukkit.getPluginManager().getPlugins()[0], 0, 6); // 3x/sec
        animationTasks.put(uuid, task);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (!openWindows.contains(player.getUniqueId())) return;
        Inventory inv = e.getInventory();
        e.setCancelled(true);
        if (!GUI_TITLE.equals(e.getView().getTitle())) return;
        int slot = e.getRawSlot();
        ItemStack clicked = e.getCurrentItem();
        UUID uuid = player.getUniqueId();
        // Click in repair slot
        if (slot == REPAIR_SLOT) {
            if (repairSlotMap.containsKey(uuid)) {
                // Return item to player
                ItemStack toReturn = repairSlotMap.remove(uuid);
                BerryUtility.give(player,toReturn);
                inv.setItem(REPAIR_SLOT, null);
                //player.sendMessage(ChatColor.YELLOW + "Returned spellbook to your inventory.");
            }
            e.setCancelled(true);
            return;
        }
        // Click anvil
        if (slot == ANVIL_SLOT) {
            ItemStack toRepair = repairSlotMap.get(uuid);
            if (toRepair == null) {
                player.sendMessage(ChatColor.RED + "Place a spellbook in the repair slot.");
                e.setCancelled(true);
                return;
            }
            NBTItem nbti = new NBTItem(toRepair);
            if (!"spell_book".equals(nbti.getString("CustomItem"))) {
                player.sendMessage(ChatColor.RED + "Only spellbooks can be repaired.");
                e.setCancelled(true);
                return;
            }
            String rarity = getRarityFromLore(toRepair);
            int cost = rarityCost.getOrDefault(rarity, 2);
            int playerBerries = Rainbowberry.getAmount(player);
            if (playerBerries < cost) {
                player.sendMessage(ChatColor.RED + "You need " + cost + " rainbowberries to repair this spellbook (" + rarity + ").");
                e.setCancelled(true);
                return;
            }
            // Charge berries, repair, return
            BerryUtility.removeBerries(player, cost);
            DynamicSpells.resetSpellBookLives(toRepair);
            BerryUtility.give(player,toRepair);
            repairSlotMap.remove(uuid);
            inv.setItem(REPAIR_SLOT, null);
            player.sendMessage(ChatColor.GREEN + "Spellbook repaired! Uses restored to full.");
            e.setCancelled(true);
            return;
        }
        // Click in inventory: add spellbook to repair slot
        if (slot >= inv.getSize()) {
            if (repairSlotMap.containsKey(uuid)) {
                e.setCancelled(true);
                return;
            }
            if (clicked == null || clicked.getType() == Material.AIR) return;
            NBTItem nbti = new NBTItem(clicked);
            if (!"spell_book".equals(nbti.getString("CustomItem"))) return;
            // Move to repair slot
            repairSlotMap.put(uuid, clicked.clone());
            inv.setItem(REPAIR_SLOT, clicked.clone());
            player.getInventory().setItem(e.getSlot(), null);
            //player.sendMessage(ChatColor.AQUA + "Placed spellbook in repair slot.");
            e.setCancelled(true);
            return;
        }
        // Prevent taking glass/anvil
        if (Arrays.stream(GLASS_SLOTS).anyMatch(s -> s == slot) || slot == ANVIL_SLOT) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!openWindows.contains(uuid)) return;
        // Return item if present
        ItemStack toReturn = repairSlotMap.remove(uuid);
        if (toReturn != null) {
            BerryUtility.give(player,toReturn);
        }
        openWindows.remove(uuid);
        BukkitRunnable task = animationTasks.remove(uuid);
        if (task != null) task.cancel();
    }

    private static ItemStack glassPane(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack anvilItem() {
        ItemStack item = new ItemStack(Material.ANVIL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Repair Spellbook");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Click to repair your spellbook!");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static String getRarityFromLore(ItemStack item) {
        if (!item.hasItemMeta()) return "COMMON";
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) return "COMMON";
        for (String line : meta.getLore()) {
            String upper = ChatColor.stripColor(line).toUpperCase();
            if (rarityCost.containsKey(upper)) return upper;
        }
        return "COMMON";
    }
} 