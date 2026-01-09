package com.notificationplatform.service.waitstate;

import com.notificationplatform.entity.ExecutionWaitState;

import java.time.LocalDateTime;
import java.util.Map;

public interface ExecutionWaitStateService {

    /**
     * Create wait state for a node
     */
    ExecutionWaitState createWaitState(String executionId, String nodeId, 
                                      String correlationId, String waitType,
                                      LocalDateTime expiresAt);

    /**
     * Update wait state when event received
     */
    ExecutionWaitState updateWaitState(String executionId, String correlationId, 
                                      String eventType, Map<String, Object> eventData);

    /**
     * Get wait state by correlation ID
     */
    ExecutionWaitState getWaitStateByCorrelationId(String correlationId);

    /**
     * Get wait state by execution ID and correlation ID
     */
    ExecutionWaitState getWaitStateByExecutionIdAndCorrelationId(String executionId, String correlationId);

    /**
     * Check for expired wait states and handle timeouts
     */
    void checkTimeouts();
}

