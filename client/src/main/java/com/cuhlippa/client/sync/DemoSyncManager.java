package com.cuhlippa.client.sync;

import com.cuhlippa.client.clipboard.ClipboardItem;
import com.cuhlippa.client.clipboard.ClipboardListener;
import com.cuhlippa.client.config.DeviceManager;
import com.cuhlippa.client.storage.LocalDatabase;
import com.cuhlippa.client.sync.dto.ClipboardItemDTO;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Demo Synchronization Manager that enables cross-device communication
 * between demo instances through file-based message passing
 */
public class DemoSyncManager implements ClipboardListener {
    private static final String DEMO_SYNC_DIR = "demo-sync";
    private static final String OUTBOX_DIR = DEMO_SYNC_DIR + "/outbox";
    private static final String DEVICE_PREFIX = "device-";
    private static final String DEMO_LOG_PREFIX = "🎬 Demo [";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");
    private static final long CLEANUP_AGE_MILLIS = TimeUnit.MINUTES.toMillis(5);
    
    private final LocalDatabase db;
    private final String deviceId;
    private final String deviceName;
    private final List<ClipboardListener> listeners = new ArrayList<>();
    private final ScheduledExecutorService scheduler;
    private volatile boolean processingSync = false;
    private volatile boolean isRunning = false;
    
    public DemoSyncManager(LocalDatabase db, String deviceName) {
        this.db = db;
        this.deviceId = DeviceManager.getDeviceId();
        this.deviceName = deviceName;
        this.scheduler = Executors.newScheduledThreadPool(2);
        
        setupSyncDirectories();
    }    private void setupSyncDirectories() {
        try {
            Files.createDirectories(Paths.get(OUTBOX_DIR));
            System.out.println(DEMO_LOG_PREFIX + deviceName + "] sync directories created");
        } catch (IOException e) {
            System.err.println("Failed to create demo sync directories: " + e.getMessage());
        }
    }
    
    public void initialize() {
        if (isRunning) return;
        
        isRunning = true;
        
        // Start periodic inbox checking
        scheduler.scheduleWithFixedDelay(this::checkInbox, 1, 1, TimeUnit.SECONDS);
        
        // Start periodic cleanup of old messages
        scheduler.scheduleWithFixedDelay(this::cleanupOldMessages, 30, 30, TimeUnit.SECONDS);
        
        System.out.println(DEMO_LOG_PREFIX + deviceName + "] sync manager initialized");
    }
    
    public void addClipboardListener(ClipboardListener listener) {
        listeners.add(listener);
    }
    
    private void notifyListeners(ClipboardItem item) {
        for (ClipboardListener listener : listeners) {
            listener.onClipboardItemAdded(item);
        }
    }
    
    @Override
    public void onClipboardItemAdded(ClipboardItem item) {
        if (processingSync) {
            System.out.println(DEMO_LOG_PREFIX + deviceName + "] skipping sync - currently processing");
            return;
        }
        
        try {
            ClipboardItemDTO dto = ClipboardItemDTO.fromClipboardItem(item, deviceId);
            sendToOtherDevices(dto);
            System.out.println(DEMO_LOG_PREFIX + deviceName + "] broadcasted clipboard item");
        } catch (Exception e) {
            System.err.println(DEMO_LOG_PREFIX + deviceName + "] failed to send sync message: " + e.getMessage());
        }
    }
      private void sendToOtherDevices(ClipboardItemDTO dto) throws IOException {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String filename = String.format("%s_%s_%s.json", 
            DEVICE_PREFIX + deviceName.toLowerCase(), 
            timestamp, 
            dto.getHash().substring(0, 8));
        
        File messageFile = new File(OUTBOX_DIR, filename);
        
        try (FileWriter writer = new FileWriter(messageFile)) {
            writer.write(dtoToJson(dto));
        }
          System.out.println(DEMO_LOG_PREFIX + deviceName + "] sent message: " + filename);
    }
    
