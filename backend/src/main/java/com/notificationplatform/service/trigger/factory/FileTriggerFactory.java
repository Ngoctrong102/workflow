package com.notificationplatform.service.trigger.factory;

import com.notificationplatform.dto.request.CreateFileTriggerRequest;
import com.notificationplatform.entity.Trigger;
import com.notificationplatform.entity.enums.TriggerStatus;
import com.notificationplatform.entity.enums.TriggerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Factory for creating File triggers.
 */
@Component
@Slf4j
public class FileTriggerFactory implements TriggerFactory {

    // TODO: FILE_TRIGGER not yet supported in TriggerType enum
    // @Override
    // public TriggerType getSupportedType() {
    //     return TriggerType.FILE_TRIGGER;
    // }

    @Override
    public TriggerType getSupportedType() {
        throw new UnsupportedOperationException("File trigger not yet supported");
    }

    @Override
    public Trigger createFromFileRequest(CreateFileTriggerRequest request) {
        throw new UnsupportedOperationException("File trigger not yet supported");
        // TODO: Implement when FILE_TRIGGER is added to TriggerType enum
        // Trigger trigger = new Trigger();
        // trigger.setId(UUID.randomUUID().toString());
        // trigger.setTriggerType(TriggerType.FILE_TRIGGER);
        // trigger.setStatus(TriggerStatus.ACTIVE);
        // 
        // // Build config
        // Map<String, Object> config = new HashMap<>();
        // config.put("fileFormats", request.getFileFormats() != null ? 
        //     request.getFileFormats() : Arrays.asList("csv", "json", "xlsx"));
        // config.put("maxFileSize", request.getMaxFileSize() != null ? 
        //     request.getMaxFileSize() : 10485760L);
        // config.put("dataMapping", request.getDataMapping() != null ? 
        //     request.getDataMapping() : new HashMap<>());
        // config.put("processingMode", request.getProcessingMode() != null ? 
        //     request.getProcessingMode() : "batch");
        // 
        // trigger.setConfig(config);
        // 
        // log.debug("Created file trigger: formats={}, maxSize={}", 
        //         request.getFileFormats(), request.getMaxFileSize());
        // return trigger;
    }
}

