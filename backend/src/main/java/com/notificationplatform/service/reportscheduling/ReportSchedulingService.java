package com.notificationplatform.service.reportscheduling;

import com.notificationplatform.dto.request.CreateScheduledReportRequest;
import com.notificationplatform.dto.request.UpdateScheduledReportRequest;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.dto.response.ScheduledReportResponse;

/**
 * Service for managing scheduled reports
 */
public interface ReportSchedulingService {

    /**
     * Create a new scheduled report
     */
    ScheduledReportResponse createScheduledReport(CreateScheduledReportRequest request);

    /**
     * Get scheduled report by ID
     */
    ScheduledReportResponse getScheduledReport(String id);

    /**
     * List scheduled reports with pagination
     */
    PagedResponse<ScheduledReportResponse> listScheduledReports(String reportType, String status, int limit, int offset);

    /**
     * Update scheduled report
     */
    ScheduledReportResponse updateScheduledReport(String id, UpdateScheduledReportRequest request);

    /**
     * Delete scheduled report
     */
    void deleteScheduledReport(String id);

    /**
     * Pause scheduled report
     */
    ScheduledReportResponse pauseScheduledReport(String id);

    /**
     * Resume scheduled report
     */
    ScheduledReportResponse resumeScheduledReport(String id);

    /**
     * Execute scheduled report immediately
     */
    ScheduledReportResponse executeScheduledReport(String id);

    /**
     * Process due scheduled reports (called by scheduler)
     */
    void processDueReports();
}

