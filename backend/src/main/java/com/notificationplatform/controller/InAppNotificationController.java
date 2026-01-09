package com.notificationplatform.controller;

import com.notificationplatform.dto.response.InAppNotificationResponse;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.service.inapp.InAppNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/in-app-notifications")
public class InAppNotificationController {

    private final InAppNotificationService notificationService;

    public InAppNotificationController(InAppNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<InAppNotificationResponse> getNotification(
            @PathVariable String id,
            @RequestParam String userId) {
        InAppNotificationResponse response = notificationService.getNotificationById(id, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<InAppNotificationResponse>> getUserNotifications(
            @RequestParam String userId,
            @RequestParam(required = false) Boolean read,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        PagedResponse<InAppNotificationResponse> responses = notificationService.getUserNotifications(
                userId, read, type, limit, offset);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount(@RequestParam String userId) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<InAppNotificationResponse> markAsRead(
            @PathVariable String id,
            @RequestParam String userId) {
        InAppNotificationResponse response = notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/unread")
    public ResponseEntity<InAppNotificationResponse> markAsUnread(
            @PathVariable String id,
            @RequestParam String userId) {
        InAppNotificationResponse response = notificationService.markAsUnread(id, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@RequestParam String userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable String id,
            @RequestParam String userId) {
        notificationService.deleteNotification(id, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllNotifications(@RequestParam String userId) {
        notificationService.deleteAllNotifications(userId);
        return ResponseEntity.noContent().build();
    }
}

