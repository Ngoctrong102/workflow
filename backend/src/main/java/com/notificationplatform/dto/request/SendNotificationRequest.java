package com.notificationplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

public class SendNotificationRequest {

    @NotBlank(message = "Channel is required")
    private String channel; // email, sms, push

    @NotBlank(message = "Recipients are required")
    private List<Recipient> recipients;

    private String templateId; // Optional template ID

    private Map<String, Object> data; // Variables for template rendering

    // For direct sending without template
    private String subject; // For email
    private String body; // Message content
    private String title; // For push notifications

    // Getters and Setters
    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public List<Recipient> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<Recipient> recipients) {
        this.recipients = recipients;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // Inner class for Recipient
    public static class Recipient {
        private String email;
        private String phone;
        private String deviceToken;
        private String name;
        private Map<String, Object> customData;

        // Getters and Setters
        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getDeviceToken() {
            return deviceToken;
        }

        public void setDeviceToken(String deviceToken) {
            this.deviceToken = deviceToken;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<String, Object> getCustomData() {
            return customData;
        }

        public void setCustomData(Map<String, Object> customData) {
            this.customData = customData;
        }
    }
}

