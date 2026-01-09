package com.notificationplatform.service.trigger.handler;

import com.notificationplatform.entity.Trigger;
import com.notificationplatform.entity.enums.TriggerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler for File triggers.
 * File triggers handle file uploads and processing.
 */
@Component
@Slf4j
public class FileTriggerHandler implements TriggerHandler {

    // File triggers use EVENT type with file-specific configuration
    @Override
    public TriggerType getSupportedType() {
        return TriggerType.EVENT;
    }

    @Override
    public void onActivate(Trigger trigger) {
        log.debug("File trigger activated: triggerId={}", trigger.getId());
        // File triggers are handled by FileUploadController
    }

    @Override
    public void onDeactivate(Trigger trigger) {
        log.debug("File trigger deactivated: triggerId={}", trigger.getId());
        // File triggers are handled by FileUploadController
    }

    @Override
    public void onUpdate(Trigger trigger) {
        log.debug("File trigger updated: triggerId={}", trigger.getId());
        // File triggers are handled by FileUploadController
    }

    @Override
    public void onDelete(Trigger trigger) {
        log.debug("File trigger deleted: triggerId={}", trigger.getId());
        // File triggers are handled by FileUploadController
    }
}

