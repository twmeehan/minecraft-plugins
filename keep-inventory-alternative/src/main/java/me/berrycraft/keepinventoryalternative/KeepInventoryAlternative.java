package me.berrycraft.keepinventoryalternative;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class KeepInventoryAlternative extends JavaPlugin implements Listener {

    private File logFile;
    private YamlConfiguration logConfig;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        loadLogFile();
        scheduleHourlyResetTask();
    }

    private void loadLogFile() {
        logFile = new File(getDataFolder(), "logs.yml");
        if (!logFile.exists()) {
            logFile.getParentFile().mkdirs();
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        logConfig = YamlConfiguration.loadConfiguration(logFile);
    }

    private void saveLogFile() {
        try {
            logConfig.save(logFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetPlayerHealth(Player player) {
        double baseHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        if (baseHealth < 20.0) {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
            
        }
        logConfig.set(player.getUniqueId().toString(), false);
            saveLogFile();
    }

    private void scheduleHourlyResetTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                long lastReset = logConfig.getLong("last-reset", 0L);

                if (isPastMidnight(now) && !isPastMidnight(lastReset)) {
                    getLogger().info("Running daily health reset...");

                    // Set all UUIDs to true
                    for (String key : logConfig.getKeys(false)) {
                        if (key.equals("last-reset")) continue;
                        logConfig.set(key, true);
                    }

                    // Update reset time
                    logConfig.set("last-reset", now);
                    saveLogFile();

                    // Heal online players
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        resetPlayerHealth(player);
                    }
                }
            }
        }.runTaskTimer(this, 0L, 20L * 60 * 60); // every hour
    }

    private boolean isPastMidnight(long millis) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        int hour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
        return hour >= 0;
    }


    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        if (!logConfig.contains(uuid)) {
            logConfig.set(uuid, false);
            saveLogFile();
            return;
        }

        boolean shouldHeal = logConfig.getBoolean(uuid);
        if (shouldHeal) {
            resetPlayerHealth(player);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.getEntity().getWorld().setGameRule(GameRule.KEEP_INVENTORY,true);
        event.setKeepInventory(true);
        event.setKeepLevel(true);
        Player player = event.getEntity();
        double currentHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        if (currentHealth > 10.0) {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(currentHealth - 2.0);
        }
    }
}
