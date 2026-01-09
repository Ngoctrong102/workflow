package com.notificationplatform.engine;

import java.util.HashMap;
import java.util.Map;

/**
 * Execution context holds data during workflow execution
 */
public class ExecutionContext {

    private final String executionId;
    private final String workflowId;
    private final Map<String, Object> variables; // Global workflow variables
    private final Map<String, Object> nodeOutputs; // Output from each node (including trigger nodes)
    private final Map<String, Object> metadata; // Execution metadata
    private String waitStateId; // Wait state ID if execution is waiting
    private String waitingNodeId; // Node ID that is waiting
    // Map to store trigger data temporarily before trigger node execution
    // Key: trigger nodeId, Value: trigger data
    private final Map<String, Map<String, Object>> triggerDataMap;

    public ExecutionContext(String executionId, String workflowId) {
        this.executionId = executionId;
        this.workflowId = workflowId;
        this.variables = new HashMap<>();
        this.nodeOutputs = new HashMap<>();
        this.metadata = new HashMap<>();
        this.triggerDataMap = new HashMap<>();
    }

    // Getters and Setters
    public String getExecutionId() {
        return executionId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariable(String key, Object value) {
        this.variables.put(key, value);
    }

    public Object getVariable(String key) {
        return this.variables.get(key);
    }

    public Map<String, Object> getNodeOutputs() {
        return nodeOutputs;
    }

    public void setNodeOutput(String nodeId, Object output) {
        this.nodeOutputs.put(nodeId, output);
    }

    public Object getNodeOutput(String nodeId) {
        return this.nodeOutputs.get(nodeId);
    }

    /**
     * Set trigger data for a specific trigger node
     * This data will be used when the trigger node is executed
     */
    public void setTriggerDataForNode(String triggerNodeId, Map<String, Object> triggerData) {
        this.triggerDataMap.put(triggerNodeId, triggerData != null ? triggerData : new HashMap<>());
    }

    /**
     * Get trigger data for a specific trigger node
     */
    public Map<String, Object> getTriggerDataForNode(String triggerNodeId) {
        return triggerDataMap.getOrDefault(triggerNodeId, new HashMap<>());
    }

    /**
     * @deprecated Use getTriggerDataForNode(triggerNodeId) instead
     * This method is kept for backward compatibility but should not be used
     * as it doesn't support multiple trigger nodes
     */
    @Deprecated
    public Map<String, Object> getTriggerData() {
        // Return first trigger data if available (for backward compatibility)
        if (!triggerDataMap.isEmpty()) {
            return triggerDataMap.values().iterator().next();
    }
        return new HashMap<>();
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    /**
     * Get data for a node (combines variables and previous node outputs)
     * Note: Trigger data is now accessed via _nodeOutputs.{triggerNodeId} to support multiple triggers
     */
    public Map<String, Object> getDataForNode(String nodeId) {
        Map<String, Object> data = new HashMap<>();
        // Variables are global and available to all nodes
        data.putAll(variables);
        // Node outputs include trigger node outputs, accessible via _nodeOutputs.{nodeId}
        data.put("_nodeOutputs", nodeOutputs);
        // Metadata for execution info
        data.put("_metadata", metadata);
        return data;
    }

    /**
     * Store wait state reference
     */
    public void setWaitState(String waitStateId, String nodeId) {
        this.waitStateId = waitStateId;
        this.waitingNodeId = nodeId;
    }

    /**
     * Get wait state ID
     */
    public String getWaitStateId() {
        return waitStateId;
    }

    /**
     * Get waiting node ID
     */
    public String getWaitingNodeId() {
        return waitingNodeId;
    }

    /**
     * Check if execution is waiting for events
     */
    public boolean isWaiting() {
        return waitStateId != null && !waitStateId.isEmpty();
    }

    /**
     * Get trigger data map for serialization.
     * Used by ExecutionContextCache and ExecutionStateService.
     */
    public Map<String, Map<String, Object>> getTriggerDataMap() {
        return triggerDataMap;
    }
}

