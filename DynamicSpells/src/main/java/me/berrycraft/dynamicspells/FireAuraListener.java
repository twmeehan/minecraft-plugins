package me.berrycraft.dynamicspells;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class FireAuraListener implements Listener {

  @EventHandler
  public void onEntityDamage(EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player)) {
      return;
    }

    Player player = (Player) event.getEntity();

    // Check if the player is an active FireAura caster
    if (SpellEngine.activeFireAuraCasters.containsKey(player.getUniqueId())) {
      // Check if the damage cause is fire-related
      if (event.getCause() == DamageCause.FIRE ||
          event.getCause() == DamageCause.FIRE_TICK ||
          event.getCause() == DamageCause.LAVA) {

        event.setCancelled(true);
      }
    }
  }
}