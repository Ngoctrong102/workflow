package com.notificationplatform.service.trigger.factory;

import com.notificationplatform.dto.request.CreateApiTriggerRequest;
import com.notificationplatform.entity.Trigger;
import com.notificationplatform.entity.enums.TriggerStatus;
import com.notificationplatform.entity.enums.TriggerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Factory for creating API triggers.
 */
@Component
@Slf4j
public class ApiTriggerFactory implements TriggerFactory {

    @Override
    public TriggerType getSupportedType() {
        return TriggerType.API_CALL;
    }

    @Override
    public Trigger createFromApiRequest(CreateApiTriggerRequest request) {
        Trigger trigger = new Trigger();
        trigger.setId(UUID.randomUUID().toString());
        trigger.setTriggerType(TriggerType.API_CALL);
        trigger.setStatus(TriggerStatus.ACTIVE);
        
        // Build config with path and method
        Map<String, Object> config = new HashMap<>();
        config.put("path", request.getPath());
        config.put("method", request.getMethod());
        if (request.getApiKey() != null && !request.getApiKey().isEmpty()) {
            config.put("apiKey", request.getApiKey());
        }
        if (request.getRequestSchema() != null) {
            config.put("requestSchema", request.getRequestSchema());
        }
        trigger.setConfig(config);
        
        log.debug("Created API trigger: path={}, method={}", request.getPath(), request.getMethod());
        return trigger;
    }
}

