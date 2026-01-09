package com.notificationplatform.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ValidateQueryRequest {

    @NotBlank(message = "Analyst query is required")
    private String analystQuery;

    public String getAnalystQuery() {
        return analystQuery;
    }

    public void setAnalystQuery(String analystQuery) {
        this.analystQuery = analystQuery;
    }
}

