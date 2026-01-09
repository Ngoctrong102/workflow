package com.notificationplatform.service.channel;

import com.notificationplatform.entity.Channel;

public interface DiscordChannel {

    /**
     * Send message to Discord channel
     * @param channel Channel configuration
     * @param channelId Discord channel ID
     * @param message Message content
     * @param embed Optional embed object
     * @return DeliveryResult
     */
    com.notificationplatform.dto.response.DeliveryResult send(Channel channel, String channelId, String message, com.notificationplatform.dto.response.DiscordEmbed embed);

    /**
     * Test connection to Discord API
     * @param channel Channel configuration
     * @return true if connection successful
     */
    boolean testConnection(Channel channel);
}

