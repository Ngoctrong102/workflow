package com.notificationplatform.engine.nodes;

import com.notificationplatform.engine.ExecutionContext;
import com.notificationplatform.engine.NodeExecutionResult;

import java.util.Map;

/**
 * Interface for specific trigger executors.
 * These executors are not registered directly in NodeExecutorRegistry.
 * Instead, they are called by TriggerNodeExecutor based on TriggerType.
 */
public interface TriggerExecutor {
    
    /**
     * Execute a trigger node.
     * 
     * Trigger nodes are entry points that receive trigger data and pass it to workflow context.
     * Each trigger type may have different logic for processing/validating/transforming trigger data.
     *
     * @param nodeId Node identifier
     * @param nodeData Node configuration/data
     * @param context Execution context
     * @return Node execution result with trigger data
     */
    NodeExecutionResult execute(String nodeId, Map<String, Object> nodeData, ExecutionContext context);
}

