package com.notificationplatform.service.notification;

import com.notificationplatform.dto.request.SendNotificationRequest;
import com.notificationplatform.dto.response.NotificationResponse;
import com.notificationplatform.dto.response.NotificationStatusResponse;
import com.notificationplatform.dto.response.PagedResponse;

import java.util.List;

public interface NotificationService {

    NotificationResponse sendNotification(SendNotificationRequest request);

    NotificationResponse getNotification(String id);

    NotificationStatusResponse getNotificationStatus(String id);

    List<NotificationResponse> listNotifications(String workflowId, String executionId, String status, int limit, int offset);

    PagedResponse<NotificationResponse> listNotificationsPaged(String workflowId, String executionId, String status, String search, int limit, int offset);
}

