package com.cuhlippa.shared.discovery;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class NetworkDiscoveryProtocol {

    private NetworkDiscoveryProtocol() {
        // utility class
    }    /**
     * create a multicast socket for discovery
     */
    public static MulticastSocket createDiscoverySocket() throws IOException {
        MulticastSocket socket = new MulticastSocket(DiscoveryConstants.DISCOVERY_PORT);
        socket.setSoTimeout(DiscoveryConstants.SOCKET_TIMEOUT_MS);
        socket.setReuseAddress(true);

        // Join multicast group
        InetAddress group = InetAddress.getByName(DiscoveryConstants.MULTICAST_GROUP);
        InetSocketAddress groupAddress = new InetSocketAddress(group, DiscoveryConstants.DISCOVERY_PORT);
        socket.joinGroup(groupAddress, null); // null means use default network interface

        return socket;
    }/**
     * create a datagram packet for sending discovery messages
     */
    public static DatagramPacket createDiscoveryPacket(DiscoveryMessage message) throws IOException {
        try {
            byte[] data = message.toBytes();
            InetAddress group = InetAddress.getByName(DiscoveryConstants.MULTICAST_GROUP);
            return new DatagramPacket(data, data.length, group, DiscoveryConstants.DISCOVERY_PORT);
        } catch (Exception e) {
            throw new IOException("Failed to create discovery packet: " + e.getMessage(), e);
        }
    }

    /**
     * create a receive packet for discovery messages
     */
    public static DatagramPacket createReceivePacket() {
        byte[] buffer = new byte[DiscoveryConstants.BUFFER_SIZE];
        return new DatagramPacket(buffer, buffer.length);
    }
      /**
     * Parse a received discovery message
     */
    public static DiscoveryMessage parseReceivedMessage(DatagramPacket packet) throws IOException {
        byte[] data = new byte[packet.getLength()];
        System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());
        return DiscoveryMessage.fromBytes(data);
    }

    /**
     * get all available network interfaces suitable for discovery
     */
    public static List<NetworkInterface> getDiscoveryNetworkInterfaces() {
        List<NetworkInterface> interfaces = new ArrayList<>();

        try {
            Enumeration<NetworkInterface> allInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allInterfaces.nextElement();

                if (isValidDiscoveryInterface(netInterface)) {
                    interfaces.add(netInterface);
                }
            }
        } catch (SocketException e) {
            System.err.println("Failed to enumerate network interfaces: " + e.getMessage());
        }

        interfaces.sort((a, b) -> {
            int scoreA = getInterfacePreferenceScore(a);
            int scoreB = getInterfacePreferenceScore(b);
            return Integer.compare(scoreB, scoreA);
        });

        return interfaces;
    }

    public static boolean isValidDiscoveryInterface(NetworkInterface netInterface) {
        try {
            return netInterface.isUp() &&
                    !netInterface.isLoopback() &&
                    netInterface.supportsMulticast() &&
                    !netInterface.isVirtual();
        } catch (SocketException e) {
            return false;
        }
    }

    /**
     * Get preference score for network interface selection
     */
    private static int getInterfacePreferenceScore(NetworkInterface netInterface) {
        String name = netInterface.getName().toLowerCase();

        for (int i = 0; i < DiscoveryConstants.PREFERRED_INTERFACE_NAMES.length; i++) {
            if (name.startsWith(DiscoveryConstants.PREFERRED_INTERFACE_NAMES[i])) {
                return DiscoveryConstants.PREFERRED_INTERFACE_NAMES.length - i;
            }
        }

        return 0;
    }

    /**
     * Get the best IP address from a network instance
     */
    public static InetAddress getBestIPAddress(NetworkInterface netInterface) {
        Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
        InetAddress bestAddress = null;

        while (addresses.hasMoreElements()) {
            InetAddress addr = addresses.nextElement();

            // prefer IPv4 addresses
            if (addr instanceof Inet4Address &&
                    !addr.isLoopbackAddress() &&
                    !addr.isLinkLocalAddress()) {

                // prefer private network addresses
                if (addr.isSiteLocalAddress()) {
                    return addr;
                } else if (bestAddress == null) {
                    bestAddress = addr;
                }
            }
        }

        return bestAddress;
    }

    /**
     * test if multicast is supported on the current network
     */
    public static boolean isMulticastSupported() {
        try (MulticastSocket testSocket = new MulticastSocket()) {
            InetAddress group = InetAddress.getByName(DiscoveryConstants.MULTICAST_GROUP);
            SocketAddress groupAddress = new InetSocketAddress(group, DiscoveryConstants.DISCOVERY_PORT);

            NetworkInterface netInterface = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            if (netInterface == null) {
                for (NetworkInterface ni : java.util.Collections.list(NetworkInterface.getNetworkInterfaces())) {
                    if (ni.isUp() && !ni.isLoopback()) {
                        netInterface = ni;
                        break;
                    }
                }
            }

            if (netInterface == null) {
                return false;
            }
            testSocket.joinGroup(groupAddress, netInterface);
            testSocket.leaveGroup(groupAddress, netInterface);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * generate network scan ranges for fallback discovery
     */
    public static List<String> generateScanRanges() {
        List<String> ranges = new ArrayList<>();

        List<NetworkInterface> interfaces = getDiscoveryNetworkInterfaces();
        for (NetworkInterface netInterface : interfaces) {
            InetAddress addr = getBestIPAddress(netInterface);
            if (addr != null) {
                String ip = addr.getHostAddress();
                String networkPrefix = getNetworkPrefix(ip);
                if (networkPrefix != null && !ranges.contains(networkPrefix)) {
                    ranges.add(networkPrefix);
                }
            }
        }

        return ranges;
    }

    /**
     * extract network prefix from IP address (e.g., "192.168.1.100" -> "192.168.1.")
     */
    private static String getNetworkPrefix(String ip) {
        int lastDot = ip.lastIndexOf('.');
        if (lastDot > 0) {
            return ip.substring(0, lastDot + 1);
        }

        return null;
    }
}
