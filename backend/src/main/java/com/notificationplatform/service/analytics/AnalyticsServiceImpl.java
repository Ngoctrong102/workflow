package com.notificationplatform.service.analytics;

import com.notificationplatform.dto.response.ChannelAnalyticsResponse;
import com.notificationplatform.dto.response.DeliveryAnalyticsResponse;
import com.notificationplatform.dto.response.ErrorAnalyticsResponse;
import com.notificationplatform.dto.response.WorkflowAnalyticsResponse;
import com.notificationplatform.entity.Channel;
import com.notificationplatform.entity.Execution;
import com.notificationplatform.entity.NodeExecution;
import com.notificationplatform.entity.Workflow;
import com.notificationplatform.repository.AnalyticsRepository;
import com.notificationplatform.repository.ChannelRepository;
import com.notificationplatform.repository.DeliveryRepository;
import com.notificationplatform.repository.ExecutionRepository;
import com.notificationplatform.repository.NodeExecutionRepository;
import com.notificationplatform.repository.NotificationRepository;
import com.notificationplatform.repository.WorkflowRepository;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@Transactional
public class AnalyticsServiceImpl implements AnalyticsService {

    private final WorkflowRepository workflowRepository;
    private final ExecutionRepository executionRepository;
    private final NotificationRepository notificationRepository;
    private final DeliveryRepository deliveryRepository;
    private final ChannelRepository channelRepository;
    private final AnalyticsRepository analyticsRepository;
    private final NodeExecutionRepository nodeExecutionRepository;
    private final AnalyticsAggregator analyticsAggregator;

