package com.cuhlippa.client.discovery;

import com.cuhlippa.shared.discovery.DiscoveryConstants;
import com.cuhlippa.shared.discovery.DiscoveryMessage;
import com.cuhlippa.shared.discovery.NetworkDiscoveryProtocol;
import com.cuhlippa.client.config.DeviceManager;

import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkDiscoveryService {
    
    private final Map<String, DiscoveredServer> discoveredServers;
    private final List<DiscoveryListener> listeners;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean isDiscovering;
    private MulticastSocket socket;
    private String deviceId;
    
    public NetworkDiscoveryService() {
        this.discoveredServers = new ConcurrentHashMap<>();
        this.listeners = new ArrayList<>();
        this.scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "NetworkDiscovery");
            t.setDaemon(true);
            return t;
        });
        this.isDiscovering = new AtomicBoolean(false);
        this.deviceId = DeviceManager.getDeviceId();
    }
    
    public void startDiscovery() {
        if (isDiscovering.get()) {
            return;
        }
        
        try {
            // Create multicast socket for listening
            this.socket = NetworkDiscoveryProtocol.createDiscoverySocket();
            
            isDiscovering.set(true);
            
            // Start listening for advertisements
            scheduler.submit(this::listenForAdvertisements);
            
            // Schedule periodic cleanup of stale servers
            scheduler.scheduleWithFixedDelay(
                this::cleanupStaleServers,
                DiscoveryConstants.SERVER_TIMEOUT_MS,
                DiscoveryConstants.SERVER_TIMEOUT_MS,
                TimeUnit.MILLISECONDS
            );
            
            // Send initial discovery request
            sendDiscoveryRequest();
            
            System.out.println("🔍 Network discovery started - listening on " + 
                             DiscoveryConstants.MULTICAST_GROUP + ":" + DiscoveryConstants.DISCOVERY_PORT);
            
            notifyDiscoveryStatusChanged(true);
            
        } catch (Exception e) {
            System.err.println("Failed to start network discovery: " + e.getMessage());
            e.printStackTrace();
            isDiscovering.set(false);
        }
    }
    
    public void stopDiscovery() {
        if (!isDiscovering.get()) {
            return;
        }
        
        isDiscovering.set(false);
        
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        
        // Clear discovered servers
        discoveredServers.clear();
        
        System.out.println("🔍 Network discovery stopped");
        notifyDiscoveryStatusChanged(false);
    }
      private void listenForAdvertisements() {
        while (isDiscovering.get()) {
            try {
                DatagramPacket packet = NetworkDiscoveryProtocol.createReceivePacket();
                socket.receive(packet);
                
                DiscoveryMessage message = NetworkDiscoveryProtocol.parseReceivedMessage(packet);
                
                if (message.isServerAdvertisement()) {
                    handleServerAdvertisement(message);
                }
                
            } catch (Exception e) {
                if (isDiscovering.get()) { // Only log if we're supposed to be running
                    System.err.println("Error receiving discovery message: " + e.getMessage());
                }
            }
        }
    }
    
    private void handleServerAdvertisement(DiscoveryMessage message) {
        String serverId = message.getDeviceId();
        
        // Don't discover ourselves (if client and server are on same machine)
        if (serverId.equals(deviceId)) {
            return;
        }
        
        DiscoveredServer existingServer = discoveredServers.get(serverId);
        DiscoveredServer server = new DiscoveredServer(
            serverId,
            message.getServerName(),
            message.getServerIP(),
            message.getServerPort(),
            message.getConnectedDevices()
        );
        
        boolean isNew = existingServer == null;
        discoveredServers.put(serverId, server);
        
        if (isNew) {
            System.out.println("🔍 Discovered new server: " + message.getServerName() + 
                             " (" + message.getServerIP() + ":" + message.getServerPort() + ")");
            notifyServerDiscovered(server);
        } else {
            // Update existing server info
            existingServer.updateFromAdvertisement(message);
        }
    }
    
    private void sendDiscoveryRequest() {
        try {
            DiscoveryMessage request = new DiscoveryMessage(deviceId);
            DatagramPacket packet = NetworkDiscoveryProtocol.createDiscoveryPacket(request);
            socket.send(packet);
            
            System.out.println("🔍 Sent discovery request");
            
        } catch (Exception e) {
            System.err.println("Failed to send discovery request: " + e.getMessage());
        }
    }
    
    private void cleanupStaleServers() {
        long currentTime = System.currentTimeMillis();
        List<String> staleServers = new ArrayList<>();
        
        for (Map.Entry<String, DiscoveredServer> entry : discoveredServers.entrySet()) {
            DiscoveredServer server = entry.getValue();
            if (currentTime - server.getLastSeen() > DiscoveryConstants.SERVER_TIMEOUT_MS) {
                staleServers.add(entry.getKey());
            }
        }
        
        for (String serverId : staleServers) {
            DiscoveredServer server = discoveredServers.remove(serverId);
            if (server != null) {
                System.out.println("🔍 Server lost: " + server.getServerName());
                notifyServerLost(server);
            }
        }
    }
    
    // Public API methods
    public List<DiscoveredServer> getDiscoveredServers() {
        return new ArrayList<>(discoveredServers.values());
    }
    
    public void addDiscoveryListener(DiscoveryListener listener) {
        listeners.add(listener);
    }
    
    public void removeDiscoveryListener(DiscoveryListener listener) {
        listeners.remove(listener);
    }
    
    public boolean isDiscovering() {
        return isDiscovering.get();
    }
    
    public void refreshDiscovery() {
        if (isDiscovering.get()) {
            sendDiscoveryRequest();
        }
    }
    
    // Notification methods
    private void notifyServerDiscovered(DiscoveredServer server) {
        for (DiscoveryListener listener : listeners) {
            try {
                listener.onServerDiscovered(server);
            } catch (Exception e) {
                System.err.println("Error notifying discovery listener: " + e.getMessage());
            }
        }
    }
    
    private void notifyServerLost(DiscoveredServer server) {
        for (DiscoveryListener listener : listeners) {
            try {
                listener.onServerLost(server);
            } catch (Exception e) {
                System.err.println("Error notifying discovery listener: " + e.getMessage());
            }
        }
    }
    
    private void notifyDiscoveryStatusChanged(boolean active) {
        for (DiscoveryListener listener : listeners) {
            try {
                listener.onDiscoveryStatusChanged(active);
            } catch (Exception e) {
                System.err.println("Error notifying discovery listener: " + e.getMessage());
            }
        }
    }
    
    public void shutdown() {
        stopDiscovery();
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
}
