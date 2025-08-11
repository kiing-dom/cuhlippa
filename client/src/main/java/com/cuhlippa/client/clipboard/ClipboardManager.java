package com.cuhlippa.client.clipboard;

import com.cuhlippa.client.storage.LocalDatabase;
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

import java.util.Arrays;
import java.util.List;

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
                System.out.println("Saved new text item to clipboard: " + data);
            }
            if (t != null && t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                Image image = (Image) t.getTransferData(DataFlavor.imageFlavor);
                BufferedImage bufferedImage = new BufferedImage(
                    image.getWidth(null),
                    image.getHeight(null),
                    BufferedImage.TYPE_INT_ARGB
                );
                Graphics2D g2d = bufferedImage.createGraphics();
                g2d.drawImage(image, 0, 0, null);
                g2d.dispose();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", baos);
                baos.flush();

                byte[] contentBytes = baos.toByteArray();
                String hash = sha256(contentBytes);
                ClipboardItem item = new ClipboardItem(ItemType.IMAGE, contentBytes, LocalDateTime.now(), hash);
                db.saveItem(item);
                System.out.println("Saved new image item to clipboard");
            }
            if (t != null && t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                @SuppressWarnings("unchecked")
                List<File> fileList = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                StringBuilder sb = new StringBuilder();
                for (File file : fileList) {
                    sb.append(file.getAbsolutePath()).append("\n");
                }

                byte[] contentBytes = sb.toString().getBytes();
                String hash = sha256(contentBytes);
                ClipboardItem item = new ClipboardItem(ItemType.FILE_PATH, contentBytes, LocalDateTime.now(), hash);
                db.saveItem(item);
                System.out.println("Saved new file path item(s) to clipboard");
            }
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