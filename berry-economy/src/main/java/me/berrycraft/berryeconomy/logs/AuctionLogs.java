package me.berrycraft.berryeconomy.logs;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class AuctionLogs {

    private static Connection connection;
    private static String url;
    private static String user;
    private static String password;

    public AuctionLogs(String url, String user, String password) throws SQLException {
        AuctionLogs.url = url;
        AuctionLogs.user = user;
        AuctionLogs.password = password;
        connect(); // initial connection
    }

    private static void connect() throws SQLException {
        if (connection == null || connection.isClosed() || !connection.isValid(2)) {
            connection = DriverManager.getConnection(url, user, password);
        }
    }

    public static void logAuctionAction(OfflinePlayer seller, Player buyer, String itemName, int amount, int price) {
        String sql = "INSERT INTO auction_logs " +
                     "(seller_name, sell_UUID, buyer_name, buyer_UUID, item_name, amount, price) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            connect(); // <-- always check before use
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, seller.getName());
                stmt.setString(2, seller.getUniqueId().toString());
                stmt.setString(3, buyer.getName());
                stmt.setString(4, buyer.getUniqueId().toString());
                stmt.setString(5, itemName);
                stmt.setInt(6, amount);
                stmt.setInt(7, price);
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

