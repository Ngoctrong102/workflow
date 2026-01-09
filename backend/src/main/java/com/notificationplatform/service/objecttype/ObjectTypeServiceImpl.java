package com.notificationplatform.service.objecttype;

import com.notificationplatform.dto.mapper.ObjectTypeMapper;
import com.notificationplatform.dto.request.CreateObjectTypeRequest;
import com.notificationplatform.dto.request.UpdateObjectTypeRequest;
import com.notificationplatform.dto.response.FieldDefinitionResponseDTO;
import com.notificationplatform.dto.response.ObjectTypeResponse;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.entity.FieldDefinition;
import com.notificationplatform.entity.ObjectType;
import com.notificationplatform.exception.ConflictException;
import com.notificationplatform.exception.ResourceNotFoundException;
import com.notificationplatform.exception.ValidationException;
import com.notificationplatform.repository.ObjectTypeRepository;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@Transactional
public class ObjectTypeServiceImpl implements ObjectTypeService {

    private final ObjectTypeRepository objectTypeRepository;
    private final ObjectTypeMapper objectTypeMapper;
    private final FieldDefinitionValidator fieldDefinitionValidator;

    public ObjectTypeServiceImpl(ObjectTypeRepository objectTypeRepository,
                                ObjectTypeMapper objectTypeMapper,
                                FieldDefinitionValidator fieldDefinitionValidator) {
        this.objectTypeRepository = objectTypeRepository;
        this.objectTypeMapper = objectTypeMapper;
        this.fieldDefinitionValidator = fieldDefinitionValidator;
    }

