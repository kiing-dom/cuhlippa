package com.cuhlippa.server;

import com.cuhlippa.shared.config.NetworkUtils;
import com.cuhlippa.server.discovery.ServerAdvertisementService;
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
    
    private final ServerAdvertisementService advertisementService;
    
    public SyncServerApplication(ServerAdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }
    
    public static void main(String[] args) {
        SpringApplication.run(SyncServerApplication.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        String localIP = NetworkUtils.getLocalNetworkIP();
        
        // Start network discovery advertisement
        advertisementService.startAdvertising();
        
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
        System.out.println("🔍 Network Discovery:");
        System.out.println("   ├─ Broadcasting on: " + com.cuhlippa.shared.discovery.DiscoveryConstants.MULTICAST_GROUP + ":" + com.cuhlippa.shared.discovery.DiscoveryConstants.DISCOVERY_PORT);
        System.out.println("   └─ Clients can now auto-discover this server");
        System.out.println();
        System.out.println("📋 Client Setup:");
        System.out.println("   1. Open Cuhlippa client settings");
        System.out.println("   2. Enable sync and click 'Discover Devices'");
        System.out.println("   3. Select this server from the discovery list");
        System.out.println("=".repeat(50) + "\n");
    }
}
