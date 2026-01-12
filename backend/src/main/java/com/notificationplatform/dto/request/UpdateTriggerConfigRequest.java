package com.notificationplatform.dto.request;

import jakarta.validation.constraints.Size;
import java.util.Map;

/**
 * Request DTO for updating trigger config.
 */
public class UpdateTriggerConfigRequest {

    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    private Map<String, Object> config; // Type-specific configuration

    private String status; // active, inactive

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