    public AnalyticsServiceImpl(WorkflowRepository workflowRepository,
                               ExecutionRepository executionRepository,
                               NotificationRepository notificationRepository,
                               DeliveryRepository deliveryRepository,
                               ChannelRepository channelRepository,
                               AnalyticsRepository analyticsRepository,
                               NodeExecutionRepository nodeExecutionRepository,
                               AnalyticsAggregator analyticsAggregator) {
        this.workflowRepository = workflowRepository;
        this.executionRepository = executionRepository;
        this.notificationRepository = notificationRepository;
        this.deliveryRepository = deliveryRepository;
        this.channelRepository = channelRepository;
        this.analyticsRepository = analyticsRepository;
        this.nodeExecutionRepository = nodeExecutionRepository;
        this.analyticsAggregator = analyticsAggregator;
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowAnalyticsResponse getWorkflowAnalytics(String workflowId, LocalDate startDate, LocalDate endDate) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found: " + workflowId));

        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : null;

        // Get execution metrics
        List<com.notificationplatform.entity.Execution> executions = executionRepository
                .findByWorkflowIdAndDateRange(workflowId, start, end);

        long totalExecutions = executions.size();
        long successfulExecutions = executions.stream()
                .filter(e -> "completed".equals(e.getStatus()))
                .count();
        long failedExecutions = executions.stream()
                .filter(e -> "failed".equals(e.getStatus()))
                .count();

        double averageExecutionTime = executions.stream()
                .filter(e -> e.getDuration() != null)
                .mapToInt(com.notificationplatform.entity.Execution::getDuration)
                .average()
                .orElse(0.0);

        double successRate = totalExecutions > 0 ? (double) successfulExecutions / totalExecutions * 100 : 0.0;

        // Group by status
        Map<String, Long> executionsByStatus = executions.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getStatus() != null ? e.getStatus().getValue() : "unknown",
                        Collectors.counting()
                ));

        LocalDateTime lastExecutionAt = executions.stream()
                .map(com.notificationplatform.entity.Execution::getStartedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime firstExecutionAt = executions.stream()
                .map(com.notificationplatform.entity.Execution::getStartedAt)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        WorkflowAnalyticsResponse response = new WorkflowAnalyticsResponse();
        response.setWorkflowId(workflowId);
        response.setWorkflowName(workflow.getName());
        response.setTotalExecutions(totalExecutions);
        response.setSuccessfulExecutions(successfulExecutions);
        response.setFailedExecutions(failedExecutions);
        response.setAverageExecutionTime(averageExecutionTime);
        response.setSuccessRate(successRate);
        response.setExecutionsByStatus(executionsByStatus);
        response.setLastExecutionAt(lastExecutionAt);
        response.setFirstExecutionAt(firstExecutionAt);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowAnalyticsResponse> getAllWorkflowsAnalytics(LocalDate startDate, LocalDate endDate) {
        List<Workflow> workflows = workflowRepository.findAllActive();
        return workflows.stream()
                .map(w -> getWorkflowAnalytics(w.getId(), startDate, endDate))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryAnalyticsResponse getDeliveryAnalytics(LocalDate startDate, LocalDate endDate, String channel) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : null;

        List<com.notificationplatform.entity.Delivery> deliveries;
        if (channel != null && !channel.isEmpty()) {
            deliveries = deliveryRepository.findByChannelAndDateRange(channel, start, end);
        } else {
            deliveries = deliveryRepository.findByDateRange(start, end);
        }

        long totalSent = deliveries.size();
        long delivered = deliveries.stream()
                .filter(d -> "delivered".equals(d.getStatus()))
                .count();
        long failed = deliveries.stream()
                .filter(d -> "failed".equals(d.getStatus()))
                .count();
        long pending = deliveries.stream()
                .filter(d -> "pending".equals(d.getStatus()) || "sending".equals(d.getStatus()))
                .count();

        double deliveryRate = totalSent > 0 ? (double) delivered / totalSent * 100 : 0.0;

        // Group by channel
        Map<String, Long> byChannel = deliveries.stream()
                .collect(Collectors.groupingBy(
                        com.notificationplatform.entity.Delivery::getChannel,
                        Collectors.counting()
                ));

        // Group by status
        Map<String, Long> byStatus = deliveries.stream()
                .collect(Collectors.groupingBy(
                        com.notificationplatform.entity.Delivery::getStatus,
                        Collectors.counting()
                ));

        DeliveryAnalyticsResponse response = new DeliveryAnalyticsResponse();
        response.setTotalSent(totalSent);
        response.setDelivered(delivered);
        response.setFailed(failed);
        response.setPending(pending);
        response.setDeliveryRate(deliveryRate);
        response.setByChannel(byChannel);
        response.setByStatus(byStatus);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ChannelAnalyticsResponse getChannelAnalytics(String channelId, LocalDate startDate, LocalDate endDate) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found: " + channelId));

        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : null;

        List<com.notificationplatform.entity.Delivery> deliveries = deliveryRepository
                .findByChannelAndDateRange(channel.getType(), start, end);

        long totalSent = deliveries.size();
        long delivered = deliveries.stream()
                .filter(d -> "delivered".equals(d.getStatus()))
                .count();
        long failed = deliveries.stream()
                .filter(d -> "failed".equals(d.getStatus()))
                .count();

        double deliveryRate = totalSent > 0 ? (double) delivered / totalSent * 100 : 0.0;

        double averageDeliveryTime = deliveries.stream()
                .filter(d -> d.getDeliveredAt() != null && d.getSentAt() != null)
                .mapToLong(d -> java.time.Duration.between(d.getSentAt(), d.getDeliveredAt()).toMillis())
                .average()
                .orElse(0.0);

        // Group errors by type (extract from error message)
        Map<String, Long> errorsByType = deliveries.stream()
                .filter(d -> d.getError() != null)
                .collect(Collectors.groupingBy(
                        d -> extractErrorType(d.getError()),
                        Collectors.counting()
                ));

        ChannelAnalyticsResponse response = new ChannelAnalyticsResponse();
        response.setChannelId(channelId);
        response.setChannelName(channel.getName());
        response.setChannelType(channel.getType());
        response.setTotalSent(totalSent);
        response.setDelivered(delivered);
        response.setFailed(failed);
        response.setDeliveryRate(deliveryRate);
        response.setAverageDeliveryTime(averageDeliveryTime);
        response.setErrorsByType(errorsByType);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChannelAnalyticsResponse> getAllChannelsAnalytics(LocalDate startDate, LocalDate endDate) {
        List<Channel> channels = channelRepository.findAllActive();
        return channels.stream()
                .map(c -> getChannelAnalytics(c.getId(), startDate, endDate))
                .collect(Collectors.toList());
    }

    @Override
    public ErrorAnalyticsResponse getErrorAnalytics(LocalDate startDate, LocalDate endDate, String workflowId, String errorType) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : null;

        // Get failed executions
        List<Execution> failedExecutions;
        if (workflowId != null && !workflowId.isEmpty()) {
            failedExecutions = executionRepository.findByWorkflowIdAndDateRange(workflowId, start, end)
                    .stream()
                    .filter(e -> "failed".equals(e.getStatus()))
                    .collect(Collectors.toList());
        } else {
            failedExecutions = executionRepository.findByDateRange(start, end)
                    .stream()
                    .filter(e -> "failed".equals(e.getStatus()))
                    .collect(Collectors.toList());
        }

        // Get failed node executions
        List<NodeExecution> failedNodeExecutions = new ArrayList<>();
        for (Execution execution : failedExecutions) {
            failedNodeExecutions.addAll(nodeExecutionRepository.findByExecutionIdAndStatus(
                    execution.getId(), "failed"));
        }

        // Get failed deliveries
        List<com.notificationplatform.entity.Delivery> failedDeliveries = deliveryRepository
                .findByDateRange(start, end)
                .stream()
                .filter(d -> "failed".equals(d.getStatus()))
                .collect(Collectors.toList());

        long totalErrors = failedExecutions.size() + failedDeliveries.size();

        // Group errors by type
        Map<String, Long> errorsByType = new HashMap<>();
        for (Execution exec : failedExecutions) {
            String errorTypeFromExec = extractErrorType(exec.getError());
            if (errorType == null || errorType.equals(errorTypeFromExec)) {
                errorsByType.put(errorTypeFromExec, errorsByType.getOrDefault(errorTypeFromExec, 0L) + 1);
            }
        }
        for (com.notificationplatform.entity.Delivery delivery : failedDeliveries) {
            String errorTypeFromDelivery = extractErrorType(delivery.getError());
            if (errorType == null || errorType.equals(errorTypeFromDelivery)) {
                errorsByType.put(errorTypeFromDelivery, errorsByType.getOrDefault(errorTypeFromDelivery, 0L) + 1);
            }
        }

        // Group errors by workflow
        Map<String, Long> errorsByWorkflow = failedExecutions.stream()
                .filter(e -> e.getWorkflow() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getWorkflow().getId(),
                        Collectors.counting()
                ));

        // Group errors by channel
        Map<String, Long> errorsByChannel = failedDeliveries.stream()
                .collect(Collectors.groupingBy(
                        com.notificationplatform.entity.Delivery::getChannel,
                        Collectors.counting()
                ));

        // Group errors by node
        Map<String, Long> errorsByNode = failedNodeExecutions.stream()
                .filter(ne -> ne.getNodeId() != null)
                .collect(Collectors.groupingBy(
                        NodeExecution::getNodeId,
                        Collectors.counting()
                ));

        // Calculate error rate
        long totalExecutions = executionRepository.findByDateRange(start, end).size();
        double errorRate = totalExecutions > 0 ? (double) failedExecutions.size() / totalExecutions * 100 : 0.0;

        // Get last error timestamp
        LocalDateTime lastErrorAt = failedExecutions.stream()
                .map(Execution::getStartedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        // Error trends (by date)
        Map<String, Double> errorTrends = new HashMap<>();
        if (startDate != null && endDate != null) {
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                LocalDateTime dayStart = current.atStartOfDay();
                LocalDateTime dayEnd = current.atTime(23, 59, 59);
                long dayErrors = executionRepository.findByDateRange(dayStart, dayEnd)
                        .stream()
                        .filter(e -> "failed".equals(e.getStatus()))
                        .count();
                errorTrends.put(current.toString(), (double) dayErrors);
                current = current.plusDays(1);
            }
        }

        ErrorAnalyticsResponse response = new ErrorAnalyticsResponse();
        response.setStartDate(startDate);
        response.setEndDate(endDate);
        response.setTotalErrors(totalErrors);
        response.setErrorsByType(errorsByType);
        response.setErrorsByWorkflow(errorsByWorkflow);
        response.setErrorsByChannel(errorsByChannel);
        response.setErrorsByNode(errorsByNode);
        response.setErrorRate(errorRate);
        response.setLastErrorAt(lastErrorAt);
        response.setErrorTrends(errorTrends);

        return response;
    }

    @Override
    public void aggregateDailyMetrics(LocalDate date) {
        analyticsAggregator.aggregateForDate(date);
    }

    @Override
    public void aggregateAnalytics(LocalDate date) {
        analyticsAggregator.aggregateForDate(date);
    }

    private String extractErrorType(String error) {
        if (error == null || error.isEmpty()) {
            return "unknown";
        }

        // Simple error type extraction
        String lowerError = error.toLowerCase();
        if (lowerError.contains("timeout")) {
            return "timeout";
        } else if (lowerError.contains("connection") || lowerError.contains("connect")) {
            return "connection";
        } else if (lowerError.contains("authentication") || lowerError.contains("auth")) {
            return "authentication";
        } else if (lowerError.contains("invalid") || lowerError.contains("validation")) {
            return "validation";
        } else {
            return "other";
        }
    }
}

