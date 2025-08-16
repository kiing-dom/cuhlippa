package com.cuhlippa.client.storage;

import com.cuhlippa.client.clipboard.ClipboardItem;
import com.cuhlippa.client.clipboard.ItemType;
import com.cuhlippa.client.config.Settings;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LocalDatabase {
    private static final String DB_URL = "jdbc:sqlite:cuhlippa.db";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public LocalDatabase() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement()) {
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS clipboard (
                         id INTEGER PRIMARY KEY AUTOINCREMENT,
                         type TEXT NOT NULL,
                         content BLOB NOT NULL,
                         timestamp TEXT NOT NULL,
                         hash TEXT NOT NULL UNIQUE
                     )
                     """);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveItem(Connection conn, ClipboardItem item) throws SQLException {
        String sql = "INSERT OR IGNORE INTO clipboard(type, content, timestamp, hash) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getType().name());
            pstmt.setBytes(2, item.getContent());
            pstmt.setString(3, FORMATTER.format(item.getTimestamp()));
            pstmt.setString(4, item.getHash());

            pstmt.executeUpdate();
        }
    }    public void saveItemAndUpdateHistory(ClipboardItem item, Settings settings) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            saveItem(conn, item);
            enforceHistoryLimit(conn, settings.getMaxHistoryItems());
            
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<ClipboardItem> getAllItems() {
        List<ClipboardItem> items = new ArrayList<>();

        String sql = "SELECT type, content, timestamp, hash FROM clipboard ORDER by id DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ItemType type = ItemType.valueOf(rs.getString("type"));
                byte[] content = rs.getBytes("content");
                LocalDateTime timestamp = LocalDateTime.parse(rs.getString("timestamp"));
                String hash = rs.getString("hash");
                items.add(new ClipboardItem(type, content, timestamp, hash));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }

    public boolean deleteItemByHash(String hash) {
        String sql = "DELETE FROM clipboard WHERE hash = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(sql)
            ) {
            pstmt.setString(1, hash);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting item: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteAllItems() {
        String sql = "DELETE FROM clipboard";

        try (Connection conn = DriverManager.getConnection(DB_URL);
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Deleted " + rowsAffected + " items from clipboard");

            return true;
         } catch (SQLException e) {
            System.err.println("Error deleting items: " + e.getMessage());
            return false;
         }
    }

    private void enforceHistoryLimit(Connection conn, int maxItems) throws SQLException {
        String sql = "DELETE FROM clipboard WHERE id NOT IN " +
            "(SELECT id FROM clipboard ORDER by id DESC LIMIT ?)";
            
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, maxItems);
                pstmt.executeUpdate();
        }
    }
}