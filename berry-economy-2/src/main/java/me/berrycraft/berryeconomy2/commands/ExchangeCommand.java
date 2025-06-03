package me.berrycraft.berryeconomy2.commands;

import de.tr7zw.nbtapi.NBTItem;
import me.berrycraft.berryeconomy.BerryUtility;
import me.berrycraft.berryeconomy.items.Pinkberry;
import me.berrycraft.berryeconomy.items.Rainbowberry;
import me.berrycraft.berryeconomy.items.Raspberry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

/*
 * Opens a GUI when /exchange is run
 * Allows players to switch between
 * rainbowberries, pinkberries, and
 * raspberries
 */
public class ExchangeCommand implements CommandExecutor, Listener {

    // list of players with the GUI open
    LinkedList<String> playersExchanging = new LinkedList<>();

    // when the player runs /exchange
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        // only works for players
        if (!(commandSender instanceof Player)) return false;
        Player p = (Player)commandSender;

        // add this player to the list of players with the GUI open
        playersExchanging.add(p.getName());

        Inventory GUI = p.getServer().createInventory(p,36,ChatColor.DARK_GRAY+"Berry Exchange");

        // create the items for the icons in the GUI
        ItemStack empty = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = empty.getItemMeta();
        meta.setDisplayName(" ");
        empty.setItemMeta(meta);

        ItemStack raspberry = new ItemStack(Material.COPPER_INGOT);
        meta = raspberry.getItemMeta();
        meta.setCustomModelData(1);
        meta.setDisplayName(ChatColor.GRAY + "Convert to " + ChatColor.YELLOW + "Copper");
        raspberry.setItemMeta(meta);

        ItemStack pinkberry = new ItemStack(Material.IRON_INGOT);
        meta = pinkberry.getItemMeta();
        meta.setCustomModelData(1);
        meta.setDisplayName(ChatColor.GRAY + "Convert to " + ChatColor.YELLOW + "Silver");
        pinkberry.setItemMeta(meta);

        ItemStack rainbowberry = new ItemStack(Material.GOLD_INGOT);
        meta = rainbowberry.getItemMeta();
        meta.setCustomModelData(1);
        meta.setDisplayName(ChatColor.GRAY + "Convert to " +  ChatColor.YELLOW + "Gold");
        rainbowberry.setItemMeta(meta);

