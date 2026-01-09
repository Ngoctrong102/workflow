package com.notificationplatform.service.dlq;

import com.notificationplatform.dto.response.DeadLetterQueueResponse;
import com.notificationplatform.dto.response.PagedResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service for managing dead letter queue
 */
public interface DeadLetterQueueService {

    /**
     * Add message to dead letter queue
     */
    String addToDLQ(String sourceType, String sourceId, Object originalMessage,
                    String errorMessage, String errorType, Map<String, Object> metadata);

    /**
     * Get DLQ entry by ID
     */
    DeadLetterQueueResponse getDLQEntry(String id);

    /**
     * List DLQ entries with pagination
     */
    PagedResponse<DeadLetterQueueResponse> listDLQEntries(String sourceType, String channelId,
                                                          String status, String errorType,
                                                          LocalDateTime startDate, LocalDateTime endDate,
                                                          int limit, int offset);

    /**
     * Retry DLQ entry
     */
    DeadLetterQueueResponse retryDLQEntry(String id);

    /**
     * Retry multiple DLQ entries
     */
    List<DeadLetterQueueResponse> retryDLQEntries(List<String> ids);

    /**
     * Resolve DLQ entry (mark as resolved)
     */
    DeadLetterQueueResponse resolveDLQEntry(String id, String resolvedBy);

    /**
     * Delete DLQ entry
     */
    void deleteDLQEntry(String id);

    /**
     * Cleanup old DLQ entries
     */
    int cleanupOldEntries(int daysOld);

    /**
     * Get DLQ statistics
     */
    Map<String, Object> getDLQStatistics();
}

