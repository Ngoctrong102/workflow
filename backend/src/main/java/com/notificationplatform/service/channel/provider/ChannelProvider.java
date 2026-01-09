package com.notificationplatform.service.channel.provider;

import com.notificationplatform.dto.response.DeliveryResult;
import com.notificationplatform.entity.Channel;
import com.notificationplatform.entity.enums.ChannelType;

import java.util.List;
import java.util.Map;

/**
 * Strategy interface for channel providers.
 * Each channel type (Email, SMS, Push, Slack, Discord, etc.) has its own provider implementation.
 * Spring will auto-inject all implementations into a Map for easy lookup.
 */
public interface ChannelProvider {

    /**
     * Get the channel type this provider supports.
     */
    ChannelType getSupportedType();

    /**
     * Test connection to the channel service.
     * @param channel Channel entity with configuration
     * @return true if connection is successful
     */
    boolean testConnection(Channel channel);

    /**
     * Send notification through this channel.
     * @param channel Channel entity with configuration
     * @param recipients List of recipient addresses/IDs
     * @param subject Subject/title of the notification (optional)
     * @param content Content/body of the notification
     * @param variables Template variables for rendering (optional)
     * @param additionalData Additional channel-specific data (optional)
     * @return DeliveryResult with success status and details
     */
    DeliveryResult send(Channel channel, List<String> recipients, String subject, 
                       String content, Map<String, Object> variables, 
                       Map<String, Object> additionalData);
}

