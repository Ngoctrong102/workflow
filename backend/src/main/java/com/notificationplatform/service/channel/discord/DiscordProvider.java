package com.notificationplatform.service.channel.discord;

import com.notificationplatform.dto.response.DeliveryResult;
import com.notificationplatform.dto.response.DiscordEmbed;
import com.notificationplatform.entity.Channel;
import com.notificationplatform.service.channel.DiscordChannel;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
/**
 * Discord channel provider implementation
 * Uses Discord Webhook API for sending messages
 */
@Slf4j
@Component
public class DiscordProvider implements DiscordChannel {

    private static final String DISCORD_API_URL = "https://discord.com/api/v10/channels/{channelId}/messages";

    private final RestTemplate restTemplate;

    public DiscordProvider(@Autowired(required = false) RestTemplate restTemplate) {
        this.restTemplate = restTemplate != null ? restTemplate : new RestTemplate();
    }

    @Override
    public DeliveryResult send(Channel channel, String channelId, String message, DiscordEmbed embed) {
        log.info("Sending Discord message to channel: {}", channelId);

        try {
            Map<String, Object> config = channel.getConfig() != null ? (Map<String, Object>) channel.getConfig() : new HashMap<>();
            String botToken = (String) config.get("botToken");

            if (botToken == null || botToken.isEmpty()) {
                throw new IllegalArgumentException("Discord bot token is required");
            }

            // Build request payload
            Map<String, Object> payload = new HashMap<>();
            if (message != null && !message.isEmpty()) {
                payload.put("content", message);
            }

            if (embed != null) {
                List<Map<String, Object>> embeds = new ArrayList<>();
                Map<String, Object> embedMap = new HashMap<>();
                if (embed.getTitle() != null) embedMap.put("title", embed.getTitle());
                if (embed.getDescription() != null) embedMap.put("description", embed.getDescription());
                if (embed.getUrl() != null) embedMap.put("url", embed.getUrl());
                if (embed.getColor() != null) embedMap.put("color", embed.getColor());
                if (embed.getTimestamp() != null) embedMap.put("timestamp", embed.getTimestamp());
                embeds.add(embedMap);
                payload.put("embeds", embeds);
            }

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bot " + botToken);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            // Send request
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    DISCORD_API_URL,
                    HttpMethod.POST,
                    request,
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {},
                    channelId
            );

            // Check response
            Map<String, Object> body = response.getBody();
            if (response.getStatusCode().is2xxSuccessful() && body != null) {
                String messageId = (String) body.get("id");
                log.info("Discord message sent successfully: messageId={}", messageId);
                return DeliveryResult.success("discord-" + messageId);
            } else {
                log.error("Discord API request failed: {}", response.getStatusCode());
                return DeliveryResult.failure("Discord API request failed");
            }

        } catch (Exception e) {
            log.error("Error sending Discord message", e);
            return DeliveryResult.failure("Error sending Discord message: " + e.getMessage());
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

            // Test with gateway API
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bot " + botToken);

            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    "https://discord.com/api/v10/gateway/bot",
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            return response.getStatusCode().is2xxSuccessful() && response.getBody() != null;
        } catch (Exception e) {
            log.error("Error testing Discord connection", e);
            return false;
        }
    }
}

