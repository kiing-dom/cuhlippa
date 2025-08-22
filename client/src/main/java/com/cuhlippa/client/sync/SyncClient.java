package com.cuhlippa.client.sync;

import com.cuhlippa.client.sync.dto.ClipboardItemDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SyncClient extends WebSocketClient {
    private final ObjectMapper objectMapper;
    private final SyncMessageListener messageListener;
    private final ScheduledExecutorService scheduler;
    private final int reconnectDelay;
    private volatile boolean shouldReconnect = true;
    
    public interface SyncMessageListener {
        void onItemReceived(ClipboardItemDTO item);
        void onConnected();
        void onDisconnected();
        void onError(String error);
    }

    public SyncClient(URI serverUri, SyncMessageListener listener, int reconnectDelay) {
        super(serverUri);
        this.messageListener = listener;
        this.reconnectDelay = reconnectDelay;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("Connected to sync server");
        messageListener.onConnected();
    }

    @Override
    public void onMessage(String message) {
        try {
            ClipboardItemDTO item = objectMapper.readValue(message, ClipboardItemDTO.class);
            messageListener.onItemReceived(item);
        } catch (Exception e) {
            System.err.println("Failed to parse sync message: " + e.getMessage());
            messageListener.onError("Failed to parse message: " + e.getMessage());
        }
    }    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Disconnected from sync server: " + reason);
        messageListener.onDisconnected();

        if (shouldReconnect && remote) {
            scheduleReconnect();
        }
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("Sync client error: " + ex.getMessage());
        messageListener.onError(ex.getMessage());
    }

    public CompletableFuture<Void> sendItem(ClipboardItemDTO item) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (isOpen()) {
                    String json = objectMapper.writeValueAsString(item);
                    send(json);
                } else {
                    throw new WebSocketConnectionException("Websocket connection is not open");
                }
            } catch (Exception e) {
                throw new SendClipboardItemException("Failed to send clipboard item", e);
            }
        });
    }

    private void scheduleReconnect() {
        scheduler.schedule(() -> {
            if (shouldReconnect && !isOpen()) {
                System.out.println("Attempting to reconnect to sync server...");
                try {
                    reconnect();
                } catch (Exception e) {
                    System.err.println("Reconnection failed: " + e.getMessage());
                    scheduleReconnect();
                }
            }
        }, reconnectDelay, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        shouldReconnect = false;
        scheduler.shutdown();
        close();
    }
}
