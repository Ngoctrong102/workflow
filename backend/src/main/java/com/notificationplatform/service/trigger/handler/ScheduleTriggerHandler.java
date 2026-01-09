package com.notificationplatform.service.trigger.handler;

import com.notificationplatform.entity.Trigger;
import com.notificationplatform.entity.enums.TriggerType;
import com.notificationplatform.service.trigger.schedule.ScheduleTriggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler for Schedule triggers.
 * Registers/cancels cron schedules when trigger is activated/deactivated.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleTriggerHandler implements TriggerHandler {

    private final ScheduleTriggerService scheduleTriggerService;

    @Override
    public TriggerType getSupportedType() {
        return TriggerType.SCHEDULER;
    }

    @Override
    public void onActivate(Trigger trigger) {
        log.info("Activating schedule trigger: triggerId={}", trigger.getId());
        scheduleTriggerService.registerSchedule(trigger);
    }

    @Override
    public void onDeactivate(Trigger trigger) {
        log.info("Deactivating schedule trigger: triggerId={}", trigger.getId());
        scheduleTriggerService.cancelSchedule(trigger.getId());
    }

    @Override
    public void onUpdate(Trigger trigger) {
        log.info("Updating schedule trigger: triggerId={}", trigger.getId());
        // Cancel old schedule and register new one
        scheduleTriggerService.cancelSchedule(trigger.getId());
        if (trigger.getStatus() != null && trigger.getStatus().isActive()) {
            scheduleTriggerService.registerSchedule(trigger);
        }
    }

    @Override
    public void onDelete(Trigger trigger) {
        log.info("Deleting schedule trigger: triggerId={}", trigger.getId());
        scheduleTriggerService.cancelSchedule(trigger.getId());
    }
}

