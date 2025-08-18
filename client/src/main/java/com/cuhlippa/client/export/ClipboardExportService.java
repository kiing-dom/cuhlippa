package com.cuhlippa.client.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.cuhlippa.client.clipboard.ClipboardItem;
import com.cuhlippa.client.storage.LocalDatabase;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClipboardExportService {
    private final ObjectMapper objectMapper;
    private final LocalDatabase db;

    public ClipboardExportService(LocalDatabase db) {
        this.db = db;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
    }

    public void exportToFile(File file, ExportOptions options) throws IOException {
        List<ClipboardItem> items = getItemsToExport(options);

        ClipboardExportData exportData = new ClipboardExportData();
        exportData.setExportDate(LocalDateTime.now());
        exportData.setTotalItems(items.size());
        exportData.setItems(items.stream()
        .map(ExportClipboardItem::fromClipboardItem)
        .toList());

        objectMapper.writeValue(file, exportData);
    }

    public ImportResult importFromFile(File file, ImportOptions options) throws IOException {
        ClipboardExportData exportData = objectMapper.readValue(file, ClipboardExportData.class);

        ImportResult result = new ImportResult();
        int imported = 0;
        int skipped = 0;
        int errors = 0;

        for (ExportClipboardItem exportItem : exportData.getItems()) {
            try {
                ClipboardItem item = exportItem.toClipboardItem();
    
                if (options.isSkipDuplicates() && db.itemExistsByHash(item.getHash())) {
                    skipped++;
                    continue;
                }

                db.saveItem(item);
                imported++;
            } catch (Exception e) {
                errors++;
                result.addError("Failed to import item: " + e.getMessage());
            }
        }

        result.setImported(imported);
        result.setSkipped(skipped);
        result.setErrors(errors);
        result.setTotalProcessed(exportData.getTotalItems());

        return result;
    }    

    private List<ClipboardItem> getItemsToExport(ExportOptions options) {
        List<ClipboardItem> items;

        if (options.getCategory() != null) {
            items = db.getItemsByCategory(options.getCategory());
        } else if (options.getTag() != null) {
            items = db.getItemsByTag(options.getTag());
        } else {
            items = db.getAllItems();
        }

        return items.stream()
            .filter(item -> shouldIncludeItem(item, options))
            .toList();
    }

    private boolean shouldIncludeItem(ClipboardItem item, ExportOptions options) {
        if (item == null || item.getType() == null) {
            return false;
        }

        return switch (item.getType()) {
            case TEXT -> options.isIncludeText();
            case IMAGE -> options.isIncludeImages();
            case FILE_PATH -> options.isIncludeFiles();
        };
    }

    public String generateDefaultFilename() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return "cuhlippa_export_" + timestamp + ".json";
    }
}
