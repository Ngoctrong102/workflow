package com.notificationplatform.service.channel.email;

import com.notificationplatform.dto.response.DeliveryResult;
import com.notificationplatform.entity.Channel;
import com.notificationplatform.service.channel.EmailChannel;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;


import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
public class SmtpEmailProvider implements EmailChannel {

    @Override
    public DeliveryResult send(Channel channel, List<String> to, List<String> cc, List<String> bcc,
                               String subject, String body, String contentType) {
        try {
            // Get SMTP configuration from channel config
            Map<String, Object> config = channel.getConfig() != null ? (Map<String, Object>) channel.getConfig() : new HashMap<>();
            String host = (String) config.get("host");
            int port = getIntValue(config, "port", 587);
            String username = (String) config.get("username");
            String password = (String) config.get("password");
            String encryption = (String) config.getOrDefault("encryption", "TLS");
            String from = (String) config.get("from");
            String replyTo = (String) config.get("replyTo");

            // Create mail session
            Session session = createMailSession(host, port, username, password, encryption);

            // Create message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            
            if (replyTo != null && !replyTo.isEmpty()) {
                message.setReplyTo(new InternetAddress[]{new InternetAddress(replyTo)});
            }

            // Set recipients
            message.setRecipients(Message.RecipientType.TO, to.stream()
                    .map(email -> {
                        try {
                            return new InternetAddress(email);
                        } catch (Exception e) {
                            log.error("Invalid email address: {}", email, e);
                            return null;
                        }
                    })
                    .filter(address -> address != null)
                    .toArray(InternetAddress[]::new));

            if (cc != null && !cc.isEmpty()) {
                message.setRecipients(Message.RecipientType.CC, cc.stream()
                        .map(email -> {
                            try {
                                return new InternetAddress(email);
                            } catch (Exception e) {
                                log.error("Invalid email address: {}", email, e);
                                return null;
                            }
                        })
                        .filter(address -> address != null)
                        .toArray(InternetAddress[]::new));
            }

            if (bcc != null && !bcc.isEmpty()) {
                message.setRecipients(Message.RecipientType.BCC, bcc.stream()
                        .map(email -> {
                            try {
                                return new InternetAddress(email);
                            } catch (Exception e) {
                                log.error("Invalid email address: {}", email, e);
                                return null;
                            }
                        })
                        .filter(address -> address != null)
                        .toArray(InternetAddress[]::new));
            }

            // Set subject and body
            message.setSubject(subject);
            message.setContent(body, contentType);

            // Send message
            Transport.send(message);

            String messageId = UUID.randomUUID().toString();
            log.info("Email sent successfully. Message ID: {}", messageId);

            return new DeliveryResult(true, messageId, "sent");

        } catch (MessagingException e) {
            log.error("Failed to send email", e);
            DeliveryResult result = new DeliveryResult(false, null, "failed");
            result.setError(e.getMessage());
            return result;
        } catch (Exception e) {
            log.error("Unexpected error sending email", e);
            DeliveryResult result = new DeliveryResult(false, null, "failed");
            result.setError(e.getMessage());
            return result;
        }
    }

    @Override
    public boolean testConnection(Channel channel) {
        try {
            Map<String, Object> config = channel.getConfig() != null ? (Map<String, Object>) channel.getConfig() : new HashMap<>();
            String host = (String) config.get("host");
            int port = getIntValue(config, "port", 587);
            String username = (String) config.get("username");
            String password = (String) config.get("password");
            String encryption = (String) config.getOrDefault("encryption", "TLS");

            Session session = createMailSession(host, port, username, password, encryption);
            Transport transport = session.getTransport("smtp");
            transport.connect();
            transport.close();

            log.info("SMTP connection test successful for channel: {}", channel.getId());
            return true;

        } catch (Exception e) {
            log.error("SMTP connection test failed for channel: {}", channel.getId(), e);
            return false;
        }
    }

    private Session createMailSession(String host, int port, String username, String password, String encryption) {
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");

        if ("TLS".equalsIgnoreCase(encryption)) {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        } else if ("SSL".equalsIgnoreCase(encryption)) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.ssl.trust", host);
        }

        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");

        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };

        return Session.getInstance(props, authenticator);
    }

    private int getIntValue(Map<String, Object> config, String key, int defaultValue) {
        Object value = config.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}

