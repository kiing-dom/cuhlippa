package com.cuhlippa.shared.config;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Utility for network-related operations
 */
public class NetworkUtils {
    
    private NetworkUtils() {
        // Utility class
    }

    /**
     * Get the local network IP address (not localhost)
     */
    public static String getLocalNetworkIP() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                
                // Skip loopback and inactive interfaces
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    
                    // We want IPv4 addresses that are not loopback
                    if (!addr.isLoopbackAddress() && 
                        !addr.isLinkLocalAddress() && 
                        addr.getAddress().length == 4) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            System.err.println("Failed to get network IP: " + e.getMessage());
        }
        
        return "localhost"; // Fallback
    }

    /**
     * Build WebSocket URL with auto-detected IP
     */
    public static String buildDefaultSyncUrl() {
        String ip = getLocalNetworkIP();
        return "ws://" + ip + ":8080/sync";
    }    /**
     * Test if a server is reachable on a specific port
     */
    public static boolean isServerReachable(String host, int port) {
        try (java.net.Socket socket = new java.net.Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, port), 3000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}