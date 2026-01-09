package com.notificationplatform.engine;

import com.notificationplatform.entity.enums.NodeType;

import java.util.Map;

/**
 * Interface for node executors
 * Each node type will have its own executor implementation
 */
public interface NodeExecutor {

    /**
     * Execute a node
     *
     * @param nodeId Node identifier
     * @param nodeData Node configuration/data
     * @param context Execution context
     * @return Node execution result
     */
    NodeExecutionResult execute(String nodeId, Map<String, Object> nodeData, ExecutionContext context);

    /**
     * Get the node type this executor handles
     *
     * @return Node type enum
     */
    NodeType getNodeType();
}

