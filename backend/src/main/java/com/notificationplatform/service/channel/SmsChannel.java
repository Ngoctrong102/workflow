package com.notificationplatform.service.channel;

import com.notificationplatform.dto.response.DeliveryResult;
import com.notificationplatform.entity.Channel;

import java.util.List;

public interface SmsChannel {

    /**
     * Send SMS via provider
     *
     * @param channel Channel configuration
     * @param to Recipient phone numbers
     * @param message SMS message content
     * @return Delivery result
     */
    DeliveryResult send(Channel channel, List<String> to, String message);

    /**
     * Test SMS provider connection
     *
     * @param channel Channel configuration
     * @return true if connection successful, false otherwise
     */
    boolean testConnection(Channel channel);
}

