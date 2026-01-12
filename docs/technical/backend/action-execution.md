# Action Execution - Backend Implementation

> **Tài liệu này mô tả cách backend execute actions với MVEL evaluation và output mapping.**

## Tổng quan

Khi backend execute một action node, cần:
1. Load action registry để get schemas và outputMapping
2. Build execution context từ previous nodes, trigger, variables
3. Evaluate MVEL expressions trong config values
4. Validate resolved config
5. Execute action → get raw response
6. Build output context với `_response`
7. Apply output mapping với MVEL
8. Validate mapped output
9. Add mapped output vào execution context

## Runtime Execution Flow

### 1. Load Action Registry

```java
// In ActionNodeExecutor
ActionRegistryItem action = actionRegistryService.getById(node.getRegistryId());

// Extract schemas
List<SchemaDefinition> inputSchema = action.getConfigTemplate().getInputSchema();
List<SchemaDefinition> configTemplateSchema = action.getConfigTemplate().getConfigTemplate();
List<SchemaDefinition> outputSchema = action.getConfigTemplate().getOutputSchema();
Map<String, String> outputMapping = action.getConfigTemplate().getOutputMapping();

// Get node config
NodeConfig nodeConfig = node.getConfig();
Map<String, Object> configValues = nodeConfig.getConfigValues();
Map<String, FieldMapping> inputMappings = nodeConfig.getInputMappings();
Map<String, String> customOutputMapping = nodeConfig.getOutputMapping(); // Optional override
```

### 2. Build Execution Context (cho Config Evaluation)

```java
// In ExecutionContextBuilder
public static Map<String, Object> buildContext(ExecutionContext executionContext) {
    Map<String, Object> context = new HashMap<>();
    
    // Add previous node outputs (keyed by nodeId)
    if (executionContext.getNodeOutputs() != null) {
        for (Map.Entry<String, NodeExecutionResult> entry : 
             executionContext.getNodeOutputs().entrySet()) {
            String nodeId = entry.getKey();
            Object nodeOutput = entry.getValue().getOutput();
            context.put(nodeId, nodeOutput);
        }
    }
    
    // Add trigger data (prefixed with _trigger)
    if (executionContext.getTriggerData() != null) {
        context.put("_trigger", executionContext.getTriggerData());
    }
    
    // Add workflow variables (prefixed with _vars)
    if (executionContext.getVariables() != null) {
        context.put("_vars", executionContext.getVariables());
    }
    
    // Add built-in functions
    context.put("_now", (Supplier<Long>) () -> System.currentTimeMillis());
    context.put("_uuid", (Supplier<String>) () -> UUID.randomUUID().toString());
    context.put("_date", (Supplier<String>) () -> LocalDate.now().toString());
    context.put("_timestamp", (Supplier<Long>) () -> System.currentTimeMillis() / 1000);
    
    return context;
}
```

### 3. Evaluate MVEL Expressions trong Config Values

```java
// In MvelEvaluator
public static Object evaluateObject(Object value, Map<String, Object> context) {
    if (value == null) {
        return null;
    }
    
    if (value instanceof String) {
        return evaluateString((String) value, context);
    }
    
    if (value instanceof Map) {
        Map<String, Object> map = (Map<String, Object>) value;
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            result.put(entry.getKey(), evaluateObject(entry.getValue(), context));
        }
        return result;
    }
    
    if (value instanceof List) {
        List<Object> list = (List<Object>) value;
        return list.stream()
            .map(item -> evaluateObject(item, context))
            .collect(Collectors.toList());
    }
    
    return value; // Primitive types, no evaluation needed
}

private static Object evaluateString(String value, Map<String, Object> context) {
    // Pattern: @{expression}
    Pattern pattern = Pattern.compile("@\\{([^}]+)\\}");
    Matcher matcher = pattern.matcher(value);
    
    if (!matcher.find()) {
        return value; // No MVEL expressions
    }
    
    StringBuffer result = new StringBuffer();
    matcher.reset();
    
    while (matcher.find()) {
        String expression = matcher.group(1);
        Object evaluated = MVEL.eval(expression, context);
        matcher.appendReplacement(result, String.valueOf(evaluated));
    }
    matcher.appendTail(result);
    
    return result.toString();
}
```

### 4. Validate Resolved Config

