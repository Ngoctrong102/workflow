package com.notificationplatform.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationResponse {

    private String id;
    private String executionId;
    private String workflowId;
    private String channel;
    private String templateId;
    private String status; // queued, sending, sent, failed
    private Integer recipientsCount;
    private List<DeliveryStatusResponse> deliveries;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;

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

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getRecipientsCount() {
        return recipientsCount;
    }

    public void setRecipientsCount(Integer recipientsCount) {
        this.recipientsCount = recipientsCount;
    }

    public List<DeliveryStatusResponse> getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(List<DeliveryStatusResponse> deliveries) {
        this.deliveries = deliveries;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}

