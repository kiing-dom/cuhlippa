package com.cuhlippa.client.discovery;

import com.cuhlippa.shared.discovery.DiscoveryMessage;

public class DiscoveredServer {
    private final String serverId;
    private String serverName;
    private String serverIP;
    private int serverPort;
    private int connectedDevices;
    private long lastSeen;
    private boolean isOnline;
    
    public DiscoveredServer(String serverId, String serverName, String serverIP, 
                           int serverPort, int connectedDevices) {
        this.serverId = serverId;
        this.serverName = serverName;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.connectedDevices = connectedDevices;
        this.lastSeen = System.currentTimeMillis();
        this.isOnline = true;
    }
    
    public void updateFromAdvertisement(DiscoveryMessage message) {
        this.serverName = message.getServerName();
        this.serverIP = message.getServerIP();
        this.serverPort = message.getServerPort();
        this.connectedDevices = message.getConnectedDevices();
        this.lastSeen = System.currentTimeMillis();
        this.isOnline = true;
    }
    
    public String getWebSocketUrl() {
        return "ws://" + serverIP + ":" + serverPort + "/sync";
    }
    
    public long getTimeSinceLastSeen() {
        return System.currentTimeMillis() - lastSeen;
    }
    
    // Getters
    public String getServerId() { return serverId; }
    public String getServerName() { return serverName; }
    public String getServerIP() { return serverIP; }
    public int getServerPort() { return serverPort; }
    public int getConnectedDevices() { return connectedDevices; }
    public long getLastSeen() { return lastSeen; }
    public boolean isOnline() { return isOnline; }
    
    // Setters
    public void setOnline(boolean online) { this.isOnline = online; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscoveredServer that = (DiscoveredServer) o;
        return serverId.equals(that.serverId);
    }
    
    @Override
    public int hashCode() {
        return serverId.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s:%d) - %d devices", 
                           serverName, serverIP, serverPort, connectedDevices);
    }
}
