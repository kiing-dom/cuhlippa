package com.cuhlippa.client.storage;

import com.cuhlippa.client.clipboard.ClipboardItem;
import com.cuhlippa.client.clipboard.ItemType;
import com.cuhlippa.client.config.Settings;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class LocalDatabase {    private final String DB_URL;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final int CURRENT_DB_VERSION = 3;
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_HASH = "hash";
    private static final String COLUMN_PINNED = "pinned";

    private static String getProcessId() {
        return String.valueOf(ProcessHandle.current().pid());
    }
    
    /**
     * Constructor with database name based on demo mode
     */
    public LocalDatabase() {
        this(false); // Default to production mode
    }
    
    /**
     * Constructor that allows demo mode specification
     */
    public LocalDatabase(boolean isDemoMode) {
        if (isDemoMode) {
            // In demo mode, use process ID to avoid conflicts between demo instances
            this.DB_URL = "jdbc:sqlite:cuhlippa_" + getProcessId() + ".db";
            System.out.println("🎬 Demo database: " + this.DB_URL);
        } else {
            // Production mode uses consistent database name
            this.DB_URL = "jdbc:sqlite:cuhlippa.db";
        }
        
        optimizeDatabaseConfiguration();
        createTableIfNotExists();
        checkAndMigrateDatabase();
        createTagsTable();
    }

    private void optimizeDatabaseConfiguration() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            // Enable WAL mode for better concurrent access
            stmt.execute("PRAGMA journal_mode=WAL");
            
            // Set busy timeout to handle lock contention
            stmt.execute("PRAGMA busy_timeout=5000");
            
            // Optimize performance
            stmt.execute("PRAGMA synchronous=NORMAL");
            stmt.execute("PRAGMA cache_size=10000");
            stmt.execute("PRAGMA temp_store=memory");
            
            System.out.println("Database optimizations applied for concurrent access");
        } catch (SQLException e) {
            System.err.println("Failed to optimize database: " + e.getMessage());
        }
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
                         hash TEXT NOT NULL UNIQUE,
                         category TEXT NOT NULL DEFAULT 'General'
                     )
                     """);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTagsTable() {
        String sql = """
                    CREATE TABLE IF NOT EXISTS item_tags (
                        item_hash TEXT NOT NULL,
                        tag TEXT NOT NULL,
                        PRIMARY KEY (item_hash, tag),
                        FOREIGN KEY (item_hash) REFERENCES clipboard(hash)
                    )
                """;
        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Failed to create tags table: " + e.getMessage());
        }
    }

    private void saveItem(Connection conn, ClipboardItem item) throws SQLException {
        String sql = "INSERT OR IGNORE INTO clipboard(type, content, timestamp, hash, category, pinned) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getType().name());
            pstmt.setBytes(2, item.getContent());
            pstmt.setString(3, FORMATTER.format(item.getTimestamp()));
            pstmt.setString(4, item.getHash());
            pstmt.setString(5, item.getCategory());
            pstmt.setBoolean(6, item.isPinned());

            pstmt.executeUpdate();
            saveTags(item.getHash(), item.getTags());
        }
    }

    public void saveItem(ClipboardItem item) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);
            saveItem(conn, item);
            conn.commit();
        } catch (SQLException e) {
            System.err.println("Error saving item: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void saveItemAndUpdateHistory(ClipboardItem item, Settings settings) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            saveItem(conn, item);
            enforceHistoryLimit(conn, settings.getMaxHistoryItems());

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }    private void saveTags(String itemHash, Set<String> tags) {
        int maxRetries = 3;
        int retryDelay = 100; // milliseconds
        
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                conn.setAutoCommit(false);
                try {
                    deleteOldTags(conn, itemHash);
                    insertNewTags(conn, itemHash, tags);
                    conn.commit();
                    return; // Success, exit retry loop
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            } catch (SQLException e) {
                if (attempt == maxRetries - 1) {
                    System.err.println("Failed to save tags after " + maxRetries + " attempts: " + e.getMessage());
                } else {
                    System.out.println("Tag save attempt " + (attempt + 1) + " failed, retrying: " + e.getMessage());
                    try {
                        Thread.sleep(retryDelay * (attempt + 1)); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
    }

    private void deleteOldTags(Connection conn, String itemHash) {
        String sql = "DELETE FROM item_tags WHERE item_hash = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, itemHash);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Failed to delete old tags: " + e.getMessage());
        }
    }

    private void insertNewTags(Connection conn, String itemHash, Set<String> tags) {
        String sql = "INSERT INTO item_tags (item_hash, tag) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, itemHash);
            for (String tag : tags) {
                pstmt.setString(2, tag);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            System.out.println("Unable to insert new tags: " + e.getMessage());
        }
    }

    public List<ClipboardItem> getAllItems() {
        List<ClipboardItem> items = new ArrayList<>();

        String sql = "SELECT type, content, timestamp, hash, category, pinned FROM clipboard ORDER by id DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ItemType type = ItemType.valueOf(rs.getString(COLUMN_TYPE));
                byte[] content = rs.getBytes(COLUMN_CONTENT);
                LocalDateTime timestamp = LocalDateTime.parse(rs.getString(COLUMN_TIMESTAMP));
                String hash = rs.getString(COLUMN_HASH);
                Set<String> tags = loadTagsForItem(hash);
                String category = rs.getString(COLUMN_CATEGORY);
                boolean pinned = rs.getBoolean(COLUMN_PINNED);

                items.add(new ClipboardItem(type, content, timestamp, hash, tags, category, pinned));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }

    public List<ClipboardItem> getItemsByTag(String tag) {
        String sql = """
                    SELECT DISTINCT c.* from clipboard c
                    JOIN item_tags it ON c.hash = it.item_hash
                    WHERE it.tag = ?
                    ORDER by c.timestamp DESC
                """;
        List<ClipboardItem> items = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tag.toLowerCase());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ClipboardItem item = createItemFromResultSet(rs);
                items.add(item);
            }
        } catch (SQLException e) {
            System.out.println("Failed to get items: " + e.getMessage());
        }

        return items;
    }

    public List<ClipboardItem> getItemsByCategory(String category) {
        String sql = "SELECT type, content, timestamp, hash, category, pinned FROM clipboard WHERE category = ?";

        List<ClipboardItem> items = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ClipboardItem item = createItemFromResultSet(rs);
                items.add(item);
            }
        } catch (SQLException e) {
            System.out.println("Failed to get items: " + e.getMessage());
        }

        return items;
    }

    public Set<String> getAllTags() {
        String sql = "SELECT DISTINCT tag FROM item_tags ORDER BY tag";
        Set<String> tags = new HashSet<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tags.add(rs.getString("tag"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all tags: " + e.getMessage());
        }
        return tags;
    }

    public Set<String> getAllCategories() {
        String sql = "SELECT DISTINCT category FROM clipboard ORDER BY category";
        Set<String> categories = new HashSet<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categories.add(rs.getString(COLUMN_CATEGORY));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all categories: " + e.getMessage());
        }
        return categories;
    }

    private Set<String> loadTagsForItem(String itemHash) {
        String sql = "SELECT tag FROM item_tags WHERE item_hash = ?";
        Set<String> tags = new HashSet<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, itemHash);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                tags.add(rs.getString("tag"));
            }
        } catch (SQLException e) {
            System.out.println("Failed to load tags: " + e.getMessage());
        }

        return tags;
    }

    public boolean deleteItemByHash(String hash) {
        String sql = "DELETE FROM clipboard WHERE hash = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hash);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting item: " + e.getMessage());
            return false;
        }
    }

    public boolean itemExistsByHash(String hash) {
        String sql = "SELECT 1 FROM clipboard WHERE hash = ? LIMIT 1";
        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hash);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking item existence: " + e.getMessage());
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

    private ClipboardItem createItemFromResultSet(ResultSet rs) throws SQLException {
        ItemType type = ItemType.valueOf(rs.getString(COLUMN_TYPE));
        byte[] content = rs.getBytes(COLUMN_CONTENT);
        LocalDateTime timestamp = LocalDateTime.parse(rs.getString(COLUMN_TIMESTAMP));
        String hash = rs.getString(COLUMN_HASH);
        String category = rs.getString(COLUMN_CATEGORY);
        boolean pinned = rs.getBoolean(COLUMN_PINNED);

        Set<String> tags = loadTagsForItem(hash);

        return new ClipboardItem(type, content, timestamp, hash, tags, category, pinned);
    }

    private void enforceHistoryLimit(Connection conn, int maxItems) throws SQLException {
        // Don't delete pinned items - only delete unpinned items beyond the limit
        String sql = """
                    DELETE FROM clipboard
                    WHERE pinned = FALSE
                    AND id NOT IN (
                        SELECT id FROM clipboard
                        WHERE pinned = FALSE
                        ORDER BY id DESC
                        LIMIT ?
                    )
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, maxItems);
            int deletedRows = pstmt.executeUpdate();
            if (deletedRows > 0) {
                System.out
                        .println("Deleted " + deletedRows + " items to enforce history limit (preserved pinned items)");
            }
        }
    }

    private void checkAndMigrateDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            int currentVersion = getDatabaseVersion(conn);

            if (currentVersion < CURRENT_DB_VERSION) {
                System.out.println("Migrating database from version " + currentVersion + " to " + CURRENT_DB_VERSION);
                performMigrations(conn, currentVersion);
                setDatabaseVersion(conn, CURRENT_DB_VERSION);
                System.out.println("Database migration completed successfully");
            }
        } catch (SQLException e) {
            System.err.println("Migration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int getDatabaseVersion(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("PRAGMA user_version")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private void setDatabaseVersion(Connection conn, int version) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA user_version = " + version);
        }
    }

    private void performMigrations(Connection conn, int fromVersion) throws SQLException {
        conn.setAutoCommit(false);

        try {
            switch (fromVersion) {
                case 0:
                    migrateToVersion1(conn);
                    System.out.println("Applied migration to version 1");
                    // fall through case 1:
                    migrateToVersion2(conn);
                    System.out.println("Applied migration to version 2");
                    // fall through
                case 2:
                    migrateToVersion3(conn);
                    System.out.println("Applied migration to version 3");
                    // fall through
                default:
                    // All migrations complete
                    break;
            }

            conn.commit();
            System.out.println("All migrations applied successfully");

        } catch (SQLException e) {
            conn.rollback();
            throw new SQLException("Migration failed, rolling back: " + e.getMessage(), e);
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private void migrateToVersion1(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "clipboard", COLUMN_CATEGORY);

            if (!columns.next()) {
                stmt.execute("ALTER TABLE clipboard ADD COLUMN category TEXT NOT NULL DEFAULT 'General'");
                System.out.println("Migration v1: Added category column");
            }
            columns.close();
        }
    }

    private void migrateToVersion2(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS item_tags (
                            item_hash TEXT NOT NULL,
                            tag TEXT NOT NULL,
                            PRIMARY KEY (item_hash, tag),
                            FOREIGN KEY (item_hash) REFERENCES clipboard(hash)
                        )
                    """);
            System.out.println("Migration v2: Created item_tags table");
        }
    }

    private void migrateToVersion3(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE clipboard ADD COLUMN pinned BOOLEAN NOT NULL DEFAULT FALSE");
            System.out.println("Migration v3: Added pinned column");
        }
    }

    public boolean toggleItemPin(String hash) {
        String sql = "UPDATE clipboard SET pinned = NOT pinned WHERE hash = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hash);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Successfully toggled pin status for item: " + hash);
                return true;
            } else {
                System.out.println("No item found with hash: " + hash);
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Failed to toggle item pin: " + e.getMessage());
            return false;
        }
    }

    public boolean isItemPinned(String hash) {
        String sql = "SELECT pinned FROM clipboard WHERE hash = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hash);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getBoolean(COLUMN_PINNED);
            }
            return false; // Item not found, so not pinned
        } catch (SQLException e) {
            System.err.println("Failed to check pin status: " + e.getMessage());
            return false;
        }
    }

    public List<ClipboardItem> getPinnedItems() {
        List<ClipboardItem> items = new ArrayList<>();
        String sql = "SELECT type, content, timestamp, hash, category, pinned FROM clipboard WHERE pinned = TRUE ORDER BY timestamp DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ClipboardItem item = createItemFromResultSet(rs);
                items.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Error getting pinned items: " + e.getMessage());
        }

        return items;
    }

}