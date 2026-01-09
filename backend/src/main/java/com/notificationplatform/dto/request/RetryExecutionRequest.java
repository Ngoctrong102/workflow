package com.notificationplatform.dto.request;

public class RetryExecutionRequest {

    private Boolean fromFailedNode; // If true, retry from the failed node; if false, retry from beginning

    // Getters and Setters
    public Boolean getFromFailedNode() {
        return fromFailedNode;
    }

    public void setFromFailedNode(Boolean fromFailedNode) {
        this.fromFailedNode = fromFailedNode;
    }
}

