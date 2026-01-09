package com.notificationplatform.service.channel;

import com.notificationplatform.dto.mapper.ChannelMapper;
import com.notificationplatform.dto.request.CreateChannelRequest;
import com.notificationplatform.dto.request.SendEmailRequest;
import com.notificationplatform.dto.request.UpdateChannelRequest;
import com.notificationplatform.dto.response.ChannelResponse;
import com.notificationplatform.dto.response.ConnectionTestResponse;
import com.notificationplatform.dto.response.DeliveryResult;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.entity.Channel;
import com.notificationplatform.exception.ResourceNotFoundException;
import com.notificationplatform.repository.ChannelRepository;
import com.notificationplatform.dto.request.SendPushRequest;
import com.notificationplatform.dto.request.SendSmsRequest;
import com.notificationplatform.entity.enums.ChannelType;
import com.notificationplatform.service.channel.provider.ChannelProviderRegistry;
import com.notificationplatform.service.template.TemplateRenderer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChannelServiceImpl implements ChannelService {

    private final ChannelRepository channelRepository;
    private final ChannelMapper channelMapper;
    private final ChannelProviderRegistry channelProviderRegistry;
    private final TemplateRenderer templateRenderer;
    private final com.notificationplatform.service.channel.email.SmtpEmailProvider smtpEmailProvider;
    private final com.notificationplatform.service.channel.sms.TwilioSmsProvider twilioSmsProvider;
    private final com.notificationplatform.service.channel.push.FcmPushProvider fcmPushProvider;
    private final com.notificationplatform.service.channel.discord.DiscordProvider discordProvider;
    private final com.notificationplatform.service.channel.teams.TeamsProvider teamsProvider;
    private final com.notificationplatform.service.channel.webhook.WebhookProvider webhookProvider;
    private final com.notificationplatform.service.channel.slack.SlackProvider slackProvider;
    private final com.notificationplatform.service.channel.inapp.InAppProvider inAppProvider;

    public ChannelServiceImpl(ChannelRepository channelRepository,
                             ChannelMapper channelMapper,
                             ChannelProviderRegistry channelProviderRegistry,
                             TemplateRenderer templateRenderer,
                             com.notificationplatform.service.channel.email.SmtpEmailProvider smtpEmailProvider,
                             com.notificationplatform.service.channel.sms.TwilioSmsProvider twilioSmsProvider,
                             com.notificationplatform.service.channel.push.FcmPushProvider fcmPushProvider,
                             com.notificationplatform.service.channel.discord.DiscordProvider discordProvider,
                             com.notificationplatform.service.channel.teams.TeamsProvider teamsProvider,
                             com.notificationplatform.service.channel.webhook.WebhookProvider webhookProvider,
                             com.notificationplatform.service.channel.slack.SlackProvider slackProvider,
                             com.notificationplatform.service.channel.inapp.InAppProvider inAppProvider) {
        this.channelRepository = channelRepository;
        this.channelMapper = channelMapper;
        this.channelProviderRegistry = channelProviderRegistry;
        this.templateRenderer = templateRenderer;
        this.smtpEmailProvider = smtpEmailProvider;
        this.twilioSmsProvider = twilioSmsProvider;
        this.fcmPushProvider = fcmPushProvider;
        this.discordProvider = discordProvider;
        this.teamsProvider = teamsProvider;
        this.webhookProvider = webhookProvider;
        this.slackProvider = slackProvider;
        this.inAppProvider = inAppProvider;
    }

    @Override
    public ChannelResponse createChannel(CreateChannelRequest request) {
        // Validate channel type
        validateChannelType(request.getType());

        // Create entity
        Channel channel = channelMapper.toEntity(request);

        // Save
        Channel saved = channelRepository.save(channel);
        return channelMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "channels", key = "#id")
    public ChannelResponse getChannelById(String id) {
        Channel channel = channelRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found with id: " + id));
        return channelMapper.toResponse(channel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChannelResponse> listChannels(String type, String status) {
        List<Channel> channels;

        if (type != null && !type.isEmpty()) {
            channels = channelRepository.findByType(type);
        } else {
            channels = channelRepository.findAllActive();
        }

        // Filter by status if provided
        if (status != null && !status.isEmpty()) {
            channels = channels.stream()
                    .filter(c -> status.equals(c.getStatus()))
                    .collect(Collectors.toList());
        }

        return channelMapper.toResponseList(channels);
    }

    @Override
    @org.springframework.cache.annotation.CacheEvict(value = "channels", key = "#id")
    public ChannelResponse updateChannel(String id, UpdateChannelRequest request) {
        Channel channel = channelRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found with id: " + id));

        // Validate channel type if being updated
        if (request.getType() != null) {
            validateChannelType(request.getType());
        }

        // Update entity
        channelMapper.updateEntity(channel, request);

        // Save
        Channel saved = channelRepository.save(channel);
        return channelMapper.toResponse(saved);
    }

    @Override
    @org.springframework.cache.annotation.CacheEvict(value = "channels", key = "#id")
    public void deleteChannel(String id) {
        Channel channel = channelRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found with id: " + id));

        // Soft delete
        channel.setDeletedAt(LocalDateTime.now());
        channelRepository.save(channel);
    }

    @Override
    public ConnectionTestResponse testConnection(String id) {
        Channel channel = channelRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found with id: " + id));

        boolean success = false;
        String message = "Connection test successful";

        try {
            // Use Strategy Pattern to test connection
            ChannelType channelType = ChannelType.fromValue(channel.getType());
            if (channelType == null) {
                throw new IllegalArgumentException("Unsupported channel type: " + channel.getType());
            }
            
            success = channelProviderRegistry.testConnection(channel);

            // Update channel status based on test result
            if (success) {
                channel.setStatus("active");
                message = "Connection test successful";
            } else {
                channel.setStatus("error");
                message = "Connection test failed";
            }
            channelRepository.save(channel);

        } catch (Exception e) {
            success = false;
            message = "Connection test failed: " + e.getMessage();
            channel.setStatus("error");
            channelRepository.save(channel);
        }

        ConnectionTestResponse response = new ConnectionTestResponse();
        response.setSuccess(success);
        response.setMessage(message);
        return response;
    }

    @Override
    public DeliveryResult sendEmail(SendEmailRequest request) {
        Channel channel = channelRepository.findByIdAndNotDeleted(request.getChannelId())
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found with id: " + request.getChannelId()));

        if (!"email".equals(channel.getType())) {
            throw new IllegalArgumentException("Channel is not an email channel: " + channel.getType());
        }

        // Parse recipients
        List<String> to = parseRecipients(request.getTo());
        List<String> cc = request.getCc() != null ? parseRecipients(request.getCc()) : new ArrayList<>();
        List<String> bcc = request.getBcc() != null ? parseRecipients(request.getBcc()) : new ArrayList<>();

        // Render template if variables provided
        String subject = request.getSubject();
        String body = request.getBody();

        if (request.getVariables() != null && !request.getVariables().isEmpty()) {
            subject = templateRenderer.render(subject, request.getVariables());
            body = templateRenderer.render(body, request.getVariables());
        }

        // Send email
        return smtpEmailProvider.send(channel, to, cc, bcc, subject, body, request.getContentType());
    }

    @Override
    public DeliveryResult sendSms(SendSmsRequest request) {
        Channel channel = channelRepository.findByIdAndNotDeleted(request.getChannelId())
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found with id: " + request.getChannelId()));

        if (!"sms".equals(channel.getType())) {
            throw new IllegalArgumentException("Channel is not an SMS channel: " + channel.getType());
        }

        // Parse recipients
        List<String> to = parseRecipients(request.getTo());

        // Render template if variables provided
        String message = request.getMessage();
        if (request.getVariables() != null && !request.getVariables().isEmpty()) {
            message = templateRenderer.render(message, request.getVariables());
        }

        // Send SMS
        return twilioSmsProvider.send(channel, to, message);
    }

    @Override
    public DeliveryResult sendPush(SendPushRequest request) {
        Channel channel = channelRepository.findByIdAndNotDeleted(request.getChannelId())
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found with id: " + request.getChannelId()));

        if (!"push".equals(channel.getType())) {
            throw new IllegalArgumentException("Channel is not a push channel: " + channel.getType());
        }

        // Render template if variables provided
        String title = request.getTitle();
        String body = request.getBody();

        if (request.getVariables() != null && !request.getVariables().isEmpty()) {
            title = templateRenderer.render(title, request.getVariables());
            body = templateRenderer.render(body, request.getVariables());
        }

        // Send push notification
        return fcmPushProvider.send(channel, request.getDeviceTokens(), title, body,
                request.getIcon(), request.getImage(), request.getSound(), request.getBadge(), request.getData());
    }

    @Override
    public DeliveryResult sendSlack(com.notificationplatform.dto.request.SendSlackRequest request) {
        Channel channel = channelRepository.findByIdAndNotDeleted(request.getChannelId())
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found with id: " + request.getChannelId()));

        if (!"slack".equals(channel.getType())) {
            throw new IllegalArgumentException("Channel is not a Slack channel: " + channel.getType());
        }

        // Render template if variables provided
        String message = request.getMessage();
        if (request.getVariables() != null && !request.getVariables().isEmpty()) {
            message = templateRenderer.render(message, request.getVariables());
        }

        return slackProvider.send(channel, request.getChannelName(), message, request.getAttachments());
    }

    @Override
    public DeliveryResult sendDiscord(com.notificationplatform.dto.request.SendDiscordRequest request) {
        Channel channel = channelRepository.findByIdAndNotDeleted(request.getChannelId())
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found with id: " + request.getChannelId()));

        if (!"discord".equals(channel.getType())) {
            throw new IllegalArgumentException("Channel is not a Discord channel: " + channel.getType());
        }

        // Render template if variables provided
        String message = request.getMessage();
        if (request.getVariables() != null && !request.getVariables().isEmpty() && message != null) {
            message = templateRenderer.render(message, request.getVariables());
        }

        // Render embed if provided
        com.notificationplatform.dto.response.DiscordEmbed embed = request.getEmbed();
        if (embed != null && request.getVariables() != null && !request.getVariables().isEmpty()) {
            if (embed.getTitle() != null) {
                embed.setTitle(templateRenderer.render(embed.getTitle(), request.getVariables()));
            }
            if (embed.getDescription() != null) {
                embed.setDescription(templateRenderer.render(embed.getDescription(), request.getVariables()));
            }
        }

        return discordProvider.send(channel, request.getDiscordChannelId(), message, embed);
    }

    @Override
    public DeliveryResult sendTeams(com.notificationplatform.dto.request.SendTeamsRequest request) {
        Channel channel = channelRepository.findByIdAndNotDeleted(request.getChannelId())
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found with id: " + request.getChannelId()));

        if (!"teams".equals(channel.getType())) {
            throw new IllegalArgumentException("Channel is not a Teams channel: " + channel.getType());
        }

        // Get webhook URL from channel config
        Map<String, Object> config = channel.getConfig() != null ? (Map<String, Object>) channel.getConfig() : new HashMap<>();
        String webhookUrl = (String) config.get("webhookUrl");

        if (webhookUrl == null || webhookUrl.isEmpty()) {
            throw new IllegalArgumentException("Teams webhook URL is required in channel configuration");
        }

        // Render template if variables provided
        String title = request.getTitle();
        String text = request.getText();
        if (request.getVariables() != null && !request.getVariables().isEmpty()) {
            title = templateRenderer.render(title, request.getVariables());
            text = templateRenderer.render(text, request.getVariables());
        }

        return teamsProvider.send(channel, webhookUrl, title, text, request.getThemeColor());
    }

    @Override
    public DeliveryResult sendWebhook(com.notificationplatform.dto.request.SendWebhookRequest request) {
        Channel channel = channelRepository.findByIdAndNotDeleted(request.getChannelId())
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found with id: " + request.getChannelId()));

        if (!"webhook".equals(channel.getType())) {
            throw new IllegalArgumentException("Channel is not a webhook channel: " + channel.getType());
        }

        // Use URL from request or channel config
        String url = request.getUrl();
        if (url == null || url.isEmpty()) {
            Map<String, Object> config = channel.getConfig() != null ? (Map<String, Object>) channel.getConfig() : new HashMap<>();
            url = (String) config.get("url");
            if (url == null || url.isEmpty()) {
                throw new IllegalArgumentException("Webhook URL is required");
            }
        }

        // Render body if variables provided
        Object body = request.getBody();
        if (body instanceof String && request.getVariables() != null && !request.getVariables().isEmpty()) {
            body = templateRenderer.render((String) body, request.getVariables());
        }

        return webhookProvider.send(channel, url, request.getMethod(), request.getHeaders(), body);
    }

    @Override
    public DeliveryResult sendInApp(com.notificationplatform.dto.request.SendInAppRequest request) {
        Channel channel = channelRepository.findByIdAndNotDeleted(request.getChannelId())
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found with id: " + request.getChannelId()));

        if (!"in-app".equals(channel.getType()) && !"inapp".equals(channel.getType())) {
            throw new IllegalArgumentException("Channel is not an in-app channel: " + channel.getType());
        }

        // Render template if variables provided
        String title = request.getTitle();
        String message = request.getMessage();
        if (request.getVariables() != null && !request.getVariables().isEmpty()) {
            title = templateRenderer.render(title, request.getVariables());
            message = templateRenderer.render(message, request.getVariables());
        }

        return inAppProvider.send(
                channel,
                request.getUserId(),
                title,
                message,
                request.getType(),
                request.getActionUrl(),
                request.getActionLabel(),
                request.getImageUrl(),
                request.getExpiresAt(),
                request.getMetadata()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ChannelResponse> listChannelsPaged(String type, String status, String search, int limit, int offset) {
        // Validate pagination
        if (limit < 1) limit = 20;
        if (limit > 100) limit = 100;
        if (offset < 0) offset = 0;

        List<Channel> channels;

        // Build query based on filters
        if (type != null && !type.isEmpty() && status != null && !status.isEmpty()) {
            channels = channelRepository.findByTypeAndStatus(type, status);
        } else if (type != null && !type.isEmpty()) {
            channels = channelRepository.findByType(type);
        } else if (status != null && !status.isEmpty()) {
            channels = channelRepository.findByStatus(status);
        } else {
            channels = channelRepository.findAllActive();
        }

        // Filter by search if provided
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            channels = channels.stream()
                    .filter(c -> c.getName().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
        }

        long total = channels.size();

        // Apply pagination
        int fromIndex = Math.min(offset, channels.size());
        int toIndex = Math.min(offset + limit, channels.size());
        List<Channel> pagedChannels = channels.subList(fromIndex, toIndex);

        List<ChannelResponse> responses = channelMapper.toResponseList(pagedChannels);
        return new PagedResponse<>(responses, total, limit, offset);
    }

    private void validateChannelType(String type) {
        List<String> validTypes = Arrays.asList("email", "sms", "push", "slack", "discord", "teams", "webhook", "in-app", "inapp");
        if (!validTypes.contains(type.toLowerCase())) {
            throw new IllegalArgumentException("Invalid channel type: " + type);
        }
    }

    private List<String> parseRecipients(String recipients) {
        if (recipients == null || recipients.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(recipients.split(","))
                .map(String::trim)
                .filter(email -> !email.isEmpty())
                .collect(Collectors.toList());
    }
}

