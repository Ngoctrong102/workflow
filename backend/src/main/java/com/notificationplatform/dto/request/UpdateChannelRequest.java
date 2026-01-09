package com.notificationplatform.dto.request;

import jakarta.validation.constraints.Size;
import java.util.Map;

public class UpdateChannelRequest {

    @Size(max = 50, message = "Type must not exceed 50 characters")
    private String type;

    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Size(max = 100, message = "Provider must not exceed 100 characters")
    private String provider;

    private Map<String, Object> config;

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }
}

