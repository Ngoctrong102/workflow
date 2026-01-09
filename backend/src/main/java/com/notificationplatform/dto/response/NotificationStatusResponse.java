package com.notificationplatform.dto.response;

import java.util.List;

public class NotificationStatusResponse {

    private String id;
    private String status;
    private Integer totalRecipients;
    private Integer deliveredCount;
    private Integer failedCount;
    private Integer pendingCount;
    private List<DeliveryStatusResponse> deliveries;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getTotalRecipients() {
        return totalRecipients;
    }

    public void setTotalRecipients(Integer totalRecipients) {
        this.totalRecipients = totalRecipients;
    }

    public Integer getDeliveredCount() {
        return deliveredCount;
    }

    public void setDeliveredCount(Integer deliveredCount) {
        this.deliveredCount = deliveredCount;
    }

    public Integer getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(Integer failedCount) {
        this.failedCount = failedCount;
    }

    public Integer getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(Integer pendingCount) {
        this.pendingCount = pendingCount;
    }

    public List<DeliveryStatusResponse> getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(List<DeliveryStatusResponse> deliveries) {
        this.deliveries = deliveries;
    }
}

