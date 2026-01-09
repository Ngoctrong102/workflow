package com.notificationplatform.service.notification;

import com.notificationplatform.dto.mapper.NotificationMapper;
import com.notificationplatform.dto.request.SendNotificationRequest;
import com.notificationplatform.dto.request.RenderTemplateRequest;
import com.notificationplatform.dto.response.DeliveryResult;
import com.notificationplatform.dto.response.DeliveryStatusResponse;
import com.notificationplatform.dto.response.NotificationResponse;
import com.notificationplatform.dto.response.NotificationStatusResponse;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.dto.response.RenderTemplateResponse;
import com.notificationplatform.entity.Channel;
import com.notificationplatform.entity.Delivery;
import com.notificationplatform.entity.Notification;
import com.notificationplatform.exception.ResourceNotFoundException;
import com.notificationplatform.repository.ChannelRepository;
import com.notificationplatform.repository.DeliveryRepository;
import com.notificationplatform.repository.NotificationRepository;
import com.notificationplatform.service.channel.ChannelService;
import com.notificationplatform.service.template.TemplateService;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private static final int MAX_RETRIES = 3;

    private final NotificationRepository notificationRepository;
    private final DeliveryRepository deliveryRepository;
    private final ChannelRepository channelRepository;
    private final NotificationMapper notificationMapper;
    private final TemplateService templateService;
    private final ChannelService channelService;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                  DeliveryRepository deliveryRepository,
                                  ChannelRepository channelRepository,
                                  NotificationMapper notificationMapper,
                                  TemplateService templateService,
                                  ChannelService channelService) {
        this.notificationRepository = notificationRepository;
        this.deliveryRepository = deliveryRepository;
        this.channelRepository = channelRepository;
        this.notificationMapper = notificationMapper;
        this.templateService = templateService;
        this.channelService = channelService;
    }

    @Override
    public NotificationResponse sendNotification(SendNotificationRequest request) {
        // Select channel
        Channel channel = selectChannel(request.getChannel());
        if (channel == null) {
            throw new IllegalArgumentException("No active channel found for type: " + request.getChannel());
        }

        // Create notification entity
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID().toString());
        notification.setChannel(request.getChannel());
        notification.setStatus("queued");
        notification.setRecipientsCount(request.getRecipients().size());
        notification.setCreatedAt(LocalDateTime.now());

        // Render template if provided
        String subject = request.getSubject();
        String body = request.getBody();
        String title = request.getTitle();

        if (request.getTemplateId() != null && !request.getTemplateId().isEmpty()) {
            // Render template
            Map<String, Object> variables = request.getData() != null ? request.getData() : new HashMap<>();
            RenderTemplateRequest renderRequest = new RenderTemplateRequest();
            renderRequest.setTemplateId(request.getTemplateId());
            renderRequest.setVariables(variables);
            RenderTemplateResponse rendered = templateService.renderTemplate(renderRequest);
            if (rendered.getRenderedSubject() != null) {
                subject = rendered.getRenderedSubject();
            }
            body = rendered.getRenderedBody();
        } else if (request.getData() != null && !request.getData().isEmpty()) {
            // Render direct content with variables
            if (subject != null) {
                subject = renderContent(subject, request.getData());
            }
            if (body != null) {
                body = renderContent(body, request.getData());
            }
            if (title != null) {
                title = renderContent(title, request.getData());
            }
        }

        // Save notification
        notification = notificationRepository.save(notification);

        // Send notification to each recipient
        List<Delivery> deliveries = new ArrayList<>();
        for (SendNotificationRequest.Recipient recipient : request.getRecipients()) {
            Delivery delivery = sendToRecipient(notification, channel, recipient, subject, body, title);
            deliveries.add(delivery);
        }

        // Update notification status
        long successCount = deliveries.stream().filter(d -> "delivered".equals(d.getStatus()) || "sent".equals(d.getStatus())).count();
        if (successCount == deliveries.size()) {
            notification.setStatus("sent");
        } else if (successCount > 0) {
            notification.setStatus("sending");
        } else {
            notification.setStatus("failed");
        }
        notification.setSentAt(LocalDateTime.now());
        notification = notificationRepository.save(notification);

        return notificationMapper.toResponse(notification);
    }

    private Delivery sendToRecipient(Notification notification, Channel channel,
                                     SendNotificationRequest.Recipient recipient,
                                     String subject, String body, String title) {
        Delivery delivery = new Delivery();
        delivery.setId(UUID.randomUUID().toString());
        delivery.setNotification(notification);
        delivery.setChannel(notification.getChannel());
        delivery.setStatus("pending");
        delivery.setCreatedAt(LocalDateTime.now());

        // Set recipient based on channel type
        String recipientValue = getRecipientValue(recipient, notification.getChannel());
        delivery.setRecipient(recipientValue);

        delivery = deliveryRepository.save(delivery);

        try {
            // Update status to sending
            delivery.setStatus("sending");
            delivery.setSentAt(LocalDateTime.now());
            delivery = deliveryRepository.save(delivery);

            // Send via channel service
            DeliveryResult result = sendViaChannel(channel, recipient, subject, body, title);

            // Update delivery status
            if (result.isSuccess()) {
                delivery.setStatus("delivered");
                delivery.setDeliveredAt(LocalDateTime.now());
                delivery.setProviderMessageId(result.getMessageId());
            } else {
                delivery.setStatus("failed");
                delivery.setError(result.getError());
            }
            delivery = deliveryRepository.save(delivery);

        } catch (Exception e) {
            log.error("Failed to send notification to recipient: {}", recipientValue, e);
            delivery.setStatus("failed");
            delivery.setError(e.getMessage());
            delivery = deliveryRepository.save(delivery);
        }

        return delivery;
    }

    private DeliveryResult sendViaChannel(Channel channel, SendNotificationRequest.Recipient recipient,
                                          String subject, String body, String title) {
        String channelType = channel.getType();

        if ("email".equals(channelType)) {
            com.notificationplatform.dto.request.SendEmailRequest emailRequest = new com.notificationplatform.dto.request.SendEmailRequest();
            emailRequest.setChannelId(channel.getId());
            emailRequest.setTo(recipient.getEmail());
            emailRequest.setSubject(subject);
            emailRequest.setBody(body);
            emailRequest.setContentType("text/html");
            return channelService.sendEmail(emailRequest);

        } else if ("sms".equals(channelType)) {
            com.notificationplatform.dto.request.SendSmsRequest smsRequest = new com.notificationplatform.dto.request.SendSmsRequest();
            smsRequest.setChannelId(channel.getId());
            smsRequest.setTo(recipient.getPhone());
            smsRequest.setMessage(body);
            return channelService.sendSms(smsRequest);

        } else if ("push".equals(channelType)) {
            com.notificationplatform.dto.request.SendPushRequest pushRequest = new com.notificationplatform.dto.request.SendPushRequest();
            pushRequest.setChannelId(channel.getId());
            pushRequest.setDeviceTokens(Arrays.asList(recipient.getDeviceToken()));
            pushRequest.setTitle(title != null ? title : "Notification");
            pushRequest.setBody(body);
            return channelService.sendPush(pushRequest);

        } else {
            throw new IllegalArgumentException("Unsupported channel type: " + channelType);
        }
    }

    private String getRecipientValue(SendNotificationRequest.Recipient recipient, String channel) {
        if ("email".equals(channel)) {
            return recipient.getEmail();
        } else if ("sms".equals(channel)) {
            return recipient.getPhone();
        } else if ("push".equals(channel)) {
            return recipient.getDeviceToken();
        }
        return recipient.getEmail() != null ? recipient.getEmail() : recipient.getPhone();
    }

    private Channel selectChannel(String channelType) {
        List<Channel> channels = channelRepository.findActiveByType(channelType);
        if (channels.isEmpty()) {
            return null;
        }
        // For MVP, return first active channel
        // In production, could implement load balancing, priority, etc.
        return channels.get(0);
    }

    private String renderContent(String content, Map<String, Object> variables) {
        // Simple variable substitution
        String result = content;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String key = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(key, value);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotification(String id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        return notificationMapper.toResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationStatusResponse getNotificationStatus(String id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        List<Delivery> deliveries = deliveryRepository.findByNotificationId(id);

        NotificationStatusResponse response = new NotificationStatusResponse();
        response.setId(notification.getId());
        response.setStatus(notification.getStatus());
        response.setTotalRecipients(deliveries.size());
        response.setDeliveredCount((int) deliveries.stream().filter(d -> "delivered".equals(d.getStatus())).count());
        response.setFailedCount((int) deliveries.stream().filter(d -> "failed".equals(d.getStatus())).count());
        response.setPendingCount((int) deliveries.stream().filter(d -> "pending".equals(d.getStatus()) || "sending".equals(d.getStatus())).count());

        List<DeliveryStatusResponse> deliveryResponses = deliveries.stream()
                .map(notificationMapper::toDeliveryStatusResponse)
                .collect(Collectors.toList());
        response.setDeliveries(deliveryResponses);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> listNotifications(String workflowId, String executionId, String status,
                                                         int limit, int offset) {
        List<Notification> notifications;

        if (workflowId != null && !workflowId.isEmpty()) {
            notifications = notificationRepository.findByWorkflowId(workflowId);
        } else if (executionId != null && !executionId.isEmpty()) {
            notifications = notificationRepository.findByExecutionId(executionId);
        } else {
            notifications = notificationRepository.findAll();
        }

        // Filter by status if provided
        if (status != null && !status.isEmpty()) {
            notifications = notifications.stream()
                    .filter(n -> status.equals(n.getStatus()))
                    .collect(Collectors.toList());
        }

        // Apply pagination
        int fromIndex = Math.min(offset, notifications.size());
        int toIndex = Math.min(offset + limit, notifications.size());
        List<Notification> pagedNotifications = notifications.subList(fromIndex, toIndex);

        return notificationMapper.toResponseList(pagedNotifications);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> listNotificationsPaged(String workflowId, String executionId, String status, String search, int limit, int offset) {
        List<Notification> notifications;

        if (workflowId != null && !workflowId.isEmpty()) {
            notifications = notificationRepository.findByWorkflowId(workflowId);
        } else if (executionId != null && !executionId.isEmpty()) {
            notifications = notificationRepository.findByExecutionId(executionId);
        } else {
            notifications = notificationRepository.findAll();
        }

        // Filter by status if provided
        if (status != null && !status.isEmpty()) {
            notifications = notifications.stream()
                    .filter(n -> status.equals(n.getStatus()))
                    .collect(Collectors.toList());
        }

        // Filter by search if provided
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            notifications = notifications.stream()
                    .filter(n -> n.getId().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
        }

        long total = notifications.size();

        // Apply pagination
        int fromIndex = Math.min(offset, notifications.size());
        int toIndex = Math.min(offset + limit, notifications.size());
        List<Notification> pagedNotifications = notifications.subList(fromIndex, toIndex);

        List<NotificationResponse> responses = notificationMapper.toResponseList(pagedNotifications);
        return new PagedResponse<>(responses, total, limit, offset);
    }
}

