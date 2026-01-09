package com.notificationplatform.service.trigger.factory;

import com.notificationplatform.constants.ApplicationConstants;
import com.notificationplatform.dto.request.CreateEventTriggerRequest;
import com.notificationplatform.entity.Trigger;
import com.notificationplatform.entity.enums.TriggerStatus;
import com.notificationplatform.entity.enums.TriggerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Factory for creating Event triggers.
 */
@Component
@Slf4j
public class EventTriggerFactory implements TriggerFactory {

    @Override
    public TriggerType getSupportedType() {
        return TriggerType.EVENT;
    }

    @Override
    public Trigger createFromEventRequest(CreateEventTriggerRequest request) {
        Trigger trigger = new Trigger();
        trigger.setId(UUID.randomUUID().toString());
        trigger.setTriggerType(TriggerType.EVENT);
        trigger.setStatus(TriggerStatus.ACTIVE);
        
        // Build config
        Map<String, Object> config = new HashMap<>();
        config.put(ApplicationConstants.ConfigKeys.QUEUE_TYPE, request.getQueueType());
        config.put(ApplicationConstants.ConfigKeys.TOPIC, request.getTopic());
        
        if (request.getConsumerGroup() != null) {
            config.put(ApplicationConstants.ConfigKeys.CONSUMER_GROUP, request.getConsumerGroup());
        }
        if (request.getBrokers() != null) {
            config.put(ApplicationConstants.ConfigKeys.BROKERS, request.getBrokers());
        }
        config.put(ApplicationConstants.ConfigKeys.OFFSET, 
            request.getOffset() != null ? request.getOffset() : ApplicationConstants.Defaults.KAFKA_OFFSET_LATEST);
        
        if (request.getFilter() != null) {
            config.put(ApplicationConstants.ConfigKeys.EVENT_FILTER, request.getFilter());
        }
        
        trigger.setConfig(config);
        
        log.debug("Created event trigger: topic={}, queueType={}", 
                request.getTopic(), request.getQueueType());
        return trigger;
    }
}

