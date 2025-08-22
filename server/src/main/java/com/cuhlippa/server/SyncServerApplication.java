package com.cuhlippa.server;

import com.cuhlippa.shared.config.NetworkUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SyncServerApplication implements CommandLineRunner {
    
    @Value("${server.port:8080}")
    private String serverPort;
    
    @Value("${server.address:0.0.0.0}")
    private String serverAddress;
    
    public static void main(String[] args) {
        SpringApplication.run(SyncServerApplication.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        String localIP = NetworkUtils.getLocalNetworkIP();
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🚀 Cuhlippa Sync Server Started Successfully!");
        System.out.println("=".repeat(50));
        System.out.println("📡 Server Configuration:");
        System.out.println("   ├─ Bind Address: " + serverAddress);
        System.out.println("   ├─ Port: " + serverPort);
        System.out.println("   └─ WebSocket Path: /sync");
        System.out.println();
        System.out.println("🌐 Connection URLs:");
        System.out.println("   ├─ Local: ws://localhost:" + serverPort + "/sync");
        if (!"localhost".equals(localIP)) {
            System.out.println("   └─ Network: ws://" + localIP + ":" + serverPort + "/sync");
        }
        System.out.println();
        System.out.println("📋 Client Setup:");
        System.out.println("   1. Open Cuhlippa client settings");
        System.out.println("   2. Enable sync and set server address");
        System.out.println("   3. Use 'Auto-detect' button for easy setup");
        System.out.println("=".repeat(50) + "\n");
    }
}
