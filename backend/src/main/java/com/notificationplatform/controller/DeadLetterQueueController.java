package com.notificationplatform.controller;

import com.notificationplatform.dto.response.DeadLetterQueueResponse;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.service.dlq.DeadLetterQueueService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dlq")
public class DeadLetterQueueController {

    private final DeadLetterQueueService dlqService;

    public DeadLetterQueueController(DeadLetterQueueService dlqService) {
        this.dlqService = dlqService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeadLetterQueueResponse> getDLQEntry(@PathVariable String id) {
        DeadLetterQueueResponse response = dlqService.getDLQEntry(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<DeadLetterQueueResponse>> listDLQEntries(
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) String channelId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String errorType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        PagedResponse<DeadLetterQueueResponse> responses = dlqService.listDLQEntries(
                sourceType, channelId, status, errorType, startDate, endDate, limit, offset);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<DeadLetterQueueResponse> retryDLQEntry(@PathVariable String id) {
        DeadLetterQueueResponse response = dlqService.retryDLQEntry(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/retry")
    public ResponseEntity<List<DeadLetterQueueResponse>> retryDLQEntries(@RequestBody List<String> ids) {
        List<DeadLetterQueueResponse> responses = dlqService.retryDLQEntries(ids);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<DeadLetterQueueResponse> resolveDLQEntry(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "system") String resolvedBy) {
        DeadLetterQueueResponse response = dlqService.resolveDLQEntry(id, resolvedBy);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDLQEntry(@PathVariable String id) {
        dlqService.deleteDLQEntry(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Integer>> cleanupOldEntries(@RequestParam(defaultValue = "30") int daysOld) {
        int deleted = dlqService.cleanupOldEntries(daysOld);
        return ResponseEntity.ok(Map.of("deleted", deleted));
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getDLQStatistics() {
        Map<String, Object> stats = dlqService.getDLQStatistics();
        return ResponseEntity.ok(stats);
    }
}

