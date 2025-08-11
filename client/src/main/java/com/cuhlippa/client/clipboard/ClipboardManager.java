package com.cuhlippa.client.clipboard;

import com.cuhlippa.client.storage.LocalDatabase;
import com.cuhlippa.client.exception.ClipboardHashingException;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.DigestException;
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

    @Override
    public void lostOwnership(Clipboard c, Transferable t) {
        try {
            Thread.sleep(200);
            processClipboard();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
            return;
        }

        regainOwnership(systemClipboard.getContents(this));
    }

    private void processClipboard() {
        Transferable t = systemClipboard.getContents(this);
        try {
            if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                String data = (String) t.getTransferData(DataFlavor.stringFlavor);
                byte[] contentBytes = data.getBytes();
                String hash = sha256(contentBytes);
                ClipboardItem item = new ClipboardItem(ItemType.TEXT, contentBytes, LocalDateTime.now(), hash);
                db.saveItem(item);
                System.out.println("Saved new item to clipboard: " + data);
            } // TODO: add image and file path handling
        } catch (UnsupportedFlavorException | IOException e) {
            e.printStackTrace();
        }
    }

    private String sha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Arrays.toString(digest.digest(data));
        } catch (Exception e) {
        throw new ClipboardHashingException("Failed to generate SHA-256 hash for clipboard data", e);
        }
    }
}
