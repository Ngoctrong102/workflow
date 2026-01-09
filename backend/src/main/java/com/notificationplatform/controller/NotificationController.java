package com.notificationplatform.controller;

import com.notificationplatform.dto.request.SendNotificationRequest;
import com.notificationplatform.dto.response.NotificationResponse;
import com.notificationplatform.dto.response.NotificationStatusResponse;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.service.notification.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> sendNotification(@Valid @RequestBody SendNotificationRequest request) {
        NotificationResponse response = notificationService.sendNotification(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotification(@PathVariable String id) {
        NotificationResponse response = notificationService.getNotification(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<NotificationStatusResponse> getNotificationStatus(@PathVariable String id) {
        NotificationStatusResponse response = notificationService.getNotificationStatus(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<NotificationResponse>> listNotifications(
            @RequestParam(required = false) String workflowId,
            @RequestParam(required = false) String executionId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        PagedResponse<NotificationResponse> responses = notificationService.listNotificationsPaged(workflowId, executionId, status, search, limit, offset);
        return ResponseEntity.ok(responses);
    }
}

