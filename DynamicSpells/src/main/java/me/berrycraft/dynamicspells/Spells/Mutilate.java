package me.berrycraft.dynamicspells.Spells;

import me.berrycraft.dynamicspells.*;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Mutilate extends Spell implements IExecutableSpell {

    public static final String NAME = "mutilate";
    public static final Material MATERIAL = Material.NETHERITE_AXE;
    public static YamlConfiguration config;

    private Player caster;
    private int level;
    private double chargeTime;
    private double damage;
    private double maxRange;
    private int swipes;
    private boolean fired;

    private LivingEntity target;

    public static void init() {
        config = loadSpellConfig(NAME);
    }

    public static boolean cast(Player caster, int level) {
        Mutilate mutilate = new Mutilate();
        mutilate.caster = caster;
        mutilate.level = level;
        mutilate.chargeTime = config.getDouble(level + ".charge_time", 1.0);
        mutilate.damage = config.getDouble(level + ".damage", 5.0);
        mutilate.maxRange = config.getDouble(level + ".max_range", 15.0);
        mutilate.swipes = config.getInt(level + ".swipes", 3);
        mutilate.fired = false;

        SpellEngine.register(mutilate, mutilate.chargeTime);
        return true;
    }

    @Override
    public void start() {
        caster.setWalkSpeed(0.05f);
        caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 0.8f);
    }

    @Override
    public void update(double t) {
        caster.getWorld().spawnParticle(Particle.SMALL_FLAME, caster.getLocation().add(0, 1.5, 0), 5, 0.2, 0.2, 0.2, 0);

        if (!fired && t >= chargeTime) {
            fired = true;
            attemptMutilate();
            cancel();
        }
    }

    @Override
    public void finish() {
        caster.setWalkSpeed(0.2f);
        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 0.5f, 1.0f);
        caster.getWorld().spawnParticle(Particle.SMOKE, caster.getLocation(), 20, 1, 1, 1, 0.1);
    }

    private void attemptMutilate() {
        target = getTargetEntity();

        if (target == null) {
            caster.sendMessage(ChatColor.RED + "No valid target in sight!");
            return;
        }

        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f);

        final int[] swipeCount = { 0 };
        final int[] taskId = new int[1];
        taskId[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                DynamicSpells.getInstance(),
                new Runnable() {
                    @Override
                    public void run() {
                        if (swipeCount[0] >= swipes || target.isDead() || !target.isValid()) {
                            Bukkit.getScheduler().cancelTask(taskId[0]);
                            return;
                        }

                        launchToTarget(swipeCount[0]);
                        performSwipe();
                        swipeCount[0]++;
                    }
                },
                0L, 8L // 8 ticks = 0.4 seconds between dashes
        );
    }

    private LivingEntity getTargetEntity() {
        Location eyeLoc = caster.getEyeLocation();
        Vector direction = eyeLoc.getDirection().normalize();

        for (double distance = 1.0; distance <= maxRange; distance += 0.5) {
            Location point = eyeLoc.clone().add(direction.clone().multiply(distance));

            for (Entity entity : caster.getWorld().getNearbyEntities(point, 1.0, 1.0, 1.0)) {
                if (entity instanceof LivingEntity && !entity.equals(caster)) {
                    return (LivingEntity) entity;
                }
            }
        }
        return null;
    }

    private void launchToTarget(int swipeIndex) {
        Location start = caster.getLocation();
        Location end = target.getLocation();
        double distance = start.distance(end);

        double speed;
        if (swipeIndex == 0) {
            // First dash: reach the target even at maxRange
            speed = Math.min(distance * 3.5, 6.0); // first dash = big leap!
        } else {
            // Subsequent dashes: quick reposition without overshooting
            speed = Math.min(distance * 0.75, 2.0); // reduced dash speed
        }

        // Make the player face the target
        Vector direction = end.toVector().subtract(start.toVector()).normalize();
        caster.teleport(start.setDirection(direction));

        // Launch the player
        Vector velocity = direction.multiply(speed);
        velocity.setY(0.3); // Slight upward lift
        caster.setVelocity(velocity);

        drawTrail(start, end);
    }

    private void drawTrail(Location start, Location end) {
        Vector dir = end.toVector().subtract(start.toVector());
        int points = (int) (dir.length() * 4);
        for (int i = 0; i <= points; i++) {
            Location point = start.clone().add(dir.clone().multiply((double) i / points));
            point.getWorld().spawnParticle(Particle.CRIT, point, 1, 0, 0, 0, 0);
        }
    }

    private void performSwipe() {
        // Play sweep particles
        Location loc = target.getLocation();
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45);
            double x = Math.cos(angle) * 1.5;
            double z = Math.sin(angle) * 1.5;
            Location particleLoc = loc.clone().add(x, 1, z);
            loc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, particleLoc, 1, 0, 0, 0, 0);
        }

        // Damage target with spell damage only (no weapon multiplier)
        SpellEngine.damage(caster, target, damage, SpellDamageType.MAGIC);
    }
}
