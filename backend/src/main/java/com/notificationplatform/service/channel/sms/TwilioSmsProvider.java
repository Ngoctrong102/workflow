package com.notificationplatform.service.channel.sms;

import com.notificationplatform.dto.response.DeliveryResult;
import com.notificationplatform.entity.Channel;
import com.notificationplatform.service.channel.SmsChannel;


import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
/**
 * Mock Twilio SMS Provider for MVP
 * In production, this would integrate with Twilio API
 */
@Slf4j
@Component
public class TwilioSmsProvider implements SmsChannel {

    private static final int MAX_SMS_LENGTH = 1600; // Standard SMS limit

    @Override
    public DeliveryResult send(Channel channel, List<String> to, String message) {
        try {
            // Validate message length
            if (message.length() > MAX_SMS_LENGTH) {
                log.warn("SMS message exceeds maximum length: {}", message.length());
                DeliveryResult result = new DeliveryResult(false, null, "failed");
                result.setError("Message exceeds maximum length of " + MAX_SMS_LENGTH + " characters");
                return result;
            }

            // Get configuration
            Map<String, Object> config = channel.getConfig() != null ? (Map<String, Object>) channel.getConfig() : new java.util.HashMap<>();
            String accountSid = (String) config.get("accountSid");
            String authToken = (String) config.get("authToken");
            String fromNumber = (String) config.get("fromNumber");

            // Validate configuration
            if (accountSid == null || authToken == null || fromNumber == null) {
                DeliveryResult result = new DeliveryResult(false, null, "failed");
                result.setError("Missing required SMS configuration (accountSid, authToken, fromNumber)");
                return result;
            }

            // Mock SMS sending - In production, this would call Twilio API
            // Example: Twilio.init(accountSid, authToken);
            // Message.creator(new PhoneNumber(to), new PhoneNumber(fromNumber), message).create();
            
            log.info("SMS sent via Twilio (mock) to {} recipients from {}", to.size(), fromNumber);
            
            String messageId = UUID.randomUUID().toString();
            return new DeliveryResult(true, messageId, "sent");

        } catch (Exception e) {
            log.error("Failed to send SMS", e);
            DeliveryResult result = new DeliveryResult(false, null, "failed");
            result.setError(e.getMessage());
            return result;
        }
    }

    @Override
    public boolean testConnection(Channel channel) {
        try {
            Map<String, Object> config = channel.getConfig() != null ? (Map<String, Object>) channel.getConfig() : new java.util.HashMap<>();
            String accountSid = (String) config.get("accountSid");
            String authToken = (String) config.get("authToken");

            if (accountSid == null || authToken == null) {
                log.error("Missing Twilio credentials for connection test");
                return false;
            }

            // Mock connection test - In production, this would validate Twilio credentials
            // Example: Twilio.init(accountSid, authToken);
            // Account account = Account.fetcher(accountSid).fetch();
            
            log.info("Twilio SMS connection test successful (mock) for channel: {}", channel.getId());
            return true;

        } catch (Exception e) {
            log.error("Twilio SMS connection test failed for channel: {}", channel.getId(), e);
            return false;
        }
    }
}

