package com.notificationplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateAlertRuleRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    private String description;

    @NotBlank(message = "Rule type is required")
    @Size(max = 50, message = "Rule type must not exceed 50 characters")
    private String ruleType; // failure_threshold, rate_limit, response_time, error_rate

    private String channelType; // null for all channels

    @NotNull(message = "Threshold value is required")
    private Double thresholdValue;

    private String thresholdUnit; // count, percentage, milliseconds

    @NotBlank(message = "Condition is required")
    @Size(max = 50, message = "Condition must not exceed 50 characters")
    private String condition; // greater_than, less_than, equals

    @Size(max = 50, message = "Severity must not exceed 50 characters")
    private String severity = "warning"; // info, warning, critical

    private Boolean enabled = true;

    private List<String> notificationChannels; // Array of notification channels

    private List<String> notificationRecipients; // Array of email addresses or user IDs

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public Double getThresholdValue() {
        return thresholdValue;
    }

    public void setThresholdValue(Double thresholdValue) {
        this.thresholdValue = thresholdValue;
    }

    public String getThresholdUnit() {
        return thresholdUnit;
    }

    public void setThresholdUnit(String thresholdUnit) {
        this.thresholdUnit = thresholdUnit;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getNotificationChannels() {
        return notificationChannels;
    }

    public void setNotificationChannels(List<String> notificationChannels) {
        this.notificationChannels = notificationChannels;
    }

    public List<String> getNotificationRecipients() {
        return notificationRecipients;
    }

    public void setNotificationRecipients(List<String> notificationRecipients) {
        this.notificationRecipients = notificationRecipients;
    }
}

