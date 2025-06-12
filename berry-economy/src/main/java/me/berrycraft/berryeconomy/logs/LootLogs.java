package me.berrycraft.berryeconomy.logs;

import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LootLogs {

    private static Connection connection;
    private static String url;
    private static String user;
    private static String password;

    public LootLogs(String url, String user, String password) throws SQLException {
        LootLogs.url = url;
        LootLogs.user = user;
        LootLogs.password = password;
        connect(); // initial connection
    }

    private static void connect() throws SQLException {
        if (connection == null || connection.isClosed() || !connection.isValid(2)) {
            connection = DriverManager.getConnection(url, user, password);
        }
    }

    public static void logLoot(Player player, String lootTableName, int value) {
        String sql = "INSERT INTO loot_logs (uuid, name, loot_table, value) VALUES (?, ?, ?, ?)";

        try {
            connect(); // check connection before use
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, player.getUniqueId().toString());
                stmt.setString(2, player.getName());
                stmt.setString(3, lootTableName);
                stmt.setInt(4, value);
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
}
