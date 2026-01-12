package com.notificationplatform.service.dashboard;

import com.notificationplatform.dto.response.ExecutionStatusResponse;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.dto.response.WorkflowDashboardDTO;
import com.notificationplatform.dto.response.WorkflowDashboardMetricsDTO;
import com.notificationplatform.dto.response.WorkflowErrorAnalysisDTO;
import com.notificationplatform.dto.response.WorkflowExecutionTrendDTO;
import com.notificationplatform.dto.response.WorkflowNodePerformanceDTO;
import com.notificationplatform.entity.Execution;
import com.notificationplatform.entity.NodeExecution;
import com.notificationplatform.entity.Workflow;
import com.notificationplatform.entity.enums.ExecutionStatus;
import com.notificationplatform.exception.ResourceNotFoundException;
import com.notificationplatform.repository.ExecutionRepository;
import com.notificationplatform.repository.NodeExecutionRepository;
import com.notificationplatform.repository.WorkflowRepository;


import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@Transactional
public class WorkflowDashboardServiceImpl implements WorkflowDashboardService {

    private final WorkflowRepository workflowRepository;
    private final ExecutionRepository executionRepository;
    private final NodeExecutionRepository nodeExecutionRepository;

    public WorkflowDashboardServiceImpl(WorkflowRepository workflowRepository,
                                      ExecutionRepository executionRepository,
                                      NodeExecutionRepository nodeExecutionRepository) {
        this.workflowRepository = workflowRepository;
        this.executionRepository = executionRepository;
        this.nodeExecutionRepository = nodeExecutionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "workflowDashboard", key = "#workflowId + '_' + #startDate + '_' + #endDate", unless = "#result == null")
    public WorkflowDashboardDTO getDashboardOverview(String workflowId, LocalDateTime startDate,
                                                      LocalDateTime endDate, String timezone) {
        log.debug("Getting dashboard overview for workflow: {}", workflowId);

        Workflow workflow = workflowRepository.findByIdAndNotDeleted(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + workflowId));

        ZoneId zoneId = timezone != null ? ZoneId.of(timezone) : ZoneId.systemDefault();
        if (startDate == null) {
            startDate = LocalDateTime.now(zoneId).minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now(zoneId);
        }

        // Convert to UTC for database queries
        ZonedDateTime startZoned = startDate.atZone(zoneId);
        ZonedDateTime endZoned = endDate.atZone(zoneId);
        LocalDateTime startUtc = startZoned.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
        LocalDateTime endUtc = endZoned.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();

        // Get execution metrics
        List<Execution> executions = executionRepository.findByWorkflowIdAndStartedAtBetween(
                workflowId, startUtc, endUtc);

        long totalExecutions = executions.size();
        long successfulExecutions = executionRepository.countByWorkflowIdAndStatusAndDateRange(
                workflowId, ExecutionStatus.COMPLETED.getValue(), startUtc, endUtc);
        long failedExecutions = executionRepository.countByWorkflowIdAndStatusAndDateRange(
                workflowId, ExecutionStatus.FAILED.getValue(), startUtc, endUtc);
        long runningExecutions = executionRepository.countByWorkflowIdAndStatusAndDateRange(
                workflowId, ExecutionStatus.RUNNING.getValue(), startUtc, endUtc);

        Double averageExecutionTime = executionRepository.getAverageExecutionTime(workflowId, startUtc, endUtc);
        if (averageExecutionTime == null) {
            averageExecutionTime = 0.0;
        }

        double successRate = totalExecutions > 0 ? (double) successfulExecutions / totalExecutions * 100 : 0.0;
        double errorRate = totalExecutions > 0 ? (double) failedExecutions / totalExecutions * 100 : 0.0;

        // Group executions by status (convert enum to string)
        Map<String, Long> executionsByStatus = executions.stream()
                .collect(Collectors.groupingBy(
                    e -> e.getStatus() != null ? e.getStatus().getValue() : "unknown",
                    Collectors.counting()
                ));

        // Get executions by trigger type
        List<Object[]> triggerTypeCounts = executionRepository.countExecutionsByTriggerType(workflowId, startUtc, endUtc);
        Map<String, Long> executionsByTriggerType = new HashMap<>();
        for (Object[] row : triggerTypeCounts) {
            String triggerType = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            executionsByTriggerType.put(triggerType != null ? triggerType : "manual", count);
        }

