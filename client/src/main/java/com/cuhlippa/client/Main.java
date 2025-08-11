package com.cuhlippa.client;

import com.cuhlippa.client.clipboard.ClipboardManager;
import com.cuhlippa.client.storage.LocalDatabase;

public class Main {
    private static volatile boolean running = true;
    
    private Main() {
        // Private constructor to prevent instantiation
    }
    
    public static void main(String[] args) {
        LocalDatabase db = new LocalDatabase();
        ClipboardManager cm = new ClipboardManager(db);

        System.out.println("Starting clipboard listener...");
        cm.startListening();

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
