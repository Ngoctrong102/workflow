package com.notificationplatform.service.alerting;

import com.notificationplatform.dto.request.CreateAlertRuleRequest;
import com.notificationplatform.dto.request.UpdateAlertRuleRequest;
import com.notificationplatform.dto.response.AlertResponse;
import com.notificationplatform.dto.response.AlertRuleResponse;
import com.notificationplatform.dto.response.PagedResponse;

/**
 * Service for managing alerts and alert rules
 */
public interface AlertingService {

    /**
     * Create alert rule
     */
    AlertRuleResponse createAlertRule(CreateAlertRuleRequest request);

    /**
     * Get alert rule by ID
     */
    AlertRuleResponse getAlertRule(String id);

    /**
     * List alert rules
     */
    PagedResponse<AlertRuleResponse> listAlertRules(String ruleType, String channelType, int limit, int offset);

    /**
     * Update alert rule
     */
    AlertRuleResponse updateAlertRule(String id, UpdateAlertRuleRequest request);

    /**
     * Delete alert rule
     */
    void deleteAlertRule(String id);

    /**
     * Evaluate alert rules and trigger alerts if needed
     */
    void evaluateAlertRules(String channelId);

    /**
     * Get alerts
     */
    PagedResponse<AlertResponse> listAlerts(String status, String severity, String channelId, int limit, int offset);

    /**
     * Get alert by ID
     */
    AlertResponse getAlert(String id);

    /**
     * Acknowledge alert
     */
    AlertResponse acknowledgeAlert(String id, String acknowledgedBy);

    /**
     * Resolve alert
     */
    AlertResponse resolveAlert(String id, String resolvedBy);

    /**
     * Close alert
     */
    AlertResponse closeAlert(String id);
}

