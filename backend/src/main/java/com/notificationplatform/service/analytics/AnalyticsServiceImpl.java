package com.notificationplatform.service.analytics;

import com.notificationplatform.dto.response.ErrorAnalyticsResponse;
import com.notificationplatform.dto.response.WorkflowAnalyticsResponse;
import com.notificationplatform.entity.Execution;
import com.notificationplatform.entity.NodeExecution;
import com.notificationplatform.entity.Workflow;
import com.notificationplatform.repository.AnalyticsRepository;
import com.notificationplatform.repository.ExecutionRepository;
import com.notificationplatform.repository.NodeExecutionRepository;
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
    private final AnalyticsRepository analyticsRepository;
    private final NodeExecutionRepository nodeExecutionRepository;
    private final AnalyticsAggregator analyticsAggregator;

    public AnalyticsServiceImpl(WorkflowRepository workflowRepository,
                               ExecutionRepository executionRepository,
                               AnalyticsRepository analyticsRepository,
                               NodeExecutionRepository nodeExecutionRepository,
                               AnalyticsAggregator analyticsAggregator) {
        this.workflowRepository = workflowRepository;
        this.executionRepository = executionRepository;
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

        long totalErrors = failedExecutions.size();

        // Group errors by type
        Map<String, Long> errorsByType = new HashMap<>();
        for (Execution exec : failedExecutions) {
            String errorTypeFromExec = extractErrorType(exec.getError());
            if (errorType == null || errorType.equals(errorTypeFromExec)) {
                errorsByType.put(errorTypeFromExec, errorsByType.getOrDefault(errorTypeFromExec, 0L) + 1);
            }
        }

        // Group errors by workflow
        Map<String, Long> errorsByWorkflow = failedExecutions.stream()
                .filter(e -> e.getWorkflow() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getWorkflow().getId(),
                        Collectors.counting()
                ));

        Map<String, Long> errorsByChannel = new HashMap<>();

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

