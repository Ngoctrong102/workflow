package com.notificationplatform.dto.request;

import jakarta.validation.constraints.Size;

import java.time.LocalTime;
import java.util.List;

public class UpdateWorkflowReportRequest {

    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    private List<String> recipients;

    @Size(max = 50, message = "Schedule type must not exceed 50 characters")
    private String scheduleType;

    private LocalTime scheduleTime;

    private Integer scheduleDay;

    @Size(max = 255, message = "Schedule cron must not exceed 255 characters")
    private String scheduleCron;

    @Size(max = 100, message = "Timezone must not exceed 100 characters")
    private String timezone;

    @Size(max = 50, message = "Format must not exceed 50 characters")
    private String format;

    private List<String> sections;

    @Size(max = 50, message = "Status must not exceed 50 characters")
    private String status;

    // Getters and Setters
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
}

