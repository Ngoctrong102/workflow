package com.notificationplatform.dto.response;

import java.util.List;

public class DeliveryResult {

    private boolean success;
    private String messageId;
    private String status; // sent, failed, bounced
    private String error;
    private List<String> failedRecipients;

    public DeliveryResult() {
    }

    public DeliveryResult(boolean success, String messageId, String status) {
        this.success = success;
        this.messageId = messageId;
        this.status = status;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<String> getFailedRecipients() {
        return failedRecipients;
    }

    public void setFailedRecipients(List<String> failedRecipients) {
        this.failedRecipients = failedRecipients;
    }

    // Static factory methods
    public static DeliveryResult success(String messageId) {
        return new DeliveryResult(true, messageId, "sent");
    }

    public static DeliveryResult failure(String error) {
        DeliveryResult result = new DeliveryResult(false, null, "failed");
        result.setError(error);
        return result;
    }
}

