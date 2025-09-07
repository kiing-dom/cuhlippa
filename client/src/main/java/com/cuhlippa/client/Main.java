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
import com.cuhlippa.ui.welcome.WelcomeDialog;
import com.cuhlippa.ui.setup.SetupWizard;
import com.cuhlippa.ui.utils.FirstRunManager;

public class Main {
    private static volatile boolean running = true;
    private static boolean demoMode = false;
    private static String demoDeviceName = null;
    
    // Version information
    private static final String VERSION = "1.0.0";
    private static final String APP_NAME = "Cuhlippa";

    private Main() {
        // Private constructor to prevent instantiation
    }    public static void main(String[] args) {
        // Display version information
        System.out.println("🚀 " + APP_NAME + " v" + VERSION + " - Enterprise Clipboard Synchronization");
        System.out.println("📋 Real-time clipboard sync with automatic device discovery");
        System.out.println();
        
        // Parse command line arguments
        parseArguments(args);
        
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Failed to set look and feel: " + e.getMessage());
        }
        SettingsManager.loadSettings();
        Settings settings = SettingsManager.getSettings();
        LocalDatabase db = new LocalDatabase(demoMode); // Pass demo mode flag to database// Create appropriate clipboard manager based on mode
        if (demoMode) {
            DemoClipboardManager cm = new DemoClipboardManager(db, settings, demoDeviceName);
            System.out.println("🎬 DEMO MODE: Starting as '" + demoDeviceName + "'");
            
            ClipboardUI ui = new ClipboardUI(db, settings);            
            ui.setDemoMode(true, demoDeviceName);
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
            // Check for first-run and show welcome dialog if needed
            handleFirstRunExperience();
            
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
    }    
    private static void parseArguments(String[] args) {
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
            System.exit(1);        
        }
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
        }        return 1;
    }
      /**
     * Handle the first-run experience by showing welcome dialog and setup wizard
     */
    private static void handleFirstRunExperience() {
        // Check if this is the user's first time running the application
        if (FirstRunManager.isFirstRun()) {
            System.out.println("🎉 First run detected - showing welcome experience");
            
            // Show welcome dialog on the Event Dispatch Thread asynchronously
            SwingUtilities.invokeLater(() -> {
                try {
                    WelcomeDialog welcomeDialog = new WelcomeDialog(null);
                    WelcomeDialog.UserChoice choice = welcomeDialog.showWelcomeDialog();
                      switch (choice) {
                        case GET_STARTED:
                            System.out.println("User chose guided setup");
                            showSetupWizard();
                            break;
                            
                        case ADVANCED_MODE:
                            System.out.println("User chose advanced mode - skipping to main UI");
                            FirstRunManager.markFirstRunCompleted();
                            break;
                            
                        case CANCELLED:
                            System.out.println("User cancelled welcome dialog - continuing to main UI");
                            // Don't mark as completed so they see welcome again next time
                            break;
                    }
                } catch (Exception e) {
                    System.err.println("Error showing welcome dialog: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }
      /**
     * Show the setup wizard to guide users through initial configuration
     */
    private static void showSetupWizard() {
        SwingUtilities.invokeLater(() -> {
            try {
                SetupWizard setupWizard = new SetupWizard(null);
                SetupWizard.SetupResult result = setupWizard.showSetupWizard();
                  switch (result) {
                    case COMPLETED:
                        System.out.println("Setup wizard completed successfully");
                        FirstRunManager.markFirstRunCompleted();
                        break;
                        
                    case SKIPPED_TO_MAIN:
                        System.out.println("User skipped setup wizard");
                        FirstRunManager.markFirstRunCompleted();
                        break;
                        
                    case CANCELLED:
                        System.out.println("Setup wizard cancelled - will show again next time");
                        // Don't mark as completed so they see it again next time
                        break;
                }
            } catch (Exception e) {
                System.err.println("Error showing setup wizard: " + e.getMessage());
                e.printStackTrace();
                // Fallback to marking as completed to avoid infinite loops
                FirstRunManager.markFirstRunCompleted();
            }
        });
    }
    
    private static void printUsage() {
        System.out.println("Cuhlippa Clipboard Sync Client");
        System.out.println("Version: " + VERSION);
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