```java
// In ConfigValidator
public static void validateConfig(
    Map<String, Object> config,
    List<SchemaDefinition> configTemplateSchema
) {
    for (SchemaDefinition field : configTemplateSchema) {
        Object value = config.get(field.getName());
        
        // Check required
        if (field.isRequired() && value == null) {
            throw new IllegalArgumentException(
                "Required field missing: " + field.getName()
            );
        }
        
        // Check type
        if (value != null) {
            validateFieldType(field, value);
        }
        
        // Check nested fields
        if (field.getFields() != null && value instanceof Map) {
            validateConfig((Map<String, Object>) value, field.getFields());
        }
    }
}
```

### 5. Execute Action và Get Raw Response

```java
// In ActionNodeExecutor
Map<String, Object> rawResponse;

switch (action.getType()) {
    case "api-call":
        rawResponse = executeApiCall(resolvedConfig);
        // rawResponse = { statusCode: 200, headers: {...}, body: {...} }
        break;
    
    case "publish-event":
        rawResponse = executePublishEvent(resolvedConfig);
        // rawResponse = { success: true, topic: "...", partition: 0, offset: 123 }
        break;
    
    case "function":
        rawResponse = executeFunction(resolvedConfig, context);
        // rawResponse = { result: "...", expression: "..." }
        break;
    
    default:
        rawResponse = executeCustomAction(action.getActionType(), resolvedConfig);
        break;
}
```

### 6. Build Output Context (cho Output Mapping)

```java
// Build output context với _response
Map<String, Object> outputContext = new HashMap<>(context); // Previous context
outputContext.put("_response", rawResponse); // Raw action response
```

### 7. Apply Output Mapping với MVEL

```java
// In OutputMappingApplier
public static Map<String, Object> applyOutputMapping(
    Map<String, String> outputMapping,
    List<SchemaDefinition> outputSchema,
    Map<String, Object> outputContext
) {
    Map<String, Object> mappedOutput = new HashMap<>();
    
    for (SchemaDefinition field : outputSchema) {
        String fieldName = field.getName();
        String mvelExpression = outputMapping.get(fieldName);
        
        if (mvelExpression == null || mvelExpression.isEmpty()) {
            // No mapping defined, skip field
            continue;
        }
        
        try {
            // Evaluate MVEL expression với output context
            Object value = MVEL.eval(mvelExpression, outputContext);
            
            // Handle nested fields
            if (field.getFields() != null && value instanceof Map) {
                // Recursively apply mapping cho nested fields
                Map<String, String> nestedMapping = extractNestedMapping(
                    outputMapping,
                    fieldName
                );
                value = applyOutputMapping(
                    nestedMapping,
                    field.getFields(),
                    outputContext
                );
            }
            
            mappedOutput.put(fieldName, value);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                String.format("Failed to map output field '%s': %s", 
                    fieldName, e.getMessage()), e
            );
        }
    }
    
    return mappedOutput;
}

private static Map<String, String> extractNestedMapping(
    Map<String, String> outputMapping,
    String prefix
) {
    Map<String, String> nested = new HashMap<>();
    String prefixWithDot = prefix + ".";
    
    for (Map.Entry<String, String> entry : outputMapping.entrySet()) {
        if (entry.getKey().startsWith(prefixWithDot)) {
            String nestedKey = entry.getKey().substring(prefixWithDot.length());
            nested.put(nestedKey, entry.getValue());
        }
    }
    
    return nested;
}
```

### 8. Validate Mapped Output

```java
// In ConfigValidator
validateConfig(mappedOutput, outputSchema);
```

### 9. Add to Execution Context

```java
// Add mapped output vào execution context với nodeId
NodeExecutionResult result = NodeExecutionResult.success(mappedOutput);
executionContext.addNodeOutput(node.getId(), result);

// Các node tiếp theo có thể sử dụng: @{nodeId.statusCode}, @{nodeId.body}, etc.
```

## Complete Example: API Call Action

