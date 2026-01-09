package com.notificationplatform.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public class ErrorAnalyticsResponse {

    private LocalDate startDate;
    private LocalDate endDate;
    private long totalErrors;
    private Map<String, Long> errorsByType;
    private Map<String, Long> errorsByWorkflow;
    private Map<String, Long> errorsByChannel;
    private Map<String, Long> errorsByNode;
    private double errorRate;
    private LocalDateTime lastErrorAt;
    private Map<String, Double> errorTrends; // Error count by date

    // Getters and Setters
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public long getTotalErrors() {
        return totalErrors;
    }

    public void setTotalErrors(long totalErrors) {
        this.totalErrors = totalErrors;
    }

    public Map<String, Long> getErrorsByType() {
        return errorsByType;
    }

    public void setErrorsByType(Map<String, Long> errorsByType) {
        this.errorsByType = errorsByType;
    }

    public Map<String, Long> getErrorsByWorkflow() {
        return errorsByWorkflow;
    }

    public void setErrorsByWorkflow(Map<String, Long> errorsByWorkflow) {
        this.errorsByWorkflow = errorsByWorkflow;
    }

    public Map<String, Long> getErrorsByChannel() {
        return errorsByChannel;
    }

    public void setErrorsByChannel(Map<String, Long> errorsByChannel) {
        this.errorsByChannel = errorsByChannel;
    }

    public Map<String, Long> getErrorsByNode() {
        return errorsByNode;
    }

    public void setErrorsByNode(Map<String, Long> errorsByNode) {
        this.errorsByNode = errorsByNode;
    }

    public double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(double errorRate) {
        this.errorRate = errorRate;
    }

    public LocalDateTime getLastErrorAt() {
        return lastErrorAt;
    }

    public void setLastErrorAt(LocalDateTime lastErrorAt) {
        this.lastErrorAt = lastErrorAt;
    }

    public Map<String, Double> getErrorTrends() {
        return errorTrends;
    }

    public void setErrorTrends(Map<String, Double> errorTrends) {
        this.errorTrends = errorTrends;
    }
}

