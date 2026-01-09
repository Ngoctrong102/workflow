package com.notificationplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public class RenderTemplateRequest {

    @NotBlank(message = "Template ID is required")
    private String templateId;

    private Map<String, Object> variables;

    // Getters and Setters
    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
}

