package com.cuhlippa.client.export;

public class ExportOptions {
    private String category;
    private String tag;
    private boolean includeImages = true;
    private boolean includeText = true;
    private boolean includeFiles = true;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public boolean isIncludeImages() {
        return includeImages;
    }

    public void setIncludeImages(boolean includeImages) {
        this.includeImages = includeImages;
    }

    public boolean isIncludeText() {
        return includeText;
    }

    public void setIncludeText(boolean includeText) {
        this.includeText = includeText;
    }

    public boolean isIncludeFiles() {
        return includeFiles;
    }

    public void setIncludeFiles(boolean includeFiles) {
        this.includeFiles = includeFiles;
    }

}
