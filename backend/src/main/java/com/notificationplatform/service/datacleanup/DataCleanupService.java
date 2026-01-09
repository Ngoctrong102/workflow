package com.notificationplatform.service.datacleanup;

/**
 * Service for data cleanup and retention management
 */
public interface DataCleanupService {

    /**
     * Cleanup old data based on retention policy
     */
    CleanupResult cleanupOldData();

    /**
     * Cleanup data older than specified months
     */
    CleanupResult cleanupDataOlderThan(int months);

    /**
     * Get cleanup statistics
     */
    CleanupStatistics getCleanupStatistics();
}

