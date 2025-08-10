package com.cuhlippa.client.storage;

import com.cuhlippa.client.clipboard.ClipboardItem;
import com.cuhlippa.client.clipboard.ItemType;

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
                    CREATE TABLE IF NONT EXISTS clipboard (
                         id INTEGER PRIMARY KEY AUTOINCREMENT
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

    private void saveItem(ClipboardItem item) {
        String sql = "INSERT OR IGNORE INTO clipboard(type, content, timestamp, hash) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, item.getType().name());
                    pstmt.setBytes(2, item.getContent());
                    pstmt.setString(3, FORMATTER.format(item.getTimestamp()));
                    pstmt.setString(4, item.getHash());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<ClipboardItem> getItems() {
        // TODO: fill in later
    }
}
