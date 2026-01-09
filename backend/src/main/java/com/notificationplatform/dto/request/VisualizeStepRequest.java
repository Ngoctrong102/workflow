package com.notificationplatform.dto.request;

/**
 * Request DTO for executing next step in visualization.
 * 
 * See: @import(api/endpoints.md#execution-visualization)
 */
public class VisualizeStepRequest {

    private String direction; // "forward" or "backward"

    // Getters and Setters
    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}

