package com.cuhlippa.client.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Manages device identification for sync purposes
 */
public class DeviceManager {
    private static final String DEVICE_ID_FILE = "config/device_id.txt";
    private static String cachedDeviceId;

    private DeviceManager() {
        // Utility class
    }

    /**
     * Get the unique device ID for this installation
     */
    public static String getDeviceId() {
        if (cachedDeviceId != null) return cachedDeviceId;
        
        File deviceFile = new File(DEVICE_ID_FILE);
        if (deviceFile.exists()) {
            try {
                cachedDeviceId = Files.readString(Paths.get(DEVICE_ID_FILE)).trim();
                if (!cachedDeviceId.isEmpty()) return cachedDeviceId;
            } catch (IOException e) {
                System.out.println("Failed to read device ID: " + e.getMessage());
            }
        }

        cachedDeviceId = "device-" + UUID.randomUUID().toString().substring(0, 8);
        saveDeviceId(cachedDeviceId);
        return cachedDeviceId;
    }

    /**
     * Save device ID to file
     */
    private static void saveDeviceId(String deviceId) {
        try {
            File configDir = new File("config");
            configDir.mkdirs();
            Files.writeString(Paths.get(DEVICE_ID_FILE), deviceId);
            System.out.println("Generated device ID: " + deviceId);
        } catch (IOException e) {
            System.out.println("Failed to save device ID: " + e.getMessage());
        }
    }
}
