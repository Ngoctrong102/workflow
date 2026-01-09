package com.notificationplatform.controller;

import com.notificationplatform.dto.request.CreateScheduledReportRequest;
import com.notificationplatform.dto.request.UpdateScheduledReportRequest;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.dto.response.ScheduledReportResponse;
import com.notificationplatform.service.reportscheduling.ReportSchedulingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/scheduled-reports")
public class ReportSchedulingController {

    private final ReportSchedulingService reportSchedulingService;

    public ReportSchedulingController(ReportSchedulingService reportSchedulingService) {
        this.reportSchedulingService = reportSchedulingService;
    }

    @PostMapping
    public ResponseEntity<ScheduledReportResponse> createScheduledReport(
            @Valid @RequestBody CreateScheduledReportRequest request) {
        ScheduledReportResponse response = reportSchedulingService.createScheduledReport(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduledReportResponse> getScheduledReport(@PathVariable String id) {
        ScheduledReportResponse response = reportSchedulingService.getScheduledReport(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ScheduledReportResponse>> listScheduledReports(
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        PagedResponse<ScheduledReportResponse> responses = reportSchedulingService.listScheduledReports(
                reportType, status, limit, offset);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduledReportResponse> updateScheduledReport(
            @PathVariable String id,
            @Valid @RequestBody UpdateScheduledReportRequest request) {
        ScheduledReportResponse response = reportSchedulingService.updateScheduledReport(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScheduledReport(@PathVariable String id) {
        reportSchedulingService.deleteScheduledReport(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<ScheduledReportResponse> pauseScheduledReport(@PathVariable String id) {
        ScheduledReportResponse response = reportSchedulingService.pauseScheduledReport(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<ScheduledReportResponse> resumeScheduledReport(@PathVariable String id) {
        ScheduledReportResponse response = reportSchedulingService.resumeScheduledReport(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<ScheduledReportResponse> executeScheduledReport(@PathVariable String id) {
        ScheduledReportResponse response = reportSchedulingService.executeScheduledReport(id);
        return ResponseEntity.ok(response);
    }
}

