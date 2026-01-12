# Sprint 27: Backend - MVEL Expression Evaluation

## Mục tiêu

Implement MVEL expression evaluation trong backend:
1. Integrate MVEL 2.x library
2. Create MVEL evaluator utility class
3. Create execution context builder từ previous nodes, trigger, variables
4. Evaluate MVEL expressions trong config values (recursive)
5. Validate resolved config against config template schema
6. Update action executors để sử dụng MVEL evaluation

## Prerequisites

- Backend action executors đã có cơ bản
- ExecutionContext đã có structure để track previous node outputs
- Action registry API trả về configTemplate schema
- NodeConfig DTO cần được update để include configValues

## Dependencies

- **Depends on**: Sprint 39 (Frontend - Config Template Schema), Sprint 40 (Frontend - PropertiesPanel)
- Backend cần nhận configTemplate schema và configValues từ frontend

## Tasks

### Task 1: Add MVEL Dependency

**File**: `backend/pom.xml`

**Changes**:
- Add MVEL 2.x dependency

```xml
<dependency>
    <groupId>org.mvel</groupId>
    <artifactId>mvel2</artifactId>
    <version>2.4.14.Final</version>
</dependency>
```

### Task 2: Create MVEL Evaluator Utility

**File**: `backend/src/main/java/com/notificationplatform/util/MvelEvaluator.java`

**Features**:
- Evaluate MVEL expressions: `@{variable}`, `@{nodeId.field}`, etc.
- Support complex expressions: `@{user.firstName} + ' ' + @{user.lastName}`
- Support built-in functions: `@{_now()}`, `@{_uuid()}`
- Recursively evaluate MVEL expressions trong nested objects và arrays
- Handle errors gracefully với detailed error messages

**Implementation**:
```java
import org.mvel2.MVEL;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MvelEvaluator {
    
    private static final Pattern MVEL_PATTERN = Pattern.compile("@\\{([^}]+)\\}");
    
    /**
     * Evaluate MVEL expression in a string
     * Supports format: @{expression}
     * Example: "/users/@{userId}" -> "/users/123"
     */
    public static Object evaluateExpression(
        String expression,
        Map<String, Object> context
    ) {
        if (expression == null || expression.isEmpty()) {
            return expression;
        }
        
        // Check if expression contains MVEL syntax
        if (!expression.contains("@{")) {
            return expression; // Static value, no MVEL expressions
        }
        
        // Extract và evaluate all MVEL expressions
        Matcher matcher = MVEL_PATTERN.matcher(expression);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String mvelExpr = matcher.group(1);
            try {
                Object value = MVEL.eval(mvelExpr, context);
                String replacement = value != null ? String.valueOf(value) : "";
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            } catch (Exception e) {
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
     * Recursively evaluate MVEL expressions in an object (Map, List, String, etc.)
     * Handles nested structures
     */
    public static Object evaluateObject(
        Object obj,
        Map<String, Object> context
    ) {
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
```

### Task 3: Create Execution Context Builder

**File**: `backend/src/main/java/com/notificationplatform/service/workflow/ExecutionContextBuilder.java`

**Features**:
- Build execution context từ previous nodes, trigger data, variables
- Structure context for MVEL evaluation
- Add built-in functions (_now, _uuid, etc.)

