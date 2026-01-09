package com.notificationplatform.dto.response;

public class ValidateFieldResponse {

    private boolean valid;
    private FieldDefinitionResponseDTO field;
    private String error;

    public ValidateFieldResponse() {
    }

    public ValidateFieldResponse(boolean valid, FieldDefinitionResponseDTO field, String error) {
        this.valid = valid;
        this.field = field;
        this.error = error;
    }

    // Getters and Setters
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public FieldDefinitionResponseDTO getField() {
        return field;
    }

    public void setField(FieldDefinitionResponseDTO field) {
        this.field = field;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

