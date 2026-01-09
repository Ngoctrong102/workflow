package com.notificationplatform.controller;

import com.notificationplatform.dto.request.CreateChannelRequest;
import com.notificationplatform.dto.request.SendEmailRequest;
import com.notificationplatform.dto.request.SendPushRequest;
import com.notificationplatform.dto.request.SendSmsRequest;
import com.notificationplatform.dto.request.SendSlackRequest;
import com.notificationplatform.dto.request.SendDiscordRequest;
import com.notificationplatform.dto.request.SendTeamsRequest;
import com.notificationplatform.dto.request.SendWebhookRequest;
import com.notificationplatform.dto.request.SendInAppRequest;
import com.notificationplatform.dto.request.UpdateChannelRequest;
import com.notificationplatform.dto.response.ChannelResponse;
import com.notificationplatform.dto.response.ConnectionTestResponse;
import com.notificationplatform.dto.response.DeliveryResult;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.service.channel.ChannelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/channels")
public class ChannelController {

    private final ChannelService channelService;

    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    @PostMapping
    public ResponseEntity<ChannelResponse> createChannel(@Valid @RequestBody CreateChannelRequest request) {
        ChannelResponse response = channelService.createChannel(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChannelResponse> getChannel(@PathVariable String id) {
        ChannelResponse response = channelService.getChannelById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ChannelResponse>> listChannels(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        PagedResponse<ChannelResponse> responses = channelService.listChannelsPaged(type, status, search, limit, offset);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChannelResponse> updateChannel(
            @PathVariable String id,
            @Valid @RequestBody UpdateChannelRequest request) {
        ChannelResponse response = channelService.updateChannel(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChannel(@PathVariable String id) {
        channelService.deleteChannel(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/test")
    public ResponseEntity<ConnectionTestResponse> testConnection(@PathVariable String id) {
        ConnectionTestResponse response = channelService.testConnection(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/email/send")
    public ResponseEntity<DeliveryResult> sendEmail(@Valid @RequestBody SendEmailRequest request) {
        DeliveryResult result = channelService.sendEmail(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/sms/send")
    public ResponseEntity<DeliveryResult> sendSms(@Valid @RequestBody SendSmsRequest request) {
        DeliveryResult result = channelService.sendSms(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/push/send")
    public ResponseEntity<DeliveryResult> sendPush(@Valid @RequestBody SendPushRequest request) {
        DeliveryResult result = channelService.sendPush(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/slack/send")
    public ResponseEntity<DeliveryResult> sendSlack(@Valid @RequestBody SendSlackRequest request) {
        DeliveryResult result = channelService.sendSlack(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/discord/send")
    public ResponseEntity<DeliveryResult> sendDiscord(@Valid @RequestBody SendDiscordRequest request) {
        DeliveryResult result = channelService.sendDiscord(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/teams/send")
    public ResponseEntity<DeliveryResult> sendTeams(@Valid @RequestBody SendTeamsRequest request) {
        DeliveryResult result = channelService.sendTeams(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/webhook/send")
    public ResponseEntity<DeliveryResult> sendWebhook(@Valid @RequestBody SendWebhookRequest request) {
        DeliveryResult result = channelService.sendWebhook(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/in-app/send")
    public ResponseEntity<DeliveryResult> sendInApp(@Valid @RequestBody SendInAppRequest request) {
        DeliveryResult result = channelService.sendInApp(request);
        return ResponseEntity.ok(result);
    }
}

