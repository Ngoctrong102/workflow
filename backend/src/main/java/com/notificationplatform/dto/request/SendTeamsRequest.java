package com.notificationplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SendTeamsRequest {

    @NotBlank(message = "Channel ID is required")
    private String channelId;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Text is required")
    private String text;

    @Size(max = 7, message = "Theme color must be a hex color code")
    private String themeColor; // Hex color code (e.g., #FF0000)

    private java.util.Map<String, Object> variables;

    // Getters and Setters
    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getThemeColor() {
        return themeColor;
    }

    public void setThemeColor(String themeColor) {
        this.themeColor = themeColor;
    }

    public java.util.Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(java.util.Map<String, Object> variables) {
        this.variables = variables;
    }
}

