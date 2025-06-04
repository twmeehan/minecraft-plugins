package me.berrycraft.keepinventoryalternative;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

public class KeepInventoryAlternative extends JavaPlugin implements Listener {

    private File logFile;
    private YamlConfiguration logConfig;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        loadLogFile();
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

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        double currentHearts = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        if (currentHearts > 10.0) {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(currentHearts - 2.0);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        if (logConfig.contains(uuid.toString())) {
            long lastLogin = logConfig.getLong(uuid.toString());
            if (now - lastLogin >= 86_400_000L) { // 24 hours
                double currentHearts = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
                if (currentHearts < 20.0) {
                    player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(Math.min(20.0, currentHearts + 2.0));
                }
                logConfig.set(uuid.toString(), now);
                saveLogFile();
            }
        }

        
    }
}
