package me.berrycraft.berryeconomy2.auction.windows;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/*
 * Window contains a inventory GUI page and all the
 * associated data such as the viewer, size,
 * and functionality. Specific windows will extend
 * this class and customize what the GUI looks like
 * and how it behaves
 */
public abstract class Window {

    protected String name;
    protected int size;
    protected Inventory window;
    protected Player viewer;

    // tells the player to open this window
    public void display() {

        viewer.openInventory(window);

    }

    // adds a border of specified ItemStack around the edge of the inventory
    public void addBorder(ItemStack border) {

        for (int i =0; i < 9; i++) {
            window.setItem(i,border);
        }
        for (int i =0;i <window.getSize();i+=9) {
            window.setItem(i,border);
        }
        for (int i =8;i <window.getSize();i+=9) {
            window.setItem(i,border);
        }
        for (int i =0; i < 9; i++) {
            window.setItem(i+window.getSize()-10,border);
        }

    }

    // fills the window with this item
    public void fill(ItemStack border) {

        for (int i =0; i < size; i++) {
            window.setItem(i,border);
        }


    }

    // clears the items out of the center of the GUI
    // used for browsing market entries
    public void clearCenter() {

        for (int i =1; i < 8; i++) {
            for (int j =i+9;j <window.getSize()-9;j+=9) {
                window.setItem(j,null);
            }
        }

    }

    // when any slot in the inventory is clicked
    public abstract void click(int slot);

    public void add(ItemStack stack) {
        window.addItem(stack);
    }

    public void add(ItemStack[] stacks) {
        window.addItem(stacks);
    }

    public int getSize() {
        return size;
    }

    public Inventory getInventory() {
        return window;
    }

    public String getName() {
        return name;
    }

    public Player getViewer() {
        return viewer;
    }
}
