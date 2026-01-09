package com.notificationplatform.dto.mapper;

import com.notificationplatform.dto.response.ExecutionWaitStateDTO;
import com.notificationplatform.entity.ExecutionWaitState;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ExecutionWaitStateMapper {

    public ExecutionWaitStateDTO toDTO(ExecutionWaitState entity) {
        if (entity == null) {
            return null;
        }

        ExecutionWaitStateDTO dto = new ExecutionWaitStateDTO();
        dto.setId(entity.getId());
        dto.setExecutionId(entity.getExecution() != null ? entity.getExecution().getId() : null);
        dto.setNodeId(entity.getNodeId());
        dto.setCorrelationId(entity.getCorrelationId());
        dto.setAggregationStrategy(entity.getAggregationStrategy());
        dto.setRequiredEvents(entity.getRequiredEvents());
        dto.setEnabledEvents(entity.getEnabledEvents());
        dto.setApiCallEnabled(entity.getApiCallEnabled());
        dto.setKafkaEventEnabled(entity.getKafkaEventEnabled());
        dto.setApiResponseData(entity.getApiResponseData());
        dto.setKafkaEventData(entity.getKafkaEventData());
        dto.setReceivedEvents(entity.getReceivedEvents());
        dto.setStatus(entity.getStatus());
        dto.setResumedAt(entity.getResumedAt());
        dto.setResumedBy(entity.getResumedBy());
        dto.setVersion(entity.getVersion());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setExpiresAt(entity.getExpiresAt());

        return dto;
    }

    public List<ExecutionWaitStateDTO> toDTOList(List<ExecutionWaitState> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }

        List<ExecutionWaitStateDTO> dtos = new ArrayList<>();
        for (ExecutionWaitState entity : entities) {
            dtos.add(toDTO(entity));
        }
        return dtos;
    }
}

