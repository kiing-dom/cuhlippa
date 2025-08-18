package com.cuhlippa.client.export;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public class ClipboardExportData {
    @JsonProperty("version")
    private String version = "1.0";

    @JsonProperty("exportDate")
    private LocalDateTime exportDate;

    @JsonProperty("items")
    private List<ExportClipboardItem> items;

    @JsonProperty("totalItems")
    private int totalItems;

    public ClipboardExportData(String version, LocalDateTime exportDate, List<ExportClipboardItem> items,
            int totalItems) {
        this.version = version;
        this.exportDate = exportDate;
        this.items = items;
        this.totalItems = totalItems;
    }

    public ClipboardExportData() {}

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LocalDateTime getExportDate() {
        return exportDate;
    }

    public void setExportDate(LocalDateTime exportDate) {
        this.exportDate = exportDate;
    }

    public List<ExportClipboardItem> getItems() {
        return items;
    }

    public void setItems(List<ExportClipboardItem> items) {
        this.items = items;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

}
