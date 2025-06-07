package me.berrycraft.berryeconomy.logs;

import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LootLogs {

    private static Connection connection;

    public LootLogs(String url, String user, String password) throws SQLException {
        LootLogs.connection = DriverManager.getConnection(url, user, password);
    }

    public static void logLoot(Player player, String lootTableName, int value) {
        if (connection == null) return;

        String sql = "INSERT INTO loot_logs (uuid, name, loot_table, value) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, player.getName());
            stmt.setString(3, lootTableName);
            stmt.setInt(4, value);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
