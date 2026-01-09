package com.notificationplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ValidateFieldRequest {

    @NotBlank(message = "Object type ID is required")
    @Size(max = 255, message = "Object type ID must not exceed 255 characters")
    private String objectTypeId;

    @NotBlank(message = "Field path is required")
    @Size(max = 500, message = "Field path must not exceed 500 characters")
    private String fieldPath;

    // Getters and Setters
    public String getObjectTypeId() {
        return objectTypeId;
    }

    public void setObjectTypeId(String objectTypeId) {
        this.objectTypeId = objectTypeId;
    }

    public String getFieldPath() {
        return fieldPath;
    }

    public void setFieldPath(String fieldPath) {
        this.fieldPath = fieldPath;
    }
}

