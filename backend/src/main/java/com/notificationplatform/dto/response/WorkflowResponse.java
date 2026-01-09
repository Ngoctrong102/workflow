package com.notificationplatform.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "Workflow response with full definition")
public class WorkflowResponse {

    @Schema(description = "Workflow ID", example = "workflow-123")
    private String id;
    
    @Schema(description = "Workflow name", example = "Welcome Email Workflow")
    private String name;
    
    @Schema(description = "Workflow description", example = "Sends welcome email to new users")
    private String description;
    
    @Schema(description = "Workflow definition with nodes and edges")
    private Map<String, Object> definition;
    
    @Schema(description = "Workflow status", example = "active", allowableValues = {"draft", "active", "inactive", "paused", "archived"})
    private String status;
    
    @Schema(description = "Workflow version", example = "1")
    private Integer version;
    
    @Schema(description = "Workflow tags", example = "[\"onboarding\", \"email\"]")
    private List<String> tags;
    
    @Schema(description = "Creation timestamp", example = "2024-01-01T00:00:00Z")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last update timestamp", example = "2024-01-01T00:00:00Z")
    private LocalDateTime updatedAt;

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
}

