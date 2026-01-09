package com.notificationplatform.service.channel;

import com.notificationplatform.dto.request.CreateChannelRequest;
import com.notificationplatform.dto.request.SendEmailRequest;
import com.notificationplatform.dto.request.SendPushRequest;
import com.notificationplatform.dto.request.SendSmsRequest;
import com.notificationplatform.dto.request.UpdateChannelRequest;
import com.notificationplatform.dto.response.ChannelResponse;
import com.notificationplatform.dto.response.ConnectionTestResponse;
import com.notificationplatform.dto.response.DeliveryResult;
import com.notificationplatform.dto.response.PagedResponse;

import java.util.List;

public interface ChannelService {

    ChannelResponse createChannel(CreateChannelRequest request);

    ChannelResponse getChannelById(String id);

    List<ChannelResponse> listChannels(String type, String status);

    PagedResponse<ChannelResponse> listChannelsPaged(String type, String status, String search, int limit, int offset);

    ChannelResponse updateChannel(String id, UpdateChannelRequest request);

    void deleteChannel(String id);

    ConnectionTestResponse testConnection(String id);

    DeliveryResult sendEmail(SendEmailRequest request);

    DeliveryResult sendSms(SendSmsRequest request);

    DeliveryResult sendPush(SendPushRequest request);

    DeliveryResult sendSlack(com.notificationplatform.dto.request.SendSlackRequest request);

    DeliveryResult sendDiscord(com.notificationplatform.dto.request.SendDiscordRequest request);

    DeliveryResult sendTeams(com.notificationplatform.dto.request.SendTeamsRequest request);

    DeliveryResult sendWebhook(com.notificationplatform.dto.request.SendWebhookRequest request);

    DeliveryResult sendInApp(com.notificationplatform.dto.request.SendInAppRequest request);
}

