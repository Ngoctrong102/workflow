package com.notificationplatform.service.channel.provider;

import com.notificationplatform.dto.response.DeliveryResult;
import com.notificationplatform.entity.Channel;
import com.notificationplatform.entity.enums.ChannelType;
import com.notificationplatform.service.channel.email.SmtpEmailProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adapter for Email channel provider using Strategy Pattern.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailChannelProvider implements ChannelProvider {

    private final SmtpEmailProvider smtpEmailProvider;

    @Override
    public ChannelType getSupportedType() {
        return ChannelType.EMAIL;
    }

    @Override
    public boolean testConnection(Channel channel) {
        return smtpEmailProvider.testConnection(channel);
    }

    @Override
    public DeliveryResult send(Channel channel, List<String> recipients, String subject,
                               String content, Map<String, Object> variables,
                               Map<String, Object> additionalData) {
        // Extract email-specific data from additionalData
        List<String> cc = additionalData != null && additionalData.containsKey("cc") ?
            (List<String>) additionalData.get("cc") : new ArrayList<>();
        List<String> bcc = additionalData != null && additionalData.containsKey("bcc") ?
            (List<String>) additionalData.get("bcc") : new ArrayList<>();
        String contentType = additionalData != null && additionalData.containsKey("contentType") ?
            (String) additionalData.get("contentType") : "text/html";

        return smtpEmailProvider.send(channel, recipients, cc, bcc, subject, content, contentType);
    }
}