    /**
     * Simple JSON serialization for ClipboardItemDTO
     */
    private String dtoToJson(ClipboardItemDTO dto) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"deviceId\":\"").append(escapeJson(dto.getDeviceId())).append("\",");
        json.append("\"content\":\"").append(escapeJson(dto.getContent())).append("\",");
        json.append("\"type\":\"").append(dto.getType().name()).append("\",");
        json.append("\"timestamp\":\"").append(dto.getTimestamp().toString()).append("\",");
        json.append("\"hash\":\"").append(escapeJson(dto.getHash())).append("\"");
        json.append("}");
        return json.toString();
    }
    
    /**
     * Simple JSON deserialization for ClipboardItemDTO
     */    private ClipboardItemDTO jsonToDto(String json) {
        ClipboardItemDTO dto = new ClipboardItemDTO();
        
        // Extract deviceId
        String dtoDeviceId = extractJsonValue(json, "deviceId");
        dto.setDeviceId(dtoDeviceId);
        
        // Extract content
        String content = extractJsonValue(json, "content");
        dto.setContent(unescapeJson(content));
        
        // Extract type
        String typeStr = extractJsonValue(json, "type");
        dto.setType(com.cuhlippa.client.clipboard.ItemType.valueOf(typeStr));
        
        // Extract timestamp
        String timestampStr = extractJsonValue(json, "timestamp");
        dto.setTimestamp(LocalDateTime.parse(timestampStr));
        
        // Extract hash
        String hash = extractJsonValue(json, "hash");
        dto.setHash(hash);
        
        return dto;
    }
    
    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    private String unescapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\\"", "\"")
                   .replace("\\\\", "\\")
                   .replace("\\n", "\n")
                   .replace("\\r", "\r")
                   .replace("\\t", "\t");
    }
    
    private String extractJsonValue(String json, String key) {
        String searchPattern = "\"" + key + "\":\"";
        int startIndex = json.indexOf(searchPattern);
        if (startIndex == -1) return "";
        
        startIndex += searchPattern.length();
        int endIndex = startIndex;
        
        // Find the closing quote, handling escaped quotes
        while (endIndex < json.length()) {
            if (json.charAt(endIndex) == '"' && 
                (endIndex == 0 || json.charAt(endIndex - 1) != '\\')) {
                break;
            }
            endIndex++;
        }
        
        if (endIndex >= json.length()) return "";
        return json.substring(startIndex, endIndex);
    }      private void checkInbox() {
        if (!isRunning) return;
        
        try {
            File outboxDir = new File(OUTBOX_DIR);
            
            // Process messages from other devices (files in outbox from others)
            File[] outboxFiles = outboxDir.listFiles(file -> 
                file.getName().endsWith(".json") && 
                !file.getName().startsWith(DEVICE_PREFIX + deviceName.toLowerCase()));
            
            if (outboxFiles != null) {
                processMessageFiles(outboxFiles);
            }
        } catch (Exception e) {
            System.err.println(DEMO_LOG_PREFIX + deviceName + "] error checking inbox: " + e.getMessage());
        }
    }
    
    private void processMessageFiles(File[] messageFiles) {
        for (File messageFile : messageFiles) {
            try {
                processIncomingMessage(messageFile);
            } catch (Exception e) {
                System.err.println(DEMO_LOG_PREFIX + deviceName + "] failed to process message " + 
                    messageFile.getName() + ": " + e.getMessage());
            }
        }
    }
      private void processIncomingMessage(File messageFile) throws IOException {
        // Check if we've already processed this message
        String processedMarker = messageFile.getAbsolutePath() + ".processed-" + deviceName.toLowerCase();
        File markerFile = new File(processedMarker);
        
        if (markerFile.exists()) {
            return; // Already processed
        }
        
        try {
            String jsonContent = readFileContent(messageFile);
            ClipboardItemDTO dto = jsonToDto(jsonContent);
            
            // Skip if it's from our own device
            if (deviceId.equals(dto.getDeviceId())) {
                createMarkerFile(markerFile);
                return;
            }
            
            ClipboardItem item = dto.toClipboardItem();
            
            // Check if item already exists to prevent duplicates
            if (!db.itemExistsByHash(item.getHash())) {
                saveItemAndNotify(item);
            }
            
            // Mark message as processed
            createMarkerFile(markerFile);
            
        } catch (Exception e) {
            System.err.println(DEMO_LOG_PREFIX + deviceName + "] failed to process message: " + e.getMessage());
        }
    }
      private void saveItemAndNotify(ClipboardItem item) {
        processingSync = true;
        try {
            db.saveItem(item);
            System.out.println(DEMO_LOG_PREFIX + deviceName + "] received sync item");
            
            // Notify UI and other listeners
            notifyListeners(item);
        } finally {
            processingSync = false;
        }
    }
    
    private void createMarkerFile(File markerFile) {
        try {
            if (!markerFile.createNewFile()) {
                // File already exists, which is fine
            }
        } catch (IOException e) {
            System.err.println(DEMO_LOG_PREFIX + deviceName + "] failed to create marker: " + e.getMessage());
        }
    }
    
    private String readFileContent(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            StringBuilder content = new StringBuilder();
            char[] buffer = new char[1024];
            int charsRead;
            while ((charsRead = reader.read(buffer)) != -1) {
                content.append(buffer, 0, charsRead);
            }
            return content.toString();
        }    }
    
    private void cleanupOldMessages() {
        try {
            File outboxDir = new File(OUTBOX_DIR);
            File[] files = outboxDir.listFiles();
            
            if (files != null) {
                long cutoffTime = System.currentTimeMillis() - CLEANUP_AGE_MILLIS;
                cleanupFilesOlderThan(files, cutoffTime, outboxDir);
            }
        } catch (Exception e) {
            System.err.println(DEMO_LOG_PREFIX + "cleanup error: " + e.getMessage());
        }
    }
    
    private void cleanupFilesOlderThan(File[] files, long cutoffTime, File outboxDir) {
        for (File file : files) {
            if (file.lastModified() < cutoffTime) {
                deleteFileAndMarkers(file, outboxDir);
            }
        }
    }
    
    private void deleteFileAndMarkers(File file, File outboxDir) {
        try {
            Files.delete(file.toPath());
            System.out.println(DEMO_LOG_PREFIX + "cleaned up old message: " + file.getName());
            
            // Also clean up processed markers
            cleanupProcessedMarkers(file, outboxDir);
        } catch (IOException e) {
            System.err.println(DEMO_LOG_PREFIX + "failed to delete file: " + file.getName());
        }
    }
    
    private void cleanupProcessedMarkers(File originalFile, File outboxDir) {
        File[] markers = outboxDir.listFiles(f -> 
            f.getName().startsWith(originalFile.getName() + ".processed-"));
        if (markers != null) {
            for (File marker : markers) {
                try {
                    Files.delete(marker.toPath());
                } catch (IOException e) {
                    System.err.println(DEMO_LOG_PREFIX + "failed to delete marker: " + marker.getName());
                }
            }
        }
    }
      public void shutdown() {
        isRunning = false;
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        System.out.println(DEMO_LOG_PREFIX + deviceName + "] sync manager shut down");
    }
    
    public boolean isConnected() {
        return isRunning;
    }
}
