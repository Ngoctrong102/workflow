package com.notificationplatform.service.trigger.schedule;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Validates cron expressions
 * Supports standard 5-field cron format: second minute hour day month
 * For Spring, we use 6-field format: second minute hour day month weekday
 */
@Component
public class CronValidator {

    // Standard cron pattern: second minute hour day month weekday
    // Fields: 0-59 0-59 0-23 1-31 1-12 0-7 (0 and 7 are Sunday)
    private static final Pattern CRON_PATTERN = Pattern.compile(
        "^([0-5]?[0-9]|\\*)(\\s+([0-5]?[0-9]|\\*)){4}\\s+([0-6]|\\*|SUN|MON|TUE|WED|THU|FRI|SAT)$"
    );

    // Simplified 5-field pattern (minute hour day month weekday)
    private static final Pattern SIMPLE_CRON_PATTERN = Pattern.compile(
        "^([0-5]?[0-9]|\\*)(\\s+([0-5]?[0-9]|\\*)){3}\\s+([0-6]|\\*|SUN|MON|TUE|WED|THU|FRI|SAT)$"
    );

    /**
     * Validate cron expression
     *
     * @param cronExpression Cron expression to validate
     * @return true if valid, false otherwise
     */
    public boolean isValid(String cronExpression) {
        if (cronExpression == null || cronExpression.trim().isEmpty()) {
            return false;
        }

        String trimmed = cronExpression.trim();

        // Check if it's a 6-field (Spring) or 5-field (standard) cron
        String[] fields = trimmed.split("\\s+");
        
        if (fields.length == 6) {
            // 6-field cron (second minute hour day month weekday)
            return validate6FieldCron(fields);
        } else if (fields.length == 5) {
            // 5-field cron (minute hour day month weekday) - convert to 6-field
            return validate5FieldCron(fields);
        }

        return false;
    }

    /**
     * Validate 6-field cron expression
     */
    private boolean validate6FieldCron(String[] fields) {
        try {
            // Second: 0-59
            validateField(fields[0], 0, 59);
            // Minute: 0-59
            validateField(fields[1], 0, 59);
            // Hour: 0-23
            validateField(fields[2], 0, 23);
            // Day: 1-31
            validateField(fields[3], 1, 31);
            // Month: 1-12
            validateField(fields[4], 1, 12);
            // Weekday: 0-7 (0 and 7 are Sunday)
            validateWeekday(fields[5]);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate 5-field cron expression
     */
    private boolean validate5FieldCron(String[] fields) {
        try {
            // Minute: 0-59
            validateField(fields[0], 0, 59);
            // Hour: 0-23
            validateField(fields[1], 0, 23);
            // Day: 1-31
            validateField(fields[2], 1, 31);
            // Month: 1-12
            validateField(fields[3], 1, 12);
            // Weekday: 0-7
            validateWeekday(fields[4]);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate a cron field
     */
    private void validateField(String field, int min, int max) {
        if ("*".equals(field)) {
            return; // Wildcard is valid
        }

        // Check for ranges (e.g., 1-5)
        if (field.contains("-")) {
            String[] range = field.split("-");
            if (range.length == 2) {
                int start = Integer.parseInt(range[0]);
                int end = Integer.parseInt(range[1]);
                if (start < min || end > max || start > end) {
                    throw new IllegalArgumentException("Invalid range");
                }
                return;
            }
        }

        // Check for step values (e.g., */5, 0-59/5)
        if (field.contains("/")) {
            String[] parts = field.split("/");
            if (parts.length == 2) {
                int step = Integer.parseInt(parts[1]);
                if (step <= 0) {
                    throw new IllegalArgumentException("Invalid step value");
                }
                // Validate the base part
                if (!parts[0].equals("*") && !parts[0].contains("-")) {
                    validateField(parts[0], min, max);
                }
                return;
            }
        }

        // Check for lists (e.g., 1,3,5)
        if (field.contains(",")) {
            String[] values = field.split(",");
            for (String value : values) {
                int num = Integer.parseInt(value.trim());
                if (num < min || num > max) {
                    throw new IllegalArgumentException("Value out of range");
                }
            }
            return;
        }

        // Single value
        int value = Integer.parseInt(field);
        if (value < min || value > max) {
            throw new IllegalArgumentException("Value out of range");
        }
    }

    /**
     * Validate weekday field
     */
    private void validateWeekday(String field) {
        if ("*".equals(field)) {
            return;
        }

        // Check for named weekdays
        String upper = field.toUpperCase();
        if (upper.matches("SUN|MON|TUE|WED|THU|FRI|SAT")) {
            return;
        }

        // Validate numeric weekday (0-7)
        validateField(field, 0, 7);
    }

    /**
     * Convert 5-field cron to 6-field cron (add second field)
     */
    public String convertTo6Field(String cronExpression) {
        if (cronExpression == null || cronExpression.trim().isEmpty()) {
            return cronExpression;
        }

        String trimmed = cronExpression.trim();
        String[] fields = trimmed.split("\\s+");

        if (fields.length == 5) {
            // Add "0" for seconds at the beginning
            return "0 " + trimmed;
        }

        return trimmed; // Already 6-field or invalid
    }
}

