package com.notificationplatform.service.workflowreport;

import com.notificationplatform.entity.WorkflowReport;
import com.notificationplatform.repository.WorkflowReportRepository;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
/**
 * Scheduled job for automatic workflow report generation
 * Runs every minute to check for due reports
 */
@Slf4j
@Component
public class WorkflowReportScheduler {

    private final WorkflowReportRepository workflowReportRepository;
    private final WorkflowReportService workflowReportService;

    public WorkflowReportScheduler(WorkflowReportRepository workflowReportRepository,
                                   WorkflowReportService workflowReportService) {
        this.workflowReportRepository = workflowReportRepository;
        this.workflowReportService = workflowReportService;
    }

    /**
     * Process due workflow reports
     * Runs every minute to check for reports that need to be generated
     */
    @Scheduled(fixedRate = 60000) // Check every minute
    @Transactional
    public void processDueReports() {
        LocalDateTime now = LocalDateTime.now();
        List<WorkflowReport> dueReports = workflowReportRepository.findDueReports(now);

        if (dueReports.isEmpty()) {
            return;
        }

        log.info("Found {} due workflow reports to process", dueReports.size());

        for (WorkflowReport report : dueReports) {
            if (!"active".equals(report.getStatus())) {
                log.debug("Skipping inactive report: workflowId={}, status={}", 
                           report.getWorkflow().getId(), report.getStatus());
                continue;
            }

            try {
                log.info("Generating workflow report: workflowId={}, name={}", 
                           report.getWorkflow().getId(), report.getName());
                
                workflowReportService.generateReport(report.getWorkflow().getId());
                
                log.info("Successfully generated workflow report: workflowId={}", 
                           report.getWorkflow().getId());
            } catch (Exception e) {
                log.error("Error generating workflow report: workflowId={}, name={}", 
                           report.getWorkflow().getId(), report.getName(), e);
                
                // Update report with error status
                report.setLastGeneratedAt(LocalDateTime.now());
                report.setLastGenerationStatus("failed");
                report.setLastGenerationError(e.getMessage());
                workflowReportRepository.save(report);
            }
        }
    }
}

