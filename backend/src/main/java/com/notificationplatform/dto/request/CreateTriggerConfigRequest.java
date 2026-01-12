package com.notificationplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;

/**
 * Request DTO for creating trigger config.
 * Trigger configs are independent and can be shared across multiple workflows.
 */
public class CreateTriggerConfigRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "Trigger type is required")
    private String triggerType; // api-call, scheduler, event

    @NotNull(message = "Config is required")
    private Map<String, Object> config; // Type-specific configuration

    private String status = "active"; // active, inactive

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
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

