package com.notificationplatform.engine.nodes;

import com.notificationplatform.engine.ExecutionContext;
import com.notificationplatform.engine.NodeExecutionResult;

import java.util.Map;

/**
 * Interface for specific action executors.
 * These executors are not registered directly in NodeExecutorRegistry.
 * Instead, they are called by ActionNodeExecutor based on ActionType.
 */
public interface ActionExecutor {
    
    /**
     * Execute an action node.
     *
     * @param nodeId Node identifier
     * @param nodeData Node configuration/data
     * @param context Execution context
     * @return Node execution result
     */
    NodeExecutionResult execute(String nodeId, Map<String, Object> nodeData, ExecutionContext context);
}

