package com.cuhlippa.shared.discovery;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Objects;

public class DiscoveryMessage {
    public enum MessageType {
        SERVER_ADVERTISEMENT,
        CLIENT_DISCOVERY_REQUEST
    }

    private final MessageType type;
    private final String serverName;
    private final String serverIP;
    private final int serverPort;
    private final String deviceId;
    private final int connectedDevices;
    private final LocalDateTime timestamp;
    private final String version;

    public DiscoveryMessage(String serverName, String serverIP, int serverPort, String deviceId, int connectedDevices) {
        this.type = MessageType.SERVER_ADVERTISEMENT;
        this.serverName = serverName;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.deviceId = deviceId;
        this.connectedDevices = connectedDevices;
        this.timestamp = LocalDateTime.now();
        this.version = "1.0";
    }

    public DiscoveryMessage(String deviceId) {
        this.type = MessageType.CLIENT_DISCOVERY_REQUEST;
        this.serverName = null;
        this.serverIP = null;
        this.serverPort = 0;
        this.deviceId = deviceId;
        this.connectedDevices = 0;
        this.timestamp = LocalDateTime.now();
        this.version = "1.0";
    }

    @JsonCreator
    public DiscoveryMessage(@JsonProperty("type") MessageType type,
            @JsonProperty("serverName") String serverName,
            @JsonProperty("serverIP") String serverIP,
            @JsonProperty("serverPort") int serverPort,
            @JsonProperty("deviceId") String deviceId,
            @JsonProperty("connectedDevices") int connectedDevices,
            @JsonProperty("timestamp") LocalDateTime timestamp,
            @JsonProperty("version") String version) {
        this.type = type;
        this.serverName = serverName;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.deviceId = deviceId;
        this.connectedDevices = connectedDevices;
        this.timestamp = timestamp;
        this.version = version;
    }

    public MessageType getType() {
        return type;
    }

    public String getServerName() {
        return serverName;
    }

    public String getServerIP() {
        return serverIP;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public int getConnectedDevices() {
        return connectedDevices;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getVersion() {
        return version;
    }

    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        return mapper.writeValueAsString(this);
    }

    public static DiscoveryMessage fromJson(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        return mapper.readValue(json, DiscoveryMessage.class);
    }

    public byte[] toBytes() throws JsonProcessingException {
        return toJson().getBytes();
    }

    public static DiscoveryMessage fromBytes(byte[] data) throws JsonProcessingException {
        return fromJson(new String(data));
    }

    // Utility Methods
    public boolean isServerAdvertisement() {
        return type == MessageType.SERVER_ADVERTISEMENT;
    }

    public boolean isDiscoveryRequest() {
        return type == MessageType.CLIENT_DISCOVERY_REQUEST;
    }

    public String getWebSocketUrl() {
        if (serverIP != null && serverPort > 0) {
            return "ws://" + serverIP + ":" + serverPort + "/sync";
        }

        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DiscoveryMessage that = (DiscoveryMessage) o;
        return serverPort == that.serverPort &&
                connectedDevices == that.connectedDevices &&
                type == that.type &&
                Objects.equals(serverName, that.serverName) &&
                Objects.equals(serverIP, that.serverIP) &&
                Objects.equals(deviceId, that.deviceId) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, serverName, serverIP, serverPort, deviceId, connectedDevices, version);
    }

    @Override
    public String toString() {
        return "DiscoveryMessage{" +
                "type=" + type +
                ", serverName='" + serverName + '\'' +
                ", serverIP='" + serverIP + '\'' +
                ", serverPort=" + serverPort +
                ", deviceId='" + deviceId + '\'' +
                ", connectedDevices=" + connectedDevices +
                ", timestamp=" + timestamp +
                ", version='" + version + '\'' +
                '}';
    }
}