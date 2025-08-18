package com.cuhlippa.client.export;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.cuhlippa.client.clipboard.ClipboardItem;
import com.cuhlippa.client.clipboard.ItemType;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.Base64;

public class ExportClipboardItem {
    @JsonProperty("type")
    private ItemType type;

    @JsonProperty("content")
    private String content;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("hash")
    private String hash;

    @JsonProperty("tags")
    private Set<String> tags;

    @JsonProperty("category")
    private String category;

    public ExportClipboardItem() {
        //Empty Constuctor for Jackson
    }

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public static ExportClipboardItem fromClipboardItem(ClipboardItem item) {
        ExportClipboardItem export = new ExportClipboardItem();
        export.type = item.getType();
        export.content = Base64.getEncoder().encodeToString(item.getContent());
        export.timestamp = item.getTimestamp();
        export.hash = item.getHash();
        export.tags = item.getTags();
        export.category = item.getCategory();
        return export;
    }

    public ClipboardItem toClipboardItem() {
        byte[] decodedContent = Base64.getDecoder().decode(content);
        return new ClipboardItem(type, decodedContent, timestamp, hash, tags, category);
    }
}
