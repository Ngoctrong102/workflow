package com.notificationplatform.service.inapp;

import com.notificationplatform.dto.response.InAppNotificationResponse;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.entity.InAppNotification;
import com.notificationplatform.exception.ResourceNotFoundException;
import com.notificationplatform.repository.InAppNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InAppNotificationServiceImpl implements InAppNotificationService {

    private final InAppNotificationRepository notificationRepository;

    public InAppNotificationServiceImpl(InAppNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public InAppNotificationResponse getNotificationById(String id, String userId) {
        InAppNotification notification = notificationRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        // Verify ownership
        if (!notification.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found with id: " + id);
        }

        return toResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<InAppNotificationResponse> getUserNotifications(
            String userId, Boolean read, String type, int limit, int offset) {
        // Validate pagination
        if (limit < 1) limit = 20;
        if (limit > 100) limit = 100;
        if (offset < 0) offset = 0;

        List<InAppNotification> notifications;
        LocalDateTime now = LocalDateTime.now();

        // Filter by read status
        if (read != null) {
            notifications = notificationRepository.findByUserIdAndReadAndNotDeleted(userId, read);
        } else {
            notifications = notificationRepository.findActiveByUserId(userId, now);
        }

        // Filter by type if provided
        if (type != null && !type.isEmpty()) {
            notifications = notifications.stream()
                    .filter(n -> type.equals(n.getType()))
                    .collect(Collectors.toList());
        }

        // Filter out expired notifications
        notifications = notifications.stream()
                .filter(n -> n.getExpiresAt() == null || n.getExpiresAt().isAfter(now))
                .collect(Collectors.toList());

        long total = notifications.size();

        // Apply pagination
        int fromIndex = Math.min(offset, notifications.size());
        int toIndex = Math.min(offset + limit, notifications.size());
        List<InAppNotification> pagedNotifications = notifications.subList(fromIndex, toIndex);

        List<InAppNotificationResponse> responses = pagedNotifications.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(responses, total, limit, offset);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(String userId) {
        LocalDateTime now = LocalDateTime.now();
        return notificationRepository.countUnreadByUserId(userId, now);
    }

    @Override
    public InAppNotificationResponse markAsRead(String id, String userId) {
        InAppNotification notification = notificationRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        // Verify ownership
        if (!notification.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found with id: " + id);
        }

        if (!notification.getRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }

        return toResponse(notification);
    }

    @Override
    public InAppNotificationResponse markAsUnread(String id, String userId) {
        InAppNotification notification = notificationRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        // Verify ownership
        if (!notification.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found with id: " + id);
        }

        if (notification.getRead()) {
            notification.setRead(false);
            notification.setReadAt(null);
            notificationRepository.save(notification);
        }

        return toResponse(notification);
    }

    @Override
    public void markAllAsRead(String userId) {
        LocalDateTime now = LocalDateTime.now();
        List<InAppNotification> notifications = notificationRepository.findActiveByUserId(userId, now);
        
        notifications.stream()
                .filter(n -> !n.getRead())
                .forEach(n -> {
                    n.setRead(true);
                    n.setReadAt(LocalDateTime.now());
                });
        
        notificationRepository.saveAll(notifications);
    }

    @Override
    public void deleteNotification(String id, String userId) {
        InAppNotification notification = notificationRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        // Verify ownership
        if (!notification.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found with id: " + id);
        }

        // Soft delete
        notification.setDeletedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Override
    public void deleteAllNotifications(String userId) {
        LocalDateTime now = LocalDateTime.now();
        List<InAppNotification> notifications = notificationRepository.findActiveByUserId(userId, now);
        
        notifications.forEach(n -> n.setDeletedAt(LocalDateTime.now()));
        notificationRepository.saveAll(notifications);
    }

    private InAppNotificationResponse toResponse(InAppNotification notification) {
        InAppNotificationResponse response = new InAppNotificationResponse();
        response.setId(notification.getId());
        response.setUserId(notification.getUserId());
        response.setChannelId(notification.getChannelId());
        response.setWorkflowId(notification.getWorkflowId());
        response.setExecutionId(notification.getExecutionId());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setType(notification.getType());
        response.setActionUrl(notification.getActionUrl());
        response.setActionLabel(notification.getActionLabel());
        response.setImageUrl(notification.getImageUrl());
        response.setRead(notification.getRead());
        response.setReadAt(notification.getReadAt());
        response.setExpiresAt(notification.getExpiresAt());
        response.setMetadata(notification.getMetadata() != null ? 
                (java.util.Map<String, Object>) notification.getMetadata() : null);
        response.setCreatedAt(notification.getCreatedAt());
        response.setUpdatedAt(notification.getUpdatedAt());
        return response;
    }
}

