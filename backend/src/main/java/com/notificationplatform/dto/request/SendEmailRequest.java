package com.notificationplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public class SendEmailRequest {

    @NotBlank(message = "Channel ID is required")
    private String channelId;

    @NotBlank(message = "To recipient is required")
    private String to; // Can be comma-separated for multiple recipients

    private String cc; // Carbon copy recipients
    private String bcc; // Blind carbon copy recipients

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Body is required")
    private String body;

    private String contentType; // text/plain or text/html, default: text/html

    private Map<String, Object> variables; // Variables for template rendering

    // Getters and Setters
    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
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

    public String getContentType() {
        return contentType != null ? contentType : "text/html";
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
}

