package com.notificationplatform.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public class WorkflowDashboardMetricsDTO {

    private Long totalExecutions;
    private Long successfulExecutions;
    private Long failedExecutions;
    private Long runningExecutions;
    private Double successRate;
    private Double errorRate;
    private Double averageExecutionTime; // in milliseconds
    private Long totalNotificationsSent;
    private Long totalNotificationsDelivered;
    private Double deliveryRate;
    private Map<String, Long> executionsByStatus;
    private Map<String, Long> executionsByTriggerType;
    private LocalDateTime lastExecutionAt;
    private LocalDateTime firstExecutionAt;
    private TrendComparison trendComparison;

    // Getters and Setters
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

    public Long getRunningExecutions() {
        return runningExecutions;
    }

    public void setRunningExecutions(Long runningExecutions) {
        this.runningExecutions = runningExecutions;
    }

    public Double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(Double successRate) {
        this.successRate = successRate;
    }

    public Double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(Double errorRate) {
        this.errorRate = errorRate;
    }

    public Double getAverageExecutionTime() {
        return averageExecutionTime;
    }

    public void setAverageExecutionTime(Double averageExecutionTime) {
        this.averageExecutionTime = averageExecutionTime;
    }

    public Long getTotalNotificationsSent() {
        return totalNotificationsSent;
    }

    public void setTotalNotificationsSent(Long totalNotificationsSent) {
        this.totalNotificationsSent = totalNotificationsSent;
    }

    public Long getTotalNotificationsDelivered() {
        return totalNotificationsDelivered;
    }

    public void setTotalNotificationsDelivered(Long totalNotificationsDelivered) {
        this.totalNotificationsDelivered = totalNotificationsDelivered;
    }

    public Double getDeliveryRate() {
        return deliveryRate;
    }

    public void setDeliveryRate(Double deliveryRate) {
        this.deliveryRate = deliveryRate;
    }

    public Map<String, Long> getExecutionsByStatus() {
        return executionsByStatus;
    }

    public void setExecutionsByStatus(Map<String, Long> executionsByStatus) {
        this.executionsByStatus = executionsByStatus;
    }

    public Map<String, Long> getExecutionsByTriggerType() {
        return executionsByTriggerType;
    }

    public void setExecutionsByTriggerType(Map<String, Long> executionsByTriggerType) {
        this.executionsByTriggerType = executionsByTriggerType;
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

    public TrendComparison getTrendComparison() {
        return trendComparison;
    }

    public void setTrendComparison(TrendComparison trendComparison) {
        this.trendComparison = trendComparison;
    }

    public static class TrendComparison {
        private Double executionCountChange; // percentage change
        private Double successRateChange;
        private Double averageExecutionTimeChange;
        private Double deliveryRateChange;

        public Double getExecutionCountChange() {
            return executionCountChange;
        }

        public void setExecutionCountChange(Double executionCountChange) {
            this.executionCountChange = executionCountChange;
        }

        public Double getSuccessRateChange() {
            return successRateChange;
        }

        public void setSuccessRateChange(Double successRateChange) {
            this.successRateChange = successRateChange;
        }

        public Double getAverageExecutionTimeChange() {
            return averageExecutionTimeChange;
        }

        public void setAverageExecutionTimeChange(Double averageExecutionTimeChange) {
            this.averageExecutionTimeChange = averageExecutionTimeChange;
        }

        public Double getDeliveryRateChange() {
            return deliveryRateChange;
        }

        public void setDeliveryRateChange(Double deliveryRateChange) {
            this.deliveryRateChange = deliveryRateChange;
        }
    }
}

