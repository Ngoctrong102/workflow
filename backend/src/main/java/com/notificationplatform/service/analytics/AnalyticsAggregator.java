package com.notificationplatform.service.analytics;

import com.notificationplatform.entity.AnalyticsDaily;
import com.notificationplatform.entity.Execution;
import com.notificationplatform.entity.Workflow;
import com.notificationplatform.repository.AnalyticsRepository;
import com.notificationplatform.repository.DeliveryRepository;
import com.notificationplatform.repository.ExecutionRepository;
import com.notificationplatform.repository.NodeExecutionRepository;
import com.notificationplatform.repository.WorkflowRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AnalyticsAggregator {

    private final WorkflowRepository workflowRepository;
    private final ExecutionRepository executionRepository;
    private final DeliveryRepository deliveryRepository;
    private final NodeExecutionRepository nodeExecutionRepository;
    private final AnalyticsRepository analyticsRepository;

    public AnalyticsAggregator(WorkflowRepository workflowRepository,
                               ExecutionRepository executionRepository,
                               DeliveryRepository deliveryRepository,
                               NodeExecutionRepository nodeExecutionRepository,
                               AnalyticsRepository analyticsRepository) {
        this.workflowRepository = workflowRepository;
        this.executionRepository = executionRepository;
        this.deliveryRepository = deliveryRepository;
        this.nodeExecutionRepository = nodeExecutionRepository;
        this.analyticsRepository = analyticsRepository;
    }

    /**
     * Aggregate analytics data for a specific date
     */
    public void aggregateForDate(LocalDate date) {
        log.info("Starting analytics aggregation for date: {}", date);

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        try {
            // Aggregate overall metrics
            aggregateOverallMetrics(date, start, end);

            // Aggregate per workflow
            aggregateWorkflowMetrics(date, start, end);

            // Aggregate per channel
            aggregateChannelMetrics(date, start, end);

            log.info("Completed analytics aggregation for date: {}", date);
        } catch (Exception e) {
            log.error("Error aggregating analytics for date: {}", date, e);
            throw new RuntimeException("Failed to aggregate analytics for date: " + date, e);
        }
    }

    private void aggregateOverallMetrics(LocalDate date, LocalDateTime start, LocalDateTime end) {
        // Get all executions for the day
        List<Execution> executions = executionRepository.findByDateRange(start, end);
        long totalExecutions = executions.size();
        long successfulExecutions = executions.stream()
                .filter(e -> "completed".equals(e.getStatus()))
                .count();
        long failedExecutions = executions.stream()
                .filter(e -> "failed".equals(e.getStatus()))
                .count();

        // Get all deliveries for the day
        List<com.notificationplatform.entity.Delivery> deliveries = deliveryRepository.findByDateRange(start, end);
        long totalDeliveries = deliveries.size();
        long delivered = deliveries.stream()
                .filter(d -> "delivered".equals(d.getStatus()))
                .count();
        long failedDeliveries = deliveries.stream()
                .filter(d -> "failed".equals(d.getStatus()))
                .count();

        // Save overall metrics
        saveAnalyticsRecord(date, null, null, "total_executions", totalExecutions);
        saveAnalyticsRecord(date, null, null, "successful_executions", successfulExecutions);
        saveAnalyticsRecord(date, null, null, "failed_executions", failedExecutions);
        saveAnalyticsRecord(date, null, null, "total_deliveries", totalDeliveries);
        saveAnalyticsRecord(date, null, null, "delivered_notifications", delivered);
        saveAnalyticsRecord(date, null, null, "failed_notifications", failedDeliveries);
    }

    private void aggregateWorkflowMetrics(LocalDate date, LocalDateTime start, LocalDateTime end) {
        List<Workflow> workflows = workflowRepository.findAllActive();

        for (Workflow workflow : workflows) {
            List<Execution> executions = executionRepository.findByWorkflowIdAndDateRange(
                    workflow.getId(), start, end);

            if (executions.isEmpty()) {
                continue;
            }

            long totalExecutions = executions.size();
            long successfulExecutions = executions.stream()
                    .filter(e -> "completed".equals(e.getStatus()))
                    .count();
            long failedExecutions = executions.stream()
                    .filter(e -> "failed".equals(e.getStatus()))
                    .count();

            // Calculate average execution time
            double avgExecutionTime = executions.stream()
                    .filter(e -> e.getDuration() != null)
                    .mapToInt(Execution::getDuration)
                    .average()
                    .orElse(0.0);

            // Save workflow metrics
            saveAnalyticsRecord(date, workflow, null, "workflow_executions", totalExecutions);
            saveAnalyticsRecord(date, workflow, null, "workflow_successful_executions", successfulExecutions);
            saveAnalyticsRecord(date, workflow, null, "workflow_failed_executions", failedExecutions);
            saveAnalyticsRecord(date, workflow, null, "workflow_avg_execution_time", (long) avgExecutionTime);
        }
    }

    private void aggregateChannelMetrics(LocalDate date, LocalDateTime start, LocalDateTime end) {
        List<com.notificationplatform.entity.Delivery> deliveries = deliveryRepository.findByDateRange(start, end);

        // Group by channel
        Map<String, List<com.notificationplatform.entity.Delivery>> deliveriesByChannel = deliveries.stream()
                .collect(Collectors.groupingBy(com.notificationplatform.entity.Delivery::getChannel));

        for (Map.Entry<String, List<com.notificationplatform.entity.Delivery>> entry : deliveriesByChannel.entrySet()) {
            String channel = entry.getKey();
            List<com.notificationplatform.entity.Delivery> channelDeliveries = entry.getValue();

            long totalDeliveries = channelDeliveries.size();
            long delivered = channelDeliveries.stream()
                    .filter(d -> "delivered".equals(d.getStatus()))
                    .count();
            long failed = channelDeliveries.stream()
                    .filter(d -> "failed".equals(d.getStatus()))
                    .count();

            // Save channel metrics
            saveAnalyticsRecord(date, null, channel, "channel_deliveries", totalDeliveries);
            saveAnalyticsRecord(date, null, channel, "channel_delivered", delivered);
            saveAnalyticsRecord(date, null, channel, "channel_failed", failed);
        }
    }

    private void saveAnalyticsRecord(LocalDate date, Workflow workflow, String channel, String metricType, Long metricValue) {
        Optional<AnalyticsDaily> existing = analyticsRepository.findByDateAndWorkflowIdAndChannelAndMetricType(
                date, workflow != null ? workflow.getId() : null, channel, metricType);

        AnalyticsDaily analytics;
        if (existing.isPresent()) {
            analytics = existing.get();
        } else {
            analytics = new AnalyticsDaily();
            analytics.setId(UUID.randomUUID().toString());
            analytics.setDate(date);
            analytics.setWorkflow(workflow);
            analytics.setChannel(channel);
            analytics.setMetricType(metricType);
        }

        analytics.setMetricValue(metricValue);
        analyticsRepository.save(analytics);
    }
}

