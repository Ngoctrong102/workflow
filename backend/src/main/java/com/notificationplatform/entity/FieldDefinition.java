package com.notificationplatform.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * POJO class representing a field definition in an object type.
 * This class defines the structure and validation rules for a field.
 */
public class FieldDefinition {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String displayName;

    @NotBlank
    @Size(max = 50)
    private String type; // FieldType enum value as string

    private Boolean required = false;

    private Object defaultValue;

    private Map<String, Object> validation = new HashMap<>(); // Validation rules

    @Size(max = 1000)
    private String description;

    private List<String> examples = new ArrayList<>();

    @Size(max = 255)
    private String objectTypeId; // For nested object types

    @Size(max = 50)
    private String itemType; // For array items

    @Size(max = 255)
    private String itemObjectTypeId; // For array of objects

    // Constructors
    public FieldDefinition() {
    }

    public FieldDefinition(String name, String type) {
        this.name = name;
        this.type = type;
    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required != null ? required : false;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Map<String, Object> getValidation() {
        return validation;
    }

    public void setValidation(Map<String, Object> validation) {
        this.validation = validation != null ? validation : new HashMap<>();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getExamples() {
        return examples;
    }

    public void setExamples(List<String> examples) {
        this.examples = examples != null ? examples : new ArrayList<>();
    }

    public String getObjectTypeId() {
        return objectTypeId;
    }

    public void setObjectTypeId(String objectTypeId) {
        this.objectTypeId = objectTypeId;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getItemObjectTypeId() {
        return itemObjectTypeId;
    }

    public void setItemObjectTypeId(String itemObjectTypeId) {
        this.itemObjectTypeId = itemObjectTypeId;
    }
}

