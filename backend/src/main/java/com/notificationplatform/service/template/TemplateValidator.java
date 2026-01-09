package com.notificationplatform.service.template;

import com.notificationplatform.dto.request.CreateTemplateRequest;
import com.notificationplatform.dto.request.UpdateTemplateRequest;
import com.notificationplatform.entity.Template;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class TemplateValidator {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");
    private static final int MAX_BODY_LENGTH = 100000; // 100KB limit
    private static final int MAX_SUBJECT_LENGTH = 500;

    public void validateCreateRequest(CreateTemplateRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Template name is required");
        }

        if (request.getChannel() == null || request.getChannel().trim().isEmpty()) {
            throw new IllegalArgumentException("Template channel is required");
        }

        if (request.getBody() == null || request.getBody().trim().isEmpty()) {
            throw new IllegalArgumentException("Template body is required");
        }

        // Validate channel
        List<String> validChannels = List.of("email", "sms", "push", "slack", "discord", "teams", "webhook");
        if (!validChannels.contains(request.getChannel().toLowerCase())) {
            throw new IllegalArgumentException("Invalid channel: " + request.getChannel());
        }

        // Validate length
        if (request.getBody().length() > MAX_BODY_LENGTH) {
            throw new IllegalArgumentException("Template body exceeds maximum length of " + MAX_BODY_LENGTH);
        }

        if (request.getSubject() != null && request.getSubject().length() > MAX_SUBJECT_LENGTH) {
            throw new IllegalArgumentException("Template subject exceeds maximum length of " + MAX_SUBJECT_LENGTH);
        }
    }

    public void validateUpdateRequest(Template existing, UpdateTemplateRequest request) {
        // If channel is being changed, validate new channel
        if (request.getChannel() != null && !request.getChannel().equals(existing.getChannel())) {
            List<String> validChannels = List.of("email", "sms", "push", "slack", "discord", "teams", "webhook");
            if (!validChannels.contains(request.getChannel().toLowerCase())) {
                throw new IllegalArgumentException("Invalid channel: " + request.getChannel());
            }
        }

        // Validate length if body is being updated
        if (request.getBody() != null) {
            if (request.getBody().length() > MAX_BODY_LENGTH) {
                throw new IllegalArgumentException("Template body exceeds maximum length of " + MAX_BODY_LENGTH);
            }
        }

        // Validate length if subject is being updated
        if (request.getSubject() != null) {
            if (request.getSubject().length() > MAX_SUBJECT_LENGTH) {
                throw new IllegalArgumentException("Template subject exceeds maximum length of " + MAX_SUBJECT_LENGTH);
            }
        }
    }

    public void validateTemplateContent(Template template) {
        // Validate that required variables are defined
        List<Map<String, Object>> variables = template.getVariables() != null ? 
            (template.getVariables() instanceof List ? (List<Map<String, Object>>) template.getVariables() : null) : null;
        if (variables != null && !variables.isEmpty()) {
            // Extract variables from template body and subject
            java.util.Set<String> usedVariables = extractVariables(template.getBody());
            if (template.getSubject() != null) {
                usedVariables.addAll(extractVariables(template.getSubject()));
            }

            // Check if all used variables are defined (optional check, can be warning)
            // For now, we'll just validate structure
        }
    }

    private java.util.Set<String> extractVariables(String text) {
        java.util.Set<String> variables = new java.util.HashSet<>();
        if (text == null || text.isEmpty()) {
            return variables;
        }

        java.util.regex.Matcher matcher = VARIABLE_PATTERN.matcher(text);
        while (matcher.find()) {
            String variable = matcher.group(1).trim();
            // Remove format and default parts
            variable = variable.split("\\|")[0].trim();
            variables.add(variable);
        }

        return variables;
    }
}

