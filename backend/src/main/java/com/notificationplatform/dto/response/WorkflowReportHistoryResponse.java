package com.notificationplatform.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class WorkflowReportHistoryResponse {

    private String id;
    private String workflowReportId;
    private String workflowId;
    private LocalDateTime reportPeriodStart;
    private LocalDateTime reportPeriodEnd;
    private String filePath;
    private Long fileSize;
    private String format;
    private List<String> recipients;
    private String deliveryStatus;
    private LocalDateTime generatedAt;
    private LocalDateTime createdAt;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWorkflowReportId() {
        return workflowReportId;
    }

    public void setWorkflowReportId(String workflowReportId) {
        this.workflowReportId = workflowReportId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

