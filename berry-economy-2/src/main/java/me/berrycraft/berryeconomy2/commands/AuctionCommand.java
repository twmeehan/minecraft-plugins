package me.berrycraft.berryeconomy2.commands;

import me.berrycraft.berryeconomy.auction.AuctionEventHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AuctionCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        AuctionEventHandler.openAuctionWindow((Player) commandSender);
        return true;
    }
}
