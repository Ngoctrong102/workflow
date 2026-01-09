package com.notificationplatform.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public class WorkflowReportPreviewResponse {

    private String workflowId;
    private String workflowName;
    private LocalDateTime reportPeriodStart;
    private LocalDateTime reportPeriodEnd;
    private Map<String, Object> executionSummary;
    private Map<String, Object> notificationSummary;
    private Map<String, Object> performanceMetrics;
    private Map<String, Object> errorAnalysis;

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

    public LocalDateTime getReportPeriodStart() {
        return reportPeriodStart;
    }

    public void setReportPeriodStart(LocalDateTime reportPeriodStart) {
        this.reportPeriodStart = reportPeriodStart;
    }

    public LocalDateTime getReportPeriodEnd() {
        return reportPeriodEnd;
    }

    public void setReportPeriodEnd(LocalDateTime reportPeriodEnd) {
        this.reportPeriodEnd = reportPeriodEnd;
    }

    public Map<String, Object> getExecutionSummary() {
        return executionSummary;
    }

    public void setExecutionSummary(Map<String, Object> executionSummary) {
        this.executionSummary = executionSummary;
    }

    public Map<String, Object> getNotificationSummary() {
        return notificationSummary;
    }

    public void setNotificationSummary(Map<String, Object> notificationSummary) {
        this.notificationSummary = notificationSummary;
    }

    public Map<String, Object> getPerformanceMetrics() {
        return performanceMetrics;
    }

    public void setPerformanceMetrics(Map<String, Object> performanceMetrics) {
        this.performanceMetrics = performanceMetrics;
    }

    public Map<String, Object> getErrorAnalysis() {
        return errorAnalysis;
    }

    public void setErrorAnalysis(Map<String, Object> errorAnalysis) {
        this.errorAnalysis = errorAnalysis;
    }
}

