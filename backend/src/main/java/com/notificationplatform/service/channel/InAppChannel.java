package com.notificationplatform.service.channel;

import com.notificationplatform.entity.Channel;

public interface InAppChannel {

    /**
     * Send in-app notification to user
     * @param channel Channel configuration
     * @param userId User ID to send notification to
     * @param title Notification title
     * @param message Notification message
     * @param type Notification type (info, warning, error, success)
     * @param actionUrl Optional action URL
     * @param actionLabel Optional action label
     * @param imageUrl Optional image URL
     * @param expiresAt Optional expiry timestamp
     * @param metadata Optional metadata
     * @return DeliveryResult
     */
    com.notificationplatform.dto.response.DeliveryResult send(
            Channel channel,
            String userId,
            String title,
            String message,
            String type,
            String actionUrl,
            String actionLabel,
            String imageUrl,
            java.time.LocalDateTime expiresAt,
            java.util.Map<String, Object> metadata
    );

    /**
     * Test connection (always returns true for in-app notifications)
     * @param channel Channel configuration
     * @return true
     */
    boolean testConnection(Channel channel);
}

