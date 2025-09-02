package com.cuhlippa.client.discovery;

public interface DiscoveryListener {
    
    /**
     * Called when a new server is discovered on the network
     */
    void onServerDiscovered(DiscoveredServer server);
    
    /**
     * Called when a previously discovered server is no longer available
     */
    void onServerLost(DiscoveredServer server);
    
    /**
     * Called when the discovery status changes (started/stopped)
     */
    void onDiscoveryStatusChanged(boolean active);
}
