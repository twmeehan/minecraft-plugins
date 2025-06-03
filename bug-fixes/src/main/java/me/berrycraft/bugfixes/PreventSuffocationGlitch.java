package me.berrycraft.bugfixes;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

public class PreventSuffocationGlitch implements Listener {


    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        e.getPlayer().sendMessage("Flagged for risk of suffocation");
        e.getPlayer().setGravity(false);
        e.getPlayer().addScoreboardTag("suffocation");


    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.getPlayer().sendMessage("Flagged for risk of suffocation");
        e.getPlayer().setGravity(false);
        e.getPlayer().addScoreboardTag("suffocation");



    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        e.getPlayer().sendMessage("Flagged for risk of suffocation");
        e.getPlayer().setGravity(false);
        e.getPlayer().addScoreboardTag("suffocation");


    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        e.getPlayer().sendMessage("Flagged for risk of suffocation");
        e.getPlayer().setGravity(false);
        e.getPlayer().addScoreboardTag("suffocation");

    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getPlayer().getScoreboardTags().contains("suffocation")) {
            e.getPlayer().removeScoreboardTag("suffocation");
            e.getPlayer().setGravity(true);
        }
    }
}
