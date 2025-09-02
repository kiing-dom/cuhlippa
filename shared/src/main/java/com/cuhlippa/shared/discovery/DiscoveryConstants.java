package com.cuhlippa.shared.discovery;

public class DiscoveryConstants {
    public static final String MULTICAST_GROUP = "224.0.0.251";
    public static final int DISCOVERY_PORT = 8081;
    public static final int BUFFER_SIZE = 1024;

    public static final int SERVER_ADVERTISEMENT_INTERVAL_MS = 5000;
    public static final int CLIENT_DISCOVERY_TIMEOUT_MS = 10000;
    public static final int SERVER_TIMEOUT_MS = 15000;
    public static final int SOCKET_TIMEOUT_MS = 3000;

    public static final String PROTOCOL_VERSION = "1.0";
    public static final String DISCOVERY_PREFIX = "CUHLIPPA_DISCOVERY";
    public static final int MAX_MESSAGE_SIZE = 512;

    public static final String DEFAULT_SERVER_NAME = "Cuhlippa Server";
    public static final int DEFAULT_WEBSOCKET_PORT = 8080;

    protected static final String[] PREFERRED_INTERFACE_NAMES = {
        "eth", "en", "lan", "wi-fi", "ethernet"
    };

    public static final String[] COMMON_NETWORK_PREFIXES = {
        "192.168.", "10.", "172.16.", "172.17.", "172.18.", "172.19.",
        "172.20.", "172.21.", "172.22.", "172.23.", "172.24.", "172.25.",
        "172.26.", "172.27.", "172.28.", "172.29.", "172.30.", "172.31."
    };

    public static final int[] COMMON_PORTS = {8080, 8081, 8082, 9090};

    private DiscoveryConstants() {
        // utility class to prevent instantiation
    }
}
