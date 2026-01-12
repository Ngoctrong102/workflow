package com.notificationplatform.service.workflowreport;

import com.notificationplatform.dto.mapper.WorkflowReportMapper;
import com.notificationplatform.dto.request.CreateWorkflowReportRequest;
import com.notificationplatform.dto.request.UpdateWorkflowReportRequest;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.dto.response.QueryValidationResponse;
import com.notificationplatform.dto.response.WorkflowReportHistoryResponse;
import com.notificationplatform.dto.response.WorkflowReportPreviewResponse;
import com.notificationplatform.dto.response.WorkflowReportResponse;
import com.notificationplatform.entity.Workflow;
import com.notificationplatform.entity.WorkflowReport;
import com.notificationplatform.entity.WorkflowReportHistory;
import com.notificationplatform.exception.ResourceNotFoundException;
import com.notificationplatform.repository.WorkflowRepository;
import com.notificationplatform.repository.WorkflowReportHistoryRepository;
import com.notificationplatform.repository.WorkflowReportRepository;
import com.notificationplatform.service.dashboard.WorkflowDashboardService;
import com.notificationplatform.service.trigger.schedule.CronValidator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@Transactional
public class WorkflowReportServiceImpl implements WorkflowReportService {

    private final WorkflowReportRepository workflowReportRepository;
    private final WorkflowReportHistoryRepository workflowReportHistoryRepository;
    private final WorkflowRepository workflowRepository;
    private final WorkflowReportMapper workflowReportMapper;
    private final WorkflowDashboardService workflowDashboardService;
    private final CronValidator cronValidator;
    private final ReportQueryExecutor reportQueryExecutor;

    public WorkflowReportServiceImpl(WorkflowReportRepository workflowReportRepository,
                                    WorkflowReportHistoryRepository workflowReportHistoryRepository,
                                    WorkflowRepository workflowRepository,
                                    WorkflowReportMapper workflowReportMapper,
                                    WorkflowDashboardService workflowDashboardService,
                                    CronValidator cronValidator,
                                    ReportQueryExecutor reportQueryExecutor) {
        this.workflowReportRepository = workflowReportRepository;
        this.workflowReportHistoryRepository = workflowReportHistoryRepository;
        this.workflowRepository = workflowRepository;
        this.workflowReportMapper = workflowReportMapper;
        this.workflowDashboardService = workflowDashboardService;
        this.cronValidator = cronValidator;
        this.reportQueryExecutor = reportQueryExecutor;
    }

    @Override
    public WorkflowReportResponse createWorkflowReport(String workflowId, CreateWorkflowReportRequest request) {
        log.info("Creating workflow report for workflow: {}", workflowId);

        // Validate workflow exists
        Workflow workflow = workflowRepository.findByIdAndNotDeleted(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + workflowId));

        // Check if report already exists for this workflow
        Optional<WorkflowReport> existingReport = workflowReportRepository.findByWorkflowId(workflowId);
        if (existingReport.isPresent() && existingReport.get().getDeletedAt() == null) {
            throw new IllegalArgumentException("Workflow report already exists for workflow: " + workflowId);
        }

        // Validate schedule
        validateSchedule(request.getScheduleType(), request.getScheduleCron(), 
                        request.getScheduleTime(), request.getScheduleDay());

        // Create report entity
        WorkflowReport report = workflowReportMapper.toEntity(request);
        report.setId(UUID.randomUUID().toString());
        report.setWorkflow(workflow);

        // Calculate next generation time
        LocalDateTime nextGenerationAt = calculateNextGenerationTime(
                request.getScheduleType(),
                request.getScheduleCron(),
                request.getScheduleTime(),
                request.getScheduleDay(),
                request.getTimezone()
        );
        report.setNextGenerationAt(nextGenerationAt);

        report = workflowReportRepository.save(report);

        log.info("Created workflow report: id={}, workflowId={}, name={}", 
                   report.getId(), workflowId, report.getName());

