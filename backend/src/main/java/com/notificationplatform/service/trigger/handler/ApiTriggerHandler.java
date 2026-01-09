package com.notificationplatform.service.trigger.handler;

import com.notificationplatform.entity.Trigger;
import com.notificationplatform.entity.enums.TriggerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler for API triggers.
 * API triggers don't require special activation/deactivation logic
 * as they are handled directly by the API controller.
 */
@Component
@Slf4j
public class ApiTriggerHandler implements TriggerHandler {

    @Override
    public TriggerType getSupportedType() {
        return TriggerType.API_CALL;
    }

    @Override
    public void onActivate(Trigger trigger) {
        log.debug("API trigger activated: triggerId={}", trigger.getId());
        // No special action needed for API triggers
    }

    @Override
    public void onDeactivate(Trigger trigger) {
        log.debug("API trigger deactivated: triggerId={}", trigger.getId());
        // No special action needed for API triggers
    }

    @Override
    public void onUpdate(Trigger trigger) {
        log.debug("API trigger updated: triggerId={}", trigger.getId());
        // No special action needed for API triggers
    }

    @Override
    public void onDelete(Trigger trigger) {
        log.debug("API trigger deleted: triggerId={}", trigger.getId());
        // No special action needed for API triggers
    }
}

