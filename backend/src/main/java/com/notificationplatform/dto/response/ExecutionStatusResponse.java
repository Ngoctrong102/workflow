package com.notificationplatform.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ExecutionStatusResponse {

    private String executionId;
    private String workflowId;
    private String workflowName;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer duration; // in milliseconds
    private String error;
    private Map<String, Object> triggerData;
    private List<NodeExecutionStatus> nodeExecutions;

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

    public List<NodeExecutionStatus> getNodeExecutions() {
        return nodeExecutions;
    }

    public void setNodeExecutions(List<NodeExecutionStatus> nodeExecutions) {
        this.nodeExecutions = nodeExecutions;
    }

    public static class NodeExecutionStatus {
        private String nodeId;
        private String nodeType;
        private String status;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private Integer duration;
        private String error;
        private Map<String, Object> output;

        // Getters and Setters
        public String getNodeId() {
            return nodeId;
        }

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
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

        public Map<String, Object> getOutput() {
            return output;
        }

        public void setOutput(Map<String, Object> output) {
            this.output = output;
        }
    }
}

