package com.notificationplatform.service.eventaggregation;

import com.notificationplatform.dto.request.WaitForEventsConfigDTO;
import com.notificationplatform.entity.ExecutionWaitState;

import java.util.Map;

public interface EventAggregationService {

    /**
     * Register a wait state for a node waiting for multiple events
     * Returns correlation ID for use in API calls and Kafka events
     */
    ExecutionWaitState registerWaitState(String executionId, String nodeId, WaitForEventsConfigDTO config);

    /**
     * Handle API response callback
     * Validates execution_id and correlation_id to prevent cross-execution contamination
     */
    void handleApiResponse(String executionId, String correlationId, Map<String, Object> responseData);

    /**
     * Handle Kafka event
     * Validates execution_id and correlation_id to prevent cross-execution contamination
     */
    void handleKafkaEvent(String topic, Map<String, Object> eventData);

    /**
     * Check if completion condition met and resume execution if ready
     * Uses optimistic locking to ensure only one instance resumes
     */
    boolean checkAndResumeExecution(String executionId, String nodeId);

    /**
     * Handle timeout for waiting execution
     * Scheduled job calls this, but only one instance processes each expired state
     */
    void handleTimeout(String executionId, String nodeId);

    /**
     * Get correlation ID for an execution/node pair
     * Used to inject into API calls
     */
    String getCorrelationId(String executionId, String nodeId);

    /**
     * Check if completion condition is met for a wait state
     */
    boolean isCompletionConditionMet(ExecutionWaitState waitState);
}

