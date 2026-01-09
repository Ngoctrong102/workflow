package com.notificationplatform.service.analytics;

import com.notificationplatform.dto.response.ChannelAnalyticsResponse;
import com.notificationplatform.dto.response.DeliveryAnalyticsResponse;
import com.notificationplatform.dto.response.ErrorAnalyticsResponse;
import com.notificationplatform.dto.response.WorkflowAnalyticsResponse;

import java.time.LocalDate;
import java.util.List;

public interface AnalyticsService {

    WorkflowAnalyticsResponse getWorkflowAnalytics(String workflowId, LocalDate startDate, LocalDate endDate);

    List<WorkflowAnalyticsResponse> getAllWorkflowsAnalytics(LocalDate startDate, LocalDate endDate);

    DeliveryAnalyticsResponse getDeliveryAnalytics(LocalDate startDate, LocalDate endDate, String channel);

    ChannelAnalyticsResponse getChannelAnalytics(String channelId, LocalDate startDate, LocalDate endDate);

    List<ChannelAnalyticsResponse> getAllChannelsAnalytics(LocalDate startDate, LocalDate endDate);

    ErrorAnalyticsResponse getErrorAnalytics(LocalDate startDate, LocalDate endDate, String workflowId, String errorType);

    void aggregateDailyMetrics(LocalDate date);

    void aggregateAnalytics(LocalDate date);
}

