package com.notificationplatform.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "workflow_reports")
public class WorkflowReport {

    @Id
    @Column(name = "id", length = 255)
    @NotBlank
    @Size(max = 255)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false, unique = true)
    @NotNull
    private Workflow workflow;

    @Column(name = "name", nullable = false, length = 255)
    @NotBlank
    @Size(max = 255)
    private String name;

    @Column(name = "recipients", nullable = false, columnDefinition = "TEXT[]")
    @NotNull
    private List<String> recipients; // Array of email addresses

    @Column(name = "schedule_type", nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String scheduleType; // daily, weekly, monthly, custom

    @Column(name = "schedule_time")
    private LocalTime scheduleTime; // Time of day for daily/weekly/monthly

    @Column(name = "schedule_day")
    private Integer scheduleDay; // Day of week (1-7, Monday=1) for weekly, or day of month (1-31) for monthly

    @Column(name = "schedule_cron", length = 255)
    @Size(max = 255)
    private String scheduleCron; // Cron expression for custom schedules

    @Column(name = "timezone", length = 100)
    @Size(max = 100)
    private String timezone = "UTC"; // Timezone for schedule

    @Column(name = "format", nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String format = "pdf"; // pdf, excel, csv

    @Column(name = "sections", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> sections; // Array of section IDs to include

    @Column(name = "status", nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String status = "inactive"; // active, inactive, paused

    @Column(name = "last_generated_at")
    private LocalDateTime lastGeneratedAt;

    @Column(name = "next_generation_at")
    private LocalDateTime nextGenerationAt;

    @Column(name = "last_generation_status", length = 50)
    @Size(max = 50)
    private String lastGenerationStatus; // success, failed

    @Column(name = "last_generation_error", columnDefinition = "TEXT")
    private String lastGenerationError;

    @Column(name = "generation_count")
    private Integer generationCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @NotNull
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    public LocalTime getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(LocalTime scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public Integer getScheduleDay() {
        return scheduleDay;
    }

    public void setScheduleDay(Integer scheduleDay) {
        this.scheduleDay = scheduleDay;
    }

    public String getScheduleCron() {
        return scheduleCron;
    }

    public void setScheduleCron(String scheduleCron) {
        this.scheduleCron = scheduleCron;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public List<String> getSections() {
        return sections;
    }

    public void setSections(List<String> sections) {
        this.sections = sections;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLastGeneratedAt() {
        return lastGeneratedAt;
    }

    public void setLastGeneratedAt(LocalDateTime lastGeneratedAt) {
        this.lastGeneratedAt = lastGeneratedAt;
    }

    public LocalDateTime getNextGenerationAt() {
        return nextGenerationAt;
    }

    public void setNextGenerationAt(LocalDateTime nextGenerationAt) {
        this.nextGenerationAt = nextGenerationAt;
    }

    public String getLastGenerationStatus() {
        return lastGenerationStatus;
    }

    public void setLastGenerationStatus(String lastGenerationStatus) {
        this.lastGenerationStatus = lastGenerationStatus;
    }

    public String getLastGenerationError() {
        return lastGenerationError;
    }

    public void setLastGenerationError(String lastGenerationError) {
        this.lastGenerationError = lastGenerationError;
    }

    public Integer getGenerationCount() {
        return generationCount;
    }

    public void setGenerationCount(Integer generationCount) {
        this.generationCount = generationCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}

