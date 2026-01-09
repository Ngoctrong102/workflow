package com.notificationplatform.controller;

import com.notificationplatform.dto.request.CreateAlertRuleRequest;
import com.notificationplatform.dto.request.UpdateAlertRuleRequest;
import com.notificationplatform.dto.response.AlertResponse;
import com.notificationplatform.dto.response.AlertRuleResponse;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.service.alerting.AlertingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/alerting")
public class AlertingController {

    private final AlertingService alertingService;

    public AlertingController(AlertingService alertingService) {
        this.alertingService = alertingService;
    }

    @PostMapping("/rules")
    public ResponseEntity<AlertRuleResponse> createAlertRule(
            @Valid @RequestBody CreateAlertRuleRequest request) {
        AlertRuleResponse response = alertingService.createAlertRule(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rules/{id}")
    public ResponseEntity<AlertRuleResponse> getAlertRule(@PathVariable String id) {
        AlertRuleResponse response = alertingService.getAlertRule(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rules")
    public ResponseEntity<PagedResponse<AlertRuleResponse>> listAlertRules(
            @RequestParam(required = false) String ruleType,
            @RequestParam(required = false) String channelType,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        PagedResponse<AlertRuleResponse> responses = alertingService.listAlertRules(
                ruleType, channelType, limit, offset);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/rules/{id}")
    public ResponseEntity<AlertRuleResponse> updateAlertRule(
            @PathVariable String id,
            @Valid @RequestBody UpdateAlertRuleRequest request) {
        AlertRuleResponse response = alertingService.updateAlertRule(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Void> deleteAlertRule(@PathVariable String id) {
        alertingService.deleteAlertRule(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/alerts")
    public ResponseEntity<PagedResponse<AlertResponse>> listAlerts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String channelId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        PagedResponse<AlertResponse> responses = alertingService.listAlerts(
                status, severity, channelId, limit, offset);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/alerts/{id}")
    public ResponseEntity<AlertResponse> getAlert(@PathVariable String id) {
        AlertResponse response = alertingService.getAlert(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/alerts/{id}/acknowledge")
    public ResponseEntity<AlertResponse> acknowledgeAlert(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "system") String acknowledgedBy) {
        AlertResponse response = alertingService.acknowledgeAlert(id, acknowledgedBy);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/alerts/{id}/resolve")
    public ResponseEntity<AlertResponse> resolveAlert(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "system") String resolvedBy) {
        AlertResponse response = alertingService.resolveAlert(id, resolvedBy);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/alerts/{id}/close")
    public ResponseEntity<AlertResponse> closeAlert(@PathVariable String id) {
        AlertResponse response = alertingService.closeAlert(id);
        return ResponseEntity.ok(response);
    }
}

