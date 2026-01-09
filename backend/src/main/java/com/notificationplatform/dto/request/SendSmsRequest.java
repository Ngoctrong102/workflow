package com.notificationplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public class SendSmsRequest {

    @NotBlank(message = "Channel ID is required")
    private String channelId;

    @NotBlank(message = "To recipient is required")
    private String to; // Phone number, can be comma-separated for multiple recipients

    @NotBlank(message = "Message is required")
    private String message;

    private Map<String, Object> variables; // Variables for template rendering

    // Getters and Setters
    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
}

