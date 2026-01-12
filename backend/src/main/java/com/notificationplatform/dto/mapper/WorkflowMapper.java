package com.notificationplatform.dto.mapper;

import com.notificationplatform.dto.request.CreateWorkflowRequest;
import com.notificationplatform.dto.request.UpdateWorkflowRequest;
import com.notificationplatform.dto.response.WorkflowResponse;
import com.notificationplatform.entity.Workflow;
import com.notificationplatform.entity.enums.WorkflowStatus;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.UUID;

/**
 * MapStruct mapper for Workflow entity and DTOs.
 * Handles conversion between String (DTO) and WorkflowStatus enum (Entity).
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface WorkflowMapper {

    WorkflowMapper INSTANCE = Mappers.getMapper(WorkflowMapper.class);

    /**
     * Map CreateWorkflowRequest to Workflow entity.
     * Converts String status to WorkflowStatus enum.
     */
    @Mapping(target = "id", expression = "java(generateId())")
    @Mapping(target = "status", expression = "java(convertStringToStatus(request.getStatus()))")
    @Mapping(target = "version", constant = "1")
    @Mapping(target = "tags", expression = "java(request.getTags() != null ? request.getTags() : new java.util.ArrayList<>())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "executions", ignore = true)
    Workflow toEntity(CreateWorkflowRequest request);

    /**
     * Update Workflow entity from UpdateWorkflowRequest.
     * Converts String status to WorkflowStatus enum if provided.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", expression = "java(request.getStatus() != null ? convertStringToStatus(request.getStatus()) : null)")
    @Mapping(target = "version", expression = "java(workflow.getVersion() + 1)")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "executions", ignore = true)
    void updateEntity(@MappingTarget Workflow workflow, UpdateWorkflowRequest request);

    /**
     * Map Workflow entity to WorkflowResponse DTO.
     * Converts WorkflowStatus enum to String.
     */
    @Mapping(target = "status", expression = "java(convertStatusToString(workflow.getStatus()))")
    WorkflowResponse toResponse(Workflow workflow);

    /**
     * Map list of Workflow entities to list of WorkflowResponse DTOs.
     */
    List<WorkflowResponse> toResponseList(List<Workflow> workflows);

    /**
     * Default method to generate UUID for id field.
     */
    default String generateId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Convert String status to WorkflowStatus enum.
     */
    default WorkflowStatus convertStringToStatus(String status) {
        if (status == null || status.isEmpty()) {
            return WorkflowStatus.DRAFT;
        }
        WorkflowStatus workflowStatus = WorkflowStatus.fromValue(status);
        return workflowStatus != null ? workflowStatus : WorkflowStatus.DRAFT;
    }

    /**
     * Convert WorkflowStatus enum to String.
     */
    default String convertStatusToString(WorkflowStatus status) {
        return status != null ? status.getValue() : null;
    }
}

