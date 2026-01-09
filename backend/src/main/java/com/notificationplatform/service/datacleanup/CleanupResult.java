package com.notificationplatform.service.datacleanup;

import java.time.LocalDateTime;
import java.util.Map;

public class CleanupResult {

    private LocalDateTime cleanupTime;
    private int totalDeleted;
    private Map<String, Integer> deletedByTable;
    private String status; // success, failed, partial
    private String errorMessage;

    // Getters and Setters
    public LocalDateTime getCleanupTime() {
        return cleanupTime;
    }

    public void setCleanupTime(LocalDateTime cleanupTime) {
        this.cleanupTime = cleanupTime;
    }

    public int getTotalDeleted() {
        return totalDeleted;
    }

    public void setTotalDeleted(int totalDeleted) {
        this.totalDeleted = totalDeleted;
    }

    public Map<String, Integer> getDeletedByTable() {
        return deletedByTable;
    }

    public void setDeletedByTable(Map<String, Integer> deletedByTable) {
        this.deletedByTable = deletedByTable;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}

