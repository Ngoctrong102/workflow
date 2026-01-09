package com.notificationplatform.dto.response;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class WorkflowReportResponse {

    private String id;
    private String workflowId;
    private String name;
    private List<String> recipients;
    private String scheduleType;
    private LocalTime scheduleTime;
    private Integer scheduleDay;
    private String scheduleCron;
    private String timezone;
    private String format;
    private List<String> sections;
    private String status;
    private LocalDateTime lastGeneratedAt;
    private LocalDateTime nextGenerationAt;
    private String lastGenerationStatus;
    private String lastGenerationError;
    private Integer generationCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
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
}

