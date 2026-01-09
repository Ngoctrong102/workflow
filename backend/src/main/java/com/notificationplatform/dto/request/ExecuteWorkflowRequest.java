package com.notificationplatform.dto.request;

import java.util.Map;

public class ExecuteWorkflowRequest {

    private Map<String, Object> data; // Trigger data for workflow execution

    // Getters and Setters
    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}

