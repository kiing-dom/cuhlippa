package com.cuhlippa.client;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.cuhlippa.client.clipboard.ClipboardManager;
import com.cuhlippa.client.config.Settings;
import com.cuhlippa.client.config.SettingsManager;
import com.cuhlippa.client.storage.LocalDatabase;
import com.cuhlippa.ui.ClipboardUI;

public class Main {
    private static volatile boolean running = true;
    
    private Main() {
        // Private constructor to prevent instantiation
    }
      public static void main(String[] args) {        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SettingsManager.loadSettings();
        Settings settings = SettingsManager.getSettings();

        LocalDatabase db = new LocalDatabase();
        ClipboardManager cm = new ClipboardManager(db, settings);
        ClipboardUI ui = new ClipboardUI(db, settings);

        cm.addClipboardListener(ui);
        System.out.println("Starting clipboard listener...");
        cm.startListening();

        SwingUtilities.invokeLater(() -> ui.setVisible(true));

        System.out.println("Listening to clipboard. Press Ctrl+C to exit");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
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

        System.out.println("Clipboard listener stopped");
    }    
}