        ItemStack cancel = new ItemStack(Material.RED_TERRACOTTA);
        meta = cancel.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD+ "CANCEL");
        cancel.setItemMeta(meta);

        ItemStack confirm = new ItemStack(Material.LIME_TERRACOTTA);
        meta = confirm.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD+ "TAKE ALL");
        confirm.setItemMeta(meta);

        // place items in their correct slots
        GUI.setItem(1,empty);
        GUI.setItem(10,empty);
        GUI.setItem(19,empty);
        GUI.setItem(28,empty);
        GUI.setItem(18,raspberry);
        GUI.setItem(9,pinkberry);
        GUI.setItem(0,rainbowberry);
        GUI.setItem(27,confirm);

        p.openInventory(GUI);
        return true;
    }

    // when the player clicks something in the GUI
    @EventHandler
    public void onItemClick(InventoryClickEvent e) {

        // if the player doesnt have the exchange GUI open do nothing
        if (!playersExchanging.contains(e.getWhoClicked().getName())) return;
        Player p = (Player)e.getWhoClicked();

        // if player clicks their own inventory do nothing
        if (e.getClickedInventory()!=p.getInventory()) {

            // if they have the GUI open and clicked one of the icons
                if (e.getSlot()==1 || e.getSlot()==10||e.getSlot()==19||e.getSlot()==28) {
                    e.setCancelled(true);
                }

            // rainbowberry clicked
            if (e.getSlot()==0) {

                /*
                 * convert all the raspberries to pinkberries first
                 *
                 * ex. if we have 6 pink and 45 raspberries then we convert
                 * to 10 pink and 5 raspberries. Now we know we have 10 pink so
                 * we can convert it into 1 rainbowberry
                 */
                LinkedList<ItemStack> items = getItems(e.getClickedInventory());
                raspberryToPinkberry(items,e.getClickedInventory());

                items = getItems(e.getClickedInventory());
                pinkberryToRainbowberry(items,e.getClickedInventory());

                items = getItems(e.getClickedInventory());
                // the berries are all over the GUI so this method cleans things up
                rearrangeInventory(items,e.getClickedInventory());
                e.setCancelled(true);

            }

            // pinkberry clicked
            if (e.getSlot()==9) {

                LinkedList<ItemStack> items = getItems(e.getClickedInventory());
                raspberryToPinkberry(items,e.getClickedInventory());
                rainbowberryToPinkberry(items,e.getClickedInventory());

                items = getItems(e.getClickedInventory());
                rearrangeInventory(items,e.getClickedInventory());
                e.setCancelled(true);

            }

            // raspberry clicked
            if (e.getSlot()==18) {

                // same system as rainbowberries but in reverse
                LinkedList<ItemStack> items = getItems(e.getClickedInventory());
                rainbowberryToPinkberry(items,e.getClickedInventory());

                items = getItems(e.getClickedInventory());
                pinkberryToRaspberry(items,e.getClickedInventory());

                items = getItems(e.getClickedInventory());
                rearrangeInventory(items,e.getClickedInventory());
                e.setCancelled(true);

            }

            // the take all button is pressed
            if (e.getSlot()==27) {

                LinkedList<ItemStack> items = new LinkedList<ItemStack>();

                // iterate over all the items in the GUI
                for (int i = 0; i < 36;i++) {

                    // ignore the icons on the left
                    if (i % 9 == 0 || i % 9 == 1) {
                        continue;
                    }

                    // add to the list of items that are being taken
                    if (e.getClickedInventory().getItem(i) != null) {
                        items.add(e.getClickedInventory().getItem(i));
                        e.getClickedInventory().setItem(i, null);
                    }

                }

                // idk why this is in a separate loop but optimization rly doesnt matter for this GUI
                for (ItemStack item : items) {
                    BerryUtility.give(p,item);
                }

                e.setCancelled(true);

            }
        }
    }

    // check if the player has closed this GUI
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {

        // if the player is not in the list of players viewing this GUI do nothing
        if (!playersExchanging.contains(e.getPlayer().getName())) return;

        LinkedList<ItemStack> items = new LinkedList<ItemStack>();

        // iterate through all items
        for (int i = 0; i < 36;i++) {

            // ignore the icons
            if (i % 9 == 0 || i % 9 == 1) {
                continue;
            }

            // remove item from the GUI
            if (e.getInventory().getItem(i) != null) {
                items.add(e.getInventory().getItem(i));
                e.getInventory().setItem(i, null);
            }
        }

        // give items to the player
        for (ItemStack item : items) {
            BerryUtility.give((Player) e.getPlayer(),item);
        }

        // remove player from the list of players with this GUI open
        playersExchanging.remove(e.getPlayer().getName());

    }

    /*
     * takes all the items in the GUI and puts them in the first available slots since
     * often after exchanging the berries are randomly scattered around the GUI
     *
     * items - a list of the items in the GUI (use getItems())
     * inventory - the GUI itself
     */
    public void rearrangeInventory(LinkedList<ItemStack> items, Inventory inventory) {

        for (int i = 0; i < 36;i++) {

            // ignores the icons
            if (i % 9 == 0 || i % 9 == 1) {
                continue;
            }

            // deletes all items from the inventory
            if (inventory.getItem(i) != null) {
                inventory.setItem(i,null);
            }
        }

        // puts the items back in
        for (ItemStack item : items) {
            inventory.addItem(item);
        }

    }

    /*
     * converts all the pinkberries to raspberries in items at a 1:10 ratio
     *
     * items - a list of the items in the GUI (use getItems())
     * inventory - the GUI itself
     */
    public void pinkberryToRaspberry(LinkedList<ItemStack> items, Inventory inventory){

        // count the pinkberries
        int totalPink = 0;
        for (ItemStack item : items) {
            totalPink += Pinkberry.getAmount(item);
        }

        // continues looping while there are pinkberries remaining
        while (totalPink >= 1) {

            // delete 1 pinkberry
            inventory.removeItem(new Pinkberry());

            // try adding 10 raspberry
            if (!addStack(new Raspberry(10),inventory)) {

                // if there's no room add the pinkberry back and end the loop
                addStack(new Pinkberry(),inventory);
                break;
            } else {
                // on successfully added 10 raspberry
                totalPink -= 1;
            }

        }

    }

    /*
     * converts all the rainbowberries to pinkberries in items at a 1:10 ratio
     *
     * items - a list of the items in the GUI (use getItems())
     * inventory - the GUI itself
     */
    public void rainbowberryToPinkberry(LinkedList<ItemStack> items, Inventory inventory){

        // see documentation in pinkberryToRaspberry()
        int totalRain = 0;
        for (ItemStack item : items) {
            totalRain += Rainbowberry.getAmount(item);
        }

        while (totalRain >= 1) {
            inventory.removeItem(new Rainbowberry());
            if (!addStack(new Pinkberry(10),inventory)) {
                addStack(new Rainbowberry(),inventory);
                break;
            } else {
                totalRain -= 1;
            }
        }
    }

    /*
     * converts all the pinkberries to rainbowberries in items at a 10:1 ratio
     *
     * items - a list of the items in the GUI (use getItems())
     * inventory - the GUI itself
     */
    public void pinkberryToRainbowberry(LinkedList<ItemStack> items, Inventory inventory){

        // see documentation in pinkberryToRaspberry()

        int totalPink = 0;
        for (ItemStack item : items) {
            totalPink += Pinkberry.getAmount(item);
        }

        while (totalPink >= 10) {
            inventory.removeItem(new Pinkberry(10));
            if (!addStack(new Rainbowberry(),inventory)) {
                addStack(new Pinkberry(10),inventory);
                break;
            } else {
                totalPink -= 10;
            }
        }
    }

    /*
     * converts all the raspberries to pinkberries in items  at a 10:1 ratio
     *
     * items - a list of the items in the GUI (use getItems())
     * inventory - the GUI itself
     */
    public void raspberryToPinkberry(LinkedList<ItemStack> items, Inventory inventory){

        // see documentation in pinkberryToRaspberry()

        int totalRasp = 0;
        for (ItemStack item : items) {
            totalRasp += Raspberry.getAmount(item);
        }

        while (totalRasp >= 10) {
            inventory.removeItem(new Raspberry(10));
            if (!addStack(new Pinkberry(),inventory)) {
                addStack(new Raspberry(10),inventory);
                break;
            } else {
                totalRasp -= 10;
            }
        }
    }

    /*
     * Attempts to add a item to an inventory. Similar to CustomItem.give() but for
     * an inventory instead of a player and returns false if the inventory is full
     *
     * stack - item to be added
     * inventory - the inventory that the item will be added to
     */
    public boolean addStack(ItemStack stack, Inventory inventory) {

        // if there are empty slots then add the item with Bukkit's normal .addItem() method
        if (inventory.firstEmpty()!=-1) {
            inventory.addItem(stack);
            return true;
        }
        // if not:

        // loop through all the items in the inventory
        for (ItemStack i : inventory.getContents()) {

            if (i == null || i.getType()==Material.AIR) continue;

            // if the item we are adding is a player head and we found a stack of player heads in the inventory
            if (i.getType()==Material.PLAYER_HEAD && stack.getType()==Material.PLAYER_HEAD) {

                // check the nbti
                NBTItem nbti = new NBTItem(stack);
                NBTItem nbti2 = new NBTItem(i);

                // if they are the same custom item
                if( nbti.getString("CustomItem").equals(nbti2.getString("CustomItem")) && i.getAmount()<stack.getMaxStackSize()) {

                    // see if you can fit more items in the stack
                    if (stack.getMaxStackSize()-i.getAmount() >= stack.getAmount()) {
                        i.setAmount(i.getAmount()+stack.getAmount());
                        return true;
                    } else {

                        // if not the inventory if full and return false
                        return false;
                    }

                }

            }

            // if the item is a normal item, check if it's the same item with i.isSimilar(stack)
            if (i.isSimilar(stack) && i.getAmount()<stack.getMaxStackSize()) {

                // if the items being added can be fit in this stack
                if (stack.getMaxStackSize()-i.getAmount() >= stack.getAmount()) {
                    i.setAmount(i.getAmount()+stack.getAmount());
                    return true;
                } else {
                    return false;
                }
            }
        }

        // if no item was found in the inventory similar to the one we are adding
        // (and there are no free slots) return false
        return false;

    }

    // gets a list of items in the GUI
    public LinkedList<ItemStack> getItems(Inventory inventory) {

        LinkedList<ItemStack> items = new LinkedList<ItemStack>();
        for (int i = 0; i < 36;i++) {

            // ignore icons on the left
            if (i % 9 == 0 || i % 9 == 1) {
                continue;
            }

            if (inventory.getItem(i) != null) {
                items.add(inventory.getItem(i));
            }
        }
        return items;
    }

    // similar to CustomItem.setSkull() but returns the itemStack
    public ItemStack getSkull(String base64EncodedString) {

        ItemStack stack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) stack.getItemMeta();
        assert meta != null;

        // Always use the name Rainbowberry so that heads can stack
        PlayerProfile profile = Bukkit.getServer().createPlayerProfile("Rainbowberry");

        PlayerTextures texture = profile.getTextures();
        try {
            texture.setSkin(new URL(base64EncodedString), PlayerTextures.SkinModel.CLASSIC);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        profile.setTextures(texture);
        meta.setOwnerProfile(profile);

        stack.setItemMeta(meta);

        return stack;

    }

}
