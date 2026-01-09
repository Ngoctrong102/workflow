package com.notificationplatform.service.retry;

import com.notificationplatform.engine.ExecutionContext;
import com.notificationplatform.engine.ExecutionContextCache;
import com.notificationplatform.engine.NodeExecutor;
import com.notificationplatform.engine.NodeExecutorRegistry;
import com.notificationplatform.engine.WorkflowExecutor;
import com.notificationplatform.entity.Execution;
import com.notificationplatform.entity.NodeExecution;
import com.notificationplatform.entity.RetrySchedule;
import com.notificationplatform.entity.enums.ExecutionStatus;
import com.notificationplatform.entity.enums.NodeExecutionStatus;
import com.notificationplatform.repository.ExecutionRepository;
import com.notificationplatform.repository.NodeExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service for executing retries.
 * Handles retry of failed node executions and executions.
 * 
 * See: @import(features/retry-mechanism.md)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RetryExecutionService {

    private final NodeExecutionRepository nodeExecutionRepository;
    private final ExecutionRepository executionRepository;
    private final NodeExecutorRegistry nodeExecutorRegistry;
    private final ExecutionContextCache contextCache;
    private final WorkflowExecutor workflowExecutor;

    /**
     * Retry failed node execution.
     * 
     * @param retrySchedule Retry schedule
     * @return true if retry successful, false otherwise
     */
    @Transactional
    public boolean retryNodeExecution(RetrySchedule retrySchedule) {
        log.info("Retrying node execution: retryScheduleId={}, targetId={}", 
                 retrySchedule.getId(), retrySchedule.getTargetId());
        
        // Get node execution
        NodeExecution nodeExecution = nodeExecutionRepository.findById(retrySchedule.getTargetId())
                .orElseThrow(() -> new RuntimeException("Node execution not found: " + retrySchedule.getTargetId()));
        
        // Get execution
        Execution execution = nodeExecution.getExecution();
        
        // Load or recover context
        ExecutionContext context = contextCache.loadContext(execution.getId());
        if (context == null) {
            // Recover from execution context
            Map<String, Object> executionContext = execution.getContext();
            if (executionContext != null) {
                context = recoverContext(execution, executionContext);
            } else {
                log.error("Cannot recover context for retry: executionId={}", execution.getId());
                return false;
            }
        }
        
        // Get retry context
        Map<String, Object> retryContext = retrySchedule.getRetryContext();
        if (retryContext == null) {
            log.error("Retry context not found: retryScheduleId={}", retrySchedule.getId());
            return false;
        }
        
        // Get node config and input data from retry context
        @SuppressWarnings("unchecked")
        Map<String, Object> nodeConfig = (Map<String, Object>) retryContext.get("nodeConfig");
        @SuppressWarnings("unchecked")
        Map<String, Object> inputData = (Map<String, Object>) retryContext.get("inputData");
        
        // Get node executor
        String nodeTypeStr = nodeExecution.getNodeType();
        if (nodeTypeStr == null || nodeTypeStr.isEmpty()) {
            nodeTypeStr = "action";
        }
        
        com.notificationplatform.entity.enums.NodeType nodeType;
        try {
            // Convert string to enum name format (uppercase, replace "-" with "_")
            String enumName = nodeTypeStr.toUpperCase().replace("-", "_");
            nodeType = com.notificationplatform.entity.enums.NodeType.valueOf(enumName);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid node type: {} for node execution: {}", nodeTypeStr, nodeExecution.getId());
            nodeType = com.notificationplatform.entity.enums.NodeType.ACTION;
        }
        
        NodeExecutor executor = nodeExecutorRegistry.getExecutor(nodeType);
        
        if (executor == null) {
            log.error("No executor found for node type: {}", nodeType);
            return false;
        }
        
        // Update node execution status
        nodeExecution.setStatus(NodeExecutionStatus.RUNNING);
        nodeExecution.setStartedAt(LocalDateTime.now());
        nodeExecution.setRetryCount(nodeExecution.getRetryCount() + 1);
        nodeExecutionRepository.save(nodeExecution);
        
        try {
            // Execute node
            com.notificationplatform.engine.NodeExecutionResult result = 
                executor.execute(nodeExecution.getNodeId(), nodeConfig, context);
            
            if (result.isSuccess()) {
                // Retry successful
                nodeExecution.setStatus(NodeExecutionStatus.COMPLETED);
                nodeExecution.setOutputData(result.getOutput());
                nodeExecution.setCompletedAt(LocalDateTime.now());
                
                long duration = java.time.Duration.between(
                    nodeExecution.getStartedAt(), 
                    LocalDateTime.now()
                ).toMillis();
                nodeExecution.setDuration((int) duration);
                
                nodeExecutionRepository.save(nodeExecution);
                
                // Update context
                context.setNodeOutput(nodeExecution.getNodeId(), result.getOutput());
                contextCache.updateContext(execution.getId(), context);
                
                log.info("Node execution retry successful: nodeExecutionId={}, attempt={}", 
                         nodeExecution.getId(), retrySchedule.getCurrentAttempt());
                return true;
            } else {
                // Retry failed
                nodeExecution.setStatus(NodeExecutionStatus.FAILED);
                nodeExecution.setError(result.getError());
                nodeExecution.setCompletedAt(LocalDateTime.now());
                nodeExecutionRepository.save(nodeExecution);
                
                log.warn("Node execution retry failed: nodeExecutionId={}, error={}", 
                         nodeExecution.getId(), result.getError());
                return false;
            }
        } catch (Exception e) {
            log.error("Error retrying node execution: nodeExecutionId={}", nodeExecution.getId(), e);
            nodeExecution.setStatus(NodeExecutionStatus.FAILED);
            nodeExecution.setError(e.getMessage());
            nodeExecution.setCompletedAt(LocalDateTime.now());
            nodeExecutionRepository.save(nodeExecution);
            return false;
        }
    }

    /**
     * Retry failed execution.
     * 
     * @param retrySchedule Retry schedule
     * @return true if retry successful, false otherwise
     */
    @Transactional
    public boolean retryExecution(RetrySchedule retrySchedule) {
        log.info("Retrying execution: retryScheduleId={}, targetId={}", 
                 retrySchedule.getId(), retrySchedule.getTargetId());
        
        // Get execution
        Execution execution = executionRepository.findById(retrySchedule.getTargetId())
                .orElseThrow(() -> new RuntimeException("Execution not found: " + retrySchedule.getTargetId()));
        
        // Get retry context
        Map<String, Object> retryContext = retrySchedule.getRetryContext();
        if (retryContext == null) {
            log.error("Retry context not found: retryScheduleId={}", retrySchedule.getId());
            return false;
        }
        
        // Get trigger data from retry context
        @SuppressWarnings("unchecked")
        Map<String, Object> triggerData = (Map<String, Object>) retryContext.get("triggerData");
        
        // Update execution status
        execution.setStatus(ExecutionStatus.RUNNING);
        execution.setUpdatedAt(LocalDateTime.now());
        executionRepository.save(execution);
        
        try {
            // Re-execute workflow
            // Note: This is a simplified version - in a full implementation,
            // we would need to continue from the failed node
            Execution retriedExecution = workflowExecutor.execute(
                execution.getWorkflow(),
                triggerData != null ? triggerData : new java.util.HashMap<>(),
                execution.getTrigger() != null ? execution.getTrigger().getId() : null
            );
            
            if (retriedExecution.getStatus() == ExecutionStatus.COMPLETED) {
                log.info("Execution retry successful: executionId={}, attempt={}", 
                         execution.getId(), retrySchedule.getCurrentAttempt());
                return true;
            } else {
                log.warn("Execution retry failed: executionId={}, status={}", 
                         execution.getId(), retriedExecution.getStatus());
                return false;
            }
        } catch (Exception e) {
            log.error("Error retrying execution: executionId={}", execution.getId(), e);
            execution.setStatus(ExecutionStatus.FAILED);
            execution.setError(e.getMessage());
            execution.setUpdatedAt(LocalDateTime.now());
            executionRepository.save(execution);
            return false;
        }
    }

    /**
     * Recover context from execution context map.
     */
    @SuppressWarnings("unchecked")
    private ExecutionContext recoverContext(Execution execution, Map<String, Object> executionContext) {
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

