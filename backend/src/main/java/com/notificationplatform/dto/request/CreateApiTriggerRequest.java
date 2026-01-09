package com.notificationplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Map;

public class CreateApiTriggerRequest {

    @NotBlank(message = "Workflow ID is required")
    private String workflowId;

    @NotBlank(message = "Path is required")
    @Pattern(regexp = "^/[a-zA-Z0-9/_-]+$", message = "Path must start with / and contain only alphanumeric characters, /, _, or -")
    @Size(max = 255, message = "Path must not exceed 255 characters")
    private String path;

    @NotBlank(message = "HTTP method is required")
    @Pattern(regexp = "^(GET|POST|PUT|PATCH|DELETE)$", message = "HTTP method must be one of: GET, POST, PUT, PATCH, DELETE")
    private String method = "POST";

    private String apiKey; // Optional API key for authentication

    private Map<String, Object> requestSchema; // Optional JSON schema for request validation

    // Getters and Setters
    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Map<String, Object> getRequestSchema() {
        return requestSchema;
    }

    public void setRequestSchema(Map<String, Object> requestSchema) {
        this.requestSchema = requestSchema;
    }
}

