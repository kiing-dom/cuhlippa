package com.cuhlippa.client.sync;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.ArrayList;

import com.cuhlippa.client.clipboard.ClipboardItem;
import com.cuhlippa.client.clipboard.ClipboardListener;
import com.cuhlippa.client.config.DeviceManager;
import com.cuhlippa.client.config.Settings;
import com.cuhlippa.client.storage.LocalDatabase;
import com.cuhlippa.client.sync.dto.ClipboardItemDTO;

public class SyncManager implements ClipboardListener, SyncClient.SyncMessageListener {
    private final LocalDatabase db;
    private final Settings settings;
    private final String deviceId;
    private SyncClient syncClient;
    private boolean isInitialized = false;
    private final List<ClipboardListener> listeners = new ArrayList<>();

    public SyncManager(LocalDatabase db, Settings settings) {
        this.db = db;
        this.settings = settings;
        this.deviceId = DeviceManager.getDeviceId();
    }

    public void addClipboardListener(ClipboardListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners(ClipboardItem item) {
        for (ClipboardListener listener : listeners) {
            listener.onClipboardItemAdded(item);
        }
    }

    public void initialize() {
        if (!settings.getSync().isEnabled() || isInitialized)
            return;

        try {
            URI serverUri = new URI(settings.getSync().getServerAddress());
            syncClient = new SyncClient(serverUri, this, 5000);
            CompletableFuture.runAsync(() -> syncClient.connect());
            isInitialized = true;
        } catch (Exception e) {
            System.out.println("Failed to initialize sync: " + e.getMessage());
        }
    }    @Override
    public void onItemReceived(ClipboardItemDTO dto) {
        try {
            if (deviceId.equals(dto.getDeviceId())) return;
            if (!settings.getSync().getEncryptionKey().isEmpty()) {
                String decrypted = EncryptionService.decrypt(dto.getContent(), settings.getSync().getEncryptionKey());
                dto.setContent(decrypted);
            }

            ClipboardItem item = dto.toClipboardItem();
            if (!db.itemExistsByHash(item.getHash())) {
                db.saveItem(item);
                System.out.println("Saved sync item from: " + dto.getDeviceId());
                
                // Notify UI and other listeners that a new item was received
                notifyListeners(item);
            }
        } catch (Exception e) {
            System.err.println("Failed to process sync item: " + e.getMessage());
        }
    }

    @Override
    public void onConnected() {
        System.out.println("Sync connected");
    }

    @Override
    public void onDisconnected() {
        System.out.println("Sync disconnected");
    }

    @Override
    public void onError(String error) {
        System.err.println("Sync error: " + error);
    }

    @Override
    public void onClipboardItemAdded(ClipboardItem item) {
        if (!isInitialized || syncClient == null || !syncClient.isOpen())
            return;

        ClipboardItemDTO dto = ClipboardItemDTO.fromClipboardItem(item, deviceId);
        if (!settings.getSync().getEncryptionKey().isEmpty()) {
            String encrypted = EncryptionService.encrypt(dto.getContent(), settings.getSync().getEncryptionKey());
            dto.setContent(encrypted);
        }

        syncClient.sendItem(dto)
            .thenRun(() -> System.out.println("Sent to sync server"))
            .exceptionally(t -> {
            System.err.println("Sync send failed: " + t);
            return null;
        });
    }

    public void shutdown() {
        if (syncClient != null) {
            syncClient.shutdown();
            isInitialized = false;
        }
    }

    public boolean isConnected() {
        return syncClient != null && syncClient.isOpen();
    }
}
