package me.berrycraft.berryeconomy2.auction.windows.elements;

import me.berrycraft.berryeconomy.auction.windows.Window;
import org.bukkit.inventory.ItemStack;

// An elements is an item in a Window that does
// something when clicked
public abstract class Element {

    public ItemStack icon;
    protected Window window;
    protected int slot;

    // runs when this element is clicked
    public abstract void click();

    // updates the window with this element added
    public void setIcon() {
        window.getInventory().setItem(slot,icon);
        // setItem() seems to create a copy of icon so icon is set to the copy
        icon = window.getInventory().getItem(slot);
    }

}
