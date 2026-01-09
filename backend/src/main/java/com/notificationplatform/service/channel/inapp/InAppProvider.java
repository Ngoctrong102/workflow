package com.notificationplatform.service.channel.inapp;

import com.notificationplatform.dto.response.DeliveryResult;
import com.notificationplatform.entity.Channel;
import com.notificationplatform.entity.InAppNotification;
import com.notificationplatform.repository.InAppNotificationRepository;
import com.notificationplatform.service.channel.InAppChannel;


import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
/**
 * In-app notification provider implementation
 * Stores notifications in database for retrieval by users
 */
@Slf4j
@Component
public class InAppProvider implements InAppChannel {

    private final InAppNotificationRepository notificationRepository;

    public InAppProvider(InAppNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public DeliveryResult send(Channel channel, String userId, String title, String message,
                               String type, String actionUrl, String actionLabel, String imageUrl,
                               LocalDateTime expiresAt, Map<String, Object> metadata) {
        log.info("Sending in-app notification to user: {}", userId);

        try {
            // Create notification entity
            InAppNotification notification = new InAppNotification();
            notification.setId(UUID.randomUUID().toString());
            notification.setUserId(userId);
            notification.setChannelId(channel.getId());
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setType(type != null ? type : "info");
            notification.setActionUrl(actionUrl);
            notification.setActionLabel(actionLabel);
            notification.setImageUrl(imageUrl);
            notification.setExpiresAt(expiresAt);
            notification.setRead(false);
            notification.setMetadata(metadata);

            // Save notification
            InAppNotification saved = notificationRepository.save(notification);

            log.info("In-app notification saved successfully: id={}", saved.getId());
            return DeliveryResult.success("inapp-" + saved.getId());

        } catch (Exception e) {
            log.error("Error sending in-app notification", e);
            return DeliveryResult.failure("Error sending in-app notification: " + e.getMessage());
        }
    }

    @Override
    public boolean testConnection(Channel channel) {
        // In-app notifications don't require external connection
        // Just verify channel configuration is valid
        return channel != null && channel.getConfig() != null;
    }
}

