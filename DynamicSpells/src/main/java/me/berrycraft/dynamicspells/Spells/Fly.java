package me.berrycraft.dynamicspells.Spells;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.berrycraft.dynamicspells.DynamicSpells;
import me.berrycraft.dynamicspells.Spell;
import de.tr7zw.nbtapi.NBTItem;

import java.util.*;

public class Fly extends Spell implements Listener {
    public static final String NAME = "fly";
    public static final Material MATERIAL = Material.NETHER_STAR;
    public static YamlConfiguration config;
    
    private static final Map<UUID, Long> channelingPlayers = new HashMap<>();
    private static final Map<UUID, Long> flyingPlayers = new HashMap<>();
    private static final Map<UUID, Float> originalFlySpeeds = new HashMap<>();
    
    public static void init() {
        config = loadSpellConfig(NAME);
        Bukkit.getPluginManager().registerEvents(new Fly(), DynamicSpells.getInstance());
        
        // Start particle effect task
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : flyingPlayers.keySet()) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isFlying()) {
                        player.spawnParticle(Particle.CLOUD, player.getLocation().add(0, 0.1, 0), 1, 0.1, 0, 0.1, 0);
                    }
                }
            }
        }.runTaskTimer(DynamicSpells.getInstance(), 0L, 2L);
    }
    
    public static void onServerStop() {
        // Deactivate flight for all players when server stops
        for (UUID uuid : flyingPlayers.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                endFlight(player);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (flyingPlayers.containsKey(player.getUniqueId())) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(DynamicSpells.getInstance(), () -> {
                if (player.isOnline()) {
                    endFlight(player);
                }
            }, 20L);
        } else {
            Bukkit.getScheduler().scheduleSyncDelayedTask(DynamicSpells.getInstance(), () -> {
                if (player.isOnline()) {
                    player.setAllowFlight(false);
                    player.setFlying(false);
                    player.setFlySpeed(0.1f);
                }
            }, 20L);
        }
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        
        // Cancel flight if player is flying
        if (flyingPlayers.containsKey(player.getUniqueId())) {
            endFlight(player);
            return;
        }
        
        // Cancel channeling if player is channeling
        if (channelingPlayers.containsKey(player.getUniqueId())) {
            cancelChanneling(player);
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // If player is channeling, check if they moved too far
        if (channelingPlayers.containsKey(player.getUniqueId())) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (from.distanceSquared(to) > 0.1) { // Allow small movements
                cancelChanneling(player);
            }
        }
    }
    
    private static void endFlight(Player player) {
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setFlySpeed(0.1f);

        
        // Add slow falling effect
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 0, false, false));
        
        flyingPlayers.remove(player.getUniqueId());
        player.sendMessage("§cYour flight ability has ended!");
    }
    
    private void cancelChanneling(Player player) {
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        channelingPlayers.remove(player.getUniqueId());
        player.sendMessage("§cYour channeling has been interrupted!");
    }
    
    private boolean isSpellBook(ItemStack item) {
        if (item == null || item.getType() != MATERIAL) {
            return false;
        }
        
        try {
            NBTItem nbti = new NBTItem(item);
            return "spell_book".equals(nbti.getString("CustomItem")) && 
                   NAME.equals(nbti.getString("Spell"));
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean cast(Player caster, int level) {
        int channelTime = config.getInt(level + ".channel_time", 10);
        int flightTime = config.getInt(level + ".flight_time", 300);
        
        // Start channeling
        caster.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, channelTime * 20, 2, false, false));
        channelingPlayers.put(caster.getUniqueId(), System.currentTimeMillis());
        
        // Start channeling task
        new BukkitRunnable() {
            int timeLeft = channelTime;
            
            @Override
            public void run() {
                if (!channelingPlayers.containsKey(caster.getUniqueId())) {
                    this.cancel();
                    return;
                }
                
                if (timeLeft <= 0) {
                    // Channeling complete, start flight
                    channelingPlayers.remove(caster.getUniqueId());
                    startFlight(caster, flightTime);
                    this.cancel();
                    return;
                }
                
                // Show channeling progress
                caster.sendMessage("§eChanneling: " + timeLeft + " seconds remaining...");
                caster.spawnParticle(Particle.POOF, caster.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0);
                timeLeft--;
            }
        }.runTaskTimer(DynamicSpells.getInstance(), 0L, 20L);
        
        return true;
    }
    
    private static void startFlight(Player player, int flightTime) {
        // Store original fly speed
        originalFlySpeeds.put(player.getUniqueId(), player.getFlySpeed());
        
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setFlySpeed(0.05f); // Slow flight speed
        flyingPlayers.put(player.getUniqueId(), System.currentTimeMillis());
        
        // Start flight duration task
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!flyingPlayers.containsKey(player.getUniqueId())) {
                    this.cancel();
                    return;
                }
                
                // Check if flight time has expired
                long startTime = flyingPlayers.get(player.getUniqueId());
                if (System.currentTimeMillis() - startTime >= flightTime * 1000) {
                    endFlight(player);
                    this.cancel();
                }
            }
        }.runTaskTimer(DynamicSpells.getInstance(), 0L, 20L);
        
        player.sendMessage("§aYou can now fly for " + (flightTime / 60) + " minutes!");
        player.playSound(player.getLocation(), Sound.ENTITY_CAMEL_SADDLE, 1.0f, 1.0f);
    }
} 