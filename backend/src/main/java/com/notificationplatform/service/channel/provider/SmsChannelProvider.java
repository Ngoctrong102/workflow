package com.notificationplatform.service.channel.provider;

import com.notificationplatform.dto.response.DeliveryResult;
import com.notificationplatform.entity.Channel;
import com.notificationplatform.entity.enums.ChannelType;
import com.notificationplatform.service.channel.sms.TwilioSmsProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Adapter for SMS channel provider using Strategy Pattern.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SmsChannelProvider implements ChannelProvider {

    private final TwilioSmsProvider twilioSmsProvider;

    @Override
    public ChannelType getSupportedType() {
        return ChannelType.SMS;
    }

    @Override
    public boolean testConnection(Channel channel) {
        return twilioSmsProvider.testConnection(channel);
    }

    @Override
    public DeliveryResult send(Channel channel, List<String> recipients, String subject,
                               String content, Map<String, Object> variables,
                               Map<String, Object> additionalData) {
        // SMS doesn't use subject, only content
        return twilioSmsProvider.send(channel, recipients, content);
    }
}

