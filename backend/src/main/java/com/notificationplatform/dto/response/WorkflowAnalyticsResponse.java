package com.notificationplatform.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public class WorkflowAnalyticsResponse {

    private String workflowId;
    private String workflowName;
    private Long totalExecutions;
    private Long successfulExecutions;
    private Long failedExecutions;
    private Double averageExecutionTime; // in milliseconds
    private Double successRate;
    private Map<String, Long> executionsByStatus;
    private LocalDateTime lastExecutionAt;
    private LocalDateTime firstExecutionAt;

    // Getters and Setters
    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public Long getTotalExecutions() {
        return totalExecutions;
    }

    public void setTotalExecutions(Long totalExecutions) {
        this.totalExecutions = totalExecutions;
    }

    public Long getSuccessfulExecutions() {
        return successfulExecutions;
    }

    public void setSuccessfulExecutions(Long successfulExecutions) {
        this.successfulExecutions = successfulExecutions;
    }

    public Long getFailedExecutions() {
        return failedExecutions;
    }

    public void setFailedExecutions(Long failedExecutions) {
        this.failedExecutions = failedExecutions;
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

    public Map<String, Long> getExecutionsByStatus() {
        return executionsByStatus;
    }

    public void setExecutionsByStatus(Map<String, Long> executionsByStatus) {
        this.executionsByStatus = executionsByStatus;
    }

    public LocalDateTime getLastExecutionAt() {
        return lastExecutionAt;
    }

    public void setLastExecutionAt(LocalDateTime lastExecutionAt) {
        this.lastExecutionAt = lastExecutionAt;
    }

    public LocalDateTime getFirstExecutionAt() {
        return firstExecutionAt;
    }

    public void setFirstExecutionAt(LocalDateTime firstExecutionAt) {
        this.firstExecutionAt = firstExecutionAt;
    }
}

