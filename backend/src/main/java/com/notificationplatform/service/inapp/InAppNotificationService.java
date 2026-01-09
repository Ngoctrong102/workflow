package com.notificationplatform.service.inapp;

import com.notificationplatform.dto.response.InAppNotificationResponse;
import com.notificationplatform.dto.response.PagedResponse;


public interface InAppNotificationService {

    /**
     * Get notification by ID
     */
    InAppNotificationResponse getNotificationById(String id, String userId);

    /**
     * Get all notifications for a user
     */
    PagedResponse<InAppNotificationResponse> getUserNotifications(
            String userId, Boolean read, String type, int limit, int offset);

    /**
     * Get unread count for a user
     */
    long getUnreadCount(String userId);

    /**
     * Mark notification as read
     */
    InAppNotificationResponse markAsRead(String id, String userId);

    /**
     * Mark notification as unread
     */
    InAppNotificationResponse markAsUnread(String id, String userId);

    /**
     * Mark all notifications as read for a user
     */
    void markAllAsRead(String userId);

    /**
     * Delete notification
     */
    void deleteNotification(String id, String userId);

    /**
     * Delete all notifications for a user
     */
    void deleteAllNotifications(String userId);
}

