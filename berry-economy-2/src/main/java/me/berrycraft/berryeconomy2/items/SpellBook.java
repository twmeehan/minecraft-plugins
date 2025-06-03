package me.berrycraft.berryeconomy2.items;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class SpellBook extends CustomItem {

    public SpellBook() {
        super(Material.ENCHANTED_BOOK);
        ItemMeta meta = this.getItemMeta();

        meta.setDisplayName(ChatColor.GRAY+""+ChatColor.MAGIC + "S" +ChatColor.GRAY +  " Heal I "+ChatColor.GRAY+""+ChatColor.MAGIC  + "S");
        ArrayList<String> lore = new ArrayList<>();

        //lore.add(ChatColor.GRAY + "");

        //lore.add(ChatColor.DARK_GRAY+"Rating: "+ChatColor.RED+"✦✦✦✦✦"+ChatColor.GRAY+"" );//✧
        lore.add(ChatColor.DARK_GRAY+"Uses: "+ChatColor.RED + "2" + ChatColor.GRAY+"/"+ChatColor.GRAY + "34" );
        lore.add(ChatColor.DARK_GRAY+"Cooldown: "+ChatColor.GREEN+"1min" );
        lore.add(ChatColor.GRAY + "");
        //lore.add(ChatColor.GOLD + "Right-Click");
        lore.add(ChatColor.GOLD + "Cast " + ChatColor.YELLOW + "" + ChatColor.BOLD + "RIGHT-CLICK");

        lore.add(ChatColor.GRAY + "Heals "+ ChatColor.WHITE + "0.5" +ChatColor.RED + "❤" + ChatColor.GRAY + " instantly ");


        lore.add(ChatColor.GRAY + "");
        lore.add(ChatColor.DARK_GRAY + "Healing spell ranging from level I-V");
        lore.add(ChatColor.DARK_GRAY + "Low levels are quite weak while higher");
        lore.add(ChatColor.DARK_GRAY + "levels can be extremely powerful");

        lore.add(ChatColor.GRAY + "");
        lore.add(ChatColor.DARK_GRAY + ""+ChatColor.BOLD+"Common");
        meta.setLore(lore);
        this.setItemMeta(meta);
        NBTItem nbti = new NBTItem(this);
        nbti.setString("CustomItem","SpellBook");
        nbti.applyNBT(this);
        this.setAmount(1);
    }
}
