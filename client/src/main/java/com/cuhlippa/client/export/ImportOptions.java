package com.cuhlippa.client.export;

public class ImportOptions {
    private boolean skipDuplicates = true;
    private boolean mergeCategories = true;
    private boolean mergeTags = true;

    public boolean isSkipDuplicates() {
        return skipDuplicates;
    }
    public void setSkipDuplicates(boolean skipDuplicates) {
        this.skipDuplicates = skipDuplicates;
    }
    public boolean isMergeCategories() {
        return mergeCategories;
    }
    public void setMergeCategories(boolean mergeCategories) {
        this.mergeCategories = mergeCategories;
    }
    public boolean isMergeTags() {
        return mergeTags;
    }
    public void setMergeTags(boolean mergeTags) {
        this.mergeTags = mergeTags;
    }    
}
