package com.notificationplatform.dto.response;

import java.util.Map;

public class ChannelAnalyticsResponse {

    private String channelId;
    private String channelName;
    private String channelType;
    private Long totalSent;
    private Long delivered;
    private Long failed;
    private Double deliveryRate;
    private Double averageDeliveryTime; // in milliseconds
    private Map<String, Long> errorsByType;

    // Getters and Setters
    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public Long getTotalSent() {
        return totalSent;
    }

    public void setTotalSent(Long totalSent) {
        this.totalSent = totalSent;
    }

    public Long getDelivered() {
        return delivered;
    }

    public void setDelivered(Long delivered) {
        this.delivered = delivered;
    }

    public Long getFailed() {
        return failed;
    }

    public void setFailed(Long failed) {
        this.failed = failed;
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

    public Map<String, Long> getErrorsByType() {
        return errorsByType;
    }

    public void setErrorsByType(Map<String, Long> errorsByType) {
        this.errorsByType = errorsByType;
    }
}

