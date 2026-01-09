package com.notificationplatform.dto.response;

public class FieldSearchResult {

    private String objectTypeId;
    private String objectTypeName;
    private FieldDefinitionResponseDTO field;

    public FieldSearchResult() {
    }

    public FieldSearchResult(String objectTypeId, String objectTypeName, FieldDefinitionResponseDTO field) {
        this.objectTypeId = objectTypeId;
        this.objectTypeName = objectTypeName;
        this.field = field;
    }

    // Getters and Setters
    public String getObjectTypeId() {
        return objectTypeId;
    }

    public void setObjectTypeId(String objectTypeId) {
        this.objectTypeId = objectTypeId;
    }

    public String getObjectTypeName() {
        return objectTypeName;
    }

    public void setObjectTypeName(String objectTypeName) {
        this.objectTypeName = objectTypeName;
    }

    public FieldDefinitionResponseDTO getField() {
        return field;
    }

    public void setField(FieldDefinitionResponseDTO field) {
        this.field = field;
    }
}

