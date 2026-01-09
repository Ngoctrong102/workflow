package com.notificationplatform.service.trigger.factory;

import com.notificationplatform.constants.ApplicationConstants;
import com.notificationplatform.dto.request.CreateScheduleTriggerRequest;
import com.notificationplatform.entity.Trigger;
import com.notificationplatform.entity.enums.TriggerStatus;
import com.notificationplatform.entity.enums.TriggerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Factory for creating Schedule triggers.
 */
@Component
@Slf4j
public class ScheduleTriggerFactory implements TriggerFactory {

    @Override
    public TriggerType getSupportedType() {
        return TriggerType.SCHEDULER;
    }

    @Override
    public Trigger createFromScheduleRequest(CreateScheduleTriggerRequest request) {
        Trigger trigger = new Trigger();
        trigger.setId(UUID.randomUUID().toString());
        trigger.setTriggerType(TriggerType.SCHEDULER);
        trigger.setStatus(TriggerStatus.ACTIVE);
        
        // Build config
        Map<String, Object> config = new HashMap<>();
        config.put(ApplicationConstants.ConfigKeys.CRON_EXPRESSION, request.getCronExpression());
        config.put(ApplicationConstants.ConfigKeys.TIMEZONE, request.getTimezone() != null ? request.getTimezone() : "UTC");
        
        if (request.getStartDate() != null) {
            config.put("startDate", request.getStartDate().toString());
        }
        if (request.getEndDate() != null) {
            config.put("endDate", request.getEndDate().toString());
        }
        if (request.getData() != null) {
            config.put("data", request.getData());
        }
        
        trigger.setConfig(config);
        
        log.debug("Created schedule trigger: cron={}, timezone={}", 
                request.getCronExpression(), request.getTimezone());
        return trigger;
    }
}

