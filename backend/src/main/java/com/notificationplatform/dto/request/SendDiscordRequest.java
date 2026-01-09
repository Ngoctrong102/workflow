package com.notificationplatform.dto.request;

import com.notificationplatform.dto.response.DiscordEmbed;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SendDiscordRequest {

    @NotBlank(message = "Channel ID is required")
    private String channelId;

    @NotBlank(message = "Discord channel ID is required")
    @Size(max = 100, message = "Discord channel ID must not exceed 100 characters")
    private String discordChannelId; // Discord channel ID

    private String message;

    private DiscordEmbed embed;

    private java.util.Map<String, Object> variables;

    // Getters and Setters
    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getDiscordChannelId() {
        return discordChannelId;
    }

    public void setDiscordChannelId(String discordChannelId) {
        this.discordChannelId = discordChannelId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DiscordEmbed getEmbed() {
        return embed;
    }

    public void setEmbed(DiscordEmbed embed) {
        this.embed = embed;
    }

    public java.util.Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(java.util.Map<String, Object> variables) {
        this.variables = variables;
    }
}

