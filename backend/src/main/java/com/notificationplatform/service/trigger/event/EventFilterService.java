package com.notificationplatform.service.trigger.event;

import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for filtering events based on configuration
 */
@Service
public class EventFilterService {

    /**
     * Check if event data matches filter configuration
     */
    public boolean matchesFilter(Map<String, Object> eventData, Map<String, Object> filter) {
        if (filter == null || filter.isEmpty()) {
            return true; // No filter, accept all
        }

        // Check event type filter
        if (filter.containsKey("event_type")) {
            String filterEventType = (String) filter.get("event_type");
            Object eventType = eventData.get("event_type");
            
            if (eventType == null || !filterEventType.equals(eventType.toString())) {
                return false;
            }
        }

        // Check field conditions
        Map<String, Object> conditions = (Map<String, Object>) filter.get("conditions");
        
        if (conditions != null) {
            for (Map.Entry<String, Object> entry : conditions.entrySet()) {
                String field = entry.getKey();
                Object expectedValue = entry.getValue();
                
                Object actualValue = getNestedValue(eventData, field);
                
                if (!matchesValue(actualValue, expectedValue)) {
                    return false;
                }
            }
        }

        return true;
    }

    private Object getNestedValue(Map<String, Object> data, String path) {
        String[] parts = path.split("\\.");
        Object current = data;
        
        for (String part : parts) {
            if (current instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) current;
                current = map.get(part);
            } else {
                return null;
            }
        }
        
        return current;
    }

    private boolean matchesValue(Object actual, Object expected) {
        if (actual == null) {
            return expected == null;
        }
        
        return actual.toString().equals(expected.toString());
    }
}

