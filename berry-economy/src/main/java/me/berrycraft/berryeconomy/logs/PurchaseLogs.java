package me.berrycraft.berryeconomy.logs;

import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PurchaseLogs {

    private static Connection connection;

    public PurchaseLogs(String url, String user, String password) throws SQLException {
        PurchaseLogs.connection = DriverManager.getConnection(url, user, password);
    }

    public static void logPurchase(Player buyer, String itemName, int price) {
        if (connection == null) return;

        String sql = "INSERT INTO purchase_logs (buyer_name, buyer_UUID, item, price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, buyer.getName());
            stmt.setString(2, buyer.getUniqueId().toString());
            stmt.setString(3, itemName);
            stmt.setInt(4, price);
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