```java
@Override
public NodeExecutionResult execute(
    NodeDefinition node,
    ExecutionContext context
) {
    // 1. Load action registry
    ActionRegistryItem action = actionRegistryService.getById(
        node.getRegistryId()
    );
    
    // 2. Get config values from node config
    Map<String, Object> configValues = node.getConfig().getConfigValues();
    
    // 3. Build execution context (cho config evaluation)
    Map<String, Object> mvelContext = ExecutionContextBuilder.buildContext(context);
    
    // 4. Evaluate MVEL expressions trong config values
    Map<String, Object> resolvedConfig = (Map<String, Object>) 
        MvelEvaluator.evaluateObject(configValues, mvelContext);
    
    // 5. Validate resolved config
    validateConfig(resolvedConfig, action.getConfigTemplate().getConfigTemplate());
    
    // 6. Execute API call → get raw response
    Map<String, Object> rawResponse = executeApiCall(resolvedConfig);
    // rawResponse = { statusCode: 200, headers: {...}, body: {...} }
    
    // 7. Build output context với _response
    Map<String, Object> outputContext = new HashMap<>(mvelContext);
    outputContext.put("_response", rawResponse);
    
    // 8. Get output mapping (từ node config hoặc registry default)
    Map<String, String> outputMapping = node.getConfig().getOutputMapping();
    if (outputMapping == null || outputMapping.isEmpty()) {
        outputMapping = action.getConfigTemplate().getOutputMapping();
    }
    
    // 9. Apply output mapping với MVEL
    Map<String, Object> mappedOutput = OutputMappingApplier.applyOutputMapping(
        outputMapping,
        action.getConfigTemplate().getOutputSchema(),
        outputContext
    );
    
    // 10. Validate mapped output
    validateConfig(mappedOutput, action.getConfigTemplate().getOutputSchema());
    
    // 11. Return mapped output
    return NodeExecutionResult.success(mappedOutput);
}
```

## Utility Classes

### MvelEvaluator

```java
public class MvelEvaluator {
    
    /**
     * Evaluate MVEL expressions trong object (recursive)
     */
    public static Object evaluateObject(Object value, Map<String, Object> context) {
        // Implementation as above
    }
    
    /**
     * Evaluate single MVEL expression
     */
    public static Object evaluateExpression(String expression, Map<String, Object> context) {
        try {
            return MVEL.eval(expression, context);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                String.format("Failed to evaluate MVEL expression '%s': %s", 
                    expression, e.getMessage()), e
            );
        }
    }
}
```

### ExecutionContextBuilder

```java
public class ExecutionContextBuilder {
    
    /**
     * Build MVEL execution context from workflow execution context
     */
    public static Map<String, Object> buildContext(ExecutionContext executionContext) {
        // Implementation as above
    }
    
    /**
     * Build output context với _response
     */
    public static Map<String, Object> buildOutputContext(
        Map<String, Object> baseContext,
        Map<String, Object> rawResponse
    ) {
        Map<String, Object> outputContext = new HashMap<>(baseContext);
        outputContext.put("_response", rawResponse);
        return outputContext;
    }
}
```

### OutputMappingApplier

```java
public class OutputMappingApplier {
    
    /**
     * Apply output mapping với MVEL expressions
     */
    public static Map<String, Object> applyOutputMapping(
        Map<String, String> outputMapping,
        List<SchemaDefinition> outputSchema,
        Map<String, Object> outputContext
    ) {
        // Implementation as above
    }
}
```

### ConfigValidator

```java
public class ConfigValidator {
    
    /**
     * Validate config/output against schema
     */
    public static void validateConfig(
        Map<String, Object> config,
        List<SchemaDefinition> schema
    ) {
        // Implementation as above
    }
}
```

## Error Handling

```java
try {
    // Evaluate MVEL expressions
    Object result = MvelEvaluator.evaluateExpression(expression, context);
} catch (MVELCompilationException e) {
    throw new IllegalArgumentException(
        String.format("Invalid MVEL expression '%s': %s", expression, e.getMessage())
    );
} catch (PropertyAccessException e) {
    throw new IllegalArgumentException(
        String.format("Variable not found in context: %s", e.getMessage())
    );
} catch (Exception e) {
    throw new IllegalArgumentException(
        String.format("Failed to evaluate MVEL expression '%s': %s", expression, e.getMessage())
    );
}
```

## Related Documentation

- [MVEL Expression System](../../features/mvel-expression-system.md) - MVEL syntax và evaluation
- [Action Registry](../../features/action-registry.md) - Action schema structure
- [Action Node Configuration](../frontend/action-node-configuration.md) - Frontend configuration
- [Workflow Context Management](./workflow-context-management.md) - Execution context management
- [Planning: Sprint 27](../../planning/backend/sprint-27.md) - Backend MVEL Evaluation

