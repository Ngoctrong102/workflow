package com.notificationplatform.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class ABTestVariantResponse {

    private String id;
    private String abTestId;
    private String name;
    private String label;
    private String templateId;
    private String channelId;
    private Map<String, Object> config;
    private BigDecimal trafficPercentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAbTestId() {
        return abTestId;
    }

    public void setAbTestId(String abTestId) {
        this.abTestId = abTestId;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

