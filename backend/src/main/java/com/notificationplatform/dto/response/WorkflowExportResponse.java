package com.notificationplatform.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class WorkflowExportResponse {

    private String version = "1.0";
    private LocalDateTime exportedAt;
    private List<WorkflowExport> workflows;

    // Getters and Setters
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LocalDateTime getExportedAt() {
        return exportedAt;
    }

    public void setExportedAt(LocalDateTime exportedAt) {
        this.exportedAt = exportedAt;
    }

    public List<WorkflowExport> getWorkflows() {
        return workflows;
    }

    public void setWorkflows(List<WorkflowExport> workflows) {
        this.workflows = workflows;
    }

    public static class WorkflowExport {
        private String id;
        private String name;
        private String description;
        private Map<String, Object> definition;
        private String status;
        private Integer version;
        private List<String> tags;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Map<String, Object> metadata;

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

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Map<String, Object> getDefinition() {
            return definition;
        }

        public void setDefinition(Map<String, Object> definition) {
            this.definition = definition;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Integer getVersion() {
            return version;
        }

        public void setVersion(Integer version) {
            this.version = version;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }
    }
}

