package com.notificationplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

public class SendPushRequest {

    @NotBlank(message = "Channel ID is required")
    private String channelId;

    @NotBlank(message = "Device tokens are required")
    private List<String> deviceTokens; // Device tokens for push notifications

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Body is required")
    private String body;

    private String icon; // Optional icon URL
    private String image; // Optional image URL
    private String sound; // Optional sound
    private Integer badge; // Optional badge count
    private Map<String, Object> data; // Custom payload data
    private Map<String, Object> variables; // Variables for template rendering

    // Getters and Setters
    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public List<String> getDeviceTokens() {
        return deviceTokens;
    }

    public void setDeviceTokens(List<String> deviceTokens) {
        this.deviceTokens = deviceTokens;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public Integer getBadge() {
        return badge;
    }

    public void setBadge(Integer badge) {
        this.badge = badge;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
}