        // Get first and last execution dates
        Object[] dateRange = executionRepository.getFirstAndLastExecutionDates(workflowId, startUtc, endUtc);
        LocalDateTime firstExecutionAt = null;
        LocalDateTime lastExecutionAt = null;
        if (dateRange != null && dateRange.length >= 2) {
            if (dateRange[0] != null) {
                firstExecutionAt = dateRange[0] instanceof LocalDateTime ? 
                    (LocalDateTime) dateRange[0] : 
                    ((java.sql.Timestamp) dateRange[0]).toLocalDateTime();
            }
            if (dateRange[1] != null) {
                lastExecutionAt = dateRange[1] instanceof LocalDateTime ? 
                    (LocalDateTime) dateRange[1] : 
                    ((java.sql.Timestamp) dateRange[1]).toLocalDateTime();
            }
        }

        // Calculate trend comparison (compare with previous period)
        long periodDays = ChronoUnit.DAYS.between(startUtc, endUtc);
        LocalDateTime previousStartUtc = startUtc.minusDays(periodDays);
        LocalDateTime previousEndUtc = startUtc;

        List<Execution> previousExecutions = executionRepository.findByWorkflowIdAndStartedAtBetween(
                workflowId, previousStartUtc, previousEndUtc);
        long previousTotalExecutions = previousExecutions.size();
        long previousSuccessfulExecutions = previousExecutions.stream()
                .filter(e -> "completed".equals(e.getStatus()))
                .count();
        Double previousAverageExecutionTime = executionRepository.getAverageExecutionTime(
                workflowId, previousStartUtc, previousEndUtc);
        if (previousAverageExecutionTime == null) {
            previousAverageExecutionTime = 0.0;
        }

        WorkflowDashboardMetricsDTO.TrendComparison trendComparison = new WorkflowDashboardMetricsDTO.TrendComparison();
        trendComparison.setExecutionCountChange(calculatePercentageChange(totalExecutions, previousTotalExecutions));
        trendComparison.setSuccessRateChange(calculatePercentageChange(successRate,
                previousTotalExecutions > 0 ? (double) previousSuccessfulExecutions / previousTotalExecutions * 100 : 0.0));
        trendComparison.setAverageExecutionTimeChange(calculatePercentageChange(averageExecutionTime, previousAverageExecutionTime));

        // Build metrics DTO
        WorkflowDashboardMetricsDTO metrics = new WorkflowDashboardMetricsDTO();
        metrics.setTotalExecutions(totalExecutions);
        metrics.setSuccessfulExecutions(successfulExecutions);
        metrics.setFailedExecutions(failedExecutions);
        metrics.setRunningExecutions(runningExecutions);
        metrics.setSuccessRate(successRate);
        metrics.setErrorRate(errorRate);
        metrics.setAverageExecutionTime(averageExecutionTime);
        metrics.setExecutionsByStatus(executionsByStatus);
        metrics.setExecutionsByTriggerType(executionsByTriggerType);
        metrics.setFirstExecutionAt(firstExecutionAt);
        metrics.setLastExecutionAt(lastExecutionAt);
        metrics.setTrendComparison(trendComparison);

        // Build workflow info
        WorkflowDashboardDTO.WorkflowInfo workflowInfo = new WorkflowDashboardDTO.WorkflowInfo();
        workflowInfo.setId(workflow.getId());
        workflowInfo.setName(workflow.getName());
        workflowInfo.setStatus(workflow.getStatus() != null ? workflow.getStatus().getValue() : null);
        workflowInfo.setLastExecution(lastExecutionAt);

        // Build trends info from trend comparison
        WorkflowDashboardDTO.TrendsInfo trendsInfo = new WorkflowDashboardDTO.TrendsInfo();
        if (trendComparison != null) {
            trendsInfo.setExecutionsChange(trendComparison.getExecutionCountChange());
            trendsInfo.setSuccessRateChange(trendComparison.getSuccessRateChange());
            trendsInfo.setExecutionTimeChange(trendComparison.getAverageExecutionTimeChange());
        }

        // Build period info
        WorkflowDashboardDTO.PeriodInfo periodInfo = new WorkflowDashboardDTO.PeriodInfo();
        periodInfo.setStart(startDate);
        periodInfo.setEnd(endDate);

