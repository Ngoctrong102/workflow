package com.notificationplatform.controller;

import com.notificationplatform.dto.mapper.ObjectTypeMapper;
import com.notificationplatform.dto.request.CreateObjectTypeRequest;
import com.notificationplatform.dto.request.UpdateObjectTypeRequest;
import com.notificationplatform.dto.request.ValidateFieldRequest;
import com.notificationplatform.dto.response.FieldDefinitionResponseDTO;
import com.notificationplatform.dto.response.ObjectTypeResponse;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.dto.response.ValidateFieldResponse;
import com.notificationplatform.entity.FieldDefinition;
import com.notificationplatform.service.objecttype.ObjectTypeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/object-types")
public class ObjectTypeController {

    private final ObjectTypeService objectTypeService;
    private final ObjectTypeMapper objectTypeMapper;

    public ObjectTypeController(ObjectTypeService objectTypeService, ObjectTypeMapper objectTypeMapper) {
        this.objectTypeService = objectTypeService;
        this.objectTypeMapper = objectTypeMapper;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ObjectTypeResponse>> listObjectTypes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        PagedResponse<ObjectTypeResponse> response = objectTypeService.listObjectTypes(limit, offset, search, tag);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ObjectTypeResponse> getObjectType(@PathVariable String id) {
        ObjectTypeResponse response = objectTypeService.getObjectTypeById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ObjectTypeResponse> createObjectType(@Valid @RequestBody CreateObjectTypeRequest request) {
        ObjectTypeResponse response = objectTypeService.createObjectType(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ObjectTypeResponse> updateObjectType(
            @PathVariable String id,
            @Valid @RequestBody UpdateObjectTypeRequest request) {
        ObjectTypeResponse response = objectTypeService.updateObjectType(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteObjectType(@PathVariable String id) {
        objectTypeService.deleteObjectType(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/fields")
    public ResponseEntity<List<FieldDefinitionResponseDTO>> getObjectTypeFields(@PathVariable String id) {
        List<FieldDefinition> fields = objectTypeService.getObjectTypeFields(id);
        List<FieldDefinitionResponseDTO> response = fields.stream()
                .map(objectTypeMapper::toFieldDefinitionDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/fields/{fieldName}")
    public ResponseEntity<FieldDefinitionResponseDTO> getFieldDefinition(
            @PathVariable String id,
            @PathVariable String fieldName) {
        FieldDefinition field = objectTypeService.getFieldDefinition(id, fieldName);
        FieldDefinitionResponseDTO response = objectTypeMapper.toFieldDefinitionDTO(field);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate-field")
    public ResponseEntity<ValidateFieldResponse> validateField(@Valid @RequestBody ValidateFieldRequest request) {
        try {
            objectTypeService.validateFieldReference(request.getObjectTypeId(), request.getFieldPath());
            FieldDefinition field = objectTypeService.getFieldDefinition(
                    request.getObjectTypeId(), 
                    request.getFieldPath().split("\\.")[0].split("\\[")[0]);
            FieldDefinitionResponseDTO fieldDTO = objectTypeMapper.toFieldDefinitionDTO(field);
            ValidateFieldResponse response = new ValidateFieldResponse(true, fieldDTO, null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ValidateFieldResponse response = new ValidateFieldResponse(false, null, e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/search-fields")
    public ResponseEntity<List<com.notificationplatform.dto.response.FieldSearchResult>> searchFields(
            @RequestParam String q,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "20") int limit) {
        List<com.notificationplatform.dto.response.FieldSearchResult> response = 
                objectTypeService.searchFields(q, type, limit);
        return ResponseEntity.ok(response);
    }
}

