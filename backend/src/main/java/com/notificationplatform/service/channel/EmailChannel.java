package com.notificationplatform.service.channel;

import com.notificationplatform.dto.response.DeliveryResult;
import com.notificationplatform.entity.Channel;

import java.util.List;

public interface EmailChannel {

    /**
     * Send email via SMTP
     *
     * @param channel Channel configuration
     * @param to Recipient email addresses
     * @param cc CC recipients (optional)
     * @param bcc BCC recipients (optional)
     * @param subject Email subject
     * @param body Email body (HTML or plain text)
     * @param contentType Content type (text/html or text/plain)
     * @return Delivery result
     */
    DeliveryResult send(Channel channel, List<String> to, List<String> cc, List<String> bcc,
                       String subject, String body, String contentType);

    /**
     * Test SMTP connection
     *
     * @param channel Channel configuration
     * @return true if connection successful, false otherwise
     */
    boolean testConnection(Channel channel);
}

