package com.notificationplatform.service.channel.teams;

import com.notificationplatform.dto.response.DeliveryResult;
import com.notificationplatform.entity.Channel;
import com.notificationplatform.service.channel.TeamsChannel;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
/**
 * Microsoft Teams channel provider implementation
 * Uses Teams Incoming Webhook connector
 */
@Slf4j
@Component
public class TeamsProvider implements TeamsChannel {

    private final RestTemplate restTemplate;

    public TeamsProvider(@Autowired(required = false) RestTemplate restTemplate) {
        this.restTemplate = restTemplate != null ? restTemplate : new RestTemplate();
    }

    @Override
    public DeliveryResult send(Channel channel, String webhookUrl, String title, String text, String themeColor) {
        log.info("Sending Teams message to webhook");

        try {
            // Build request payload (Teams MessageCard format)
            Map<String, Object> payload = new HashMap<>();
            payload.put("@type", "MessageCard");
            payload.put("@context", "https://schema.org/extensions");
            payload.put("summary", title != null ? title : "Notification");

            if (title != null) {
                payload.put("title", title);
            }

            if (text != null) {
                payload.put("text", text);
            }

            if (themeColor != null) {
                payload.put("themeColor", themeColor);
            }

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            // Send request
            ResponseEntity<String> response = restTemplate.exchange(
                    webhookUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            // Teams webhook returns 200 OK on success
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Teams message sent successfully");
                return DeliveryResult.success("teams-" + System.currentTimeMillis());
            } else {
                log.error("Teams webhook request failed: {}", response.getStatusCode());
                return DeliveryResult.failure("Teams webhook request failed");
            }

        } catch (Exception e) {
            log.error("Error sending Teams message", e);
            return DeliveryResult.failure("Error sending Teams message: " + e.getMessage());
        }
    }

    @Override
    public boolean testConnection(Channel channel) {
        try {
            Map<String, Object> config = channel.getConfig() != null ? (Map<String, Object>) channel.getConfig() : new HashMap<>();
            String webhookUrl = (String) config.get("webhookUrl");

            if (webhookUrl == null || webhookUrl.isEmpty()) {
                return false;
            }

            // Send a test message
            Map<String, Object> payload = new HashMap<>();
            payload.put("@type", "MessageCard");
            payload.put("@context", "https://schema.org/extensions");
            payload.put("summary", "Connection Test");
            payload.put("text", "Testing connection");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    webhookUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Error testing Teams connection", e);
            return false;
        }
    }
}

