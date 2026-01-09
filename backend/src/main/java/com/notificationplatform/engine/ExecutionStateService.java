package com.notificationplatform.engine;

import com.notificationplatform.entity.Execution;
import com.notificationplatform.entity.enums.ExecutionStatus;
import com.notificationplatform.repository.ExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing execution state.
 * Handles execution creation, updates, and context persistence.
 * Uses distributed locks for state updates.
 * 
 * See: @import(features/workflow-execution-state.md)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionStateService {

    private final ExecutionRepository executionRepository;
    private final DistributedLockService lockService;

    /**
     * Create new execution record.
     * 
     * @param workflowId Workflow ID
     * @param triggerId Trigger ID (optional)
     * @param triggerNodeId Trigger node ID (optional)
     * @param triggerData Trigger data (optional)
     * @return Created execution
     */
    @Transactional
    public Execution createExecution(String workflowId, String triggerId, String triggerNodeId, 
                                     Map<String, Object> triggerData) {
        Execution execution = new Execution();
        execution.setId(java.util.UUID.randomUUID().toString());
        execution.setStatus(ExecutionStatus.RUNNING);
        execution.setStartedAt(LocalDateTime.now());
        execution.setNodesExecuted(0);
        execution.setNotificationsSent(0);
        
        // Initialize context with trigger data
        Map<String, Object> context = new HashMap<>();
        if (triggerData != null) {
            context.putAll(triggerData);
        }
        execution.setContext(context);
        
        // Set trigger data
        if (triggerData != null) {
            execution.setTriggerData(triggerData);
        }
        
        execution = executionRepository.save(execution);
        log.debug("Execution created: executionId={}, workflowId={}", execution.getId(), workflowId);
        
        return execution;
    }

    /**
     * Update execution status.
     * Uses distributed lock to prevent concurrent updates.
     * 
     * @param executionId Execution ID
     * @param status New status
     */
    @Transactional
    public void updateExecutionStatus(String executionId, ExecutionStatus status) {
        // Acquire lock for state update
        if (!lockService.acquireLock(executionId)) {
            log.warn("Failed to acquire lock for execution status update: executionId={}", executionId);
            throw new RuntimeException("Failed to acquire lock for execution: " + executionId);
        }
        
        try {
            Optional<Execution> executionOpt = executionRepository.findById(executionId);
            if (executionOpt.isEmpty()) {
                throw new RuntimeException("Execution not found: " + executionId);
            }
            
            Execution execution = executionOpt.get();
            execution.setStatus(status);
            execution.setUpdatedAt(LocalDateTime.now());
            
            if (status == ExecutionStatus.COMPLETED || status == ExecutionStatus.FAILED) {
                execution.setCompletedAt(LocalDateTime.now());
                if (execution.getStartedAt() != null) {
                    long duration = java.time.Duration.between(
                            execution.getStartedAt(), 
                            execution.getCompletedAt()
                    ).toMillis();
                    execution.setDuration((int) duration);
                }
            }
            
            executionRepository.save(execution);
            log.debug("Execution status updated: executionId={}, status={}", executionId, status);
        } finally {
            lockService.releaseLock(executionId);
        }
    }

    /**
     * Update execution with context.
     * Persists context to database when execution is paused or completed.
     * 
     * @param executionId Execution ID
     * @param context Execution context
     * @param persistContext Whether to persist context to database
     */
    @Transactional
    public void updateExecution(String executionId, ExecutionContext context, boolean persistContext) {
        // Acquire lock for state update
        if (!lockService.acquireLock(executionId)) {
            log.warn("Failed to acquire lock for execution update: executionId={}", executionId);
            throw new RuntimeException("Failed to acquire lock for execution: " + executionId);
        }
        
        try {
            Optional<Execution> executionOpt = executionRepository.findById(executionId);
            if (executionOpt.isEmpty()) {
                throw new RuntimeException("Execution not found: " + executionId);
            }
            
            Execution execution = executionOpt.get();
            
            // Persist context to database if requested (for paused/completed executions)
            if (persistContext) {
                Map<String, Object> contextMap = serializeContext(context);
                execution.setContext(contextMap);
                log.debug("Context persisted to database: executionId={}", executionId);
            }
            
            execution.setUpdatedAt(LocalDateTime.now());
            executionRepository.save(execution);
        } finally {
            lockService.releaseLock(executionId);
        }
    }

    /**
     * Persist context to database.
     * Called when execution is paused or completed.
     * 
     * @param executionId Execution ID
     * @param context Execution context
     */
    @Transactional
    public void persistContext(String executionId, ExecutionContext context) {
        updateExecution(executionId, context, true);
    }

    /**
     * Get execution by ID.
     * 
     * @param executionId Execution ID
     * @return Execution if found
     */
    @Transactional(readOnly = true)
    public Optional<Execution> getExecution(String executionId) {
        return executionRepository.findById(executionId);
    }

    /**
     * Serialize ExecutionContext to Map for database storage.
     */
    private Map<String, Object> serializeContext(ExecutionContext context) {
        Map<String, Object> map = new HashMap<>();
        map.put("executionId", context.getExecutionId());
        map.put("workflowId", context.getWorkflowId());
        map.put("variables", context.getVariables());
        map.put("nodeOutputs", context.getNodeOutputs());
        map.put("metadata", context.getMetadata());
        map.put("waitStateId", context.getWaitStateId());
        map.put("waitingNodeId", context.getWaitingNodeId());
        return map;
    }
}

