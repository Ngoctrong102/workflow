package com.notificationplatform.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Map;

public class WorkflowDefinitionValidator implements ConstraintValidator<ValidWorkflowDefinition, Object> {

    @Override
    public void initialize(ValidWorkflowDefinition constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null validation
        }

        if (!(value instanceof Map)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Workflow definition must be a JSON object")
                    .addConstraintViolation();
            return false;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> definition = (Map<String, Object>) value;

        // Validate nodes exist
        if (!definition.containsKey("nodes")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Workflow definition must contain 'nodes' array")
                    .addConstraintViolation();
            return false;
        }

        Object nodesObj = definition.get("nodes");
        if (!(nodesObj instanceof List)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Workflow 'nodes' must be an array")
                    .addConstraintViolation();
            return false;
        }

        @SuppressWarnings("unchecked")
        List<Object> nodes = (List<Object>) nodesObj;

        if (nodes.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Workflow must contain at least one node")
                    .addConstraintViolation();
            return false;
        }

        // Validate each node has required fields
        for (int i = 0; i < nodes.size(); i++) {
            Object nodeObj = nodes.get(i);
            if (!(nodeObj instanceof Map)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        String.format("Node at index %d must be a JSON object", i))
                        .addConstraintViolation();
                return false;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> node = (Map<String, Object>) nodeObj;

            if (!node.containsKey("id")) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        String.format("Node at index %d must have an 'id' field", i))
                        .addConstraintViolation();
                return false;
            }

            if (!node.containsKey("type")) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        String.format("Node at index %d must have a 'type' field", i))
                        .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}

