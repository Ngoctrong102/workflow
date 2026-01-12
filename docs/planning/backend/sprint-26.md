# Sprint 26: Action Registry - Config Template Validation

## Goal
Add validation for action config template structure to ensure config fields match expected format for each action type (api-call, publish-event, function).

## Phase
Registry Integration - Backend Validation

## Complexity
Medium

## Dependencies
Sprint 25 (Action Registry CRUD - if exists) or existing Action Registry implementation

## Compliance Check

### Before Starting
1. ✅ Read `@import(features/action-registry.md)` - Understand action registry
2. ✅ Review ApiCallNodeExecutor, PublishEventNodeExecutor, FunctionNodeExecutor
3. ✅ Check existing Action entity and CreateActionRequest/UpdateActionRequest
4. ✅ Understand expected config template structure

## Tasks

### Create Config Template Validator
- [ ] Create `backend/src/main/java/com/notificationplatform/validator/ActionConfigTemplateValidator.java`
- [ ] Implement validation for each action type:
  - **API Call**: Validate url, method, headers, body, authentication, timeout, retry
  - **Publish Event**: Validate kafka config, message
  - **Function**: Validate expression, outputField
- [ ] Add validation annotations or custom validator
- [ ] Return clear error messages for invalid config

### Update CreateActionRequest Validation
- [ ] Add validation for configTemplate structure
- [ ] Validate based on action type:
  - If type is API_CALL: Validate API call config fields
  - If type is PUBLISH_EVENT: Validate publish event config fields
  - If type is FUNCTION: Validate function config fields
- [ ] Ensure inputSchema/outputSchema are preserved
- [ ] Add validation error messages

### Update UpdateActionRequest Validation
- [ ] Add validation for configTemplate structure (if provided)
- [ ] Validate based on action type
- [ ] Allow partial updates (only validate provided fields)

### Config Template Structure Validation

**API Call**:
- [ ] url: Required if type is API_CALL, must be valid URL format
- [ ] method: Required if type is API_CALL, must be one of: GET, POST, PUT, PATCH, DELETE
- [ ] headers: Optional, must be Map<String, String>
- [ ] body: Optional, can be any JSON
- [ ] authentication: Optional, if provided:
  - type: Required, must be "api-key" or "bearer-token"
  - If "api-key": apiKey required
  - If "bearer-token": token required
- [ ] timeout: Optional, must be number > 0
- [ ] retry: Optional, if provided:
  - maxAttempts: Must be number > 0
  - backoffStrategy: Must be string

**Publish Event**:
- [ ] kafka: Required if type is PUBLISH_EVENT
  - brokers: Required, must be List<String>, at least one broker
  - topic: Required, must be non-empty string
  - key: Optional
  - headers: Optional, must be Map<String, String>
- [ ] message: Optional, can be any JSON

**Function**:
- [ ] expression: Required if type is FUNCTION, must be non-empty string
- [ ] outputField: Optional, default: "result"

### Error Handling
- [ ] Return clear validation error messages
- [ ] Include field path in error messages (e.g., "configTemplate.url is required")
- [ ] Support multiple validation errors

## Deliverables

- ✅ ActionConfigTemplateValidator created
- ✅ Config template validation for API Call
- ✅ Config template validation for Publish Event
- ✅ Config template validation for Function
- ✅ Validation integrated with CreateActionRequest
- ✅ Validation integrated with UpdateActionRequest
- ✅ Clear error messages returned

## Technical Details

### Validation Approach

Option 1: Custom Validator
```java
@Component
public class ActionConfigTemplateValidator {
    public ValidationResult validate(ActionType type, Map<String, Object> configTemplate) {
        // Validate based on type
    }
}
```

Option 2: Bean Validation
- Add @Valid annotations
- Create nested validation classes

### Validation Rules

See tasks above for detailed validation rules for each action type.

## Compliance Verification

- [ ] Config template validator works
- [ ] API Call config validation works
- [ ] Publish Event config validation works
- [ ] Function config validation works
- [ ] Error messages are clear
- [ ] Validation integrated with API endpoints
- [ ] No breaking changes to existing code

## Related Documentation

- `@import(features/action-registry.md)` ⚠️ **MUST FOLLOW**
- `docs/planning/frontend/action-config-fields-plan.md`

