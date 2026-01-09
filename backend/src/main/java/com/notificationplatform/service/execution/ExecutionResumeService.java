package com.notificationplatform.service.execution;

import com.notificationplatform.engine.ExecutionContext;
import com.notificationplatform.engine.ExecutionContextCache;
import com.notificationplatform.engine.ExecutionStateService;
import com.notificationplatform.engine.WorkflowExecutor;
import com.notificationplatform.entity.Execution;
import com.notificationplatform.entity.ExecutionWaitState;
import com.notificationplatform.entity.Workflow;
import com.notificationplatform.entity.enums.ExecutionStatus;
import com.notificationplatform.repository.ExecutionWaitStateRepository;
import com.notificationplatform.repository.ExecutionRepository;
import com.notificationplatform.repository.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for resuming paused executions.
 * Handles scheduled resume tasks and manual resume operations.
 * 
 * See: @import(features/distributed-execution-management.md#resume-execution)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionResumeService {

    private final ExecutionRepository executionRepository;
    private final ExecutionWaitStateRepository waitStateRepository;
    private final WorkflowRepository workflowRepository;
    private final ExecutionContextRecovery contextRecovery;
    private final ExecutionContextCache contextCache;
    private final ExecutionStateService executionStateService;
    private final WorkflowExecutor workflowExecutor;

    /**
     * Check and resume executions that are ready to resume.
     * Runs every minute to check for executions that should be resumed.
     */
    @Scheduled(fixedRate = 60000) // Every minute
    @Transactional
    public void checkAndResumeExecutions() {
        log.debug("Checking for executions ready to resume");
        
        // Find wait states that are ready to resume (for delay nodes)
        // Note: For wait-for-events, resume is triggered by event arrival
        LocalDateTime now = LocalDateTime.now();
        List<ExecutionWaitState> readyWaitStates = waitStateRepository
                .findByExpiresAtLessThanEqualAndStatus(now, "waiting");
        
        for (ExecutionWaitState waitState : readyWaitStates) {
            // Check if this is a delay node (waitType = DELAY)
            if ("DELAY".equals(waitState.getWaitType())) {
                try {
                    resumeExecution(waitState.getExecution().getId(), waitState.getNodeId());
                } catch (Exception e) {
                    log.error("Error resuming execution: executionId={}, nodeId={}", 
                             waitState.getExecution().getId(), waitState.getNodeId(), e);
                }
            }
        }
    }

    /**
     * Resume execution from paused state.
     * 
     * @param executionId Execution ID
     * @param nodeId Node ID to resume from
     */
    @Transactional
    public void resumeExecution(String executionId, String nodeId) {
        log.info("Resuming execution: executionId={}, nodeId={}", executionId, nodeId);
        
        // Get execution
        Optional<Execution> executionOpt = executionRepository.findById(executionId);
        if (executionOpt.isEmpty()) {
            log.warn("Execution not found: executionId={}", executionId);
            return;
        }
        
        Execution execution = executionOpt.get();
        
        // Check if execution is in a resumable state
        if (execution.getStatus() != ExecutionStatus.PAUSED && 
            execution.getStatus() != ExecutionStatus.WAITING) {
            log.warn("Execution is not in resumable state: executionId={}, status={}", 
                     executionId, execution.getStatus());
            return;
        }
        
        // Get workflow (for future use in continuation logic)
        // Workflow workflow = workflowRepository.findById(execution.getWorkflow().getId())
        //         .orElseThrow(() -> new RuntimeException("Workflow not found: " + execution.getWorkflow().getId()));
        
        // Try to load context from cache first
        ExecutionContext context = contextCache.loadContext(executionId);
        
        // If not in cache, recover from database
        if (context == null) {
            // Find wait state for this execution and node
            Optional<ExecutionWaitState> waitStateOpt = waitStateRepository
                    .findByExecutionIdAndNodeId(executionId, nodeId);
            
            if (waitStateOpt.isPresent()) {
                ExecutionWaitState waitState = waitStateOpt.get();
                context = contextRecovery.recoverContext(waitState.getId());
                
                // Mark wait state as completed
                waitState.setStatus("completed");
                waitState.setResumedAt(LocalDateTime.now());
                waitStateRepository.save(waitState);
            } else {
                // Try to recover from execution context
                Map<String, Object> executionContext = execution.getContext();
                if (executionContext != null) {
                    context = recoverFromExecutionContext(execution, executionContext);
                } else {
                    log.error("Cannot recover context: executionId={}, nodeId={}", executionId, nodeId);
                    return;
                }
            }
        }
        
        // Update execution status to RUNNING
        execution.setStatus(ExecutionStatus.RUNNING);
        execution.setUpdatedAt(LocalDateTime.now());
        executionRepository.save(execution);
        
        // Continue execution from the node
        // Note: This is a simplified version - in a full implementation,
        // we would need to continue from the specific node
        log.info("Execution resumed: executionId={}, nodeId={}", executionId, nodeId);
        
        // For now, we'll just mark as resumed
        // The actual continuation logic would be in WorkflowExecutor.continueExecution()
    }

    /**
     * Resume execution with aggregated data from events.
     * 
     * @param executionId Execution ID
     * @param nodeId Node ID to resume from
     * @param aggregatedData Aggregated data from events
     */
    @Transactional
    public void resumeExecutionWithData(String executionId, String nodeId, Map<String, Object> aggregatedData) {
        log.info("Resuming execution with data: executionId={}, nodeId={}", executionId, nodeId);
        
        // Load or recover context
        ExecutionContext context = contextCache.loadContext(executionId);
        
        if (context == null) {
            // Recover from wait state
            Optional<ExecutionWaitState> waitStateOpt = waitStateRepository
                    .findByExecutionIdAndNodeId(executionId, nodeId);
            
            if (waitStateOpt.isPresent()) {
                context = contextRecovery.recoverContext(waitStateOpt.get().getId());
            } else {
                log.error("Cannot recover context: executionId={}, nodeId={}", executionId, nodeId);
                return;
            }
        }
        
        // Add aggregated data to context
        if (aggregatedData != null) {
            aggregatedData.forEach(context::setVariable);
        }
        
        // Update execution status
        executionStateService.updateExecutionStatus(executionId, ExecutionStatus.RUNNING);
        
        // Continue execution
        // Note: Actual continuation would be handled by WorkflowExecutor
        log.info("Execution resumed with data: executionId={}, nodeId={}", executionId, nodeId);
    }

    /**
     * Recover context from execution context map.
     */
    @SuppressWarnings("unchecked")
    private ExecutionContext recoverFromExecutionContext(Execution execution, Map<String, Object> executionContext) {
        String executionId = execution.getId();
        String workflowId = execution.getWorkflow().getId();
        
        ExecutionContext context = new ExecutionContext(executionId, workflowId);
        
        // Restore node outputs
        if (executionContext.containsKey("nodeOutputs")) {
            Map<String, Object> nodeOutputs = (Map<String, Object>) executionContext.get("nodeOutputs");
            if (nodeOutputs != null) {
                nodeOutputs.forEach(context::setNodeOutput);
            }
        }
        
        // Restore variables
        if (executionContext.containsKey("variables")) {
            Map<String, Object> variables = (Map<String, Object>) executionContext.get("variables");
            if (variables != null) {
                variables.forEach(context::setVariable);
            }
        }
        
        // Restore metadata
        if (executionContext.containsKey("metadata")) {
            Map<String, Object> metadata = (Map<String, Object>) executionContext.get("metadata");
            if (metadata != null) {
                metadata.forEach(context::setMetadata);
            }
        }
        
        return context;
    }
}

