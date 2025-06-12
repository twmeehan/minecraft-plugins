package me.berrycraft.berryeconomy.logs;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerActivityLogs implements Listener {

    private static Connection connection;
    private static String url;
    private static String user;
    private static String password;

    public PlayerActivityLogs(String url, String user, String password) throws SQLException {
        PlayerActivityLogs.url = url;
        PlayerActivityLogs.user = user;
        PlayerActivityLogs.password = password;
        connect();
    }

    private static void connect() throws SQLException {
        if (connection == null || connection.isClosed() || !connection.isValid(2)) {
            connection = DriverManager.getConnection(url, user, password);
        }
    }

    public static void logEvent(UUID uuid, String name, String type) {
        String sql = "INSERT INTO player_activity (uuid, name, event_type) VALUES (?, ?, ?)";

        try {
            connect();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, name);
                stmt.setString(3, type);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeLastQuitLog(UUID uuid) {
        String sql = "DELETE FROM player_activity " +
                     "WHERE id = (SELECT id FROM (" +
                     "SELECT id FROM player_activity WHERE uuid = ? AND event_type = 'QUIT' " +
                     "ORDER BY event_time DESC LIMIT 1) AS sub)";

        try {
            connect();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        logEvent(player.getUniqueId(), player.getName(), "JOIN");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        logEvent(player.getUniqueId(), player.getName(), "QUIT");
    }
}
