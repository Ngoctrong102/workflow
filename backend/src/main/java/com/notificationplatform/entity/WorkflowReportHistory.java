package com.notificationplatform.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "workflow_report_history")
public class WorkflowReportHistory {

    @Id
    @Column(name = "id", length = 255)
    @NotBlank
    @Size(max = 255)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_report_id", nullable = false)
    @NotNull
    private WorkflowReport workflowReport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    @NotNull
    private Workflow workflow;

    @Column(name = "report_period_start", nullable = false)
    @NotNull
    private LocalDateTime reportPeriodStart;

    @Column(name = "report_period_end", nullable = false)
    @NotNull
    private LocalDateTime reportPeriodEnd;

    @Column(name = "file_path", length = 500)
    @Size(max = 500)
    private String filePath; // Storage path for report file

    @Column(name = "file_size")
    private Long fileSize; // File size in bytes

    @Column(name = "format", nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String format; // pdf, excel, csv

    @Column(name = "recipients", nullable = false, columnDefinition = "TEXT[]")
    @NotNull
    private List<String> recipients; // Recipients who received the report

    @Column(name = "delivery_status", nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String deliveryStatus; // sent, failed, partial

    @Column(name = "generated_at", nullable = false)
    @NotNull
    private LocalDateTime generatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public WorkflowReport getWorkflowReport() {
        return workflowReport;
    }

    public void setWorkflowReport(WorkflowReport workflowReport) {
        this.workflowReport = workflowReport;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
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

