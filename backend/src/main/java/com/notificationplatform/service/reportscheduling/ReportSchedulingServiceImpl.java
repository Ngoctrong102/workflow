package com.notificationplatform.service.reportscheduling;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notificationplatform.dto.request.CreateScheduledReportRequest;
import com.notificationplatform.dto.request.UpdateScheduledReportRequest;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.dto.response.ScheduledReportResponse;
import com.notificationplatform.entity.ScheduledReport;
import com.notificationplatform.exception.ResourceNotFoundException;
import com.notificationplatform.repository.ScheduledReportRepository;
import com.notificationplatform.service.analytics.AnalyticsService;
import com.notificationplatform.service.trigger.schedule.CronValidator;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@Transactional
public class ReportSchedulingServiceImpl implements ReportSchedulingService {

    private final ScheduledReportRepository scheduledReportRepository;
    private final AnalyticsService analyticsService;
    private final ReportGeneratorService reportGeneratorService;
    private final CronValidator cronValidator;
    private final ObjectMapper objectMapper;

    public ReportSchedulingServiceImpl(ScheduledReportRepository scheduledReportRepository,
                                      AnalyticsService analyticsService,
                                      ReportGeneratorService reportGeneratorService,
                                      CronValidator cronValidator,
                                      ObjectMapper objectMapper) {
        this.scheduledReportRepository = scheduledReportRepository;
        this.analyticsService = analyticsService;
        this.reportGeneratorService = reportGeneratorService;
        this.cronValidator = cronValidator;
        this.objectMapper = objectMapper;
    }

