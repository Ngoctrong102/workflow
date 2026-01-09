package com.notificationplatform.service.channel.webhook;

import com.notificationplatform.dto.response.DeliveryResult;
import com.notificationplatform.entity.Channel;
import com.notificationplatform.service.channel.WebhookChannel;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
/**
 * Webhook channel provider implementation
 * Supports HTTP methods: GET, POST, PUT, PATCH
 * Supports authentication: Basic, Bearer, API Key
 */
@Slf4j
@Component
public class WebhookProvider implements WebhookChannel {

    private static final int MAX_RETRIES = 3;

    private final RestTemplate restTemplate;

    public WebhookProvider(@Autowired(required = false) RestTemplate restTemplate) {
        this.restTemplate = restTemplate != null ? restTemplate : new RestTemplate();
    }

    @Override
    public DeliveryResult send(Channel channel, String url, String method, Map<String, String> headers, Object body) {
        log.info("Sending webhook request: method={}, url={}", method, url);

        try {
            Map<String, Object> config = channel.getConfig() != null ? (Map<String, Object>) channel.getConfig() : new HashMap<>();
            
            // Build request headers
            HttpHeaders httpHeaders = new HttpHeaders();
            if (headers != null) {
                headers.forEach(httpHeaders::set);
            }

            // Add authentication if configured
            String authType = (String) config.get("authType");
            if (authType != null) {
                switch (authType.toLowerCase()) {
                    case "basic":
                        String username = (String) config.get("username");
                        String password = (String) config.get("password");
                        if (username != null && password != null) {
                            String credentials = username + ":" + password;
                            String encoded = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
                            httpHeaders.set("Authorization", "Basic " + encoded);
                        }
                        break;
                    case "bearer":
                        String bearerToken = (String) config.get("bearerToken");
                        if (bearerToken != null) {
                            httpHeaders.setBearerAuth(bearerToken);
                        }
                        break;
                    case "apikey":
                        String apiKey = (String) config.get("apiKey");
                        String apiKeyHeader = (String) config.getOrDefault("apiKeyHeader", "X-API-Key");
                        if (apiKey != null) {
                            httpHeaders.set(apiKeyHeader, apiKey);
                        }
                        break;
                }
            }

            // Set content type if not specified
            if (!httpHeaders.containsKey(HttpHeaders.CONTENT_TYPE) && body != null) {
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            }

            // Create request entity
            HttpEntity<Object> request = new HttpEntity<>(body, httpHeaders);

            // Determine HTTP method
            HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());

            // Send request with retry logic
            ResponseEntity<String> response = null;
            Exception lastException = null;

            for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
                try {
                    response = restTemplate.exchange(
                            url,
                            httpMethod,
                            request,
                            String.class
                    );
                    break; // Success, exit retry loop
                } catch (Exception e) {
                    lastException = e;
                    if (attempt < MAX_RETRIES) {
                        log.warn("Webhook request failed, retrying (attempt {}/{}): {}", attempt, MAX_RETRIES, e.getMessage());
                        try {
                            Thread.sleep(1000 * attempt); // Exponential backoff
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }

            // Check response
            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                log.info("Webhook request sent successfully");
                return DeliveryResult.success("webhook-" + System.currentTimeMillis());
            } else {
                String errorMsg = lastException != null ? lastException.getMessage() : "Webhook request failed";
                log.error("Webhook request failed: {}", errorMsg);
                return DeliveryResult.failure("Webhook request failed: " + errorMsg);
            }

        } catch (Exception e) {
            log.error("Error sending webhook request", e);
            return DeliveryResult.failure("Error sending webhook request: " + e.getMessage());
        }
    }

    @Override
    public boolean testConnection(Channel channel) {
        try {
            Map<String, Object> config = channel.getConfig() != null ? (Map<String, Object>) channel.getConfig() : new HashMap<>();
            String url = (String) config.get("url");

            if (url == null || url.isEmpty()) {
                return false;
            }

            // Send a test GET request
            HttpHeaders headers = new HttpHeaders();
            
            // Add authentication if configured
            String authType = (String) config.get("authType");
            if (authType != null) {
                switch (authType.toLowerCase()) {
                    case "basic":
                        String username = (String) config.get("username");
                        String password = (String) config.get("password");
                        if (username != null && password != null) {
                            String credentials = username + ":" + password;
                            String encoded = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
                            headers.set("Authorization", "Basic " + encoded);
                        }
                        break;
                    case "bearer":
                        String bearerToken = (String) config.get("bearerToken");
                        if (bearerToken != null) {
                            headers.setBearerAuth(bearerToken);
                        }
                        break;
                    case "apikey":
                        String apiKey = (String) config.get("apiKey");
                        String apiKeyHeader = (String) config.getOrDefault("apiKeyHeader", "X-API-Key");
                        if (apiKey != null) {
                            headers.set(apiKeyHeader, apiKey);
                        }
                        break;
                }
            }

            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Error testing webhook connection", e);
            return false;
        }
    }
}

