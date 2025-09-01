package com.cuhlippa.shared.discovery;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class NetworkDiscoveryProtocol {

    private NetworkDiscoveryProtocol(){
        // utility class
    }

    /**
     * create a multicast socket for discovery
     */
    public static MulticastSocket createDiscoverySocket() throws IOException {
        MulticastSocket socket = new MulticastSocket(DiscoveryConstants.DISCOVERY_PORT);
        socket.setSoTimeout(DiscoveryConstants.SOCKET_TIMEOUT_MS);
        socket.setReuseAddress(true);

        InetAddress group = InetAddress.getByName(DiscoveryConstants.MULTICAST_GROUP);
        InetSocketAddress groupAddress = new InetSocketAddress(group, DiscoveryConstants.DISCOVERY_PORT);
        NetworkInterface netIf = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());

        socket.joinGroup(groupAddress, netIf);

        return socket;
    }
}

