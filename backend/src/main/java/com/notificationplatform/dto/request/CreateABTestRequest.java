package com.notificationplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class CreateABTestRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    private String description;

    @NotBlank(message = "Workflow ID is required")
    private String workflowId;

    @NotBlank(message = "Success metric is required")
    @Size(max = 100, message = "Success metric must not exceed 100 characters")
    private String successMetric = "open_rate"; // open_rate, click_rate, conversion_rate, engagement_rate

    @NotBlank(message = "Traffic split type is required")
    @Size(max = 50, message = "Traffic split type must not exceed 50 characters")
    private String trafficSplitType = "equal"; // equal, custom, weighted

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Integer minSampleSize = 1000;

    private BigDecimal confidenceLevel = new BigDecimal("95.00");

    @NotNull(message = "Variants are required")
    @Size(min = 2, message = "At least 2 variants are required")
    @Valid
    private List<com.notificationplatform.dto.request.CreateABTestVariantRequest> variants;

    private Map<String, Object> metadata;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getSuccessMetric() {
        return successMetric;
    }

    public void setSuccessMetric(String successMetric) {
        this.successMetric = successMetric;
    }

    public String getTrafficSplitType() {
        return trafficSplitType;
    }

    public void setTrafficSplitType(String trafficSplitType) {
        this.trafficSplitType = trafficSplitType;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Integer getMinSampleSize() {
        return minSampleSize;
    }

    public void setMinSampleSize(Integer minSampleSize) {
        this.minSampleSize = minSampleSize;
    }

    public BigDecimal getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(BigDecimal confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }

    public List<CreateABTestVariantRequest> getVariants() {
        return variants;
    }

    public void setVariants(List<CreateABTestVariantRequest> variants) {
        this.variants = variants;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}

