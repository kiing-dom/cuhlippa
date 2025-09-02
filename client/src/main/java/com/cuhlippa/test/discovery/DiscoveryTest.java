package com.cuhlippa.test.discovery;

import com.cuhlippa.client.discovery.NetworkDiscoveryService;
import com.cuhlippa.client.discovery.DiscoveryListener;
import com.cuhlippa.client.discovery.DiscoveredServer;

/**
 * Simple test to verify network discovery functionality
 */
public class DiscoveryTest implements DiscoveryListener {
    
    public static void main(String[] args) {
        System.out.println("🔍 Testing Network Discovery...");
        DiscoveryTest test = new DiscoveryTest();
        test.runTest();
    }
    
    public void runTest() {
        NetworkDiscoveryService discoveryService = new NetworkDiscoveryService();
        discoveryService.addDiscoveryListener(this);
        
        System.out.println("Starting discovery service...");
        discoveryService.startDiscovery();
        
        // Wait for discoveries
        try {
            System.out.println("Listening for servers for 15 seconds...");
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Stopping discovery service...");
        discoveryService.stopDiscovery();
        
        System.out.println("Discovery test completed!");
        System.exit(0);
    }
    
    @Override
    public void onServerDiscovered(DiscoveredServer server) {
        System.out.println("✅ DISCOVERED SERVER:");
        System.out.println("   Name: " + server.getServerName());
        System.out.println("   IP: " + server.getServerIP());
        System.out.println("   Port: " + server.getServerPort());
        System.out.println("   WebSocket URL: " + server.getWebSocketUrl());
        System.out.println("   Connected Devices: " + server.getConnectedDevices());
        System.out.println();
    }
    
    @Override
    public void onServerLost(DiscoveredServer server) {
        System.out.println("❌ LOST SERVER: " + server.getServerName());
    }
    
    @Override
    public void onDiscoveryStatusChanged(boolean active) {
        System.out.println("🔄 Discovery status changed: " + (active ? "ACTIVE" : "INACTIVE"));
    }
}
