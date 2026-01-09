# Sprint 17: Exception Handling & Validation

## Goal
Implement global exception handling and request validation, ensuring consistent error responses.

## Phase
API Layer

## Complexity
Simple

## Dependencies
Sprint 15, Sprint 16

## Compliance Check

### Before Starting
1. ✅ Read `@import(api/error-handling.md)` - Understand error handling
2. ✅ Read `@import(api/schemas.md)` - Understand request/response schemas
3. ✅ Verify Sprint 15 and 16 are completed

## Tasks

### Analytics Controller
- [ ] Create `AnalyticsController.java`:
  - `GET /analytics/workflows/{workflowId}` - Get workflow analytics
  - `GET /analytics/deliveries` - Get delivery analytics
  - `GET /analytics/channels` - Get channel analytics
  - **MUST MATCH**: `@import(api/endpoints.md#analytics)`

### Global Exception Handler
- [ ] Create `GlobalExceptionHandler.java` with `@ControllerAdvice`:
  - Handle `ResourceNotFoundException` → 404
  - Handle `ValidationException` → 400
  - Handle `IllegalArgumentException` → 400
  - Handle `RuntimeException` → 500
  - Return standardized error response format
  - **MUST MATCH**: `@import(api/error-handling.md)`

### Error Response DTO
- [ ] Create `ErrorResponse.java`:
  - `error.code`, `error.message`, `error.details`, `error.request_id`
  - Match error response format from `@import(api/error-handling.md)` ⚠️ **MUST MATCH**

### Custom Exceptions
- [ ] Create `ResourceNotFoundException.java`
- [ ] Create `ValidationException.java`
- [ ] Create `WorkflowValidationException.java`
- [ ] Create `ExecutionException.java`

### Request Validation
- [ ] Add `@Valid` annotations to all request DTOs
- [ ] Create custom validators:
  - `WorkflowDefinitionValidator` - Validate workflow definition
  - `CronExpressionValidator` - Validate cron expressions

### Validation Error Handling
- [ ] Handle `MethodArgumentNotValidException` from `@Valid`
- [ ] Format validation errors consistently

## Deliverables

- ✅ Analytics API controller implemented
- ✅ Global exception handling working
- ✅ Standardized error responses
- ✅ Request validation working
- ✅ Custom validators implemented

## Technical Details

### Error Response Format
- **Error Handling**: `@import(api/error-handling.md)` ⚠️ **MUST MATCH**
- **Error Codes**: Use error codes from `@import(api/error-handling.md#error-codes)`

## Compliance Verification

- [ ] Verify error responses match `@import(api/error-handling.md)`
- [ ] Test exception handling for all error types
- [ ] Test validation error responses

## Related Documentation

- `@import(api/endpoints.md#analytics)` ⚠️ **MUST MATCH**
- `@import(api/error-handling.md)` ⚠️ **MUST MATCH**
- `@import(api/schemas.md)` ⚠️ **MUST MATCH**

