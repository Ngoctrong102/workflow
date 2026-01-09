package com.notificationplatform.controller;

import com.notificationplatform.dto.response.CleanupResultResponse;
import com.notificationplatform.dto.response.CleanupStatisticsResponse;
import com.notificationplatform.service.datacleanup.CleanupResult;
import com.notificationplatform.service.datacleanup.CleanupStatistics;
import com.notificationplatform.service.datacleanup.DataCleanupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/data-cleanup")
public class DataCleanupController {

    private final DataCleanupService dataCleanupService;

    public DataCleanupController(DataCleanupService dataCleanupService) {
        this.dataCleanupService = dataCleanupService;
    }

    @PostMapping("/cleanup")
    public ResponseEntity<CleanupResultResponse> cleanupOldData(
            @RequestParam(required = false) Integer months) {
        CleanupResult result;
        if (months != null) {
            result = dataCleanupService.cleanupDataOlderThan(months);
        } else {
            result = dataCleanupService.cleanupOldData();
        }
        return ResponseEntity.ok(toResponse(result));
    }

    @GetMapping("/statistics")
    public ResponseEntity<CleanupStatisticsResponse> getCleanupStatistics() {
        CleanupStatistics stats = dataCleanupService.getCleanupStatistics();
        return ResponseEntity.ok(toStatisticsResponse(stats));
    }

    private CleanupResultResponse toResponse(CleanupResult result) {
        CleanupResultResponse response = new CleanupResultResponse();
        response.setCleanupTime(result.getCleanupTime());
        response.setTotalDeleted(result.getTotalDeleted());
        response.setDeletedByTable(result.getDeletedByTable());
        response.setStatus(result.getStatus());
        response.setErrorMessage(result.getErrorMessage());
        return response;
    }

    private CleanupStatisticsResponse toStatisticsResponse(CleanupStatistics stats) {
        CleanupStatisticsResponse response = new CleanupStatisticsResponse();
        response.setRetentionMonths(stats.getRetentionMonths());
        response.setCutoffDate(stats.getCutoffDate());
        response.setRecordCountsByTable(stats.getRecordCountsByTable());
        response.setRecordsToDelete(stats.getRecordsToDelete());
        response.setLastCleanupTime(stats.getLastCleanupTime());
        response.setLastCleanupDeleted(stats.getLastCleanupDeleted());
        return response;
    }
}

