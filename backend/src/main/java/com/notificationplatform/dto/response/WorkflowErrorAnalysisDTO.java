package com.notificationplatform.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public class WorkflowErrorAnalysisDTO {

    private String errorId;
    private String errorType;
    private String errorMessage;
    private String nodeId;
    private String nodeType;
    private String executionId;
    private LocalDateTime occurredAt;
    private Long occurrenceCount;
    private Map<String, Object> errorContext;

    // Getters and Setters
    public String getErrorId() {
        return errorId;
    }

    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
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

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Long getOccurrenceCount() {
        return occurrenceCount;
    }

    public void setOccurrenceCount(Long occurrenceCount) {
        this.occurrenceCount = occurrenceCount;
    }

    public Map<String, Object> getErrorContext() {
        return errorContext;
    }

    public void setErrorContext(Map<String, Object> errorContext) {
        this.errorContext = errorContext;
    }
}

