package com.cuhlippa.client.sync;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.ArrayList;

import com.cuhlippa.client.clipboard.ClipboardItem;
import com.cuhlippa.client.clipboard.ClipboardListener;
import com.cuhlippa.client.clipboard.ItemType;
import com.cuhlippa.client.config.DeviceManager;
import com.cuhlippa.client.config.Settings;
import com.cuhlippa.client.storage.LocalDatabase;
import com.cuhlippa.client.sync.dto.ClipboardItemDTO;
import com.cuhlippa.ui.utils.UserFriendlyErrors;

public class SyncManager implements ClipboardListener, SyncClient.SyncMessageListener {
    private final LocalDatabase db;
    private final Settings settings;
    private final String deviceId;
    private SyncClient syncClient;
    private boolean isInitialized = false;
    private final List<ClipboardListener> listeners = new ArrayList<>();
    private volatile boolean processingSync = false;

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
    }    public void initialize() {
        if (!settings.getSync().isEnabled() || isInitialized)
            return;

        try {
            URI serverUri = new URI(settings.getSync().getServerAddress());
            syncClient = new SyncClient(serverUri, this, 5000);
            CompletableFuture.runAsync(() -> syncClient.connect());
            isInitialized = true;
        } catch (Exception e) {
            UserFriendlyErrors.showError(
                "Could not connect to other computer. Check that both computers are on the same Wi-Fi network.",
                "Failed to initialize sync to " + settings.getSync().getServerAddress() + ": " + e.getMessage()
            );
        }
    }@Override
    public void onItemReceived(ClipboardItemDTO dto) {
        System.out.println("SyncManager received item from device: " + dto.getDeviceId());
        try {
            if (deviceId.equals(dto.getDeviceId())) {
                System.out.println("Ignoring item from own device: " + deviceId);
                return;
            }
            
            if (!settings.getSync().getEncryptionKey().isEmpty()) {
                String decrypted = EncryptionService.decrypt(dto.getContent(), settings.getSync().getEncryptionKey());
                dto.setContent(decrypted);
            }

            ClipboardItem item = dto.toClipboardItem();
            if (!db.itemExistsByHash(item.getHash())) {
                // Set flag to prevent triggering sync when we notify listeners
                processingSync = true;
                try {
                    db.saveItem(item);
                    System.out.println("Saved sync item from: " + dto.getDeviceId());
                    
                    // Notify UI and other listeners that a new item was received
                    System.out.println("Notifying " + listeners.size() + " listeners about new sync item");
                    notifyListeners(item);
                } finally {
                    processingSync = false;
                }
            } else {
                System.out.println("Item already exists, skipping: " + item.getHash());
            }        } catch (Exception e) {
            UserFriendlyErrors.logError("Sync item processing failed", "Failed to process sync item: " + e.getMessage());
            e.printStackTrace();
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
    }    @Override
    public void onClipboardItemAdded(ClipboardItem item) {
        // Skip if we're currently processing a sync item to prevent infinite loop
        if (processingSync) {
            System.out.println("Skipping clipboard event - currently processing sync");
            return;
        }
        
        if (!isInitialized || syncClient == null || !syncClient.isOpen())
            return;        // Skip large images to prevent WebSocket buffer overflow
        if (item.getType() == ItemType.IMAGE) {
            byte[] content = item.getContent();
            if (content.length > 10 * 1024 * 1024) {  // 10MB limit
                System.out.println("Skipping large image (" + content.length + " bytes) - too big for sync");
                return;
            }
        }

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
