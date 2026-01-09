package com.notificationplatform.service.execution;

import com.notificationplatform.engine.ExecutionContext;
import com.notificationplatform.entity.ExecutionWaitState;
import com.notificationplatform.repository.ExecutionWaitStateRepository;
import com.notificationplatform.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Service for recovering execution context from wait states.
 * Restores full context including node outputs, variables, metadata, and trigger information.
 * 
 * See: @import(features/workflow-execution-state.md#context-recovery)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionContextRecovery {

    private final ExecutionWaitStateRepository waitStateRepository;

    /**
     * Recover context from wait state.
     * 
     * @param waitStateId Wait state ID
     * @return Recovered execution context
     */
    @Transactional(readOnly = true)
    public ExecutionContext recoverContext(String waitStateId) {
        log.info("Recovering context from wait state: waitStateId={}", waitStateId);
        
        ExecutionWaitState waitState = waitStateRepository.findById(waitStateId)
                .orElseThrow(() -> new ResourceNotFoundException("Wait state not found: " + waitStateId));
        
        // Get execution from wait state
        String executionId = waitState.getExecution().getId();
        String workflowId = waitState.getExecution().getWorkflow().getId();
        
        // Create context
        ExecutionContext context = new ExecutionContext(executionId, workflowId);
        
        // Restore trigger information
        // Note: ExecutionContext doesn't have setTriggerId/setTriggerNodeId methods
        // This information is stored in triggerDataMap instead
        
        // Recover context from execution context field (if available)
        // For now, we'll use the execution's context field
        Map<String, Object> executionContext = waitState.getExecution().getContext();
        if (executionContext != null) {
            recoverFromExecutionContext(context, executionContext);
        }
        
        log.info("Context recovered: executionId={}, workflowId={}", executionId, workflowId);
        return context;
    }

    /**
     * Recover context from execution context map.
     */
    @SuppressWarnings("unchecked")
    private void recoverFromExecutionContext(ExecutionContext context, Map<String, Object> executionContext) {
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
        
        // Restore wait state reference
        if (executionContext.containsKey("waitStateId")) {
            String waitStateId = (String) executionContext.get("waitStateId");
            String waitingNodeId = (String) executionContext.get("waitingNodeId");
            if (waitStateId != null && waitingNodeId != null) {
                context.setWaitState(waitStateId, waitingNodeId);
            }
        }
        
        // Restore trigger data map
        if (executionContext.containsKey("triggerDataMap")) {
            Map<String, Map<String, Object>> triggerDataMap = 
                (Map<String, Map<String, Object>>) executionContext.get("triggerDataMap");
            if (triggerDataMap != null) {
                triggerDataMap.forEach(context::setTriggerDataForNode);
            }
        }
    }
}

