package com.cuhlippa.client.clipboard;

import com.cuhlippa.client.storage.LocalDatabase;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Arrays;

public class ClipboardManager implements ClipboardOwner {
    private final LocalDatabase db;
    private final Clipboard systemClipboard;

    public ClipboardManager(LocalDatabase db) {
        this.db = db;
        this.systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    }

    public void startListening() {
        Transferable content = systemClipboard.getContents(this);
        regainOwnership(content);
    }

    private void regainOwnership(Transferable t) {
        systemClipboard.setContents(t, this);
    }
}
