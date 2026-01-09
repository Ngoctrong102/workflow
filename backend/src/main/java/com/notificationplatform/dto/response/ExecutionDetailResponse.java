package com.notificationplatform.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Detailed execution response with full context and logs
 */
public class ExecutionDetailResponse {

    private String executionId;
    private String workflowId;
    private String workflowName;
    private String triggerId;
    private String triggerType;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer duration; // in milliseconds
    private String error;
    private Map<String, Object> triggerData;
    private Map<String, Object> executionContext;
    private List<NodeExecutionDetail> nodeExecutions;
    private List<ExecutionLog> logs;
    private Map<String, Object> performanceMetrics;

    // Getters and Setters
    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(String triggerId) {
        this.triggerId = triggerId;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Map<String, Object> getTriggerData() {
        return triggerData;
    }

    public void setTriggerData(Map<String, Object> triggerData) {
        this.triggerData = triggerData;
    }

    public Map<String, Object> getExecutionContext() {
        return executionContext;
    }

    public void setExecutionContext(Map<String, Object> executionContext) {
        this.executionContext = executionContext;
    }

    public List<NodeExecutionDetail> getNodeExecutions() {
        return nodeExecutions;
    }

    public void setNodeExecutions(List<NodeExecutionDetail> nodeExecutions) {
        this.nodeExecutions = nodeExecutions;
    }

    public List<ExecutionLog> getLogs() {
        return logs;
    }

    public void setLogs(List<ExecutionLog> logs) {
        this.logs = logs;
    }

    public Map<String, Object> getPerformanceMetrics() {
        return performanceMetrics;
    }

    public void setPerformanceMetrics(Map<String, Object> performanceMetrics) {
        this.performanceMetrics = performanceMetrics;
    }

    public static class NodeExecutionDetail {
        private String nodeId;
        private String nodeName;
        private String nodeType;
        private String status;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private Integer duration;
        private String error;
        private Map<String, Object> inputData;
        private Map<String, Object> outputData;
        private List<ExecutionLog> logs;

        // Getters and Setters
        public String getNodeId() {
            return nodeId;
        }

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }

        public String getNodeName() {
            return nodeName;
        }

        public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
        }

        public String getNodeType() {
            return nodeType;
        }

        public void setNodeType(String nodeType) {
            this.nodeType = nodeType;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDateTime getStartedAt() {
            return startedAt;
        }

        public void setStartedAt(LocalDateTime startedAt) {
            this.startedAt = startedAt;
        }

        public LocalDateTime getCompletedAt() {
            return completedAt;
        }

        public void setCompletedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
        }

        public Integer getDuration() {
            return duration;
        }

        public void setDuration(Integer duration) {
            this.duration = duration;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public Map<String, Object> getInputData() {
            return inputData;
        }

        public void setInputData(Map<String, Object> inputData) {
            this.inputData = inputData;
        }

        public Map<String, Object> getOutputData() {
            return outputData;
        }

        public void setOutputData(Map<String, Object> outputData) {
            this.outputData = outputData;
        }

        public List<ExecutionLog> getLogs() {
            return logs;
        }

        public void setLogs(List<ExecutionLog> logs) {
            this.logs = logs;
        }
    }

    public static class ExecutionLog {
        private LocalDateTime timestamp;
        private String level; // INFO, WARN, ERROR, DEBUG
        private String message;
        private String nodeId;
        private Map<String, Object> metadata;

        // Getters and Setters
        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getNodeId() {
            return nodeId;
        }

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }
    }
}

