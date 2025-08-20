package com.cuhlippa.client.export;

import java.util.List;
import java.util.ArrayList;

public class ImportResult {
    private int imported;
    private int skipped;
    private int errors;
    private int totalProcessed;
    private List<String> errorMessages = new ArrayList<>();

    public ImportResult() {
        this.imported = 0;
        this.skipped = 0;
        this.errors = 0;
        this.totalProcessed = 0;
        this.errorMessages = new ArrayList<>();
    }

    public int getImported() {
        return imported;
    }

    public void setImported(int imported) {
        this.imported = imported;
    }

    public int getSkipped() {
        return skipped;
    }

    public void setSkipped(int skipped) {
        this.skipped = skipped;
    }

    public int getErrors() {
        return errors;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }

    public int getTotalProcessed() {
        return totalProcessed;
    }

    public void setTotalProcessed(int totalProcessed) {
        this.totalProcessed = totalProcessed;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }

    public void addError(String errorMessage) {
       this.errorMessages.add(errorMessage); 
    }
}