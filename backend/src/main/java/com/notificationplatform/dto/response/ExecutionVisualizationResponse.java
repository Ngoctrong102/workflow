package com.notificationplatform.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for execution visualization.
 * 
 * See: @import(api/endpoints.md#execution-visualization)
 */
public class ExecutionVisualizationResponse {

    private ExecutionInfo execution;
    private WorkflowInfo workflow;
    private TriggerInfo trigger;
    private Integer currentStep;
    private Integer totalSteps;
    private List<NodeInfo> nodes;
    private Map<String, Object> context;

    // Getters and Setters
    public ExecutionInfo getExecution() {
        return execution;
    }

    public void setExecution(ExecutionInfo execution) {
        this.execution = execution;
    }

    public WorkflowInfo getWorkflow() {
        return workflow;
    }

    public void setWorkflow(WorkflowInfo workflow) {
        this.workflow = workflow;
    }

    public TriggerInfo getTrigger() {
        return trigger;
    }

    public void setTrigger(TriggerInfo trigger) {
        this.trigger = trigger;
    }

    public Integer getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(Integer currentStep) {
        this.currentStep = currentStep;
    }

    public Integer getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(Integer totalSteps) {
        this.totalSteps = totalSteps;
    }

    public List<NodeInfo> getNodes() {
        return nodes;
    }

    public void setNodes(List<NodeInfo> nodes) {
        this.nodes = nodes;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    public static class ExecutionInfo {
        private String id;
        private String workflowId;
        private String status;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getWorkflowId() {
            return workflowId;
        }

        public void setWorkflowId(String workflowId) {
            this.workflowId = workflowId;
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
    }

    public static class WorkflowInfo {
        private String id;
        private String name;
        private Map<String, Object> definition;

        // Getters and Setters
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

        public Map<String, Object> getDefinition() {
            return definition;
        }

        public void setDefinition(Map<String, Object> definition) {
            this.definition = definition;
        }
    }

    public static class TriggerInfo {
        private String type;
        private Map<String, Object> data;

        // Getters and Setters
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Map<String, Object> getData() {
            return data;
        }

        public void setData(Map<String, Object> data) {
            this.data = data;
        }
    }

    public static class NodeInfo {
        private String id;
        private String type;
        private String status;
        private Map<String, Object> data;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Map<String, Object> getData() {
            return data;
        }

        public void setData(Map<String, Object> data) {
            this.data = data;
        }
    }
}

