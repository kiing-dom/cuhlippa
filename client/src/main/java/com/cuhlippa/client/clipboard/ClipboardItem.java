package com.cuhlippa.client.clipboard;

import java.time.LocalDateTime;
import java.util.Objects;

public class ClipboardItem {
    private ItemType type;
    private byte[] content;
    private LocalDateTime timestamp;
    private String hash;


    public ClipboardItem(ItemType type, byte[] content, LocalDateTime timestamp, String hash) {
        this.type = type;
        this.content = content;
        this.timestamp = timestamp;
        this.hash = hash;
    }
}