package com.notificationplatform.service.alerting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notificationplatform.dto.request.CreateAlertRuleRequest;
import com.notificationplatform.dto.request.UpdateAlertRuleRequest;
import com.notificationplatform.dto.response.AlertResponse;
import com.notificationplatform.dto.response.AlertRuleResponse;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.entity.Alert;
import com.notificationplatform.entity.AlertRule;
import com.notificationplatform.entity.ProviderHealth;
import com.notificationplatform.exception.ResourceNotFoundException;
import com.notificationplatform.repository.AlertRepository;
import com.notificationplatform.repository.AlertRuleRepository;
import com.notificationplatform.repository.ProviderHealthRepository;
import com.notificationplatform.service.healthmonitoring.HealthMonitoringService;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@Transactional
public class AlertingServiceImpl implements AlertingService {

    private final AlertRuleRepository alertRuleRepository;
    private final AlertRepository alertRepository;
    private final ProviderHealthRepository providerHealthRepository;
    private final HealthMonitoringService healthMonitoringService;
    private final ObjectMapper objectMapper;

    public AlertingServiceImpl(AlertRuleRepository alertRuleRepository,
                               AlertRepository alertRepository,
                               ProviderHealthRepository providerHealthRepository,
                               HealthMonitoringService healthMonitoringService,
                               ObjectMapper objectMapper) {
        this.alertRuleRepository = alertRuleRepository;
        this.alertRepository = alertRepository;
        this.providerHealthRepository = providerHealthRepository;
        this.healthMonitoringService = healthMonitoringService;
        this.objectMapper = objectMapper;
    }

    @Override
    public AlertRuleResponse createAlertRule(CreateAlertRuleRequest request) {
        AlertRule rule = new AlertRule();
        rule.setId(UUID.randomUUID().toString());
        rule.setName(request.getName());
        rule.setDescription(request.getDescription());
        rule.setRuleType(request.getRuleType());
        rule.setChannelType(request.getChannelType());
        rule.setThresholdValue(request.getThresholdValue());
        rule.setThresholdUnit(request.getThresholdUnit());
        rule.setCondition(request.getCondition());
        rule.setSeverity(request.getSeverity());
        rule.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        rule.setNotificationChannels(request.getNotificationChannels());
        rule.setNotificationRecipients(request.getNotificationRecipients());

        rule = alertRuleRepository.save(rule);

        log.info("Created alert rule: id={}, name={}, ruleType={}", 
                   rule.getId(), rule.getName(), rule.getRuleType());

        return toRuleResponse(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public AlertRuleResponse getAlertRule(String id) {
        AlertRule rule = alertRuleRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert rule not found with id: " + id));
        return toRuleResponse(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AlertRuleResponse> listAlertRules(String ruleType, String channelType, int limit, int offset) {
        if (limit < 1) limit = 20;
        if (limit > 100) limit = 100;
        if (offset < 0) offset = 0;

        List<AlertRule> rules = alertRuleRepository.findAllActive();

        if (ruleType != null && !ruleType.isEmpty()) {
            rules = rules.stream()
                    .filter(r -> ruleType.equals(r.getRuleType()))
                    .collect(Collectors.toList());
        }

        if (channelType != null && !channelType.isEmpty()) {
            rules = rules.stream()
                    .filter(r -> channelType.equals(r.getChannelType()) || r.getChannelType() == null)
                    .collect(Collectors.toList());
        }

        long total = rules.size();
        int fromIndex = Math.min(offset, rules.size());
        int toIndex = Math.min(offset + limit, rules.size());
        List<AlertRule> pagedRules = rules.subList(fromIndex, toIndex);

        List<AlertRuleResponse> responses = pagedRules.stream()
                .map(this::toRuleResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(responses, total, limit, offset);
    }

    @Override
    public AlertRuleResponse updateAlertRule(String id, UpdateAlertRuleRequest request) {
        AlertRule rule = alertRuleRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert rule not found with id: " + id));

        if (request.getName() != null) {
            rule.setName(request.getName());
        }
        if (request.getDescription() != null) {
            rule.setDescription(request.getDescription());
        }
        if (request.getThresholdValue() != null) {
            rule.setThresholdValue(request.getThresholdValue());
        }
        if (request.getCondition() != null) {
            rule.setCondition(request.getCondition());
        }
        if (request.getSeverity() != null) {
            rule.setSeverity(request.getSeverity());
        }
        if (request.getEnabled() != null) {
            rule.setEnabled(request.getEnabled());
        }
        if (request.getNotificationChannels() != null) {
            rule.setNotificationChannels(request.getNotificationChannels());
        }
        if (request.getNotificationRecipients() != null) {
            rule.setNotificationRecipients(request.getNotificationRecipients());
        }

        rule = alertRuleRepository.save(rule);

        log.info("Updated alert rule: id={}", id);

        return toRuleResponse(rule);
    }

    @Override
    public void deleteAlertRule(String id) {
        AlertRule rule = alertRuleRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert rule not found with id: " + id));

        rule.setDeletedAt(LocalDateTime.now());
        rule.setEnabled(false);
        alertRuleRepository.save(rule);

        log.info("Deleted alert rule: id={}", id);
    }

    @Override
    public void evaluateAlertRules(String channelId) {
        Optional<ProviderHealth> healthOpt = providerHealthRepository.findByChannelId(channelId);
        if (healthOpt.isEmpty()) {
            return;
        }

        ProviderHealth health = healthOpt.get();
        List<AlertRule> rules = alertRuleRepository.findEnabledRules().stream()
                .filter(r -> r.getChannelType() == null || r.getChannelType().equals(health.getChannelType()))
                .collect(Collectors.toList());

        for (AlertRule rule : rules) {
            if (shouldTriggerAlert(rule, health)) {
                triggerAlert(rule, health);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AlertResponse> listAlerts(String status, String severity, String channelId, int limit, int offset) {
        if (limit < 1) limit = 20;
        if (limit > 100) limit = 100;
        if (offset < 0) offset = 0;

        List<Alert> alerts = alertRepository.findAll();

        if (status != null && !status.isEmpty()) {
            alerts = alerts.stream()
                    .filter(a -> status.equals(a.getStatus()))
                    .collect(Collectors.toList());
        }

        if (severity != null && !severity.isEmpty()) {
            alerts = alerts.stream()
                    .filter(a -> severity.equals(a.getSeverity()))
                    .collect(Collectors.toList());
        }

        if (channelId != null && !channelId.isEmpty()) {
            alerts = alerts.stream()
                    .filter(a -> channelId.equals(a.getChannelId()))
                    .collect(Collectors.toList());
        }

        // Sort by triggered_at descending
        alerts.sort((a, b) -> b.getTriggeredAt().compareTo(a.getTriggeredAt()));

        long total = alerts.size();
        int fromIndex = Math.min(offset, alerts.size());
        int toIndex = Math.min(offset + limit, alerts.size());
        List<Alert> pagedAlerts = alerts.subList(fromIndex, toIndex);

        List<AlertResponse> responses = pagedAlerts.stream()
                .map(this::toAlertResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(responses, total, limit, offset);
    }

    @Override
    @Transactional(readOnly = true)
    public AlertResponse getAlert(String id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + id));
        return toAlertResponse(alert);
    }

    @Override
    public AlertResponse acknowledgeAlert(String id, String acknowledgedBy) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + id));

        alert.setStatus("acknowledged");
        alert.setAcknowledgedAt(LocalDateTime.now());
        alert.setAcknowledgedBy(acknowledgedBy);
        alert = alertRepository.save(alert);

        log.info("Acknowledged alert: id={}, acknowledgedBy={}", id, acknowledgedBy);

        return toAlertResponse(alert);
    }

    @Override
    public AlertResponse resolveAlert(String id, String resolvedBy) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + id));

        alert.setStatus("resolved");
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolvedBy(resolvedBy);
        alert = alertRepository.save(alert);

        log.info("Resolved alert: id={}, resolvedBy={}", id, resolvedBy);

        return toAlertResponse(alert);
    }

    @Override
    public AlertResponse closeAlert(String id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + id));

