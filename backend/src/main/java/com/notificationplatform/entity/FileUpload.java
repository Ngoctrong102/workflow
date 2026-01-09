package com.notificationplatform.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_uploads")
public class FileUpload {

    @Id
    @Column(name = "id", length = 255)
    @NotBlank
    @Size(max = 255)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trigger_id", nullable = false)
    @NotNull
    private Trigger trigger;

    @Column(name = "filename", nullable = false, length = 255)
    @NotBlank
    @Size(max = 255)
    private String filename;

    @Column(name = "file_size", nullable = false)
    @NotNull
    private Long fileSize; // File size in bytes

    @Column(name = "file_type", nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String fileType; // csv, json, xlsx

    @Column(name = "file_path", nullable = false, length = 500)
    @NotBlank
    @Size(max = 500)
    private String filePath; // Storage path

    @Column(name = "status", nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String status; // processing, completed, failed

    @Column(name = "rows_total")
    private Integer rowsTotal;

    @Column(name = "rows_processed")
    private Integer rowsProcessed = 0;

    @Column(name = "notifications_sent")
    private Integer notificationsSent = 0;

    @Column(name = "error", columnDefinition = "TEXT")
    private String error;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    @NotNull
    private LocalDateTime uploadedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public void setTrigger(Trigger trigger) {
        this.trigger = trigger;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getRowsTotal() {
        return rowsTotal;
    }

    public void setRowsTotal(Integer rowsTotal) {
        this.rowsTotal = rowsTotal;
    }

    public Integer getRowsProcessed() {
        return rowsProcessed;
    }

    public void setRowsProcessed(Integer rowsProcessed) {
        this.rowsProcessed = rowsProcessed;
    }

    public Integer getNotificationsSent() {
        return notificationsSent;
    }

    public void setNotificationsSent(Integer notificationsSent) {
        this.notificationsSent = notificationsSent;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}

