package com.notificationplatform.service.channel.provider;

import com.notificationplatform.dto.response.DeliveryResult;
import com.notificationplatform.entity.Channel;
import com.notificationplatform.entity.enums.ChannelType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for channel providers using Strategy Pattern.
 * Automatically collects all ChannelProvider implementations via Spring's List injection
 * and creates a lookup map for O(1) access by channel type.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChannelProviderRegistry {

    private final List<ChannelProvider> providers;
    private final Map<ChannelType, ChannelProvider> providerMap = new HashMap<>();

    @PostConstruct
    public void init() {
        for (ChannelProvider provider : providers) {
            ChannelType type = provider.getSupportedType();
            if (providerMap.containsKey(type)) {
                log.warn("Multiple providers found for channel type: {}. Using: {}", 
                        type, provider.getClass().getSimpleName());
            }
            providerMap.put(type, provider);
            log.debug("Registered channel provider: {} for type: {}", 
                    provider.getClass().getSimpleName(), type);
        }
        log.info("Initialized ChannelProviderRegistry with {} providers", providerMap.size());
    }

    /**
     * Get provider for a specific channel type.
     */
    public ChannelProvider getProvider(ChannelType type) {
        ChannelProvider provider = providerMap.get(type);
        if (provider == null) {
            log.warn("No provider found for channel type: {}", type);
        }
        return provider;
    }

    /**
     * Get provider for a channel.
     * Handles both String type (from entity) and ChannelType enum.
     */
    public ChannelProvider getProvider(Channel channel) {
        if (channel.getType() == null) {
            log.warn("Channel has null type: channelId={}", channel.getId());
            return null;
        }
        
        // Convert String type to enum
        // channel.getType() returns String, convert to enum
        ChannelType channelType = ChannelType.fromValue(channel.getType());
        
        if (channelType == null) {
            log.warn("Cannot convert channel type to enum: channelId={}, type={}", 
                    channel.getId(), channel.getType());
            return null;
        }
        
        return getProvider(channelType);
    }

    /**
     * Test connection for a channel.
     * Handles String type from entity.
     */
    public boolean testConnection(Channel channel) {
        ChannelProvider provider = getProvider(channel);
        if (provider == null) {
            log.warn("No provider found for channel: channelId={}, type={}", 
                    channel.getId(), channel.getType());
            return false;
        }
        return provider.testConnection(channel);
    }

    /**
     * Send notification through a channel.
     */
    public DeliveryResult send(Channel channel, List<String> recipients, String subject,
                              String content, Map<String, Object> variables,
                              Map<String, Object> additionalData) {
        ChannelProvider provider = getProvider(channel);
        if (provider == null) {
            return DeliveryResult.failure("No provider found for channel type: " + channel.getType());
        }
        return provider.send(channel, recipients, subject, content, variables, additionalData);
    }
}

