package com.notificationplatform.dto.mapper;

import com.notificationplatform.constants.ApplicationConstants;
import com.notificationplatform.dto.request.CreateObjectTypeRequest;
import com.notificationplatform.dto.request.FieldDefinitionDTO;
import com.notificationplatform.dto.request.UpdateObjectTypeRequest;
import com.notificationplatform.dto.response.FieldDefinitionResponseDTO;
import com.notificationplatform.dto.response.ObjectTypeResponse;
import com.notificationplatform.entity.FieldDefinition;
import com.notificationplatform.entity.ObjectType;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * MapStruct mapper for ObjectType entity and DTOs.
 * MapStruct will generate implementation at compile time.
 */
@Mapper(componentModel = "spring", 
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        imports = {UUID.class, ArrayList.class})
public interface ObjectTypeMapper {

    ObjectTypeMapper INSTANCE = Mappers.getMapper(ObjectTypeMapper.class);

    /**
     * Map CreateObjectTypeRequest to ObjectType entity.
     * Generates UUID for id and sets default version.
     */
    @Mapping(target = "id", expression = "java(generateId())")
    @Mapping(target = "version", constant = "1")
    @Mapping(target = "tags", expression = "java(request.getTags() != null ? request.getTags() : new java.util.ArrayList<>())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    ObjectType toEntity(CreateObjectTypeRequest request);

    /**
     * Update ObjectType entity from UpdateObjectTypeRequest.
     * Only updates non-null fields and increments version.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", expression = "java(objectType.getVersion() + 1)")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromDTO(@MappingTarget ObjectType objectType, UpdateObjectTypeRequest request);

    /**
     * Map ObjectType entity to ObjectTypeResponse DTO.
     */
    @Mapping(target = "fields", source = "fields")
    ObjectTypeResponse toDTO(ObjectType objectType);

    /**
     * Map FieldDefinition entity to FieldDefinitionResponseDTO.
     */
    FieldDefinitionResponseDTO toFieldDefinitionDTO(FieldDefinition fieldDefinition);

    /**
     * Map FieldDefinitionDTO to FieldDefinition entity.
     */
    FieldDefinition toFieldDefinition(FieldDefinitionDTO dto);

    /**
     * Map list of FieldDefinitionDTO to list of FieldDefinition.
     */
    List<FieldDefinition> toFieldDefinitionList(List<FieldDefinitionDTO> dtos);

    /**
     * Map list of FieldDefinition to list of FieldDefinitionResponseDTO.
     */
    List<FieldDefinitionResponseDTO> toFieldDefinitionDTOList(List<FieldDefinition> fieldDefinitions);

    /**
     * Generate a new UUID string for entity ID.
     * This is used in mapping expressions.
     */
    default String generateId() {
        return UUID.randomUUID().toString();
    }
}

