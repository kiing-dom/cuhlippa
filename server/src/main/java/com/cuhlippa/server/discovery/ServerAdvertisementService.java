package com.cuhlippa.server.discovery;

import com.cuhlippa.shared.discovery.DiscoveryConstants;
import com.cuhlippa.shared.discovery.DiscoveryMessage;
import com.cuhlippa.shared.discovery.NetworkDiscoveryProtocol;
import com.cuhlippa.shared.config.NetworkUtils;
import com.cuhlippa.server.config.ClipboardSyncHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ServerAdvertisementService {
    
    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${discovery.server.name:Cuhlippa Server}")
    private String serverName;

    private final ClipboardSyncHandler syncHandler;
    private final ScheduledExecutorService scheduler;
    private AtomicBoolean isRunning;
    private MulticastSocket socket;
    private String serverIP;
    private String deviceId;

    public ServerAdvertisementService(ClipboardSyncHandler syncHandler) {
        this.syncHandler = syncHandler;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ServerAdvertisement");
            t.setDaemon(true);
            return t;
        });

        this.isRunning = new AtomicBoolean(false);
    }

    public void startAdvertising() {
        if (isRunning.get()) {
            return;
        }

        try {
            this.serverIP = NetworkUtils.getLocalNetworkIP();
            this.deviceId = generateServerDeviceId();

            this.socket = NetworkDiscoveryProtocol.createDiscoverySocket();
            isRunning.set(true);            scheduler.scheduleWithFixedDelay(
                this::broadcastAdvertisement,
                0,
                DiscoveryConstants.SERVER_ADVERTISEMENT_INTERVAL_MS,
                TimeUnit.MILLISECONDS
            );

            System.out.println("Server advertisement started - broadcasting on " + DiscoveryConstants.MULTICAST_GROUP + ":" + DiscoveryConstants.DISCOVERY_PORT);
        } catch (Exception e) {
            System.err.println("Failed to start server advertisement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopAdvertising() {
        if (!isRunning.get()) {
            return;
        }

        isRunning.set(false);
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }

        System.out.println("server advertisement stopped");
    }

    public void broadcastAdvertisement() {
        if (!isRunning.get()) {
            return;
        }

        try {
            int connectedDevices = syncHandler.getConnectedClientCount();

            DiscoveryMessage advertisement = new DiscoveryMessage(
                serverName,
                serverIP,
                serverPort,
                deviceId,
                connectedDevices
            );

            DatagramPacket packet = NetworkDiscoveryProtocol.createDiscoveryPacket(advertisement);
            socket.send(packet);

            System.out.println("Broadcasted server advertisement: " + serverName + " (" + connectedDevices + " devices connected)");
        } catch (Exception e) {
            if (isRunning.get()) {
                System.err.println("Failed to broadcast advertisement: " + e.getMessage());
            }
        }
    }

    private String generateServerDeviceId() {
        return "server-" + System.currentTimeMillis() % 100000;
    }

    public boolean isAdvertising() {
        return isRunning.get();
    }    public String getServerInfo() {
        return String.format("Server: %s, IP: %s, Port: %d, Devices: %d",
            serverName, serverIP, serverPort, syncHandler.getConnectedClientCount());
    }

    @PreDestroy
    public void shutdown() {
        stopAdvertising();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdown();
            Thread.currentThread().interrupt();
        }
    }
}
