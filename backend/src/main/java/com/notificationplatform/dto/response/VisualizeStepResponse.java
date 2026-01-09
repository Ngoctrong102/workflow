package com.notificationplatform.dto.response;

import java.util.Map;

/**
 * Response DTO for executing a step in visualization.
 * 
 * See: @import(api/endpoints.md#execution-visualization)
 */
public class VisualizeStepResponse {

    private Integer stepNumber;
    private String nodeId;
    private String nodeType;
    private String status;
    private ExecutionVisualizationResponse.ExecutionInfo execution;
    private Map<String, Object> context;
    private String nextNode;
    private Boolean hasNext;
    private Boolean hasPrevious;

    // Getters and Setters
    public Integer getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(Integer stepNumber) {
        this.stepNumber = stepNumber;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ExecutionVisualizationResponse.ExecutionInfo getExecution() {
        return execution;
    }

    public void setExecution(ExecutionVisualizationResponse.ExecutionInfo execution) {
        this.execution = execution;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    public String getNextNode() {
        return nextNode;
    }

    public void setNextNode(String nextNode) {
        this.nextNode = nextNode;
    }

    public Boolean getHasNext() {
        return hasNext;
    }

    public void setHasNext(Boolean hasNext) {
        this.hasNext = hasNext;
    }

    public Boolean getHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(Boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
}

