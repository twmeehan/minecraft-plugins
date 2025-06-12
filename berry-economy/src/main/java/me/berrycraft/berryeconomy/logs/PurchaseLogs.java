package me.berrycraft.berryeconomy.logs;

import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PurchaseLogs {

    private static Connection connection;
    private static String url;
    private static String user;
    private static String password;

    public PurchaseLogs(String url, String user, String password) throws SQLException {
        PurchaseLogs.url = url;
        PurchaseLogs.user = user;
        PurchaseLogs.password = password;
        connect(); // Initial connection
    }

    private static void connect() throws SQLException {
        if (connection == null || connection.isClosed() || !connection.isValid(2)) {
            connection = DriverManager.getConnection(url, user, password);
        }
    }

    public static void logPurchase(Player buyer, String itemName, int price) {
        String sql = "INSERT INTO purchase_logs (buyer_name, buyer_UUID, item, price) VALUES (?, ?, ?, ?)";

        try {
            connect(); // Ensure valid connection
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, buyer.getName());
                stmt.setString(2, buyer.getUniqueId().toString());
                stmt.setString(3, itemName);
                stmt.setInt(4, price);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Graceful error reporting
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
