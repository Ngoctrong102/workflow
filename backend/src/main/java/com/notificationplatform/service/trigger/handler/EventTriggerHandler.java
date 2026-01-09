package com.notificationplatform.service.trigger.handler;

import com.notificationplatform.entity.Trigger;
import com.notificationplatform.entity.enums.TriggerStatus;
import com.notificationplatform.entity.enums.TriggerType;
import com.notificationplatform.service.trigger.event.EventConsumerManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler for Event triggers (Kafka/RabbitMQ).
 * Registers/unregisters event consumers when trigger is activated/deactivated.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventTriggerHandler implements TriggerHandler {

    private final EventConsumerManager eventConsumerManager;

    @Override
    public TriggerType getSupportedType() {
        return TriggerType.EVENT;
    }

    @Override
    public void onActivate(Trigger trigger) {
        log.info("Activating event trigger: triggerId={}", trigger.getId());
        if (trigger.getStatus() == TriggerStatus.ACTIVE) {
            eventConsumerManager.registerConsumer(trigger);
        }
    }

    @Override
    public void onDeactivate(Trigger trigger) {
        log.info("Deactivating event trigger: triggerId={}", trigger.getId());
        eventConsumerManager.unregisterConsumer(trigger);
    }

    @Override
    public void onUpdate(Trigger trigger) {
        log.info("Updating event trigger: triggerId={}", trigger.getId());
        if (trigger.getStatus() == TriggerStatus.ACTIVE) {
            eventConsumerManager.updateConsumer(trigger);
        } else {
            eventConsumerManager.unregisterConsumer(trigger);
        }
    }

    @Override
    public void onDelete(Trigger trigger) {
        log.info("Deleting event trigger: triggerId={}", trigger.getId());
        eventConsumerManager.unregisterConsumer(trigger);
    }
}

