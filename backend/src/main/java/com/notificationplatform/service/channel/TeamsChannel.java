package com.notificationplatform.service.channel;

import com.notificationplatform.entity.Channel;

public interface TeamsChannel {

    /**
     * Send message to Microsoft Teams channel
     * @param channel Channel configuration
     * @param webhookUrl Teams webhook URL
     * @param title Message title
     * @param text Message text
     * @param themeColor Optional theme color
     * @return DeliveryResult
     */
    com.notificationplatform.dto.response.DeliveryResult send(Channel channel, String webhookUrl, String title, String text, String themeColor);

    /**
     * Test connection to Teams webhook
     * @param channel Channel configuration
     * @return true if connection successful
     */
    boolean testConnection(Channel channel);
}

