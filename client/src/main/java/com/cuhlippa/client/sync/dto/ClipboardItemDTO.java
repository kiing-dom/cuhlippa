package com.cuhlippa.client.sync.dto;

import com.cuhlippa.client.clipboard.ClipboardItem;
import com.cuhlippa.client.clipboard.ItemType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

public class ClipboardItemDTO {
    @JsonProperty("type")
    private ItemType type;

    @JsonProperty("content")
    private String content;
    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @JsonProperty("hash")
    private String hash;

    @JsonProperty("tags")
    private Set<String> tags = new HashSet<>();

    @JsonProperty("category")
    private String category;

    @JsonProperty("deviceId")
    private String deviceId;

    public ClipboardItemDTO() {
        // default constructor
    }

    public static ClipboardItemDTO fromClipboardItem(ClipboardItem item, String deviceId) {
        ClipboardItemDTO dto = new ClipboardItemDTO();
        dto.type = item.getType();
        dto.content = Base64.getEncoder().encodeToString(item.getContent());
        dto.timestamp = item.getTimestamp();
        dto.hash = item.getHash();
        dto.tags = new HashSet<>(item.getTags());
        dto.category = item.getCategory();
        dto.deviceId = deviceId;
        return dto;
    }

    public ClipboardItem toClipboardItem() {
        byte[] decodedContent = Base64.getDecoder().decode(content);
        return new ClipboardItem(type, decodedContent, timestamp, hash, tags, category, false);
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

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}