    @Override
    public ScheduledReportResponse createScheduledReport(CreateScheduledReportRequest request) {
        ScheduledReport report = new ScheduledReport();
        report.setId(UUID.randomUUID().toString());
        report.setName(request.getName());
        report.setDescription(request.getDescription());
        report.setReportType(request.getReportType());
        report.setFrequency(request.getFrequency());
        report.setFormat(request.getFormat());
        report.setRecipients(request.getRecipients());
        report.setStatus("active");
        report.setCreatedBy(request.getCreatedBy());

        // Set cron expression
        String cronExpression = request.getCronExpression();
        if (cronExpression == null || cronExpression.isEmpty()) {
            cronExpression = generateCronFromFrequency(request.getFrequency());
        }

        // Validate cron expression
        if (!cronValidator.isValid(cronExpression)) {
            throw new IllegalArgumentException("Invalid cron expression: " + cronExpression);
        }

        report.setCronExpression(cronValidator.convertTo6Field(cronExpression));

        // Set filters
        if (request.getFilters() != null) {
            report.setFilters(request.getFilters());
        }

        // Calculate next run time
        report.setNextRunAt(calculateNextRunTime(report.getCronExpression()));

        report = scheduledReportRepository.save(report);

        log.info("Created scheduled report: id={}, name={}, frequency={}", 
                   report.getId(), report.getName(), report.getFrequency());

        return toResponse(report);
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduledReportResponse getScheduledReport(String id) {
        ScheduledReport report = scheduledReportRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled report not found with id: " + id));
        return toResponse(report);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ScheduledReportResponse> listScheduledReports(String reportType, String status, int limit, int offset) {
        // Validate pagination
        if (limit < 1) limit = 20;
        if (limit > 100) limit = 100;
        if (offset < 0) offset = 0;

        List<ScheduledReport> reports = scheduledReportRepository.findAllActive();

        // Filter by report type if provided
        if (reportType != null && !reportType.isEmpty()) {
            reports = reports.stream()
                    .filter(r -> reportType.equals(r.getReportType()))
                    .collect(Collectors.toList());
        }

        // Filter by status if provided
        if (status != null && !status.isEmpty()) {
            reports = reports.stream()
                    .filter(r -> status.equals(r.getStatus()))
                    .collect(Collectors.toList());
        }

        long total = reports.size();

        // Apply pagination
        int fromIndex = Math.min(offset, reports.size());
        int toIndex = Math.min(offset + limit, reports.size());
        List<ScheduledReport> pagedReports = reports.subList(fromIndex, toIndex);

        List<ScheduledReportResponse> responses = pagedReports.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(responses, total, limit, offset);
    }

    @Override
    public ScheduledReportResponse updateScheduledReport(String id, UpdateScheduledReportRequest request) {
        ScheduledReport report = scheduledReportRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled report not found with id: " + id));

        if (request.getName() != null) {
            report.setName(request.getName());
        }
        if (request.getDescription() != null) {
            report.setDescription(request.getDescription());
        }
        if (request.getFrequency() != null) {
            report.setFrequency(request.getFrequency());
            // Update cron expression if frequency changed
            String cronExpression = request.getCronExpression();
            if (cronExpression == null || cronExpression.isEmpty()) {
                cronExpression = generateCronFromFrequency(request.getFrequency());
            }
            if (cronValidator.isValid(cronExpression)) {
                report.setCronExpression(cronValidator.convertTo6Field(cronExpression));
                report.setNextRunAt(calculateNextRunTime(report.getCronExpression()));
            }
        }
        if (request.getCronExpression() != null) {
            if (cronValidator.isValid(request.getCronExpression())) {
                report.setCronExpression(cronValidator.convertTo6Field(request.getCronExpression()));
                report.setNextRunAt(calculateNextRunTime(report.getCronExpression()));
            } else {
                throw new IllegalArgumentException("Invalid cron expression: " + request.getCronExpression());
            }
        }
        if (request.getFormat() != null) {
            report.setFormat(request.getFormat());
        }
        if (request.getRecipients() != null) {
            report.setRecipients(request.getRecipients());
        }
        if (request.getFilters() != null) {
            report.setFilters(request.getFilters());
        }
        if (request.getStatus() != null) {
            report.setStatus(request.getStatus());
        }

        report = scheduledReportRepository.save(report);

        log.info("Updated scheduled report: id={}", report.getId());

        return toResponse(report);
    }

    @Override
    public void deleteScheduledReport(String id) {
        ScheduledReport report = scheduledReportRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled report not found with id: " + id));

        report.setDeletedAt(LocalDateTime.now());
        report.setStatus("inactive");
        scheduledReportRepository.save(report);

        log.info("Deleted scheduled report: id={}", id);
    }

    @Override
    public ScheduledReportResponse pauseScheduledReport(String id) {
        ScheduledReport report = scheduledReportRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled report not found with id: " + id));

        report.setStatus("paused");
        report = scheduledReportRepository.save(report);

        log.info("Paused scheduled report: id={}", id);

        return toResponse(report);
    }

    @Override
    public ScheduledReportResponse resumeScheduledReport(String id) {
        ScheduledReport report = scheduledReportRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled report not found with id: " + id));

        report.setStatus("active");
        if (report.getNextRunAt() == null) {
            report.setNextRunAt(calculateNextRunTime(report.getCronExpression()));
        }
        report = scheduledReportRepository.save(report);

        log.info("Resumed scheduled report: id={}", id);

        return toResponse(report);
    }

    @Override
    public ScheduledReportResponse executeScheduledReport(String id) {
        ScheduledReport report = scheduledReportRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled report not found with id: " + id));

        try {
            generateAndSendReport(report);
            report.setLastRunAt(LocalDateTime.now());
            report.setLastRunStatus("success");
            report.setLastRunError(null);
            report.setRunCount((report.getRunCount() != null ? report.getRunCount() : 0) + 1);
            report = scheduledReportRepository.save(report);

            log.info("Executed scheduled report: id={}", id);

            return toResponse(report);
        } catch (Exception e) {
            report.setLastRunAt(LocalDateTime.now());
            report.setLastRunStatus("failed");
            report.setLastRunError(e.getMessage());
            report = scheduledReportRepository.save(report);

            log.error("Error executing scheduled report: id={}", id, e);
            throw new RuntimeException("Error executing scheduled report: " + e.getMessage(), e);
        }
    }

    @Override
    @Scheduled(fixedRate = 60000) // Check every minute
    public void processDueReports() {
        LocalDateTime now = LocalDateTime.now();
        List<ScheduledReport> dueReports = scheduledReportRepository.findDueSchedules(now);

        for (ScheduledReport report : dueReports) {
            if ("active".equals(report.getStatus())) {
                try {
                    generateAndSendReport(report);
                    report.setLastRunAt(now);
                    report.setLastRunStatus("success");
                    report.setLastRunError(null);
                    report.setRunCount((report.getRunCount() != null ? report.getRunCount() : 0) + 1);
                    report.setNextRunAt(calculateNextRunTime(report.getCronExpression()));
                    scheduledReportRepository.save(report);

                    log.info("Processed scheduled report: id={}, name={}", report.getId(), report.getName());
                } catch (Exception e) {
                    report.setLastRunAt(now);
                    report.setLastRunStatus("failed");
                    report.setLastRunError(e.getMessage());
                    scheduledReportRepository.save(report);

                    log.error("Error processing scheduled report: id={}, name={}", report.getId(), report.getName(), e);
                }
            }
        }
    }

    private void generateAndSendReport(ScheduledReport report) {
        // Generate report data based on report type
        Object reportData = generateReportData(report);

        // Generate report file
        byte[] reportFile = reportGeneratorService.generateReport(reportData, report.getFormat());

        // Send report via email
        reportGeneratorService.sendReport(report, reportFile);
    }

    private Object generateReportData(ScheduledReport report) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30); // Default to last 30 days

        // Extract date range from filters if available
        if (report.getFilters() != null) {
            try {
                Map<String, Object> filters = objectMapper.convertValue(report.getFilters(), Map.class);
                if (filters.containsKey("startDate")) {
                    startDate = LocalDate.parse(filters.get("startDate").toString());
                }
                if (filters.containsKey("endDate")) {
                    endDate = LocalDate.parse(filters.get("endDate").toString());
                }
            } catch (Exception e) {
                log.warn("Error parsing filters for report: id={}", report.getId(), e);
            }
        }

        switch (report.getReportType()) {
            case "workflow":
                String workflowId = extractFilterValue(report, "workflowId");
                if (workflowId != null) {
                    return analyticsService.getWorkflowAnalytics(workflowId, startDate, endDate);
                }
                return analyticsService.getAllWorkflowsAnalytics(startDate, endDate);
            case "delivery":
                String channel = extractFilterValue(report, "channel");
                return analyticsService.getDeliveryAnalytics(startDate, endDate, channel);
            case "channel":
                String channelId = extractFilterValue(report, "channelId");
                if (channelId != null) {
                    return analyticsService.getChannelAnalytics(channelId, startDate, endDate);
                }
                return analyticsService.getAllChannelsAnalytics(startDate, endDate);
            default:
                throw new IllegalArgumentException("Unsupported report type: " + report.getReportType());
        }
    }

