package com.cuhlippa.client.clipboard;

import com.cuhlippa.client.storage.LocalDatabase;
import com.cuhlippa.client.config.Settings;
import com.cuhlippa.client.exception.ClipboardHashingException;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Demo version of ClipboardManager that provides virtual clipboard isolation
 * for demonstrating multi-device sync without sharing the same system clipboard.
 */
public class DemoClipboardManager implements ClipboardOwner, IClipboardManager {
    private final LocalDatabase db;
    private final Settings settings;
    private final String deviceName;
    private final List<ClipboardListener> listeners = new ArrayList<>();
    private static final String CATEGORY_GENERAL = "General";
    private static final String DEMO_LOG_PREFIX = "🎬 Demo [";
    
    // Virtual clipboard for demo mode - isolated per device
    private String virtualClipboardText = "";
    private BufferedImage virtualClipboardImage = null;
    private List<File> virtualClipboardFiles = new ArrayList<>();
    private String lastProcessedHash = null;

    /**
     * Constructor for DemoClipboardManager
     */
    public DemoClipboardManager(LocalDatabase db, Settings settings, String deviceName) {
        this.db = db;
        this.settings = settings;
        this.deviceName = deviceName != null ? deviceName : "Demo-Device";
        System.out.println("🎬 Demo ClipboardManager initialized for device: " + this.deviceName);
    }

    /**
     * Start the demo clipboard manager (no system clipboard monitoring)
     */
    public void startListening() {
        System.out.println("🎬 Demo clipboard manager started for: " + deviceName);
        System.out.println("🎬 Virtual clipboard mode active - use demo controls to simulate clipboard operations");
    }

    /**
     * Add a clipboard listener (same interface as regular ClipboardManager)
     */
    public void addClipboardListener(ClipboardListener listener) {
        listeners.add(listener);
        System.out.println("🎬 Demo: Added clipboard listener for " + deviceName);
    }

    /**
     * Notify all listeners about a new clipboard item
     */
    public void notifyListeners(ClipboardItem item) {
        for (ClipboardListener listener : listeners) {
            listener.onClipboardItemAdded(item);
        }
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        // In demo mode, we don't monitor system clipboard changes
        // Instead, clipboard content is manually added via copyToVirtualClipboard()
    }

    /**
     * Manually copy text to the virtual clipboard (simulates user copying text)
     */
    public void copyTextToVirtualClipboard(String text) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        
        virtualClipboardText = text;
        virtualClipboardImage = null;
        virtualClipboardFiles.clear();
        
        processVirtualClipboardContent();
    }

    /**
     * Manually copy image to the virtual clipboard (simulates user copying image)
     */
    public void copyImageToVirtualClipboard(BufferedImage image) {
        if (image == null) {
            return;
        }
        
        virtualClipboardText = "";
        virtualClipboardImage = image;
        virtualClipboardFiles.clear();
        
        processVirtualClipboardContent();
    }

    /**
     * Manually copy files to the virtual clipboard (simulates user copying files)
     */
    public void copyFilesToVirtualClipboard(List<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        
        virtualClipboardText = "";
        virtualClipboardImage = null;
        virtualClipboardFiles = new ArrayList<>(files);
        
        processVirtualClipboardContent();
    }

    /**
     * Process virtual clipboard content and create ClipboardItem
     */
    private void processVirtualClipboardContent() {
        try {
            ClipboardItem item = null;
            
            // Process text content
            if (!virtualClipboardText.isEmpty()) {
                if (shouldIgnoreContent(virtualClipboardText))
                    return;

                byte[] contentBytes = virtualClipboardText.getBytes();
                String hash = sha256(contentBytes);
                
                if (!hash.equals(lastProcessedHash)) {
                    lastProcessedHash = hash;
                    item = new ClipboardItem(ItemType.TEXT, contentBytes, LocalDateTime.now(), hash,
                            new HashSet<>(), CATEGORY_GENERAL, false);
                    System.out.println(DEMO_LOG_PREFIX + deviceName + "]: Virtual text copied: " + virtualClipboardText);
                }
            }
            // Process image content
            else if (virtualClipboardImage != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(virtualClipboardImage, "png", baos);
                byte[] contentBytes = baos.toByteArray();
                String hash = sha256(contentBytes);
                
                if (!hash.equals(lastProcessedHash)) {
                    lastProcessedHash = hash;
                    item = new ClipboardItem(ItemType.IMAGE, contentBytes, LocalDateTime.now(), hash,
                            new HashSet<>(), CATEGORY_GENERAL, false);
                    System.out.println(DEMO_LOG_PREFIX + deviceName + "]: Virtual image copied");
                }
            }
            // Process file content
            else if (!virtualClipboardFiles.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (File file : virtualClipboardFiles) {
                    sb.append(file.getAbsolutePath()).append("\n");
                }
                byte[] contentBytes = sb.toString().getBytes();
                String hash = sha256(contentBytes);
                
                if (!hash.equals(lastProcessedHash)) {
                    lastProcessedHash = hash;
                    item = new ClipboardItem(ItemType.FILE_PATH, contentBytes, LocalDateTime.now(), hash,
                            new HashSet<>(), CATEGORY_GENERAL, false);
                    System.out.println(DEMO_LOG_PREFIX + deviceName + "]: Virtual files copied: " + virtualClipboardFiles.size() + " files");
                }
            }
            
            // Save and notify if we have a new item
            if (item != null) {
                db.saveItemAndUpdateHistory(item, settings);
                notifyListeners(item);
            }
            
        } catch (Exception e) {
            System.err.println(DEMO_LOG_PREFIX + deviceName + "]: Error processing virtual clipboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generate SHA-256 hash for content (duplicate detection)
     */
    private String sha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();        } catch (Exception e) {
            throw new ClipboardHashingException("Failed to generate SHA-256 hash for clipboard data", e);
        }
    }

    /**
     * Check if content should be ignored based on patterns
     */
    private boolean shouldIgnoreContent(String content) {
        return settings.getIgnorePatterns().stream()
                .map(Pattern::compile)
                .anyMatch(p -> p.matcher(content).find());
    }

    /**
     * Get current device name
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * Get current virtual clipboard text
     */
    public String getVirtualClipboardText() {
        return virtualClipboardText;
    }

    /**
     * Get current virtual clipboard image
     */
    public BufferedImage getVirtualClipboardImage() {
        return virtualClipboardImage;
    }

    /**
     * Get current virtual clipboard files
     */
    public List<File> getVirtualClipboardFiles() {
        return new ArrayList<>(virtualClipboardFiles);
    }

    /**
     * Clear virtual clipboard
     */
    public void clearVirtualClipboard() {
        virtualClipboardText = "";
        virtualClipboardImage = null;
        virtualClipboardFiles.clear();
        System.out.println(DEMO_LOG_PREFIX + deviceName + "]: Virtual clipboard cleared");
    }
}
