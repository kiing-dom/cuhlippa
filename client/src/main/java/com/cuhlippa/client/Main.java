package com.cuhlippa.client;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.cuhlippa.client.clipboard.ClipboardManager;
import com.cuhlippa.client.clipboard.DemoClipboardManager;
import com.cuhlippa.client.config.Settings;
import com.cuhlippa.client.config.SettingsManager;
import com.cuhlippa.client.storage.LocalDatabase;
import com.cuhlippa.client.sync.SyncManager;
import com.cuhlippa.client.sync.DemoSyncManager;
import com.cuhlippa.ui.ClipboardUI;

public class Main {
    private static volatile boolean running = true;
    private static boolean demoMode = false;
    private static String demoDeviceName = null;

    private Main() {
        // Private constructor to prevent instantiation
    }

    public static void main(String[] args) {
        // Parse command line arguments
        parseArguments(args);
        
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }        SettingsManager.loadSettings();
        Settings settings = SettingsManager.getSettings();
        LocalDatabase db = new LocalDatabase(demoMode); // Pass demo mode flag to database// Create appropriate clipboard manager based on mode
        if (demoMode) {
            DemoClipboardManager cm = new DemoClipboardManager(db, settings, demoDeviceName);
            System.out.println("🎬 DEMO MODE: Starting as '" + demoDeviceName + "'");
            
            ClipboardUI ui = new ClipboardUI(db, settings);            ui.setDemoMode(true, demoDeviceName);
            ui.setDemoClipboardManager(cm); // Pass the demo clipboard manager to UI
            
            DemoSyncManager demoSyncManager = new DemoSyncManager(db, demoDeviceName);
            
            cm.addClipboardListener(ui);
            cm.addClipboardListener(demoSyncManager);
            demoSyncManager.addClipboardListener(ui);
              demoSyncManager.initialize();
            System.out.println("Starting demo clipboard listener...");
            cm.startListening();
            
            SwingUtilities.invokeLater(() -> ui.setVisible(true));
            
            System.out.println("Listening to clipboard. Press Ctrl+C to exit");
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down...");
                demoSyncManager.shutdown();
                running = false;
            }));
            
            try {
                while (running) {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("Exiting...");
                Thread.currentThread().interrupt();
            }
            
        } else {
            ClipboardManager cm = new ClipboardManager(db, settings);
            
            ClipboardUI ui = new ClipboardUI(db, settings);
            SyncManager syncManager = new SyncManager(db, settings);
            
            cm.addClipboardListener(ui);
            cm.addClipboardListener(syncManager);
            syncManager.addClipboardListener(ui);
            
            syncManager.initialize();
            System.out.println("Starting clipboard listener...");
            cm.startListening();
            
            SwingUtilities.invokeLater(() -> ui.setVisible(true));
            
            System.out.println("Listening to clipboard. Press Ctrl+C to exit");
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down...");
                syncManager.shutdown();
                running = false;
            }));
            
            try {
                while (running) {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("Exiting...");
                Thread.currentThread().interrupt();
            }
        }
    }    private static void parseArguments(String[] args) {
        int i = 0;
        while (i < args.length) {
            String arg = args[i];
            
            if (arg.contains("=")) {
                i += handleEqualsFormatArgument(arg);
            } else {
                i += handleSpaceSeparatedArgument(args, i);
            }
        }
        
        // Set default device name if demo mode is enabled but no name provided
        if (demoMode && demoDeviceName == null) {
            demoDeviceName = "Demo-Client-" + System.currentTimeMillis() % 1000;
        }
    }
    
    private static int handleEqualsFormatArgument(String arg) {
        String[] parts = arg.split("=", 2);
        String key = parts[0];
        String value = parts.length > 1 ? parts[1] : "";
        
        if ("--device-name".equals(key)) {
            if (value.isEmpty()) {
                System.err.println("Error: --device-name requires a value");
                System.exit(1);
            }
            demoDeviceName = value;
        } else {
            System.err.println("Unknown argument: " + key);
            printUsage();
            System.exit(1);        }
        return 1;
    }
    
    private static int handleSpaceSeparatedArgument(String[] args, int currentIndex) {
        String arg = args[currentIndex];
        
        if ("--demo-mode".equals(arg)) {
            demoMode = true;
            return 1;
        } else if ("--device-name".equals(arg)) {
            if (currentIndex + 1 < args.length) {
                demoDeviceName = args[currentIndex + 1];
                return 2; // Skip both current and next argument
            } else {
                System.err.println("Error: --device-name requires a value");
                System.exit(1);
            }
        } else if ("--help".equals(arg)) {
            printUsage();
            System.exit(0);
        } else if (arg.startsWith("--")) {
            System.err.println("Unknown argument: " + arg);
            printUsage();
            System.exit(1);
        }
        return 1;
    }
      private static void printUsage() {
        System.out.println("Cuhlippa Clipboard Sync Client");
        System.out.println("Usage: java -jar cuhlippa-client.jar [OPTIONS]");
        System.out.println("Options:");
        System.out.println("  --demo-mode           Enable demo mode with virtual clipboard isolation");
        System.out.println("  --device-name <name>  Set device name for demo mode (e.g., 'Laptop-Demo')");
        System.out.println("  --help                Show this help message");
        System.out.println("");
        System.out.println("Examples:");
        System.out.println("  java -jar cuhlippa-client.jar --demo-mode --device-name \"Laptop-Demo\"");
        System.out.println("  java -jar cuhlippa-client.jar --demo-mode --device-name=Desktop-Demo");
        System.out.println("");
        System.out.println("Note: Both formats are supported:");
        System.out.println("  --device-name \"Device Name\"  (space-separated)");
        System.out.println("  --device-name=DeviceName     (equals format)");
    }
}
