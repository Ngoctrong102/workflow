package com.notificationplatform.service.channel.provider;

import com.notificationplatform.dto.response.DeliveryResult;
import com.notificationplatform.entity.Channel;
import com.notificationplatform.entity.enums.ChannelType;
import com.notificationplatform.service.channel.push.FcmPushProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Adapter for Push notification channel provider using Strategy Pattern.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PushChannelProvider implements ChannelProvider {

    private final FcmPushProvider fcmPushProvider;

    @Override
    public ChannelType getSupportedType() {
        return ChannelType.PUSH;
    }

    @Override
    public boolean testConnection(Channel channel) {
        return fcmPushProvider.testConnection(channel);
    }

    @Override
    public DeliveryResult send(Channel channel, List<String> recipients, String subject,
                               String content, Map<String, Object> variables,
                               Map<String, Object> additionalData) {
        // Extract push-specific data from additionalData
        String icon = additionalData != null && additionalData.containsKey("icon") ?
            (String) additionalData.get("icon") : null;
        String image = additionalData != null && additionalData.containsKey("image") ?
            (String) additionalData.get("image") : null;
        String sound = additionalData != null && additionalData.containsKey("sound") ?
            (String) additionalData.get("sound") : null;
        Integer badge = additionalData != null && additionalData.containsKey("badge") ?
            (Integer) additionalData.get("badge") : null;
        Map<String, Object> data = additionalData != null && additionalData.containsKey("data") ?
            (Map<String, Object>) additionalData.get("data") : null;

        // For push, recipients are device tokens, subject is title, content is body
        return fcmPushProvider.send(channel, recipients, subject, content, icon, image, sound, badge, data);
    }
}

