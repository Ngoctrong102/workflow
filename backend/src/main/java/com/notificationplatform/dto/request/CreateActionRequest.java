package com.notificationplatform.dto.request;

import com.notificationplatform.entity.enums.ActionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;

/**
 * Request DTO for creating a new action definition in the registry.
 */
public class CreateActionRequest {

    @NotBlank(message = "Action ID is required")
    @Size(max = 255, message = "Action ID must not exceed 255 characters")
    private String id;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @NotNull(message = "Type is required")
    private ActionType type;

    private String description;

    @NotNull(message = "Config template is required")
    private Map<String, Object> configTemplate;

    private Map<String, Object> metadata; // Icon, color, version, etc.

    @Size(max = 50, message = "Version must not exceed 50 characters")
    private String version = "1.0.0";

    private Boolean enabled = true;

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

    public ActionType getType() {
        return type;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getConfigTemplate() {
        return configTemplate;
    }

    public void setConfigTemplate(Map<String, Object> configTemplate) {
        this.configTemplate = configTemplate;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version != null ? version : "1.0.0";
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled != null ? enabled : true;
    }
}

