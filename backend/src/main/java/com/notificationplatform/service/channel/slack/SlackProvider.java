package com.notificationplatform.service.channel.slack;

import com.notificationplatform.dto.response.DeliveryResult;
import com.notificationplatform.dto.response.SlackAttachment;
import com.notificationplatform.entity.Channel;
import com.notificationplatform.service.channel.SlackChannel;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
/**
 * Slack channel provider implementation
 * Uses Slack Web API for sending messages
 */
@Slf4j
@Component
public class SlackProvider implements SlackChannel {

    private static final String SLACK_API_URL = "https://slack.com/api/chat.postMessage";

    private final RestTemplate restTemplate;

    public SlackProvider(@Autowired(required = false) RestTemplate restTemplate) {
        this.restTemplate = restTemplate != null ? restTemplate : new RestTemplate();
    }

    @Override
    public DeliveryResult send(Channel channel, String channelName, String message, List<SlackAttachment> attachments) {
        log.info("Sending Slack message to channel: {}", channelName);

        try {
            Map<String, Object> config = channel.getConfig() != null ? (Map<String, Object>) channel.getConfig() : new HashMap<>();
            String botToken = (String) config.get("botToken");

            if (botToken == null || botToken.isEmpty()) {
                throw new IllegalArgumentException("Slack bot token is required");
            }

            // Build request payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("channel", channelName);
            payload.put("text", message);

            if (attachments != null && !attachments.isEmpty()) {
                payload.put("attachments", attachments);
            }

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(botToken);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            // Send request
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    SLACK_API_URL,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            // Check response
            Map<String, Object> body = response.getBody();
            if (response.getStatusCode().is2xxSuccessful() && body != null) {
                Boolean ok = (Boolean) body.get("ok");
                if (Boolean.TRUE.equals(ok)) {
                    String ts = (String) body.get("ts");
                    log.info("Slack message sent successfully: ts={}", ts);
                    return DeliveryResult.success("slack-" + ts);
                } else {
                    String error = (String) body.get("error");
                    log.error("Slack API error: {}", error);
                    return DeliveryResult.failure("Slack API error: " + error);
                }
            } else {
                log.error("Slack API request failed: {}", response.getStatusCode());
                return DeliveryResult.failure("Slack API request failed");
            }

        } catch (Exception e) {
            log.error("Error sending Slack message", e);
            return DeliveryResult.failure("Error sending Slack message: " + e.getMessage());
        }
    }

    @Override
    public boolean testConnection(Channel channel) {
        try {
            Map<String, Object> config = channel.getConfig() != null ? (Map<String, Object>) channel.getConfig() : new HashMap<>();
            String botToken = (String) config.get("botToken");

            if (botToken == null || botToken.isEmpty()) {
                return false;
            }

            // Test with auth.test API
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(botToken);

            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    "https://slack.com/api/auth.test",
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> body = response.getBody();
            if (response.getStatusCode().is2xxSuccessful() && body != null) {
                Boolean ok = (Boolean) body.get("ok");
                return Boolean.TRUE.equals(ok);
            }

            return false;
        } catch (Exception e) {
            log.error("Error testing Slack connection", e);
            return false;
        }
    }
}

