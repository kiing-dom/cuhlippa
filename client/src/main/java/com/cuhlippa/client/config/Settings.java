package com.cuhlippa.client.config;

import java.util.List;
import java.util.ArrayList;

public class Settings {
    private String theme = "light";
    private int maxHistoryItems = 200;
    private int thumbnailSize = 64;
    private List<String> ignorePatterns = new ArrayList<>();
    private SyncSettings sync = new SyncSettings();
    
    public static class SyncSettings {
        private boolean enabled = false;
        private String serverAddress = "ws://localhost:8080/sync";
        private String encryptionKey = "";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public String getServerAddress() { return serverAddress; }
        public void setServerAddress(String serverAddress) { this.serverAddress = serverAddress; }
        
        public String getEncryptionKey() { return encryptionKey; }
        public void setEncryptionKey(String encryptionKey) { this.encryptionKey = encryptionKey; }
    }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public int getMaxHistoryItems() { return maxHistoryItems; }
    public void setMaxHistoryItems(int maxHistoryItems) { this.maxHistoryItems = maxHistoryItems; }

    public int getThumbnailSize() { return thumbnailSize; }
    public void setThumbnailSize(int thumbnailSize) {this.thumbnailSize = thumbnailSize; }

    public List<String> getIgnorePatterns() { return ignorePatterns; }
    public void setIgnorePatterns(List<String> ignorePatterns) { this.ignorePatterns = ignorePatterns; }

    public SyncSettings getSync() { return sync; }
    public void setSync(SyncSettings sync) { this.sync = sync; }
}
