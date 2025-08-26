package com.cuhlippa.client.clipboard;

/**
 * Common interface for clipboard managers
 */
public interface IClipboardManager {
    void startListening();
    void addClipboardListener(ClipboardListener listener);
    void notifyListeners(ClipboardItem item);
}
