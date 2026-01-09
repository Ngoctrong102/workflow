package com.notificationplatform.service.template;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TemplateRenderer {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");
    private static final Pattern DEFAULT_PATTERN = Pattern.compile("([^|]+)\\|default:([^}]+)");
    private static final Pattern FORMAT_PATTERN = Pattern.compile("([^|]+)\\|format:([^}]+)");

    /**
     * Renders a template string by replacing variables with actual values.
     * Supports:
     * - Simple variables: {{variable}}
     * - Nested variables: {{user.name}}
     * - Default values: {{variable|default:value}}
     * - Format helpers: {{date|format:YYYY-MM-DD}}
     *
     * @param template The template string with variables
     * @param variables Map of variable values
     * @return Rendered template string
     */
    public String render(String template, Map<String, Object> variables) {
        if (template == null || template.isEmpty()) {
            return template;
        }

        if (variables == null || variables.isEmpty()) {
            return template;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String variableExpression = matcher.group(1).trim();
            String replacement = resolveVariable(variableExpression, variables);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private String resolveVariable(String expression, Map<String, Object> variables) {
        // Check for default value
        Matcher defaultMatcher = DEFAULT_PATTERN.matcher(expression);
        if (defaultMatcher.matches()) {
            String variablePath = defaultMatcher.group(1).trim();
            String defaultValue = defaultMatcher.group(2).trim();
            Object value = getNestedValue(variables, variablePath);
            return value != null ? String.valueOf(value) : defaultValue;
        }

        // Check for format helper
        Matcher formatMatcher = FORMAT_PATTERN.matcher(expression);
        if (formatMatcher.matches()) {
            String variablePath = formatMatcher.group(1).trim();
            String format = formatMatcher.group(2).trim();
            Object value = getNestedValue(variables, format);
            // For now, just return the value as string
            // Format helpers can be extended later
            return value != null ? String.valueOf(value) : "";
        }

        // Simple variable
        Object value = getNestedValue(variables, expression);
        return value != null ? String.valueOf(value) : "";
    }

    private Object getNestedValue(Map<String, Object> variables, String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        String[] parts = path.split("\\.");
        Object current = variables;

        for (String part : parts) {
            if (current == null) {
                return null;
            }

            if (current instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) current;
                current = map.get(part);
            } else {
                return null;
            }
        }

        return current;
    }
}

