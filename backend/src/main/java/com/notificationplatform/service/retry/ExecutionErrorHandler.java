package com.notificationplatform.service.retry;

import com.notificationplatform.entity.Execution;
import com.notificationplatform.entity.NodeExecution;
import com.notificationplatform.entity.enums.ExecutionStatus;
import com.notificationplatform.entity.enums.NodeExecutionStatus;
import com.notificationplatform.repository.ExecutionRepository;
import com.notificationplatform.repository.NodeExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for handling execution errors.
 * Handles node execution errors and execution errors, creates retry schedules for retryable errors.
 * 
 * See: @import(features/retry-mechanism.md)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionErrorHandler {

    private final NodeExecutionRepository nodeExecutionRepository;
    private final ExecutionRepository executionRepository;
    private final RetryScheduleService retryScheduleService;

    /**
     * Handle node execution error.
     * 
     * @param nodeExecution Failed node execution
     * @param error Error message
     * @param errorDetails Error details
     * @param retryConfig Retry configuration (if retry is enabled)
     */
    @Transactional
    public void handleNodeExecutionError(NodeExecution nodeExecution, String error, 
                                         Map<String, Object> errorDetails, Map<String, Object> retryConfig) {
        log.error("Handling node execution error: nodeExecutionId={}, nodeId={}, error={}", 
                  nodeExecution.getId(), nodeExecution.getNodeId(), error);
        
        // Update node execution with error
        nodeExecution.setStatus(NodeExecutionStatus.FAILED);
        nodeExecution.setError(error);
        nodeExecution.setErrorDetails(errorDetails);
        nodeExecution.setCompletedAt(LocalDateTime.now());
        
        if (nodeExecution.getStartedAt() != null) {
            long duration = java.time.Duration.between(
                nodeExecution.getStartedAt(), 
                LocalDateTime.now()
            ).toMillis();
            nodeExecution.setDuration((int) duration);
        }
        
        nodeExecutionRepository.save(nodeExecution);
        
        // Check if retry is enabled
        if (retryConfig != null && isRetryEnabled(retryConfig)) {
            // Create retry schedule
            retryScheduleService.createRetryScheduleForNode(nodeExecution, retryConfig);
            log.info("Retry schedule created for node execution: nodeExecutionId={}", nodeExecution.getId());
        }
    }

    /**
     * Handle execution error.
     * 
     * @param execution Failed execution
     * @param error Error message
     * @param errorDetails Error details
     * @param retryConfig Retry configuration (if retry is enabled)
     */
    @Transactional
    public void handleExecutionError(Execution execution, String error, 
                                    Map<String, Object> errorDetails, Map<String, Object> retryConfig) {
        log.error("Handling execution error: executionId={}, workflowId={}, error={}", 
                  execution.getId(), execution.getWorkflow().getId(), error);
        
        // Update execution with error
        execution.setStatus(ExecutionStatus.FAILED);
        execution.setError(error);
        execution.setErrorDetails(errorDetails);
        execution.setCompletedAt(LocalDateTime.now());
        
        if (execution.getStartedAt() != null) {
            long duration = java.time.Duration.between(
                execution.getStartedAt(), 
                LocalDateTime.now()
            ).toMillis();
            execution.setDuration((int) duration);
        }
        
        executionRepository.save(execution);
        
        // Check if retry is enabled
        if (retryConfig != null && isRetryEnabled(retryConfig)) {
            // Create retry schedule
            retryScheduleService.createRetryScheduleForExecution(execution, retryConfig);
            log.info("Retry schedule created for execution: executionId={}", execution.getId());
        }
    }

    /**
     * Check if retry is enabled in configuration.
     */
    private boolean isRetryEnabled(Map<String, Object> retryConfig) {
        Object enabledObj = retryConfig.get("enabled");
        if (enabledObj instanceof Boolean) {
            return (Boolean) enabledObj;
        }
        // Default to enabled if maxAttempts is specified
        return retryConfig.containsKey("maxAttempts");
    }

    /**
     * Create error details map.
     */
    public Map<String, Object> createErrorDetails(Exception error) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("message", error.getMessage());
        errorDetails.put("type", error.getClass().getName());
        errorDetails.put("timestamp", LocalDateTime.now().toString());
        
        // Add stack trace (truncated)
        StackTraceElement[] stackTrace = error.getStackTrace();
        if (stackTrace != null && stackTrace.length > 0) {
            String[] stackTraceStr = new String[Math.min(stackTrace.length, 10)];
            for (int i = 0; i < stackTraceStr.length; i++) {
                stackTraceStr[i] = stackTrace[i].toString();
            }
            errorDetails.put("stackTrace", stackTraceStr);
        }
        
        return errorDetails;
    }
}