        alert.setStatus("closed");
        alert = alertRepository.save(alert);

        log.info("Closed alert: id={}", id);

        return toAlertResponse(alert);
    }

    private boolean shouldTriggerAlert(AlertRule rule, ProviderHealth health) {
        double threshold = rule.getThresholdValue();
        String condition = rule.getCondition();

        switch (rule.getRuleType()) {
            case "failure_threshold":
                int consecutiveFailures = health.getConsecutiveFailures() != null ? health.getConsecutiveFailures() : 0;
                return evaluateCondition(consecutiveFailures, threshold, condition);
            case "error_rate":
                double errorRate = health.getErrorRate() != null ? health.getErrorRate() : 0.0;
                return evaluateCondition(errorRate, threshold, condition);
            case "response_time":
                int responseTime = health.getResponseTimeMs() != null ? health.getResponseTimeMs() : 0;
                return evaluateCondition(responseTime, threshold, condition);
            case "health_score":
                double healthScore = health.getHealthScore() != null ? health.getHealthScore() : 100.0;
                return evaluateCondition(healthScore, threshold, condition);
            default:
                return false;
        }
    }

    private boolean evaluateCondition(double value, double threshold, String condition) {
        switch (condition) {
            case "greater_than":
                return value > threshold;
            case "less_than":
                return value < threshold;
            case "equals":
                return Math.abs(value - threshold) < 0.01;
            default:
                return false;
        }
    }

    private void triggerAlert(AlertRule rule, ProviderHealth health) {
        // Check if alert already exists for this rule and channel
        List<Alert> existingAlerts = alertRepository.findByAlertRuleId(rule.getId()).stream()
                .filter(a -> a.getChannelId() != null && a.getChannelId().equals(health.getChannelId()))
                .filter(a -> "open".equals(a.getStatus()) || "acknowledged".equals(a.getStatus()))
                .collect(Collectors.toList());

        if (!existingAlerts.isEmpty()) {
            // Alert already exists, don't create duplicate
            return;
        }

        Alert alert = new Alert();
        alert.setId(UUID.randomUUID().toString());
        alert.setAlertRuleId(rule.getId());
        alert.setChannelId(health.getChannelId());
        alert.setChannelType(health.getChannelType());
        alert.setSeverity(rule.getSeverity());
        alert.setStatus("open");

        // Set alert type and message based on rule type
        String alertType = determineAlertType(rule.getRuleType());
        alert.setAlertType(alertType);
        alert.setTitle(generateAlertTitle(rule, health));
        alert.setMessage(generateAlertMessage(rule, health));

        // Set metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("ruleType", rule.getRuleType());
        metadata.put("thresholdValue", rule.getThresholdValue());
        metadata.put("currentValue", getCurrentValue(rule, health));
        metadata.put("healthScore", health.getHealthScore());
        alert.setMetadata(metadata);

        alert = alertRepository.save(alert);

        // Send notifications
        sendAlertNotifications(alert, rule);

        log.info("Triggered alert: id={}, ruleId={}, channelId={}", 
                   alert.getId(), rule.getId(), health.getChannelId());
    }

    private String determineAlertType(String ruleType) {
        switch (ruleType) {
            case "failure_threshold":
                return "provider_down";
            case "error_rate":
                return "high_error_rate";
            case "response_time":
                return "slow_response";
            default:
                return "provider_issue";
        }
    }

    private String generateAlertTitle(AlertRule rule, ProviderHealth health) {
        return String.format("%s Alert: %s Channel", 
                rule.getSeverity().substring(0, 1).toUpperCase() + rule.getSeverity().substring(1),
                health.getChannelType());
    }

    private String generateAlertMessage(AlertRule rule, ProviderHealth health) {
        return String.format("Alert rule '%s' triggered for channel %s. Current value: %s, Threshold: %s %s",
                rule.getName(),
                health.getChannelId(),
                getCurrentValue(rule, health),
                rule.getThresholdValue(),
                rule.getThresholdUnit() != null ? rule.getThresholdUnit() : "");
    }

    private Object getCurrentValue(AlertRule rule, ProviderHealth health) {
        switch (rule.getRuleType()) {
            case "failure_threshold":
                return health.getConsecutiveFailures();
            case "error_rate":
                return health.getErrorRate();
            case "response_time":
                return health.getResponseTimeMs();
            case "health_score":
                return health.getHealthScore();
            default:
                return 0;
        }
    }

    private void sendAlertNotifications(Alert alert, AlertRule rule) {
        // In production, implement actual notification sending
        // For now, just log
        log.info("Sending alert notifications: alertId={}, recipients={}", 
                   alert.getId(), rule.getNotificationRecipients());
    }

    private AlertRuleResponse toRuleResponse(AlertRule rule) {
        AlertRuleResponse response = new AlertRuleResponse();
        response.setId(rule.getId());
        response.setName(rule.getName());
        response.setDescription(rule.getDescription());
        response.setRuleType(rule.getRuleType());
        response.setChannelType(rule.getChannelType());
        response.setThresholdValue(rule.getThresholdValue());
        response.setThresholdUnit(rule.getThresholdUnit());
        response.setCondition(rule.getCondition());
        response.setSeverity(rule.getSeverity());
        response.setEnabled(rule.getEnabled());
        response.setNotificationChannels(rule.getNotificationChannels());
        response.setNotificationRecipients(rule.getNotificationRecipients());
        response.setCreatedAt(rule.getCreatedAt());
        response.setUpdatedAt(rule.getUpdatedAt());
        return response;
    }

    private AlertResponse toAlertResponse(Alert alert) {
        AlertResponse response = new AlertResponse();
        response.setId(alert.getId());
        response.setAlertRuleId(alert.getAlertRuleId());
        response.setChannelId(alert.getChannelId());
        response.setChannelType(alert.getChannelType());
        response.setAlertType(alert.getAlertType());
        response.setSeverity(alert.getSeverity());
        response.setTitle(alert.getTitle());
        response.setMessage(alert.getMessage());
        response.setStatus(alert.getStatus());
        response.setTriggeredAt(alert.getTriggeredAt());
        response.setAcknowledgedAt(alert.getAcknowledgedAt());
        response.setAcknowledgedBy(alert.getAcknowledgedBy());
        response.setResolvedAt(alert.getResolvedAt());
        response.setResolvedBy(alert.getResolvedBy());
        response.setMetadata(alert.getMetadata());
        response.setCreatedAt(alert.getCreatedAt());
        response.setUpdatedAt(alert.getUpdatedAt());
        return response;
    }
}

