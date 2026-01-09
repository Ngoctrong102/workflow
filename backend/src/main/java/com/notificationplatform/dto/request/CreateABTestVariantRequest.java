package com.notificationplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Map;

public class CreateABTestVariantRequest {

    @NotBlank(message = "Variant name is required")
    @Size(max = 100, message = "Variant name must not exceed 100 characters")
    private String name; // A, B, C, etc.

    @Size(max = 255, message = "Label must not exceed 255 characters")
    private String label; // Descriptive label

    private String templateId;

    private String channelId;

    private Map<String, Object> config; // Variant-specific configuration

    private BigDecimal trafficPercentage; // Percentage of traffic for this variant

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }

    public BigDecimal getTrafficPercentage() {
        return trafficPercentage;
    }

    public void setTrafficPercentage(BigDecimal trafficPercentage) {
        this.trafficPercentage = trafficPercentage;
    }
}

