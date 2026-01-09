package com.notificationplatform.dto.mapper;

import com.notificationplatform.dto.request.CreateChannelRequest;
import com.notificationplatform.dto.request.UpdateChannelRequest;
import com.notificationplatform.dto.response.ChannelResponse;
import com.notificationplatform.entity.Channel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ChannelMapper {

    public Channel toEntity(CreateChannelRequest request) {
        Channel channel = new Channel();
        channel.setId(UUID.randomUUID().toString());
        channel.setType(request.getType());
        channel.setName(request.getName());
        channel.setProvider(request.getProvider());
        channel.setConfig(request.getConfig());
        channel.setStatus("active");
        return channel;
    }

    public void updateEntity(Channel channel, UpdateChannelRequest request) {
        if (request.getType() != null) {
            channel.setType(request.getType());
        }
        if (request.getName() != null) {
            channel.setName(request.getName());
        }
        if (request.getProvider() != null) {
            channel.setProvider(request.getProvider());
        }
        if (request.getConfig() != null) {
            channel.setConfig(request.getConfig());
        }
    }

    public ChannelResponse toResponse(Channel channel) {
        ChannelResponse response = new ChannelResponse();
        response.setId(channel.getId());
        response.setType(channel.getType());
        response.setName(channel.getName());
        response.setProvider(channel.getProvider());
        Map<String, Object> config = channel.getConfig() != null ? (Map<String, Object>) channel.getConfig() : null;
        response.setConfig(config);
        response.setStatus(channel.getStatus());
        response.setCreatedAt(channel.getCreatedAt());
        response.setUpdatedAt(channel.getUpdatedAt());
        return response;
    }

    public List<ChannelResponse> toResponseList(List<Channel> channels) {
        List<ChannelResponse> responses = new ArrayList<>();
        for (Channel channel : channels) {
            responses.add(toResponse(channel));
        }
        return responses;
    }
}

