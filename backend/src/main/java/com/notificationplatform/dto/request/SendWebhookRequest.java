package com.notificationplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.Map;

public class SendWebhookRequest {

    @NotBlank(message = "Channel ID is required")
    private String channelId;

    @NotBlank(message = "URL is required")
    private String url;

    @NotBlank(message = "Method is required")
    @Pattern(regexp = "GET|POST|PUT|PATCH", message = "Method must be GET, POST, PUT, or PATCH")
    private String method = "POST";

    private Map<String, String> headers;

    private Object body;

    private java.util.Map<String, Object> variables;

    // Getters and Setters
    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public java.util.Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(java.util.Map<String, Object> variables) {
        this.variables = variables;
    }
}