    @Override
    public ObjectTypeResponse createObjectType(CreateObjectTypeRequest request) {
        log.debug("Creating object type: name={}", request.getName());

        // Validate name uniqueness (case-insensitive)
        validateNameUniqueness(request.getName(), null);

        // Create entity
        ObjectType objectType = objectTypeMapper.toEntity(request);

        // Validate field definitions
        fieldDefinitionValidator.validateFields(objectType.getFields(), objectType.getId());

        // Save
        ObjectType saved = objectTypeRepository.save(objectType);
        log.info("Created object type: id={}, name={}", saved.getId(), saved.getName());

        return objectTypeMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ObjectTypeResponse getObjectTypeById(String id) {
        ObjectType objectType = objectTypeRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Object type not found with id: " + id));
        return objectTypeMapper.toDTO(objectType);
    }

    @Override
    @Transactional(readOnly = true)
    public ObjectTypeResponse getObjectTypeByName(String name) {
        ObjectType objectType = objectTypeRepository.findByNameAndNotDeleted(name)
                .orElseThrow(() -> new ResourceNotFoundException("Object type not found with name: " + name));
        return objectTypeMapper.toDTO(objectType);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ObjectTypeResponse> listObjectTypes(int limit, int offset, String search, String tag) {
        // Validate pagination
        if (limit < 1) limit = 20;
        if (limit > 100) limit = 100;
        if (offset < 0) offset = 0;

        List<ObjectType> objectTypes;
        long total;

        // Build query based on filters
        if (tag != null && !tag.trim().isEmpty()) {
            objectTypes = objectTypeRepository.findByTagsContaining(tag);
            total = objectTypes.size();
        } else {
            objectTypes = objectTypeRepository.findAllActive();
            total = objectTypeRepository.count();
        }

        // Filter by search if provided
        if (search != null && !search.trim().isEmpty()) {
            objectTypes = objectTypeRepository.searchByNameOrDescription(search);
            total = objectTypes.size();
        }

        // Apply pagination
        int fromIndex = Math.min(offset, objectTypes.size());
        int toIndex = Math.min(offset + limit, objectTypes.size());
        List<ObjectType> pagedObjectTypes = objectTypes.subList(fromIndex, toIndex);

        List<ObjectTypeResponse> responses = pagedObjectTypes.stream()
                .map(objectTypeMapper::toDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(responses, total, limit, offset);
    }

    @Override
    public ObjectTypeResponse updateObjectType(String id, UpdateObjectTypeRequest request) {
        log.debug("Updating object type: id={}", id);

        ObjectType objectType = objectTypeRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Object type not found with id: " + id));

        // Validate name uniqueness if name is being updated
        if (request.getName() != null && !request.getName().equals(objectType.getName())) {
            validateNameUniqueness(request.getName(), id);
        }

        // Update entity
        objectTypeMapper.updateEntityFromDTO(objectType, request);

        // Validate field definitions if fields are being updated
        if (request.getFields() != null) {
            fieldDefinitionValidator.validateFields(objectType.getFields(), id);
        }

        // Save
        ObjectType saved = objectTypeRepository.save(objectType);
        log.info("Updated object type: id={}, name={}, version={}", saved.getId(), saved.getName(), saved.getVersion());

        return objectTypeMapper.toDTO(saved);
    }

    @Override
    public void deleteObjectType(String id) {
        log.debug("Deleting object type: id={}", id);

        ObjectType objectType = objectTypeRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Object type not found with id: " + id));

        // Soft delete
        objectType.setDeletedAt(LocalDateTime.now());
        objectTypeRepository.save(objectType);

        log.info("Deleted object type: id={}, name={}", objectType.getId(), objectType.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FieldDefinition> getObjectTypeFields(String id) {
        ObjectType objectType = objectTypeRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Object type not found with id: " + id));
        return objectType.getFields();
    }

    @Override
    @Transactional(readOnly = true)
    public FieldDefinition getFieldDefinition(String objectTypeId, String fieldName) {
        ObjectType objectType = objectTypeRepository.findByIdAndNotDeleted(objectTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Object type not found with id: " + objectTypeId));

        return objectType.getFields().stream()
                .filter(field -> field.getName().equals(fieldName))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Field '" + fieldName + "' not found in object type: " + objectTypeId));
    }

    @Override
    @Transactional(readOnly = true)
    public void validateFieldReference(String objectTypeId, String fieldPath) {
        ObjectType objectType = objectTypeRepository.findByIdAndNotDeleted(objectTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Object type not found with id: " + objectTypeId));

        // Parse field path (e.g., "field1.nestedField" or "field1[0].nestedField")
        String[] pathParts = fieldPath.split("\\.");
        String firstFieldName = pathParts[0].split("\\[")[0]; // Remove array index if present

        // Find first field
        FieldDefinition firstField = objectType.getFields().stream()
                .filter(field -> field.getName().equals(firstFieldName))
                .findFirst()
                .orElseThrow(() -> new ValidationException(
                        "Field '" + firstFieldName + "' not found in object type: " + objectTypeId));

        // TODO: For nested fields, validate the nested object type structure
        // For now, we only validate the first field exists
        if (pathParts.length > 1) {
            // This is a nested field reference
            if (firstField.getObjectTypeId() == null) {
                throw new ValidationException(
                        "Field '" + firstFieldName + "' is not an object type, cannot access nested field");
            }
            // Recursively validate nested field
            String nestedPath = String.join(".", java.util.Arrays.copyOfRange(pathParts, 1, pathParts.length));
            validateFieldReference(firstField.getObjectTypeId(), nestedPath);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.notificationplatform.dto.response.FieldSearchResult> searchFields(String query, String type, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // Validate limit
        if (limit < 1) limit = 20;
        if (limit > 100) limit = 100;

        String searchTerm = query.toLowerCase().trim();
        List<com.notificationplatform.dto.response.FieldSearchResult> results = new ArrayList<>();

        // Get all active object types
        List<ObjectType> objectTypes = objectTypeRepository.findAllActive();

        for (ObjectType objectType : objectTypes) {
            for (FieldDefinition field : objectType.getFields()) {
                // Filter by type if specified
                if (type != null && !type.trim().isEmpty() && !field.getType().equalsIgnoreCase(type)) {
                    continue;
                }

                // Search in field name, displayName, or description
                boolean matches = field.getName().toLowerCase().contains(searchTerm) ||
                        (field.getDisplayName() != null && field.getDisplayName().toLowerCase().contains(searchTerm)) ||
                        (field.getDescription() != null && field.getDescription().toLowerCase().contains(searchTerm));

                if (matches) {
                    FieldDefinitionResponseDTO fieldDTO = objectTypeMapper.toFieldDefinitionDTO(field);
                    com.notificationplatform.dto.response.FieldSearchResult result = 
                            new com.notificationplatform.dto.response.FieldSearchResult(
                                    objectType.getId(),
                                    objectType.getName(),
                                    fieldDTO);
                    results.add(result);

                    if (results.size() >= limit) {
                        return results;
                    }
                }
            }
        }

        return results;
    }

    /**
     * Validate name uniqueness (case-insensitive).
     * @param name The name to validate
     * @param excludeId The ID to exclude from uniqueness check (for updates)
     */
    private void validateNameUniqueness(String name, String excludeId) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Object type name is required");
        }

        // Check for existing object type with same name (case-insensitive)
        objectTypeRepository.findByNameAndNotDeleted(name)
                .ifPresent(existing -> {
                    if (excludeId == null || !existing.getId().equals(excludeId)) {
                        throw new ConflictException("Object type with name '" + name + "' already exists");
                    }
                });
    }
}

