package com.notificationplatform.dto.request;

import jakarta.validation.constraints.Size;

public class UpdateTemplateRequest {

    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Size(max = 50, message = "Channel must not exceed 50 characters")
    private String channel;

    @Size(max = 500, message = "Subject must not exceed 500 characters")
    private String subject;

    private String body;

    private java.util.List<java.util.Map<String, Object>> variables;

    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    private java.util.List<String> tags;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
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

    public java.util.List<java.util.Map<String, Object>> getVariables() {
        return variables;
    }

    public void setVariables(java.util.List<java.util.Map<String, Object>> variables) {
        this.variables = variables;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public java.util.List<String> getTags() {
        return tags;
    }

    public void setTags(java.util.List<String> tags) {
        this.tags = tags;
    }
}

