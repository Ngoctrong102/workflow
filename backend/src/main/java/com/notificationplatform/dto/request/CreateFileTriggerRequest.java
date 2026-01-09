package com.notificationplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

public class CreateFileTriggerRequest {

    @NotBlank(message = "Workflow ID is required")
    private String workflowId;

    private List<String> fileFormats; // csv, json, xlsx, xls, jsonl

    private Long maxFileSize = 10485760L; // 10MB default

    private Map<String, String> dataMapping; // Map file columns/fields to workflow variables

    private String processingMode = "batch"; // batch or aggregate

    // Getters and Setters
    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public List<String> getFileFormats() {
        return fileFormats;
    }

    public void setFileFormats(List<String> fileFormats) {
        this.fileFormats = fileFormats;
    }

    public Long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(Long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public Map<String, String> getDataMapping() {
        return dataMapping;
    }

    public void setDataMapping(Map<String, String> dataMapping) {
        this.dataMapping = dataMapping;
    }

    public String getProcessingMode() {
        return processingMode;
    }

    public void setProcessingMode(String processingMode) {
        this.processingMode = processingMode;
    }
}