**Implementation**:
```java
import java.util.Map;
import java.util.HashMap;
import java.util.function.Supplier;
import java.util.UUID;

public class ExecutionContextBuilder {
    
    /**
     * Build MVEL execution context from workflow execution context
     * 
     * Context structure:
     * {
     *   "nodeId": { "field": value },  // Previous node outputs
     *   "_trigger": { "field": value }, // Trigger data
     *   "_vars": { "varName": value },  // Workflow variables
     *   "_now": () -> timestamp,        // Built-in function
     *   "_uuid": () -> uuid             // Built-in function
     * }
     */
    public static Map<String, Object> buildContext(
        ExecutionContext executionContext
    ) {
        Map<String, Object> context = new HashMap<>();
        
        // Add previous node outputs (keyed by nodeId)
        // Example: { "fetchUser": { "userId": "123", "name": "John" } }
        if (executionContext.getNodeOutputs() != null) {
            for (Map.Entry<String, NodeExecutionResult> entry : 
                 executionContext.getNodeOutputs().entrySet()) {
                String nodeId = entry.getKey();
                Object nodeOutput = entry.getValue().getOutput();
                context.put(nodeId, nodeOutput);
            }
        }
        
        // Add trigger data (prefixed with _trigger)
        // Example: { "_trigger": { "userId": "123", "eventType": "user.created" } }
        if (executionContext.getTriggerData() != null) {
            context.put("_trigger", executionContext.getTriggerData());
        }
        
        // Add workflow variables (prefixed with _vars)
        // Example: { "_vars": { "apiKey": "secret", "baseUrl": "https://api.com" } }
        if (executionContext.getVariables() != null) {
            context.put("_vars", executionContext.getVariables());
        }
        
        // Add built-in functions
        // Note: MVEL will call these as functions: @{_now()}, @{_uuid()}
        context.put("_now", (Supplier<Long>) () -> System.currentTimeMillis());
        context.put("_uuid", (Supplier<String>) () -> UUID.randomUUID().toString());
        
        return context;
    }
}
```

### Task 4: Update Action Executors

**Files**:
- `backend/src/main/java/com/notificationplatform/service/workflow/executor/ApiCallNodeExecutor.java`
- `backend/src/main/java/com/notificationplatform/service/workflow/executor/PublishEventNodeExecutor.java`
- `backend/src/main/java/com/notificationplatform/service/workflow/executor/FunctionNodeExecutor.java`
- `backend/src/main/java/com/notificationplatform/service/workflow/executor/EmailNodeExecutor.java` (nếu có)
- `backend/src/main/java/com/notificationplatform/service/workflow/executor/ConditionNodeExecutor.java` (nếu có)

**Changes**:
1. Load config template schema, outputSchema, và outputMapping từ action registry
2. Get config values/templates từ node config
3. Build execution context
4. Evaluate MVEL expressions trong config values
5. Validate resolved config against config template schema
6. Execute action với resolved config → get raw response
7. Build output context với `_response` chứa raw response [NEW]
8. Evaluate output mapping expressions với output context [NEW]
9. Map kết quả vào output schema structure [NEW]
10. Validate mapped output against output schema [NEW]
11. Add mapped output vào execution context với nodeId [NEW]

**Note**: MvelEvaluator là utility class reusable, có thể được sử dụng bởi tất cả executors.

**Example for ApiCallNodeExecutor**:
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
    
    // 3. Build execution context
    Map<String, Object> mvelContext = ExecutionContextBuilder.buildContext(context);
    
    // 4. Evaluate MVEL expressions
    Map<String, Object> resolvedConfig = (Map<String, Object>) 
        MvelEvaluator.evaluateObject(configValues, mvelContext);
    
    // 5. Validate resolved config
    validateApiCallConfig(resolvedConfig, action.getConfigTemplate().getConfigTemplate());
    
    // 6. Execute API call
    return executeApiCall(resolvedConfig);
}
```

### Task 5: Update NodeConfig DTO

**File**: `backend/src/main/java/com/notificationplatform/dto/NodeConfig.java`

**Changes**:
- Add `configValues` field (Map<String, Object>)
- Add `inputMappings` field (optional, nếu có inputSchema)
- Add `outputMapping` field (optional - custom output mapping, override registry default)
- Remove old config fields (url, method, etc.) - sẽ được resolve từ configValues

**Structure**:
```java
public class NodeConfig {
    private Map<String, Object> configValues; // Config values với MVEL expressions
    private Map<String, FieldMapping> inputMappings; // Optional: input data mappings
    private Map<String, String> outputMapping; // Optional: custom output mapping (override registry)
    // ... other fields
}
```

### Task 6: Create Output Mapping Applier

**File**: `backend/src/main/java/com/notificationplatform/util/OutputMappingApplier.java`

**Features**:
- Apply output mapping với MVEL expressions
- Evaluate MVEL expressions với output context (có `_response`)
- Map kết quả vào output schema structure
- Handle nested fields và arrays

**Implementation**:
```java
public class OutputMappingApplier {
    
