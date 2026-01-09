package com.notificationplatform.dto.response;

import java.util.Map;

public class WorkflowNodePerformanceDTO {

    private String nodeId;
    private String nodeType;
    private String nodeName;
    private Long executionCount;
    private Long successfulCount;
    private Long failedCount;
    private Double averageExecutionTime; // in milliseconds
    private Double successRate;
    private Long totalErrors;
    private Map<String, Long> errorsByType;

    // Getters and Setters
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

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Long getExecutionCount() {
        return executionCount;
    }

    public void setExecutionCount(Long executionCount) {
        this.executionCount = executionCount;
    }

    public Long getSuccessfulCount() {
        return successfulCount;
    }

    public void setSuccessfulCount(Long successfulCount) {
        this.successfulCount = successfulCount;
    }

    public Long getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(Long failedCount) {
        this.failedCount = failedCount;
    }

    public Double getAverageExecutionTime() {
        return averageExecutionTime;
    }

    public void setAverageExecutionTime(Double averageExecutionTime) {
        this.averageExecutionTime = averageExecutionTime;
    }

    public Double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(Double successRate) {
        this.successRate = successRate;
    }

    public Long getTotalErrors() {
        return totalErrors;
    }

    public void setTotalErrors(Long totalErrors) {
        this.totalErrors = totalErrors;
    }

    public Map<String, Long> getErrorsByType() {
        return errorsByType;
    }

    public void setErrorsByType(Map<String, Long> errorsByType) {
        this.errorsByType = errorsByType;
    }
}

