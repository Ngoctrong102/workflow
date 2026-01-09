package com.notificationplatform.engine;

import java.util.Map;

/**
 * Result of node execution
 */
public class NodeExecutionResult {

    private boolean success;
    private Map<String, Object> output;
    private String error;
    private String nextNodeId; // For conditional/logic nodes
    private long duration; // Execution duration in milliseconds
    private boolean waiting; // True if node is waiting for events

    public NodeExecutionResult() {
    }

    public NodeExecutionResult(boolean success, Map<String, Object> output) {
        this.success = success;
        this.output = output;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Map<String, Object> getOutput() {
        return output;
    }

    public void setOutput(Map<String, Object> output) {
        this.output = output;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getNextNodeId() {
        return nextNodeId;
    }

    public void setNextNodeId(String nextNodeId) {
        this.nextNodeId = nextNodeId;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean isWaiting() {
        return waiting;
    }

    public void setWaiting(boolean waiting) {
        this.waiting = waiting;
    }
}