    /**
     * Apply output mapping với MVEL expressions
     * 
     * @param outputMapping MVEL expressions cho mỗi field trong output schema
     * @param outputSchema Output schema structure
     * @param outputContext Context với _response chứa raw action result
     * @return Mapped output theo output schema structure
     */
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
                Object value = MvelEvaluator.evaluateExpression(
                    mvelExpression,
                    outputContext
                );
                
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
        // Extract nested mapping với prefix
        // Example: prefix = "body", extract "body.userId", "body.userName", etc.
        // ...
    }
}
```

### Task 7: Create Config Validator

**File**: `backend/src/main/java/com/notificationplatform/util/ConfigValidator.java`

**Features**:
- Validate resolved config against config template schema
- Validate mapped output against output schema
- Check required fields
- Check field types
- Check enum values
- Check validation rules (pattern, min, max, etc.)

**Implementation**:
```java
public class ConfigValidator {
    
    public static void validateConfig(
        Map<String, Object> config,
        List<SchemaDefinition> schema
    ) {
        for (SchemaDefinition field : schema) {
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
    
    private static void validateFieldType(
        SchemaDefinition field,
        Object value
    ) {
        // Type validation logic
        // ...
    }
}
```

## Testing Checklist

- [ ] MVEL dependency added correctly
- [ ] MvelEvaluator evaluates simple expressions: `@{variable}`
- [ ] MvelEvaluator evaluates nested expressions: `@{nodeId.field}`
- [ ] MvelEvaluator evaluates complex expressions: `@{user.firstName} + ' ' + @{user.lastName}`
- [ ] MvelEvaluator evaluates built-in functions: `@{_now()}`, `@{_uuid()}`
- [ ] MvelEvaluator evaluates response expressions: `@{_response.statusCode}`, `@{_response.body.field}`
- [ ] ExecutionContextBuilder builds context correctly
- [ ] Config values với MVEL expressions evaluated correctly
- [ ] ConfigValidator validates resolved config
- [ ] OutputMappingApplier applies output mapping correctly
- [ ] Output mapping với nested fields works
- [ ] Output mapping với complex expressions works
- [ ] Mapped output validated against output schema
- [ ] API Call executor works với resolved config và output mapping
- [ ] Publish Event executor works với resolved config và output mapping
- [ ] Function executor works với resolved config và output mapping
- [ ] Mapped output added to execution context correctly
- [ ] Next nodes can reference mapped output: `@{nodeId.field}`
- [ ] Error handling for invalid MVEL expressions
- [ ] Error handling for missing context variables
- [ ] Error handling for invalid output mapping

## Notes

- **MVEL evaluation** happens at runtime, trước khi execute action/function/condition
- **MVEL được sử dụng cho TẤT CẢ dynamic logic và placeholders**:
  - Action config values
  - Email/notification templates
  - Function node expressions
  - Condition expressions
  - Field mappings
  - Any dynamic values
- **Config template schema** stored in action registry (defined in ActionEditor)
- **Config values** (với MVEL expressions) stored in `node.data.config.configValues` (from PropertiesPanel)
- **Final values** = evaluated values với execution context
- **MVEL format**: `@{expression}` (not `${expression}`)
- **Error handling**: Nếu MVEL evaluation fails, throw exception với detailed error message
- **Performance**: Cache compiled MVEL expressions nếu cần (optional optimization)
- **Reusability**: MvelEvaluator là utility class có thể được sử dụng bởi tất cả executors/processors

## Acceptance Criteria

- [ ] MVEL dependency added và compile successfully
- [ ] MvelEvaluator evaluates simple expressions: `@{variable}`
- [ ] MvelEvaluator evaluates nested expressions: `@{nodeId.field}`
- [ ] MvelEvaluator evaluates complex expressions: `@{user.firstName} + ' ' + @{user.lastName}`
- [ ] MvelEvaluator evaluates built-in functions: `@{_now()}`, `@{_uuid()}`
- [ ] MvelEvaluator recursively evaluates nested objects và arrays
- [ ] ExecutionContextBuilder builds context correctly với all sources
- [ ] Config values với MVEL expressions evaluated correctly
- [ ] ConfigValidator validates resolved config against schema
- [ ] All action executors (API Call, Publish Event, Function) work với resolved config
- [ ] Error handling for invalid MVEL expressions (detailed error messages)
- [ ] Error handling for missing context variables (clear error messages)
- [ ] NodeConfig DTO updated với configValues field