        // Build dashboard DTO
        WorkflowDashboardDTO dashboard = new WorkflowDashboardDTO();
        dashboard.setWorkflow(workflowInfo);
        dashboard.setMetrics(metrics);
        dashboard.setTrends(trendsInfo);
        dashboard.setPeriod(periodInfo);

        return dashboard;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "workflowTrends", key = "#workflowId + '_' + #startDate + '_' + #endDate + '_' + #granularity", unless = "#result == null")
    public List<WorkflowExecutionTrendDTO> getExecutionTrends(String workflowId, LocalDateTime startDate,
                                                              LocalDateTime endDate, String granularity) {
        log.debug("Getting execution trends for workflow: {} with granularity: {}", workflowId, granularity);

        Workflow workflow = workflowRepository.findByIdAndNotDeleted(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + workflowId));

        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        // Determine granularity based on time range if not specified
        if (granularity == null || granularity.isEmpty()) {
            long hours = ChronoUnit.HOURS.between(startDate, endDate);
            if (hours <= 24) {
                granularity = "hourly";
            } else if (hours <= 30 * 24) {
                granularity = "daily";
            } else if (hours <= 90 * 24) {
                granularity = "weekly";
            } else {
                granularity = "monthly";
            }
        }

        List<Execution> executions = executionRepository.findByWorkflowIdAndStartedAtBetween(
                workflowId, startDate, endDate);

        // Group executions by time period
        Map<LocalDateTime, List<Execution>> groupedExecutions = new TreeMap<>();
        for (Execution execution : executions) {
            LocalDateTime periodStart = getPeriodStart(execution.getStartedAt(), granularity);
            groupedExecutions.computeIfAbsent(periodStart, k -> new ArrayList<>()).add(execution);
        }

        // Build trend DTOs
        List<WorkflowExecutionTrendDTO> trends = new ArrayList<>();
        for (Map.Entry<LocalDateTime, List<Execution>> entry : groupedExecutions.entrySet()) {
            LocalDateTime periodStart = entry.getKey();
            List<Execution> periodExecutions = entry.getValue();

            long executionCount = periodExecutions.size();
            long successfulCount = periodExecutions.stream()
                    .filter(e -> "completed".equals(e.getStatus()))
                    .count();
            long failedCount = periodExecutions.stream()
                    .filter(e -> "failed".equals(e.getStatus()))
                    .count();

            double averageExecutionTime = periodExecutions.stream()
                    .filter(e -> e.getDuration() != null)
                    .mapToInt(Execution::getDuration)
                    .average()
                    .orElse(0.0);

            double successRate = executionCount > 0 ? (double) successfulCount / executionCount * 100 : 0.0;

            LocalDateTime periodEnd = getPeriodEnd(periodStart, granularity);

            WorkflowExecutionTrendDTO trend = new WorkflowExecutionTrendDTO();
            trend.setPeriodStart(periodStart);
            trend.setPeriodEnd(periodEnd);
            trend.setGranularity(granularity);
            trend.setExecutionCount(executionCount);
            trend.setSuccessfulCount(successfulCount);
            trend.setFailedCount(failedCount);
            trend.setAverageExecutionTime(averageExecutionTime);
            trend.setSuccessRate(successRate);

            trends.add(trend);
        }

        return trends;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "workflowNodePerformance", key = "#workflowId + '_' + #startDate + '_' + #endDate", unless = "#result == null")
    public List<WorkflowNodePerformanceDTO> getNodePerformance(String workflowId, LocalDateTime startDate,
                                                               LocalDateTime endDate) {
        log.debug("Getting node performance for workflow: {}", workflowId);

        Workflow workflow = workflowRepository.findByIdAndNotDeleted(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + workflowId));

        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        // Get node performance metrics
        List<Object[]> nodeMetrics = nodeExecutionRepository.getNodePerformanceMetrics(workflowId, startDate, endDate);

        // Get node errors by type
        List<Object[]> nodeErrors = nodeExecutionRepository.getNodeErrorsByType(workflowId, startDate, endDate);

        // Build error map by node
        Map<String, Map<String, Long>> errorsByNode = new HashMap<>();
        for (Object[] row : nodeErrors) {
            String nodeId = (String) row[0];
            String error = (String) row[1];
            Long count = ((Number) row[2]).longValue();
            errorsByNode.computeIfAbsent(nodeId, k -> new HashMap<>()).put(error, count);
        }

        // Build node performance DTOs
        List<WorkflowNodePerformanceDTO> nodePerformanceList = new ArrayList<>();
        for (Object[] row : nodeMetrics) {
            String nodeId = (String) row[0];
            Long executionCount = ((Number) row[1]).longValue();
            Long successfulCount = ((Number) row[2]).longValue();
            Long failedCount = ((Number) row[3]).longValue();
            Double averageExecutionTime = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;

            double successRate = executionCount > 0 ? (double) successfulCount / executionCount * 100 : 0.0;

            Map<String, Long> errorsByType = errorsByNode.getOrDefault(nodeId, new HashMap<>());
            long totalErrors = errorsByType.values().stream().mapToLong(Long::longValue).sum();

            WorkflowNodePerformanceDTO nodePerformance = new WorkflowNodePerformanceDTO();
            nodePerformance.setNodeId(nodeId);
            nodePerformance.setNodeType(extractNodeTypeFromWorkflow(workflow, nodeId));
            nodePerformance.setNodeName(extractNodeName(workflow, nodeId));
            nodePerformance.setExecutionCount(executionCount);
            nodePerformance.setSuccessfulCount(successfulCount);
            nodePerformance.setFailedCount(failedCount);
            nodePerformance.setAverageExecutionTime(averageExecutionTime);
            nodePerformance.setSuccessRate(successRate);
            nodePerformance.setTotalErrors(totalErrors);
            nodePerformance.setErrorsByType(errorsByType);

            nodePerformanceList.add(nodePerformance);
        }

        return nodePerformanceList;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ExecutionStatusResponse> getExecutionHistory(String workflowId, String status,
                                                                      LocalDateTime startDate, LocalDateTime endDate,
                                                                      String triggerType, int limit, int offset) {
        log.debug("Getting execution history for workflow: {}", workflowId);

        Workflow workflow = workflowRepository.findByIdAndNotDeleted(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + workflowId));

        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        // Get executions
        List<Execution> executions = executionRepository.findByWorkflowIdAndStartedAtBetween(workflowId, startDate, endDate);

        // Filter by status
        if (status != null && !status.isEmpty()) {
            ExecutionStatus statusEnum = ExecutionStatus.fromValue(status);
            if (statusEnum != null) {
                executions = executions.stream()
                        .filter(e -> statusEnum.equals(e.getStatus()))
                        .collect(Collectors.toList());
            }
        }

        // Filter by trigger type
        if (triggerType != null && !triggerType.isEmpty()) {
            executions = executions.stream()
                    .filter(e -> e.getTrigger() != null && 
                            e.getTrigger().getTriggerType() != null &&
                            triggerType.equals(e.getTrigger().getTriggerType().getValue()))
                    .collect(Collectors.toList());
        }

        // Sort by started date descending
        executions.sort((a, b) -> b.getStartedAt().compareTo(a.getStartedAt()));

        // Apply pagination
        int total = executions.size();
        int fromIndex = Math.min(offset, total);
        int toIndex = Math.min(offset + limit, total);
        List<Execution> pagedExecutions = executions.subList(fromIndex, toIndex);

        // Map to ExecutionStatusResponse
        List<ExecutionStatusResponse> executionResponses = pagedExecutions.stream()
                .map(this::mapToExecutionStatusResponse)
                .collect(Collectors.toList());

        PagedResponse<ExecutionStatusResponse> response = new PagedResponse<>();
        response.setData(executionResponses);
        response.setTotal(total);
        response.setLimit(limit);
        response.setOffset(offset);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<WorkflowErrorAnalysisDTO> getErrorAnalysis(String workflowId, LocalDateTime startDate,
                                                                    LocalDateTime endDate, String errorType,
                                                                    int limit, int offset) {
        log.debug("Getting error analysis for workflow: {}", workflowId);

        Workflow workflow = workflowRepository.findByIdAndNotDeleted(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + workflowId));

        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        // Get failed executions
        List<Execution> failedExecutions = executionRepository.findByWorkflowIdAndStartedAtBetween(workflowId, startDate, endDate)
                .stream()
                .filter(e -> "failed".equals(e.getStatus()))
                .collect(Collectors.toList());

        // Get failed node executions
        List<NodeExecution> failedNodeExecutions = new ArrayList<>();
        for (Execution execution : failedExecutions) {
            List<NodeExecution> nodeExecutions = nodeExecutionRepository.findByExecutionIdAndStatus(
                    execution.getId(), "failed");
            failedNodeExecutions.addAll(nodeExecutions);
        }

        // Group errors by type and message
        Map<String, WorkflowErrorAnalysisDTO> errorMap = new HashMap<>();
        for (NodeExecution nodeExecution : failedNodeExecutions) {
            String errorKey = nodeExecution.getError() != null ? nodeExecution.getError() : "Unknown error";
            if (errorType != null && !errorType.isEmpty() && !errorKey.contains(errorType)) {
                continue;
            }

            WorkflowErrorAnalysisDTO errorAnalysis = errorMap.computeIfAbsent(errorKey, k -> {
                WorkflowErrorAnalysisDTO dto = new WorkflowErrorAnalysisDTO();
                dto.setErrorId(UUID.randomUUID().toString());
                dto.setErrorType(extractErrorType(errorKey));
                dto.setErrorMessage(errorKey);
                dto.setOccurrenceCount(0L);
                return dto;
            });

            errorAnalysis.setOccurrenceCount(errorAnalysis.getOccurrenceCount() + 1);
            if (errorAnalysis.getNodeId() == null) {
                errorAnalysis.setNodeId(nodeExecution.getNodeId());
                errorAnalysis.setNodeType(extractNodeType(nodeExecution));
            }
            if (errorAnalysis.getExecutionId() == null) {
                errorAnalysis.setExecutionId(nodeExecution.getExecution().getId());
            }
            if (errorAnalysis.getOccurredAt() == null ||
                    nodeExecution.getStartedAt().isAfter(errorAnalysis.getOccurredAt())) {
                errorAnalysis.setOccurredAt(nodeExecution.getStartedAt());
            }
        }

        List<WorkflowErrorAnalysisDTO> errors = new ArrayList<>(errorMap.values());
        errors.sort((a, b) -> b.getOccurrenceCount().compareTo(a.getOccurrenceCount()));

        // Apply pagination
        int total = errors.size();
        int fromIndex = Math.min(offset, total);
        int toIndex = Math.min(offset + limit, total);
        List<WorkflowErrorAnalysisDTO> pagedErrors = errors.subList(fromIndex, toIndex);

        PagedResponse<WorkflowErrorAnalysisDTO> response = new PagedResponse<>();
        response.setData(pagedErrors);
        response.setTotal(total);
        response.setLimit(limit);
        response.setOffset(offset);

        return response;
    }

    // Helper methods
    private double calculatePercentageChange(double current, double previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((current - previous) / previous) * 100;
    }

    private LocalDateTime getPeriodStart(LocalDateTime dateTime, String granularity) {
        switch (granularity.toLowerCase()) {
            case "hourly":
                return dateTime.truncatedTo(ChronoUnit.HOURS);
            case "daily":
                return dateTime.truncatedTo(ChronoUnit.DAYS);
            case "weekly":
                return dateTime.with(java.time.DayOfWeek.MONDAY).truncatedTo(ChronoUnit.DAYS);
            case "monthly":
                return dateTime.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
            default:
                return dateTime.truncatedTo(ChronoUnit.DAYS);
        }
    }

    private LocalDateTime getPeriodEnd(LocalDateTime periodStart, String granularity) {
        switch (granularity.toLowerCase()) {
            case "hourly":
                return periodStart.plusHours(1);
            case "daily":
                return periodStart.plusDays(1);
            case "weekly":
                return periodStart.plusWeeks(1);
            case "monthly":
                return periodStart.plusMonths(1);
            default:
                return periodStart.plusDays(1);
        }
    }

    private String extractNodeName(Workflow workflow, String nodeId) {
        if (workflow.getDefinition() != null) {
            Map<String, Object> definition = (Map<String, Object>) workflow.getDefinition();
            if (definition.containsKey("nodes")) {
                List<Map<String, Object>> nodes = (List<Map<String, Object>>) definition.get("nodes");
                for (Map<String, Object> node : nodes) {
                    if (nodeId.equals(node.get("id"))) {
                        return (String) node.getOrDefault("name", nodeId);
                    }
                }
            }
        }
        return nodeId;
    }

    private String extractNodeTypeFromWorkflow(Workflow workflow, String nodeId) {
        if (workflow.getDefinition() != null) {
            Map<String, Object> definition = (Map<String, Object>) workflow.getDefinition();
            if (definition.containsKey("nodes")) {
                List<Map<String, Object>> nodes = (List<Map<String, Object>>) definition.get("nodes");
                for (Map<String, Object> node : nodes) {
                    if (nodeId.equals(node.get("id"))) {
                        return (String) node.getOrDefault("type", "unknown");
                    }
                }
            }
        }
        return "unknown";
    }

    private String extractNodeType(NodeExecution nodeExecution) {
        if (nodeExecution.getOutputData() != null) {
            Map<String, Object> output = (Map<String, Object>) nodeExecution.getOutputData();
            return (String) output.getOrDefault("nodeType", "unknown");
        }
        return "unknown";
    }

    private String extractErrorType(String errorMessage) {
        if (errorMessage == null || errorMessage.isEmpty()) {
            return "Unknown";
        }
        if (errorMessage.contains("timeout") || errorMessage.contains("Timeout")) {
            return "Timeout";
        } else if (errorMessage.contains("connection") || errorMessage.contains("Connection")) {
            return "Connection";
        } else if (errorMessage.contains("validation") || errorMessage.contains("Validation")) {
            return "Validation";
        } else if (errorMessage.contains("not found") || errorMessage.contains("NotFound")) {
            return "NotFound";
        } else {
            return "General";
        }
    }

    private ExecutionStatusResponse mapToExecutionStatusResponse(Execution execution) {
        ExecutionStatusResponse response = new ExecutionStatusResponse();
        response.setExecutionId(execution.getId());
        response.setWorkflowId(execution.getWorkflow().getId());
        response.setWorkflowName(execution.getWorkflow().getName());
        response.setStatus(execution.getStatus() != null ? execution.getStatus().getValue() : null);
        response.setStartedAt(execution.getStartedAt());
        response.setCompletedAt(execution.getCompletedAt());
        response.setDuration(execution.getDuration());
        response.setError(execution.getError());

        // Get trigger data
        Map<String, Object> triggerDataMap = null;
        if (execution.getContext() != null) {
            Map<String, Object> context = execution.getContext();
            if (context.containsKey("triggerData")) {
                triggerDataMap = (Map<String, Object>) context.get("triggerData");
            } else {
                triggerDataMap = context;
            }
        }
        if (triggerDataMap == null && execution.getTrigger() != null && execution.getTrigger().getConfig() != null) {
            triggerDataMap = (Map<String, Object>) execution.getTrigger().getConfig();
        }
        response.setTriggerData(triggerDataMap);

        // Get node executions
        List<NodeExecution> nodeExecutions = nodeExecutionRepository.findByExecutionIdOrderByStartedAtAsc(execution.getId());
        List<ExecutionStatusResponse.NodeExecutionStatus> nodeStatuses = nodeExecutions.stream()
                .map(ne -> {
                    ExecutionStatusResponse.NodeExecutionStatus status = new ExecutionStatusResponse.NodeExecutionStatus();
                    status.setNodeId(ne.getNodeId());
                    status.setNodeType(extractNodeType(ne));
                    status.setStatus(ne.getStatus() != null ? ne.getStatus().getValue() : null);
                    status.setStartedAt(ne.getStartedAt());
                    status.setCompletedAt(ne.getCompletedAt());
                    status.setDuration(ne.getDuration());
                    status.setError(ne.getError());
                    status.setOutput(ne.getOutputData() != null ?
                            (Map<String, Object>) ne.getOutputData() : null);
                    return status;
                })
                .collect(Collectors.toList());
        response.setNodeExecutions(nodeStatuses);

        return response;
    }

    @CacheEvict(value = {"workflowDashboard", "workflowTrends", "workflowNodePerformance"},
                key = "#workflowId + '_*'")
    public void evictDashboardCache(String workflowId) {
        log.debug("Evicting dashboard cache for workflow: {}", workflowId);
    }
}

