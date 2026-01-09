package com.notificationplatform.dto.mapper;

import com.notificationplatform.dto.response.DeliveryStatusResponse;
import com.notificationplatform.dto.response.NotificationResponse;
import com.notificationplatform.entity.Delivery;
import com.notificationplatform.entity.Notification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        if (notification.getExecution() != null) {
            response.setExecutionId(notification.getExecution().getId());
        }
        if (notification.getWorkflow() != null) {
            response.setWorkflowId(notification.getWorkflow().getId());
        }
        response.setChannel(notification.getChannel());
        if (notification.getTemplate() != null) {
            response.setTemplateId(notification.getTemplate().getId());
        }
        response.setStatus(notification.getStatus());
        response.setRecipientsCount(notification.getRecipientsCount());
        response.setCreatedAt(notification.getCreatedAt());
        response.setSentAt(notification.getSentAt());

        // Map deliveries if available
        if (notification.getDeliveries() != null && !notification.getDeliveries().isEmpty()) {
            List<DeliveryStatusResponse> deliveryResponses = new ArrayList<>();
            for (Delivery delivery : notification.getDeliveries()) {
                deliveryResponses.add(toDeliveryStatusResponse(delivery));
            }
            response.setDeliveries(deliveryResponses);
        }

        return response;
    }

    public DeliveryStatusResponse toDeliveryStatusResponse(Delivery delivery) {
        DeliveryStatusResponse response = new DeliveryStatusResponse();
        response.setId(delivery.getId());
        response.setRecipient(delivery.getRecipient());
        response.setChannel(delivery.getChannel());
        response.setStatus(delivery.getStatus());
        response.setSentAt(delivery.getSentAt());
        response.setDeliveredAt(delivery.getDeliveredAt());
        response.setOpenedAt(delivery.getOpenedAt());
        response.setClickedAt(delivery.getClickedAt());
        response.setError(delivery.getError());
        response.setProviderMessageId(delivery.getProviderMessageId());
        return response;
    }

    public List<NotificationResponse> toResponseList(List<Notification> notifications) {
        List<NotificationResponse> responses = new ArrayList<>();
        for (Notification notification : notifications) {
            responses.add(toResponse(notification));
        }
        return responses;
    }
}

