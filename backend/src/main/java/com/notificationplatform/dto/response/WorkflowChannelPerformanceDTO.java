package com.notificationplatform.dto.response;

public class WorkflowChannelPerformanceDTO {

    private String channelId;
    private String channelType;
    private String channelName;
    private Long notificationsSent;
    private Long notificationsDelivered;
    private Long notificationsFailed;
    private Double deliveryRate;
    private Double averageDeliveryTime; // in milliseconds
    private Long totalErrors;

    // Getters and Setters
    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public Long getNotificationsSent() {
        return notificationsSent;
    }

    public void setNotificationsSent(Long notificationsSent) {
        this.notificationsSent = notificationsSent;
    }

    public Long getNotificationsDelivered() {
        return notificationsDelivered;
    }

    public void setNotificationsDelivered(Long notificationsDelivered) {
        this.notificationsDelivered = notificationsDelivered;
    }

    public Long getNotificationsFailed() {
        return notificationsFailed;
    }

    public void setNotificationsFailed(Long notificationsFailed) {
        this.notificationsFailed = notificationsFailed;
    }

    public Double getDeliveryRate() {
        return deliveryRate;
    }

    public void setDeliveryRate(Double deliveryRate) {
        this.deliveryRate = deliveryRate;
    }

    public Double getAverageDeliveryTime() {
        return averageDeliveryTime;
    }

    public void setAverageDeliveryTime(Double averageDeliveryTime) {
        this.averageDeliveryTime = averageDeliveryTime;
    }

    public Long getTotalErrors() {
        return totalErrors;
    }

    public void setTotalErrors(Long totalErrors) {
        this.totalErrors = totalErrors;
    }
}

