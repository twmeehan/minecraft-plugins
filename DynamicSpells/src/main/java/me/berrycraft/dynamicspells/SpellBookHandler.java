package me.berrycraft.dynamicspells;


import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTItem;
import net.md_5.bungee.api.ChatColor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpellBookHandler implements Listener {

    private final JavaPlugin plugin;

    public static HashMap<Player,HashMap<String,Long>> cooldowns = new HashMap<Player,HashMap<String,Long>>();

    public SpellBookHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!cooldowns.containsKey(e.getPlayer())) {
            cooldowns.put(e.getPlayer(),new HashMap<String,Long>());
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null) return;

        NBTItem nbti = new NBTItem(item);
        if (!"spell_book".equals(nbti.getString("CustomItem"))) return;

        String spellName = nbti.getString("Spell");
        int level = nbti.getInteger("Level");
        int remaining = nbti.getInteger("RemainingUses");
        int maxUses = nbti.getInteger("MaxUses");
        YamlConfiguration config = Spell.loadSpellConfig(spellName);
        int cooldown = config.getInt(level + ".cooldown", 60); // default to 60 seconds

        if (remaining <= 0) {
            player.sendMessage(ChatColor.RED + "No uses left.");
            return;
        }

        if (cooldowns.get(player).containsKey(spellName) && (System.currentTimeMillis() - cooldowns.get(player).get(spellName))/1000.0 < cooldown) {
            player.sendMessage(ChatColor.RED + "Please wait " + (cooldown - (System.currentTimeMillis() - cooldowns.get(player).get(spellName))/1000) + "s before casting again.");
            return;

        }


        // Cast the spell
        try {
            Class<? extends Spell> spell = DynamicSpells.getInstance().stringToClass.get(spellName.toLowerCase());
            if (spell == null) {
                player.sendMessage("Invalid spell.");
                return;
            }

            Method castMethod = spell.getMethod("cast", Player.class, int.class);
            boolean success = (boolean) castMethod.invoke(null, player, level);

            if (success) {

                ItemMeta meta = item.getItemMeta();
                List<String> updatedLore = meta.getLore();
                updatedLore.set(0,ChatColor.DARK_GRAY + "Uses: " + ChatColor.GREEN + (remaining - 1) + ChatColor.GRAY + "/" + ChatColor.GRAY + maxUses);
                meta.setLore(updatedLore);
                item.setItemMeta(meta);

                // Reduce RemainingUses
                NBT.modify(item, nbt -> {
                    nbt.setInteger("RemainingUses", remaining - 1);
                });

                player.setCooldown(item.getType(), cooldown*20);
                cooldowns.get(player).put(spellName,System.currentTimeMillis());

            }

        } catch (Exception e) {
            player.sendMessage("An error occurred casting the spell.");
            e.printStackTrace();
        }
    }


    public static ItemStack getSpellBook(Class<? extends Spell> spell, int level) {
        String spellId = getStaticNameViaReflection(spell);
        YamlConfiguration config;
        try {
            Field field = spell.getField("config");
            config = (YamlConfiguration) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }

        int minUses = config.getInt(level + ".min_uses", 0);
        int maxUses = config.getInt(level + ".max_uses", 0);
        int uses = new Random().nextInt(maxUses-minUses+1)+minUses;

        int cooldownSeconds = config.getInt(level + ".cooldown", 60);
        String rarity = config.getString(level + ".rarity", "Common");
        String name = config.getString("name");
        String color = config.getString("color");

        List<String> loreLines = config.getStringList("lore");

        Material material;
        try {
            Field field = spell.getField("MATERIAL");
            material = (Material) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
        ItemStack book = new ItemStack(material);
        ItemMeta meta = book.getItemMeta();
        meta.setCustomModelData(1);
        meta.addEnchant(org.bukkit.enchantments.Enchantment.SHARPNESS, 1, true); // Visual glow
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        meta.setDisplayName(color + "§kP§r "+ color + "§o"+name + " " + intToRoman(level) + " §r" + color + "§kP");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.DARK_GRAY + "Uses: " + ChatColor.GREEN + uses + ChatColor.GRAY + "/" + ChatColor.GRAY + uses);
        lore.add(ChatColor.DARK_GRAY + "Cooldown: " + ChatColor.GREEN + formatCooldown(cooldownSeconds));
        lore.add(ChatColor.GRAY + "");
        lore.add(ChatColor.GOLD + "Cast " + ChatColor.YELLOW + "" + ChatColor.BOLD + "RIGHT-CLICK");

        for (String line : loreLines) {
            lore.add(formatLore(line,config,level));
        }

        lore.add(ChatColor.GRAY + "");
        lore.add(formatRarity(rarity));

        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        book.setItemMeta(meta);

        // Add NBT
        NBTItem nbti = new NBTItem(book);
        nbti.setString("CustomItem", "spell_book");
        nbti.setString("Spell", spellId.toLowerCase());
        nbti.setInteger("Level", level);
        nbti.setBoolean("OnCooldown", false);
        nbti.setInteger("RemainingUses", uses);
        nbti.setInteger("MaxUses", uses);


        return nbti.getItem();
    }

    private static String getStaticNameViaReflection(Class<?> spell) {
    try {
        Field field = spell.getField("NAME");
        return (String) field.get(null);
    } catch (Exception e) {
        e.printStackTrace();
        return spell.getSimpleName();
    }
}

    private static String intToRoman(int num) {
        switch (num) {
        case 1:
            return "I";
        case 2:
            return "II";
        case 3:
            return "III";
        case 4:
            return "IV";
        case 5:
            return "V";
        default:
            return Integer.toString(num);
        }
    }

    private static String formatCooldown(int seconds) {
        if (seconds < 60) return seconds + "s";
        return (seconds / 60) + "min";
    }

    private static String formatLore(String text, YamlConfiguration config, int level) {
        // Replace {heart} with red heart
        text = text.replace("{heart}", ChatColor.RED + "❤");

        text = ChatColor.GRAY + text;
        // Match all {placeholders}
        Matcher matcher = Pattern.compile("\\{(.*?)}").matcher(text);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            if (config.contains(level + "." + key)) {
                Object value = config.get(level + "." + key);
                String replacement;

                if (value instanceof Double) {
                    // Format doubles and floats to 1 decimal
                    double num = ((Double) value).doubleValue();
                    replacement = String.format(ChatColor.WHITE+"%.1f"+ChatColor.GRAY, num);
                } else if (value instanceof Integer) {
                    // Format doubles and floats to 1 decimal
                    int num = (Integer) value;
                    replacement = ""+ChatColor.WHITE+num+ChatColor.GRAY;
                } else {
                    // Fallback to string representation
                    replacement = value.toString();
                }

                // Escape replacement to avoid issues with $ in Matcher.appendReplacement
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String formatRarity(String rarity) {

        switch (rarity) {
            case "COMMON":
                return (ChatColor.DARK_GRAY + "" + ChatColor.BOLD + rarity);
            case "UNCOMMON":
                return (ChatColor.DARK_AQUA + "" + ChatColor.BOLD + rarity);
            case "RARE":
                return (ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + rarity);
            case "LEGENDARY":
                return (ChatColor.GOLD + "" + ChatColor.BOLD + rarity);
            case "MYTHIC":
                return (ChatColor.DARK_RED + "" + ChatColor.BOLD + rarity);
            default:
                return ("ERROR");
        }
    }
}
