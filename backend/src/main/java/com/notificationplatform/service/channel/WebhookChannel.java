package com.notificationplatform.service.channel;

import com.notificationplatform.entity.Channel;

import java.util.Map;

public interface WebhookChannel {

    /**
     * Send HTTP request to webhook URL
     * @param channel Channel configuration
     * @param url Webhook URL
     * @param method HTTP method (GET, POST, PUT, PATCH)
     * @param headers HTTP headers
     * @param body Request body
     * @return DeliveryResult
     */
    com.notificationplatform.dto.response.DeliveryResult send(Channel channel, String url, String method, Map<String, String> headers, Object body);

    /**
     * Test connection to webhook
     * @param channel Channel configuration
     * @return true if connection successful
     */
    boolean testConnection(Channel channel);
}