    private String extractFilterValue(ScheduledReport report, String key) {
        if (report.getFilters() == null) {
            return null;
        }
        try {
            Map<String, Object> filters = objectMapper.convertValue(report.getFilters(), Map.class);
            Object value = filters.get(key);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String generateCronFromFrequency(String frequency) {
        switch (frequency.toLowerCase()) {
            case "daily":
                return "0 0 9 * * *"; // 9 AM daily
            case "weekly":
                return "0 0 9 * * MON"; // 9 AM every Monday
            case "monthly":
                return "0 0 9 1 * *"; // 9 AM on 1st of every month
            default:
                return "0 0 9 * * *"; // Default to daily
        }
    }

    private LocalDateTime calculateNextRunTime(String cronExpression) {
        // Simple calculation - in production, use a proper cron parser
        // For now, assume daily at 9 AM
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.withHour(9).withMinute(0).withSecond(0).withNano(0);
        if (nextRun.isBefore(now) || nextRun.isEqual(now)) {
            nextRun = nextRun.plusDays(1);
        }
        return nextRun;
    }

    private ScheduledReportResponse toResponse(ScheduledReport report) {
        ScheduledReportResponse response = new ScheduledReportResponse();
        response.setId(report.getId());
        response.setName(report.getName());
        response.setDescription(report.getDescription());
        response.setReportType(report.getReportType());
        response.setFrequency(report.getFrequency());
        response.setCronExpression(report.getCronExpression());
        response.setFormat(report.getFormat());
        response.setRecipients(report.getRecipients());
        response.setStatus(report.getStatus());
        response.setLastRunAt(report.getLastRunAt());
        response.setNextRunAt(report.getNextRunAt());
        response.setLastRunStatus(report.getLastRunStatus());
        response.setLastRunError(report.getLastRunError());
        response.setRunCount(report.getRunCount());
        response.setCreatedBy(report.getCreatedBy());
        response.setCreatedAt(report.getCreatedAt());
        response.setUpdatedAt(report.getUpdatedAt());

        if (report.getFilters() != null) {
            try {
                Map<String, Object> filters = objectMapper.convertValue(report.getFilters(), Map.class);
                response.setFilters(filters);
            } catch (Exception e) {
                log.warn("Error converting filters to map: id={}", report.getId(), e);
            }
        }

        return response;
    }
}

