package com.notificationplatform.service.channel;

import com.notificationplatform.dto.response.DeliveryResult;
import com.notificationplatform.entity.Channel;

import java.util.List;
import java.util.Map;

public interface PushChannel {

    /**
     * Send push notification via provider
     *
     * @param channel Channel configuration
     * @param deviceTokens Device tokens for push notifications
     * @param title Notification title
     * @param body Notification body
     * @param icon Optional icon URL
     * @param image Optional image URL
     * @param sound Optional sound
     * @param badge Optional badge count
     * @param data Custom payload data
     * @return Delivery result
     */
    DeliveryResult send(Channel channel, List<String> deviceTokens, String title, String body,
                       String icon, String image, String sound, Integer badge, Map<String, Object> data);

    /**
     * Test push provider connection
     *
     * @param channel Channel configuration
     * @return true if connection successful, false otherwise
     */
    boolean testConnection(Channel channel);
}

