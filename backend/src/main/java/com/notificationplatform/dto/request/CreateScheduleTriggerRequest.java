package com.notificationplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.Map;

public class CreateScheduleTriggerRequest {

    @NotBlank(message = "Workflow ID is required")
    private String workflowId;

    @NotBlank(message = "Cron expression is required")
    @Pattern(regexp = "^[0-9*,\\-/\\s]+$", message = "Invalid cron expression format")
    private String cronExpression;

    private String timezone = "UTC"; // Default to UTC

    private LocalDateTime startDate; // When schedule becomes active

    private LocalDateTime endDate; // When schedule expires (optional)

    private Map<String, Object> data; // Static data to pass to workflow

    // Getters and Setters
    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}

