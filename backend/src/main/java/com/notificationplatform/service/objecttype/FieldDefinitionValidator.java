package com.notificationplatform.service.objecttype;

import com.notificationplatform.entity.FieldDefinition;
import com.notificationplatform.entity.FieldType;
import com.notificationplatform.exception.ValidationException;
import com.notificationplatform.repository.ObjectTypeRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class FieldDefinitionValidator {

    private static final Pattern FIELD_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");
    private final ObjectTypeRepository objectTypeRepository;

    public FieldDefinitionValidator(ObjectTypeRepository objectTypeRepository) {
        this.objectTypeRepository = objectTypeRepository;
    }

    /**
     * Validate a list of field definitions.
     * @param fields The field definitions to validate
     * @param currentObjectTypeId The ID of the current object type (for circular reference check)
     */
    public void validateFields(List<FieldDefinition> fields, String currentObjectTypeId) {
        if (fields == null || fields.isEmpty()) {
            throw new ValidationException("At least one field is required");
        }

        Set<String> fieldNames = new HashSet<>();
        List<String> errors = new ArrayList<>();

        for (FieldDefinition field : fields) {
            try {
                validateField(field, currentObjectTypeId);
                
                // Check for duplicate field names
                if (fieldNames.contains(field.getName())) {
                    errors.add("Duplicate field name: " + field.getName());
                } else {
                    fieldNames.add(field.getName());
                }
            } catch (ValidationException e) {
                errors.add("Field '" + field.getName() + "': " + e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Field validation errors: " + String.join("; ", errors));
        }
    }

    /**
     * Validate a single field definition.
     * @param field The field definition to validate
     * @param currentObjectTypeId The ID of the current object type (for circular reference check)
     */
    public void validateField(FieldDefinition field, String currentObjectTypeId) {
        if (field == null) {
            throw new ValidationException("Field definition cannot be null");
        }

        // Validate field name format
        validateFieldName(field.getName());

        // Validate field type
        validateFieldType(field.getType());

        // Validate required field and default value
        validateRequiredAndDefault(field);

        // Validate validation rules match field type
        validateValidationRules(field);

        // Validate nested object type reference
        if (field.getObjectTypeId() != null && !field.getObjectTypeId().isEmpty()) {
            validateObjectTypeReference(field.getObjectTypeId(), currentObjectTypeId);
        }

        // Validate array item object type reference
        if (field.getItemObjectTypeId() != null && !field.getItemObjectTypeId().isEmpty()) {
            validateObjectTypeReference(field.getItemObjectTypeId(), currentObjectTypeId);
        }

        // Validate array item type
        if (FieldType.ARRAY.getValue().equalsIgnoreCase(field.getType())) {
            validateArrayItemType(field);
        }
    }

    private void validateFieldName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Field name is required");
        }

        if (!FIELD_NAME_PATTERN.matcher(name).matches()) {
            throw new ValidationException("Field name must contain only alphanumeric characters, underscores, and hyphens");
        }
    }

    private void validateFieldType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new ValidationException("Field type is required");
        }

        FieldType fieldType = FieldType.fromValue(type);
        if (fieldType == null) {
            List<String> validTypes = new ArrayList<>();
            for (FieldType ft : FieldType.values()) {
                validTypes.add(ft.getValue());
            }
            throw new ValidationException("Invalid field type: " + type + ". Valid types are: " + 
                    String.join(", ", validTypes));
        }
    }

    private void validateRequiredAndDefault(FieldDefinition field) {
        if (Boolean.TRUE.equals(field.getRequired()) && field.getDefaultValue() != null) {
            // This is actually allowed - required fields can have default values
            // But we should log a warning or note
        }
    }

    private void validateValidationRules(FieldDefinition field) {
        if (field.getValidation() == null || field.getValidation().isEmpty()) {
            return; // Validation rules are optional
        }

        String fieldType = field.getType();
        Map<String, Object> validation = field.getValidation();

        if (FieldType.STRING.getValue().equalsIgnoreCase(fieldType) ||
            FieldType.EMAIL.getValue().equalsIgnoreCase(fieldType) ||
            FieldType.PHONE.getValue().equalsIgnoreCase(fieldType) ||
            FieldType.URL.getValue().equalsIgnoreCase(fieldType)) {
            // String validations: minLength, maxLength, pattern
            validateStringValidationRules(validation);
        } else if (FieldType.NUMBER.getValue().equalsIgnoreCase(fieldType)) {
            // Number validations: minValue, maxValue
            validateNumberValidationRules(validation);
        } else if (FieldType.ARRAY.getValue().equalsIgnoreCase(fieldType)) {
            // Array validations: minItems, maxItems
            validateArrayValidationRules(validation);
        }
    }

    private void validateStringValidationRules(Map<String, Object> validation) {
        if (validation.containsKey("minLength")) {
            Object minLength = validation.get("minLength");
            if (!(minLength instanceof Number) || ((Number) minLength).intValue() < 0) {
                throw new ValidationException("minLength must be a non-negative integer");
            }
        }

        if (validation.containsKey("maxLength")) {
            Object maxLength = validation.get("maxLength");
            if (!(maxLength instanceof Number) || ((Number) maxLength).intValue() < 0) {
                throw new ValidationException("maxLength must be a non-negative integer");
            }
        }

        if (validation.containsKey("minLength") && validation.containsKey("maxLength")) {
            int min = ((Number) validation.get("minLength")).intValue();
            int max = ((Number) validation.get("maxLength")).intValue();
            if (min > max) {
                throw new ValidationException("minLength cannot be greater than maxLength");
            }
        }

        if (validation.containsKey("pattern")) {
            Object pattern = validation.get("pattern");
            if (!(pattern instanceof String)) {
                throw new ValidationException("pattern must be a string (regex)");
            }
            // Validate regex pattern
            try {
                Pattern.compile((String) pattern);
            } catch (java.util.regex.PatternSyntaxException e) {
                throw new ValidationException("Invalid regex pattern: " + e.getMessage());
            }
        }
    }

    private void validateNumberValidationRules(Map<String, Object> validation) {
        if (validation.containsKey("minValue")) {
            Object minValue = validation.get("minValue");
            if (!(minValue instanceof Number)) {
                throw new ValidationException("minValue must be a number");
            }
        }

        if (validation.containsKey("maxValue")) {
            Object maxValue = validation.get("maxValue");
            if (!(maxValue instanceof Number)) {
                throw new ValidationException("maxValue must be a number");
            }
        }

        if (validation.containsKey("minValue") && validation.containsKey("maxValue")) {
            double min = ((Number) validation.get("minValue")).doubleValue();
            double max = ((Number) validation.get("maxValue")).doubleValue();
            if (min > max) {
                throw new ValidationException("minValue cannot be greater than maxValue");
            }
        }
    }

    private void validateArrayValidationRules(Map<String, Object> validation) {
        if (validation.containsKey("minItems")) {
            Object minItems = validation.get("minItems");
            if (!(minItems instanceof Number) || ((Number) minItems).intValue() < 0) {
                throw new ValidationException("minItems must be a non-negative integer");
            }
        }

        if (validation.containsKey("maxItems")) {
            Object maxItems = validation.get("maxItems");
            if (!(maxItems instanceof Number) || ((Number) maxItems).intValue() < 0) {
                throw new ValidationException("maxItems must be a non-negative integer");
            }
        }

        if (validation.containsKey("minItems") && validation.containsKey("maxItems")) {
            int min = ((Number) validation.get("minItems")).intValue();
            int max = ((Number) validation.get("maxItems")).intValue();
            if (min > max) {
                throw new ValidationException("minItems cannot be greater than maxItems");
            }
        }
    }

    private void validateObjectTypeReference(String objectTypeId, String currentObjectTypeId) {
        // Check if object type exists
        boolean exists = objectTypeRepository.findByIdAndNotDeleted(objectTypeId).isPresent();
        if (!exists) {
            throw new ValidationException("Referenced object type not found: " + objectTypeId);
        }

        // Check for circular reference (basic check - same object type)
        if (objectTypeId.equals(currentObjectTypeId)) {
            throw new ValidationException("Circular reference detected: object type cannot reference itself");
        }

        // TODO: More sophisticated circular reference detection could be added here
        // For now, we only check direct self-reference
    }

    private void validateArrayItemType(FieldDefinition field) {
        if (field.getItemType() == null && field.getItemObjectTypeId() == null) {
            throw new ValidationException("Array field must specify either itemType or itemObjectTypeId");
        }

        if (field.getItemType() != null && field.getItemObjectTypeId() != null) {
            throw new ValidationException("Array field cannot specify both itemType and itemObjectTypeId");
        }

        if (field.getItemType() != null) {
            FieldType itemType = FieldType.fromValue(field.getItemType());
            if (itemType == null) {
                throw new ValidationException("Invalid array item type: " + field.getItemType());
            }
        }
    }
}

