package com.cuhlippa.client.clipboard;

import com.cuhlippa.client.storage.LocalDatabase;
import com.cuhlippa.client.config.Settings;
import com.cuhlippa.client.exception.ClipboardHashingException;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.BufferedImage;

import java.security.MessageDigest;

import java.time.LocalDateTime;

import javax.imageio.ImageIO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.File;

import java.util.List;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.HashSet;

public class ClipboardManager implements ClipboardOwner {
    private final LocalDatabase db;
    private final Clipboard systemClipboard;
    private final List<ClipboardListener> listeners = new ArrayList<>();
    private final Settings settings;
    private static final String CATEGORY_GENERAL = "General";

    public ClipboardManager(LocalDatabase db, Settings settings) {
        this.db = db;
        this.settings = settings;
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
            // TODO: implement retry instead of using sleep. e.g. try to read 5 times before
            // backing off
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
                if (shouldIgnoreContent(data))
                    return;

                byte[] contentBytes = data.getBytes();
                String hash = sha256(contentBytes);
                ClipboardItem item = new ClipboardItem(ItemType.TEXT, contentBytes, LocalDateTime.now(), hash,
                        new HashSet<>(), CATEGORY_GENERAL, false);
                db.saveItemAndUpdateHistory(item, settings);
                notifyListeners(item);
                System.out.println("Saved new text item to clipboard: " + data);
            }
            if (t != null && t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                Image image = (Image) t.getTransferData(DataFlavor.imageFlavor);
                BufferedImage bufferedImage = new BufferedImage(
                        image.getWidth(null),
                        image.getHeight(null),
                        BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = bufferedImage.createGraphics();
                g2d.drawImage(image, 0, 0, null);
                g2d.dispose();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", baos);
                baos.flush();
                byte[] contentBytes = baos.toByteArray();
                String hash = sha256(contentBytes);
                ClipboardItem item = new ClipboardItem(ItemType.IMAGE, contentBytes, LocalDateTime.now(), hash,
                        new HashSet<>(), CATEGORY_GENERAL, false);
                db.saveItemAndUpdateHistory(item, settings);
                notifyListeners(item);
                System.out.println("Saved new image item to clipboard");
            }
            if (t != null && t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                @SuppressWarnings("unchecked")
                List<File> fileList = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                StringBuilder sb = new StringBuilder();
                for (File file : fileList) {
                    sb.append(file.getAbsolutePath()).append("\n");
                }
                if (shouldIgnoreContent(sb.toString()))
                    return;

                byte[] contentBytes = sb.toString().getBytes();
                String hash = sha256(contentBytes);
                ClipboardItem item = new ClipboardItem(ItemType.FILE_PATH, contentBytes, LocalDateTime.now(), hash,
                        new HashSet<>(), CATEGORY_GENERAL, false);
                db.saveItemAndUpdateHistory(item, settings);
                notifyListeners(item);
                System.out.println("Saved new file path item(s) to clipboard");
            }
        } catch (UnsupportedFlavorException | IOException e) {
            e.printStackTrace();
        }
    }

    private String sha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (Exception e) {
            throw new ClipboardHashingException("Failed to generate SHA-256 hash for clipboard data", e);
        }
    }

    private boolean shouldIgnoreContent(String content) {
        return settings.getIgnorePatterns().stream()
                .map(Pattern::compile)
                .anyMatch(p -> p.matcher(content).find());
    }

    public void addClipboardListener(ClipboardListener listener) {
        listeners.add(listener);
    }

    public void notifyListeners(ClipboardItem item) {
        for (ClipboardListener listener : listeners) {
            listener.onClipboardItemAdded(item);
        }
    }
}