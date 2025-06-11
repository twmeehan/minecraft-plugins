package me.berrycraft.dynamicspells.Spells;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import me.berrycraft.dynamicspells.DynamicSpells;
import me.berrycraft.dynamicspells.IExecutableSpell;
import me.berrycraft.dynamicspells.Spell;
import me.berrycraft.dynamicspells.SpellEngine;

public class Dash extends Spell implements IExecutableSpell {

    public static final String NAME = "dash";
    public static final Material MATERIAL = Material.FEATHER;
    public static YamlConfiguration config;

    private Player caster;
    private int level;
    private double speed;
    private double duration;

    public static void init() {
        config = loadSpellConfig(NAME);
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
        // Determine dash direction: use current movement direction if moving, else use facing direction
        Vector dashDirection = caster.getVelocity();
        if (dashDirection == null || dashDirection.lengthSquared() < 0.01) {
            dashDirection = caster.getLocation().getDirection();
        }

        dashDirection.setY(0).normalize();  // Remove vertical component and normalize
        if (dashDirection.lengthSquared() < 0.01) {
            dashDirection = new Vector(0, 0, 1);  // default forward vector
        }

        caster.setVelocity(dashDirection.multiply(speed));

        // Mark the player invincible
        caster.setMetadata("DASH_INVINCIBLE", new FixedMetadataValue(DynamicSpells.getInstance(), true));

        // Play sound and particle
        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
        caster.getWorld().spawnParticle(Particle.CLOUD, caster.getLocation(), 30, 0.5, 0.5, 0.5, 0.05);
    }

    @Override
    public void update(double t) {
        // Trailing particle
        caster.getWorld().spawnParticle(Particle.CLOUD, caster.getLocation(), 5, 0.2, 0.2, 0.2, 0.01);
    }

    @Override
    public void finish() {
        caster.removeMetadata("DASH_INVINCIBLE", DynamicSpells.getInstance());

        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_BREEZE_DEATH, 0.5f, 1.0f);
        caster.getWorld().spawnParticle(Particle.SMOKE, caster.getLocation(), 10, 0.5, 0.5, 0.5, 0.05);
    }
}
