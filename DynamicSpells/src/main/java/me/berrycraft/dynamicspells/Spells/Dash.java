package me.berrycraft.dynamicspells.Spells;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import me.berrycraft.dynamicspells.DynamicSpells;
import me.berrycraft.dynamicspells.IExecutableSpell;
import me.berrycraft.dynamicspells.Spell;
import me.berrycraft.dynamicspells.SpellEngine;

import java.util.HashMap;
import java.util.Map;

public class Dash extends Spell implements IExecutableSpell, Listener {

    public static final String NAME = "dash";
    public static final Material MATERIAL = Material.FEATHER;
    public static YamlConfiguration config;

    private static final Map<Player, Vector> lastMovementDirections = new HashMap<>();

    private Player caster;
    private int level;
    private double speed;
    private double duration;

    public static void init() {
        config = loadSpellConfig(NAME);
        Bukkit.getPluginManager().registerEvents(new Dash(), DynamicSpells.getInstance());
    }

    public static boolean cast(Player caster, int level) {
        Dash dash = new Dash();
        dash.caster = caster;
        dash.level = level;
        dash.speed = config.getDouble(level + ".speed", 2.0);
        dash.duration = config.getDouble(level + ".duration", 1.0);

        SpellEngine.register(dash, dash.duration);
        return true;
    }

    @Override
    public void start() {
        // Attempt to get the player's last movement direction
        Vector dashDirection = lastMovementDirections.getOrDefault(caster, new Vector(0, 0, 0)).clone();
        dashDirection.setY(0).normalize();

        // If last movement direction is too small (player standing still), fallback to camera facing direction
        if (dashDirection.lengthSquared() < 0.05) {
            dashDirection = caster.getLocation().getDirection().setY(0).normalize();
        }

        // If still too small, use a default forward vector
        if (dashDirection.lengthSquared() < 0.01) {
            dashDirection = new Vector(0, 0, 1);
        }

        // Apply dash velocity
        caster.setVelocity(dashDirection.multiply(speed));

        // Mark the player invincible
        caster.setMetadata("DASH_INVINCIBLE", new FixedMetadataValue(DynamicSpells.getInstance(), true));

        // Play sound and particle
        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_BREEZE_DEATH, 1.0f, 1.2f);
        caster.getWorld().spawnParticle(Particle.CLOUD, caster.getLocation(), 30, 0.5, 0.5, 0.5, 0.05);
    }


    @Override
    public void update(double t) {
        caster.getWorld().spawnParticle(Particle.CLOUD, caster.getLocation(), 5, 0.2, 0.2, 0.2, 0.01);
    }

    @Override
    public void finish() {
        caster.removeMetadata("DASH_INVINCIBLE", DynamicSpells.getInstance());
        caster.getWorld().spawnParticle(Particle.SMOKE, caster.getLocation(), 10, 0.5, 0.5, 0.5, 0.05);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Vector from = event.getFrom().toVector();
        Vector to = event.getTo().toVector();
        Vector movement = to.clone().subtract(from);

        // Ignore Y component (vertical movement)
        movement.setY(0);

        // If player actually moved horizontally
        if (movement.lengthSquared() > 0.001) {
            lastMovementDirections.put(player, movement.normalize());
        }
    }
}
