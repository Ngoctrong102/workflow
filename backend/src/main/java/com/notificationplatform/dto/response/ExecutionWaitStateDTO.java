package com.notificationplatform.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ExecutionWaitStateDTO {

    private String id;
    private String executionId;
    private String nodeId;
    private String correlationId;
    private String aggregationStrategy;
    private List<String> requiredEvents;
    private List<String> enabledEvents;
    private Boolean apiCallEnabled;
    private Boolean kafkaEventEnabled;
    private Map<String, Object> apiResponseData;
    private Map<String, Object> kafkaEventData;
    private List<String> receivedEvents;
    private String status;
    private LocalDateTime resumedAt;
    private String resumedBy;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getAggregationStrategy() {
        return aggregationStrategy;
    }

    public void setAggregationStrategy(String aggregationStrategy) {
        this.aggregationStrategy = aggregationStrategy;
    }

    public List<String> getRequiredEvents() {
        return requiredEvents;
    }

    public void setRequiredEvents(List<String> requiredEvents) {
        this.requiredEvents = requiredEvents;
    }

    public List<String> getEnabledEvents() {
        return enabledEvents;
    }

    public void setEnabledEvents(List<String> enabledEvents) {
        this.enabledEvents = enabledEvents;
    }

    public Boolean getApiCallEnabled() {
        return apiCallEnabled;
    }

    public void setApiCallEnabled(Boolean apiCallEnabled) {
        this.apiCallEnabled = apiCallEnabled;
    }

    public Boolean getKafkaEventEnabled() {
        return kafkaEventEnabled;
    }

    public void setKafkaEventEnabled(Boolean kafkaEventEnabled) {
        this.kafkaEventEnabled = kafkaEventEnabled;
    }

    public Map<String, Object> getApiResponseData() {
        return apiResponseData;
    }

    public void setApiResponseData(Map<String, Object> apiResponseData) {
        this.apiResponseData = apiResponseData;
    }

    public Map<String, Object> getKafkaEventData() {
        return kafkaEventData;
    }

    public void setKafkaEventData(Map<String, Object> kafkaEventData) {
        this.kafkaEventData = kafkaEventData;
    }

    public List<String> getReceivedEvents() {
        return receivedEvents;
    }

    public void setReceivedEvents(List<String> receivedEvents) {
        this.receivedEvents = receivedEvents;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getResumedAt() {
        return resumedAt;
    }

    public void setResumedAt(LocalDateTime resumedAt) {
        this.resumedAt = resumedAt;
    }

    public String getResumedBy() {
        return resumedBy;
    }

    public void setResumedBy(String resumedBy) {
        this.resumedBy = resumedBy;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}

