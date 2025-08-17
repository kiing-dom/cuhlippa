package com.cuhlippa.client.clipboard;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;

public class ClipboardItem {
    private ItemType type;
    private byte[] content;
    private LocalDateTime timestamp;
    private String hash;
    private Set<String> tags;
    private String category;

    public ClipboardItem(ItemType type, byte[] content, LocalDateTime timestamp, String hash, Set<String> tags, String category) {
        this.type = type;
        this.content = content;
        this.timestamp = timestamp;
        this.hash = hash;
        this.tags = (tags != null) ? new HashSet<>(tags) : new HashSet<>();
        this.category = (category != null && !category.isBlank()) ? category : "General";
    }

    public ItemType getType() {
        return type;
    }

    public byte[] getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getHash() {
        return hash;
    }

    public Set<String> getTags() {
        return new HashSet<>(tags);
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public String getCategory() {
        return category;
    }

    public void addTag(String tag) {
        if (tag != null && !tag.trim().isEmpty()) {
            this.tags.add(tag.trim().toLowerCase());
        }
    }

    public void removeTag(String tag) {
        if (this.tags.contains(tag)) {
            this.tags.remove(tag.toLowerCase());
        }
    }

    public boolean hasTag(String tag) {
        return tags.contains(tag.toLowerCase());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ClipboardItem))
            return false;
        ClipboardItem that = (ClipboardItem) o;
        return Objects.equals(hash, that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }

    @Override
    public String toString() {
        String preview;
        if (type == ItemType.IMAGE) {
            preview = "[IMAGE - " + content.length + " bytes]";
        } else {
            String text = new String(content);
            if (text.length() > 50) {
                preview = text.substring(0, 50) + "...";
            } else {
                preview = text;
            }
            preview = preview.replace("\n", " ").replace("\r", " ");
        }

        return timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " - " + preview;
    }
}