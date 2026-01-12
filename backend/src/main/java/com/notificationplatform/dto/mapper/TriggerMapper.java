package com.notificationplatform.dto.mapper;

import com.notificationplatform.dto.request.CreateTriggerConfigRequest;
import com.notificationplatform.dto.request.UpdateTriggerConfigRequest;
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
     * Map CreateTriggerConfigRequest to Trigger entity.
     */
    default Trigger toEntity(CreateTriggerConfigRequest request) {
        Trigger trigger = new Trigger();
        trigger.setId(generateId());
        trigger.setName(request.getName());
        trigger.setTriggerType(TriggerType.fromValue(request.getTriggerType()));
        trigger.setStatus(convertStringToStatus(request.getStatus()));
        trigger.setConfig(request.getConfig() != null ? request.getConfig() : new HashMap<>());
        return trigger;
    }

    /**
     * Update Trigger entity from UpdateTriggerConfigRequest.
     */
    default void updateEntity(Trigger trigger, UpdateTriggerConfigRequest request) {
        if (request.getName() != null) {
            trigger.setName(request.getName());
        }
        if (request.getConfig() != null) {
            trigger.setConfig(request.getConfig());
        }
        if (request.getStatus() != null) {
            trigger.setStatus(convertStringToStatus(request.getStatus()));
        }
    }

    /**
     * Map Trigger entity to TriggerResponse DTO.
     */
    @Mapping(target = "name", source = "name")
    @Mapping(target = "triggerType", expression = "java(convertTypeToString(trigger.getTriggerType()))")
    @Mapping(target = "status", expression = "java(convertStatusToString(trigger.getStatus()))")
    @Mapping(target = "config", source = "config")
    @Mapping(target = "errorMessage", source = "errorMessage")
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

}

