package com.notificationplatform.util;

import org.mvel2.MVEL;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for evaluating MVEL expressions.
 * Supports MVEL syntax: @{expression}
 * 
 * See: @import(features/mvel-expression-system.md)
 */
@Slf4j
public class MvelEvaluator {

    private static final Pattern MVEL_PATTERN = Pattern.compile("@\\{([^}]+)\\}");
    
    /**
     * Evaluate MVEL expression in a string.
     * Supports format: @{expression}
     * Example: "/users/@{userId}" -> "/users/123"
     * 
     * @param expression String containing MVEL expressions
     * @param context Execution context for MVEL evaluation
     * @return Evaluated string with MVEL expressions replaced
     */
    public static Object evaluateExpression(String expression, Map<String, Object> context) {
        if (expression == null || expression.isEmpty()) {
            return expression;
        }
        
        // Check if expression contains MVEL syntax
        if (!expression.contains("@{")) {
            return expression; // Static value, no MVEL expressions
        }
        
        // Extract and evaluate all MVEL expressions
        Matcher matcher = MVEL_PATTERN.matcher(expression);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String mvelExpr = matcher.group(1);
            try {
                Object value = MVEL.eval(mvelExpr, context);
                String replacement = value != null ? String.valueOf(value) : "";
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            } catch (Exception e) {
                log.error("Failed to evaluate MVEL expression '@{{{}}}': {}", mvelExpr, e.getMessage());
                throw new IllegalArgumentException(
                    String.format("Failed to evaluate MVEL expression '@{%s}': %s", 
                        mvelExpr, e.getMessage()), e
                );
            }
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Recursively evaluate MVEL expressions in an object (Map, List, String, etc.).
     * Handles nested structures.
     * 
     * @param obj Object to evaluate (can be Map, List, String, or primitive)
     * @param context Execution context for MVEL evaluation
     * @return Object with all MVEL expressions evaluated
     */
    @SuppressWarnings("unchecked")
    public static Object evaluateObject(Object obj, Map<String, Object> context) {
        if (obj == null) {
            return null;
        }
        
        if (obj instanceof String) {
            return evaluateExpression((String) obj, context);
        }
        
        if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;
            Map<String, Object> result = new HashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                result.put(entry.getKey(), evaluateObject(entry.getValue(), context));
            }
            return result;
        }
        
        if (obj instanceof List) {
            List<Object> list = (List<Object>) obj;
            List<Object> result = new ArrayList<>();
            for (Object item : list) {
                result.add(evaluateObject(item, context));
            }
            return result;
        }
        
        // Primitive types or other objects - return as is
        return obj;
    }
}