        return workflowReportMapper.toResponse(report);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowReportResponse getWorkflowReport(String workflowId) {
        WorkflowReport report = workflowReportRepository.findByWorkflowId(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Workflow report not found for workflow: " + workflowId));
        
        if (report.getDeletedAt() != null) {
            throw new ResourceNotFoundException(
                    "Workflow report not found for workflow: " + workflowId);
        }

        return workflowReportMapper.toResponse(report);
    }

    @Override
    public WorkflowReportResponse updateWorkflowReport(String workflowId, UpdateWorkflowReportRequest request) {
        log.info("Updating workflow report for workflow: {}", workflowId);

        WorkflowReport report = workflowReportRepository.findByWorkflowId(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Workflow report not found for workflow: " + workflowId));

        if (report.getDeletedAt() != null) {
            throw new ResourceNotFoundException(
                    "Workflow report not found for workflow: " + workflowId);
        }

        // Update fields
        workflowReportMapper.updateEntity(report, request);

        // Recalculate next generation time if schedule changed
        if (request.getScheduleType() != null || request.getScheduleCron() != null ||
            request.getScheduleTime() != null || request.getScheduleDay() != null) {
            
            String scheduleType = request.getScheduleType() != null ? 
                    request.getScheduleType() : report.getScheduleType();
            String scheduleCron = request.getScheduleCron() != null ? 
                    request.getScheduleCron() : report.getScheduleCron();
            LocalTime scheduleTime = request.getScheduleTime() != null ? 
                    request.getScheduleTime() : report.getScheduleTime();
            Integer scheduleDay = request.getScheduleDay() != null ? 
                    request.getScheduleDay() : report.getScheduleDay();
            String timezone = request.getTimezone() != null ? 
                    request.getTimezone() : report.getTimezone();

            // Validate schedule
            validateSchedule(scheduleType, scheduleCron, scheduleTime, scheduleDay);

            // Recalculate next generation time
            LocalDateTime nextGenerationAt = calculateNextGenerationTime(
                    scheduleType, scheduleCron, scheduleTime, scheduleDay, timezone);
            report.setNextGenerationAt(nextGenerationAt);
        }

        report = workflowReportRepository.save(report);

        log.info("Updated workflow report: id={}, workflowId={}", report.getId(), workflowId);

        return workflowReportMapper.toResponse(report);
    }

    @Override
    public void deleteWorkflowReport(String workflowId) {
        log.info("Deleting workflow report for workflow: {}", workflowId);

        WorkflowReport report = workflowReportRepository.findByWorkflowId(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Workflow report not found for workflow: " + workflowId));

        if (report.getDeletedAt() != null) {
            return; // Already deleted
        }

        report.setDeletedAt(LocalDateTime.now());
        report.setStatus("inactive");
        workflowReportRepository.save(report);

        log.info("Deleted workflow report: id={}, workflowId={}", report.getId(), workflowId);
    }

    @Override
    public WorkflowReportHistoryResponse generateReport(String workflowId) {
        log.info("Generating report for workflow: {}", workflowId);

        WorkflowReport report = workflowReportRepository.findByWorkflowId(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Workflow report not found for workflow: " + workflowId));

        if (report.getDeletedAt() != null) {
            throw new ResourceNotFoundException(
                    "Workflow report not found for workflow: " + workflowId);
        }

        try {
            // Calculate report period (default: last 30 days)
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(30);

            // Generate report data using WorkflowDashboardService
            Map<String, Object> reportData = generateReportData(report.getWorkflow().getId(), 
                    startDate, endDate, report.getSections());

            // TODO: ReportGeneratorService no longer exists - generate report file manually
            // For now, create empty file as placeholder
            byte[] reportFile = new byte[0]; // Placeholder

            // Save report file (in production, save to cloud storage)
            String filePath = saveReportFile(report.getWorkflow().getId(), reportFile, report.getFormat());

            // Create report history entry
            WorkflowReportHistory history = new WorkflowReportHistory();
            history.setId(UUID.randomUUID().toString());
            history.setWorkflowReport(report);
            history.setWorkflow(report.getWorkflow());
            history.setReportPeriodStart(startDate);
            history.setReportPeriodEnd(endDate);
            history.setFilePath(filePath);
            history.setFileSize((long) reportFile.length);
            history.setFormat(report.getFormat());
            history.setRecipients(new ArrayList<>(report.getRecipients()));
            history.setDeliveryStatus("sent"); // Will be updated after email sending
            history.setGeneratedAt(LocalDateTime.now());

            history = workflowReportHistoryRepository.save(history);

            // Send report via email
            try {
                sendReportEmail(report, reportFile);
                history.setDeliveryStatus("sent");
            } catch (Exception e) {
                log.error("Error sending report email: workflowId={}", workflowId, e);
                history.setDeliveryStatus("failed");
            }

            workflowReportHistoryRepository.save(history);

            // Update report metadata
            report.setLastGeneratedAt(LocalDateTime.now());
            report.setLastGenerationStatus("success");
            report.setLastGenerationError(null);
            report.setGenerationCount(report.getGenerationCount() != null ? 
                    report.getGenerationCount() + 1 : 1);

            // Calculate next generation time
            LocalDateTime nextGenerationAt = calculateNextGenerationTime(
                    report.getScheduleType(),
                    report.getScheduleCron(),
                    report.getScheduleTime(),
                    report.getScheduleDay(),
                    report.getTimezone()
            );
            report.setNextGenerationAt(nextGenerationAt);

            workflowReportRepository.save(report);

            log.info("Generated report successfully: workflowId={}, historyId={}", 
                       workflowId, history.getId());

            return workflowReportMapper.toHistoryResponse(history);

        } catch (Exception e) {
            log.error("Error generating report: workflowId={}", workflowId, e);
            
            // Update report metadata with error
            report.setLastGeneratedAt(LocalDateTime.now());
            report.setLastGenerationStatus("failed");
            report.setLastGenerationError(e.getMessage());
            workflowReportRepository.save(report);

            throw new RuntimeException("Error generating report: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowReportPreviewResponse previewReport(String workflowId) {
        log.debug("Previewing report for workflow: {}", workflowId);

        WorkflowReport report = workflowReportRepository.findByWorkflowId(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Workflow report not found for workflow: " + workflowId));

        if (report.getDeletedAt() != null) {
            throw new ResourceNotFoundException(
                    "Workflow report not found for workflow: " + workflowId);
        }

        // Calculate report period (default: last 30 days)
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(30);

        // Generate preview data
        Map<String, Object> reportData = generateReportData(report.getWorkflow().getId(), 
                startDate, endDate, report.getSections());

        // Build preview response
        WorkflowReportPreviewResponse preview = new WorkflowReportPreviewResponse();
        preview.setWorkflowId(report.getWorkflow().getId());
        preview.setWorkflowName(report.getWorkflow().getName());
        preview.setReportPeriodStart(startDate);
        preview.setReportPeriodEnd(endDate);

        // Extract sections from report data
            Map<String, Object> executionSummary = (Map<String, Object>) reportData.get("executionSummary");
            if (executionSummary != null) {
                preview.setExecutionSummary(executionSummary);
            }
            
            Map<String, Object> notificationSummary = (Map<String, Object>) reportData.get("notificationSummary");
            if (notificationSummary != null) {
                preview.setNotificationSummary(notificationSummary);
            }
            
            Map<String, Object> performanceMetrics = (Map<String, Object>) reportData.get("performanceMetrics");
            if (performanceMetrics != null) {
                preview.setPerformanceMetrics(performanceMetrics);
            }
            
            Map<String, Object> errorAnalysis = (Map<String, Object>) reportData.get("errorAnalysis");
            if (errorAnalysis != null) {
                preview.setErrorAnalysis(errorAnalysis);
            }

        return preview;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<WorkflowReportHistoryResponse> getReportHistory(String workflowId, int limit, int offset) {
        // Validate pagination
        if (limit < 1) limit = 20;
        if (limit > 100) limit = 100;
        if (offset < 0) offset = 0;

        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<WorkflowReportHistory> page = workflowReportHistoryRepository.findByWorkflowId(workflowId, pageable);

        List<WorkflowReportHistoryResponse> responses = page.getContent().stream()
                .map(workflowReportMapper::toHistoryResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(responses, page.getTotalElements(), limit, offset);
    }

    @Override
    public WorkflowReportResponse updateReportStatus(String workflowId, String status) {
        log.info("Updating report status for workflow: {}, status: {}", workflowId, status);

        if (!Arrays.asList("active", "inactive", "paused").contains(status)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        WorkflowReport report = workflowReportRepository.findByWorkflowId(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Workflow report not found for workflow: " + workflowId));

        if (report.getDeletedAt() != null) {
            throw new ResourceNotFoundException(
                    "Workflow report not found for workflow: " + workflowId);
        }

        report.setStatus(status);

        // If activating, recalculate next generation time
        if ("active".equals(status) && report.getNextGenerationAt() == null) {
            LocalDateTime nextGenerationAt = calculateNextGenerationTime(
                    report.getScheduleType(),
                    report.getScheduleCron(),
                    report.getScheduleTime(),
                    report.getScheduleDay(),
                    report.getTimezone()
            );
            report.setNextGenerationAt(nextGenerationAt);
        }

        report = workflowReportRepository.save(report);

        log.info("Updated report status: workflowId={}, status={}", workflowId, status);

        return workflowReportMapper.toResponse(report);
    }

    // Helper methods

    private void validateSchedule(String scheduleType, String scheduleCron, 
                                 LocalTime scheduleTime, Integer scheduleDay) {
        if (scheduleType == null || scheduleType.isEmpty()) {
            throw new IllegalArgumentException("Schedule type is required");
        }

        switch (scheduleType.toLowerCase()) {
            case "daily":
                if (scheduleTime == null) {
                    throw new IllegalArgumentException("Schedule time is required for daily schedule");
                }
                break;
            case "weekly":
                if (scheduleTime == null || scheduleDay == null) {
                    throw new IllegalArgumentException("Schedule time and day are required for weekly schedule");
                }
                if (scheduleDay < 1 || scheduleDay > 7) {
                    throw new IllegalArgumentException("Schedule day must be between 1 and 7 (Monday=1)");
                }
                break;
            case "monthly":
                if (scheduleTime == null || scheduleDay == null) {
                    throw new IllegalArgumentException("Schedule time and day are required for monthly schedule");
                }
                if (scheduleDay < 1 || scheduleDay > 31) {
                    throw new IllegalArgumentException("Schedule day must be between 1 and 31");
                }
                break;
            case "custom":
                if (scheduleCron == null || scheduleCron.isEmpty()) {
                    throw new IllegalArgumentException("Schedule cron is required for custom schedule");
                }
                if (!cronValidator.isValid(scheduleCron)) {
                    throw new IllegalArgumentException("Invalid cron expression: " + scheduleCron);
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid schedule type: " + scheduleType);
        }
    }

    private LocalDateTime calculateNextGenerationTime(String scheduleType, String scheduleCron,
                                                     LocalTime scheduleTime, Integer scheduleDay,
                                                     String timezone) {
        ZoneId zoneId = timezone != null && !timezone.isEmpty() ? 
                ZoneId.of(timezone) : ZoneId.of("UTC");
        LocalDateTime now = LocalDateTime.now(zoneId);

        switch (scheduleType.toLowerCase()) {
            case "daily":
                if (scheduleTime == null) {
                    scheduleTime = LocalTime.of(9, 0); // Default 9 AM
                }
                LocalDateTime nextDaily = now.toLocalDate().atTime(scheduleTime);
                if (nextDaily.isBefore(now) || nextDaily.isEqual(now)) {
                    nextDaily = nextDaily.plusDays(1);
                }
                return nextDaily;

            case "weekly":
                if (scheduleTime == null) {
                    scheduleTime = LocalTime.of(9, 0);
                }
                if (scheduleDay == null) {
                    scheduleDay = 1; // Monday
                }
                LocalDate nextWeekDate = now.toLocalDate();
                int currentDayOfWeek = nextWeekDate.getDayOfWeek().getValue();
                int daysUntilNext = (scheduleDay - currentDayOfWeek + 7) % 7;
                if (daysUntilNext == 0 && now.toLocalTime().isAfter(scheduleTime)) {
                    daysUntilNext = 7;
                }
                nextWeekDate = nextWeekDate.plusDays(daysUntilNext);
                return nextWeekDate.atTime(scheduleTime);

            case "monthly":
                if (scheduleTime == null) {
                    scheduleTime = LocalTime.of(9, 0);
                }
                if (scheduleDay == null) {
                    scheduleDay = 1; // First day of month
                }
                LocalDate nextMonthDate = now.toLocalDate();
                int currentDay = nextMonthDate.getDayOfMonth();
                if (currentDay >= scheduleDay && now.toLocalTime().isBefore(scheduleTime)) {
                    // Same month, later today
                    nextMonthDate = nextMonthDate.withDayOfMonth(scheduleDay);
                } else {
                    // Next month
                    nextMonthDate = nextMonthDate.plusMonths(1).withDayOfMonth(
                            Math.min(scheduleDay, nextMonthDate.plusMonths(1).lengthOfMonth()));
                }
                return nextMonthDate.atTime(scheduleTime);

            case "custom":
                // For custom cron, use a simple calculation (in production, use a cron library)
                // For now, default to next day at 9 AM
                if (scheduleCron != null && cronValidator.isValid(scheduleCron)) {
                    // Simple calculation - in production, use Quartz or similar
                    return now.plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
                }
                return now.plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);

            default:
                return now.plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
        }
    }

    private Map<String, Object> generateReportData(String workflowId, LocalDateTime startDate, 
                                                   LocalDateTime endDate, List<String> sections) {
        Map<String, Object> reportData = new HashMap<>();

        // Get dashboard overview
        try {
            com.notificationplatform.dto.response.WorkflowDashboardDTO dashboard = 
                    workflowDashboardService.getDashboardOverview(workflowId, startDate, endDate, "UTC");

            // Extract sections based on requested sections
            com.notificationplatform.dto.response.WorkflowDashboardMetricsDTO metrics = dashboard.getMetrics();
            
            if (sections == null || sections.isEmpty() || sections.contains("execution_summary")) {
                Map<String, Object> executionSummary = new HashMap<>();
                executionSummary.put("totalExecutions", metrics != null ? metrics.getTotalExecutions() : 0L);
                executionSummary.put("successfulExecutions", metrics != null ? metrics.getSuccessfulExecutions() : 0L);
                executionSummary.put("failedExecutions", metrics != null ? metrics.getFailedExecutions() : 0L);
                executionSummary.put("runningExecutions", metrics != null ? metrics.getRunningExecutions() : 0L);
                executionSummary.put("successRate", metrics != null ? metrics.getSuccessRate() : 0.0);
                executionSummary.put("errorRate", metrics != null ? metrics.getErrorRate() : 0.0);
                executionSummary.put("averageExecutionTime", metrics != null ? metrics.getAverageExecutionTime() : 0.0);
                reportData.put("executionSummary", executionSummary);
            }

            if (sections == null || sections.isEmpty() || sections.contains("notification_summary")) {
                Map<String, Object> notificationSummary = new HashMap<>();
                notificationSummary.put("totalNotificationsSent", metrics != null ? metrics.getTotalNotificationsSent() : 0L);
                notificationSummary.put("totalNotificationsDelivered", metrics != null ? metrics.getTotalNotificationsDelivered() : 0L);
                notificationSummary.put("deliveryRate", metrics != null ? metrics.getDeliveryRate() : 0.0);
                reportData.put("notificationSummary", notificationSummary);
            }

            if (sections == null || sections.isEmpty() || sections.contains("performance_metrics")) {
                Map<String, Object> performanceMetrics = new HashMap<>();
                performanceMetrics.put("averageExecutionTime", metrics != null ? metrics.getAverageExecutionTime() : 0.0);
                performanceMetrics.put("executionsByStatus", metrics != null ? metrics.getExecutionsByStatus() : Collections.emptyMap());
                performanceMetrics.put("executionsByTriggerType", metrics != null ? metrics.getExecutionsByTriggerType() : Collections.emptyMap());
                reportData.put("performanceMetrics", performanceMetrics);
            }

            if (sections == null || sections.isEmpty() || sections.contains("error_analysis")) {
                // Get error analysis
                try {
                    PagedResponse<com.notificationplatform.dto.response.WorkflowErrorAnalysisDTO> errorAnalysis = 
                            workflowDashboardService.getErrorAnalysis(workflowId, startDate, endDate, null, 10, 0);
                    reportData.put("errorAnalysis", errorAnalysis.getData());
                } catch (Exception e) {
                    log.warn("Error getting error analysis for report", e);
                    reportData.put("errorAnalysis", Collections.emptyList());
                }
            }

        } catch (Exception e) {
            log.error("Error generating report data for workflow: {}", workflowId, e);
            throw new RuntimeException("Error generating report data: " + e.getMessage(), e);
        }

        return reportData;
    }

    private String saveReportFile(String workflowId, byte[] reportFile, String format) {
        // In production, save to cloud storage (S3, GCS, etc.)
        // For now, return a placeholder path
        String fileName = String.format("workflow_%s_report_%s.%s", 
                workflowId, 
                LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")),
                format);
        return "/reports/" + fileName;
    }

    @Override
    public QueryValidationResponse validateQuery(String workflowId, String analystQuery) {
        log.debug("Validating query for workflow: {}", workflowId);

        // Validate workflow exists
        workflowRepository.findByIdAndNotDeleted(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + workflowId));

        // Validate query using ReportQueryExecutor
        ReportQueryExecutor.QueryValidationResult result = reportQueryExecutor.validateQuery(analystQuery);

        return new QueryValidationResponse(result.isValid(), result.getError());
    }

    @Override
    public Resource downloadReport(String workflowId, String reportId) {
        log.debug("Downloading report: workflowId={}, reportId={}", workflowId, reportId);

        // Validate workflow exists
        workflowRepository.findByIdAndNotDeleted(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + workflowId));

        // Get report history
        WorkflowReportHistory history = workflowReportHistoryRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report history not found with id: " + reportId));

        // Verify report belongs to workflow
        if (!history.getWorkflow().getId().equals(workflowId)) {
            throw new ResourceNotFoundException("Report not found for workflow: " + workflowId);
        }

        // Get file path
        String filePath = history.getFilePath();
        if (filePath == null || filePath.isEmpty()) {
            throw new ResourceNotFoundException("Report file not found");
        }

        // Load file as resource
        // Note: In production, this should load from cloud storage (S3, etc.)
        // For now, assume files are stored in local filesystem
        java.io.File file = new java.io.File(filePath);
        if (!file.exists()) {
            throw new ResourceNotFoundException("Report file not found at path: " + filePath);
        }

        log.info("Downloading report file: workflowId={}, reportId={}, filePath={}", 
                 workflowId, reportId, filePath);

        return new FileSystemResource(file);
    }

    private void sendReportEmail(WorkflowReport report, byte[] reportFile) {
        // Log report delivery information
        log.info("=== Report Delivery Log ===");
        log.info("Workflow ID: {}", report.getWorkflow().getId());
        log.info("Report Name: {}", report.getName());
        log.info("Recipients: {}", report.getRecipients());
        log.info("File Format: {}", report.getFormat());
        log.info("File Size: {} bytes", reportFile.length);
        log.info("File Size (KB): {} KB", reportFile.length / 1024.0);
        log.info("Generated At: {}", java.time.LocalDateTime.now());
        log.info("===========================");
        
        // In production, use email service to send report
        // For now, log that email would be sent
        log.info("Email would be sent to recipients: {}", report.getRecipients());
        log.info("Email subject: Workflow Report - {}", report.getName());
        log.info("Email body: Please find attached the workflow report.");
        
        // Note: Full email delivery implementation deferred (currently log only)
        // Email sending will be handled by the scheduled job or can be called separately
    }
}

