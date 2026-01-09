package com.notificationplatform.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class CreateObjectTypeRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Size(max = 255, message = "Display name must not exceed 255 characters")
    private String displayName;

    private String description;

    @NotNull(message = "Fields are required")
    @Size(min = 1, message = "At least one field is required")
    @Valid
    private List<FieldDefinitionDTO> fields = new ArrayList<>();

    private List<String> tags;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<FieldDefinitionDTO> getFields() {
        return fields;
    }

    public void setFields(List<FieldDefinitionDTO> fields) {
        this.fields = fields != null ? fields : new ArrayList<>();
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}

