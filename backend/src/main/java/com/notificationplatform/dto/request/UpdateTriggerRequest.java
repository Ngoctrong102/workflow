package com.notificationplatform.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Map;

public class UpdateTriggerRequest {

    @Pattern(regexp = "^/[a-zA-Z0-9/_-]+$", message = "Path must start with / and contain only alphanumeric characters, /, _, or -")
    @Size(max = 255, message = "Path must not exceed 255 characters")
    private String path;

    @Pattern(regexp = "^(GET|POST|PUT|PATCH|DELETE)$", message = "HTTP method must be one of: GET, POST, PUT, PATCH, DELETE")
    private String method;

    private String apiKey;

    private Map<String, Object> requestSchema;

    private String status; // active, inactive

    // Getters and Setters
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

