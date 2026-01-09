package com.notificationplatform.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public class CleanupStatisticsResponse {

    private int retentionMonths;
    private LocalDateTime cutoffDate;
    private Map<String, Long> recordCountsByTable;
    private Map<String, Long> recordsToDelete;
    private LocalDateTime lastCleanupTime;
    private int lastCleanupDeleted;

    // Getters and Setters
    public int getRetentionMonths() {
        return retentionMonths;
    }

    public void setRetentionMonths(int retentionMonths) {
        this.retentionMonths = retentionMonths;
    }

    public LocalDateTime getCutoffDate() {
        return cutoffDate;
    }

    public void setCutoffDate(LocalDateTime cutoffDate) {
        this.cutoffDate = cutoffDate;
    }

    public Map<String, Long> getRecordCountsByTable() {
        return recordCountsByTable;
    }

    public void setRecordCountsByTable(Map<String, Long> recordCountsByTable) {
        this.recordCountsByTable = recordCountsByTable;
    }

    public Map<String, Long> getRecordsToDelete() {
        return recordsToDelete;
    }

    public void setRecordsToDelete(Map<String, Long> recordsToDelete) {
        this.recordsToDelete = recordsToDelete;
    }

    public LocalDateTime getLastCleanupTime() {
        return lastCleanupTime;
    }

    public void setLastCleanupTime(LocalDateTime lastCleanupTime) {
        this.lastCleanupTime = lastCleanupTime;
    }

    public int getLastCleanupDeleted() {
        return lastCleanupDeleted;
    }

    public void setLastCleanupDeleted(int lastCleanupDeleted) {
        this.lastCleanupDeleted = lastCleanupDeleted;
    }
}

