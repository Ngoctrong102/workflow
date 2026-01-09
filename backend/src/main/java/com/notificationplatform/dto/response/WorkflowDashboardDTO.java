package com.notificationplatform.dto.response;

import java.time.LocalDateTime;

public class WorkflowDashboardDTO {

    private WorkflowInfo workflow;
    private WorkflowDashboardMetricsDTO metrics;
    private TrendsInfo trends;
    private PeriodInfo period;

    // Getters and Setters
    public WorkflowInfo getWorkflow() {
        return workflow;
    }

    public void setWorkflow(WorkflowInfo workflow) {
        this.workflow = workflow;
    }

    public WorkflowDashboardMetricsDTO getMetrics() {
        return metrics;
    }

    public void setMetrics(WorkflowDashboardMetricsDTO metrics) {
        this.metrics = metrics;
    }

    public TrendsInfo getTrends() {
        return trends;
    }

    public void setTrends(TrendsInfo trends) {
        this.trends = trends;
    }

    public PeriodInfo getPeriod() {
        return period;
    }

    public void setPeriod(PeriodInfo period) {
        this.period = period;
    }

    public static class WorkflowInfo {
        private String id;
        private String name;
        private String status;
        private LocalDateTime lastExecution;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDateTime getLastExecution() {
            return lastExecution;
        }

        public void setLastExecution(LocalDateTime lastExecution) {
            this.lastExecution = lastExecution;
        }
    }

    public static class TrendsInfo {
        private Double executionsChange;
        private Double successRateChange;
        private Double executionTimeChange;

        public Double getExecutionsChange() {
            return executionsChange;
        }

        public void setExecutionsChange(Double executionsChange) {
            this.executionsChange = executionsChange;
        }

        public Double getSuccessRateChange() {
            return successRateChange;
        }

        public void setSuccessRateChange(Double successRateChange) {
            this.successRateChange = successRateChange;
        }

        public Double getExecutionTimeChange() {
            return executionTimeChange;
        }

        public void setExecutionTimeChange(Double executionTimeChange) {
            this.executionTimeChange = executionTimeChange;
        }
    }

    public static class PeriodInfo {
        private LocalDateTime start;
        private LocalDateTime end;

        public LocalDateTime getStart() {
            return start;
        }

        public void setStart(LocalDateTime start) {
            this.start = start;
        }

        public LocalDateTime getEnd() {
            return end;
        }

        public void setEnd(LocalDateTime end) {
            this.end = end;
        }
    }
}

