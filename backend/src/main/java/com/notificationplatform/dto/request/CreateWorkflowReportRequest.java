package com.notificationplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;
import java.util.List;

public class CreateWorkflowReportRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @NotNull(message = "Recipients are required")
    private List<String> recipients; // Array of email addresses

    @NotBlank(message = "Schedule type is required")
    @Size(max = 50, message = "Schedule type must not exceed 50 characters")
    private String scheduleType; // daily, weekly, monthly, custom

    private LocalTime scheduleTime; // Time of day for daily/weekly/monthly

    private Integer scheduleDay; // Day of week (1-7, Monday=1) for weekly, or day of month (1-31) for monthly

    @Size(max = 255, message = "Schedule cron must not exceed 255 characters")
    private String scheduleCron; // Cron expression for custom schedules

    @Size(max = 100, message = "Timezone must not exceed 100 characters")
    private String timezone = "UTC"; // Timezone for schedule

    @NotBlank(message = "Format is required")
    @Size(max = 50, message = "Format must not exceed 50 characters")
    private String format = "pdf"; // pdf, excel, csv

    private List<String> sections; // Array of section IDs to include

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
}

