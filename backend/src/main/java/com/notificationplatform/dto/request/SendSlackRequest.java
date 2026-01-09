package com.notificationplatform.dto.request;

import com.notificationplatform.dto.response.SlackAttachment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class SendSlackRequest {

    @NotBlank(message = "Channel ID is required")
    private String channelId;

    @NotBlank(message = "Channel name is required")
    @Size(max = 100, message = "Channel name must not exceed 100 characters")
    private String channelName; // Slack channel name (e.g., #general)

    @NotBlank(message = "Message is required")
    private String message;

    private List<SlackAttachment> attachments;

    private java.util.Map<String, Object> variables;

    // Getters and Setters
    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<SlackAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<SlackAttachment> attachments) {
        this.attachments = attachments;
    }

    public java.util.Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(java.util.Map<String, Object> variables) {
        this.variables = variables;
    }
}

