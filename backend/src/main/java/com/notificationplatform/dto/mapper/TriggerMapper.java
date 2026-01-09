package com.notificationplatform.dto.mapper;

import com.notificationplatform.constants.ApplicationConstants;
import com.notificationplatform.dto.request.CreateApiTriggerRequest;
import com.notificationplatform.dto.request.CreateEventTriggerRequest;
import com.notificationplatform.dto.request.CreateFileTriggerRequest;
import com.notificationplatform.dto.request.CreateScheduleTriggerRequest;
import com.notificationplatform.dto.request.UpdateTriggerRequest;
import com.notificationplatform.dto.response.TriggerResponse;
import com.notificationplatform.entity.Trigger;
import com.notificationplatform.entity.enums.TriggerStatus;
import com.notificationplatform.entity.enums.TriggerType;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.*;

/**
 * MapStruct mapper for Trigger entity and DTOs.
 * Handles conversion between String (DTO) and TriggerType/TriggerStatus enums (Entity).
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface TriggerMapper {

    TriggerMapper INSTANCE = Mappers.getMapper(TriggerMapper.class);

    /**
     * Map CreateApiTriggerRequest to Trigger entity.
     */
    default Trigger toEntity(CreateApiTriggerRequest request) {
        Trigger trigger = new Trigger();
        trigger.setId(generateId());
        trigger.setType(TriggerType.API);
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
        
        return trigger;
    }

    /**
     * Map CreateScheduleTriggerRequest to Trigger entity.
     */
    default Trigger toEntity(CreateScheduleTriggerRequest request) {
        Trigger trigger = new Trigger();
        trigger.setId(generateId());
        trigger.setType(TriggerType.SCHEDULE);
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
        
        return trigger;
    }

    /**
     * Map CreateFileTriggerRequest to Trigger entity.
     */
    default Trigger toEntity(CreateFileTriggerRequest request) {
        Trigger trigger = new Trigger();
        trigger.setId(generateId());
        trigger.setType(TriggerType.FILE);
        trigger.setStatus(TriggerStatus.ACTIVE);
        
        // Build config
        Map<String, Object> config = new HashMap<>();
        config.put("fileFormats", request.getFileFormats() != null ? 
            request.getFileFormats() : Arrays.asList("csv", "json", "xlsx"));
        config.put("maxFileSize", request.getMaxFileSize() != null ? 
            request.getMaxFileSize() : 10485760L);
        config.put("dataMapping", request.getDataMapping() != null ? 
            request.getDataMapping() : new HashMap<>());
        config.put("processingMode", request.getProcessingMode() != null ? 
            request.getProcessingMode() : "batch");
        
        trigger.setConfig(config);
        
        return trigger;
    }

    /**
     * Map CreateEventTriggerRequest to Trigger entity.
     */
    default Trigger toEntity(CreateEventTriggerRequest request) {
        Trigger trigger = new Trigger();
        trigger.setId(generateId());
        trigger.setType(TriggerType.EVENT);
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
        
        return trigger;
    }

    /**
     * Update Trigger entity from UpdateTriggerRequest.
     */
    default void updateEntity(Trigger trigger, UpdateTriggerRequest request) {
        Map<String, Object> config = trigger.getConfig() != null ? trigger.getConfig() : new HashMap<>();
        
        if (request.getPath() != null) {
            config.put("path", request.getPath());
        }
        if (request.getMethod() != null) {
            config.put("method", request.getMethod());
        }
        if (request.getApiKey() != null) {
            config.put("apiKey", request.getApiKey());
        }
        if (request.getRequestSchema() != null) {
            config.put("requestSchema", request.getRequestSchema());
        }
        
        trigger.setConfig(config);
        
        if (request.getStatus() != null) {
            trigger.setStatus(convertStringToStatus(request.getStatus()));
        }
    }

    /**
     * Map Trigger entity to TriggerResponse DTO.
     */
    @Mapping(target = "workflowId", expression = "java(trigger.getWorkflow() != null ? trigger.getWorkflow().getId() : null)")
    @Mapping(target = "type", expression = "java(convertTypeToString(trigger.getType()))")
    @Mapping(target = "status", expression = "java(convertStatusToString(trigger.getStatus()))")
    @Mapping(target = "path", expression = "java(extractPathFromConfig(trigger.getConfig()))")
    @Mapping(target = "method", expression = "java(extractMethodFromConfig(trigger.getConfig()))")
    @Mapping(target = "uploadEndpoint", expression = "java(buildUploadEndpoint(trigger))")
    TriggerResponse toResponse(Trigger trigger);

    /**
     * Default method to generate UUID for id field.
     */
    default String generateId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Convert String status to TriggerStatus enum.
     */
    default TriggerStatus convertStringToStatus(String status) {
        if (status == null) {
            return TriggerStatus.ACTIVE;
        }
        TriggerStatus triggerStatus = TriggerStatus.fromValue(status);
        return triggerStatus != null ? triggerStatus : TriggerStatus.ACTIVE;
    }

    /**
     * Convert TriggerStatus enum to String.
     */
    default String convertStatusToString(TriggerStatus status) {
        return status != null ? status.getValue() : null;
    }

    /**
     * Convert TriggerType enum to String.
     */
    default String convertTypeToString(TriggerType type) {
        return type != null ? type.getValue() : null;
    }

    /**
     * Extract path from config map.
     */
    default String extractPathFromConfig(Map<String, Object> config) {
        return config != null ? (String) config.get("path") : null;
    }

    /**
     * Extract method from config map.
     */
    default String extractMethodFromConfig(Map<String, Object> config) {
        return config != null ? (String) config.get("method") : null;
    }

    /**
     * Build upload endpoint for file triggers.
     */
    default String buildUploadEndpoint(Trigger trigger) {
        if (trigger.getType() == TriggerType.FILE && trigger.getId() != null) {
            return "/api/v1/triggers/file/" + trigger.getId() + "/upload";
        }
        return null;
    }
}

