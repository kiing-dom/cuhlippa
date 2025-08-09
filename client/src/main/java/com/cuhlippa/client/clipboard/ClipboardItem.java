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

    public ItemType getType() { return type; }
    public byte[] getContent() { return content; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getHash() { return hash; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClipboardItem)) return false;
        ClipboardItem that = (ClipboardItem) o;
        return Objects.equals(hash, that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }
}