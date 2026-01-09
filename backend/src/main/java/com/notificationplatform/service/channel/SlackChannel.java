package com.notificationplatform.service.channel;

import com.notificationplatform.entity.Channel;

public interface SlackChannel {

    /**
     * Send message to Slack channel
     * @param channel Channel configuration
     * @param channelName Slack channel name (e.g., #general)
     * @param message Message text
     * @param attachments Optional attachments
     * @return DeliveryResult
     */
    com.notificationplatform.dto.response.DeliveryResult send(Channel channel, String channelName, String message, java.util.List<com.notificationplatform.dto.response.SlackAttachment> attachments);

    /**
     * Test connection to Slack API
     * @param channel Channel configuration
     * @return true if connection successful
     */
    boolean testConnection(Channel channel);
}

