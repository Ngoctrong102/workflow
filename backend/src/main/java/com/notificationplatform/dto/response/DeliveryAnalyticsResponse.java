package com.notificationplatform.dto.response;

import java.util.Map;

public class DeliveryAnalyticsResponse {

    private Long totalSent;
    private Long delivered;
    private Long failed;
    private Long pending;
    private Double deliveryRate;
    private Map<String, Long> byChannel; // Channel breakdown
    private Map<String, Long> byStatus; // Status breakdown

    // Getters and Setters
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

    public Long getPending() {
        return pending;
    }

    public void setPending(Long pending) {
        this.pending = pending;
    }

    public Double getDeliveryRate() {
        return deliveryRate;
    }

    public void setDeliveryRate(Double deliveryRate) {
        this.deliveryRate = deliveryRate;
    }

    public Map<String, Long> getByChannel() {
        return byChannel;
    }

    public void setByChannel(Map<String, Long> byChannel) {
        this.byChannel = byChannel;
    }

    public Map<String, Long> getByStatus() {
        return byStatus;
    }

    public void setByStatus(Map<String, Long> byStatus) {
        this.byStatus = byStatus;
    }
}

