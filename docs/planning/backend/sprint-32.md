# Backend Sprint 32: TriggerRegistryController Update

## 📋 Overview

Update TriggerRegistryController để trả về trigger configs từ database thay vì hardcoded trigger definitions.

## 🎯 Objectives

- Update registry endpoint to return trigger configs from database
- Maintain backward compatibility if needed
- Support filtering by trigger type

## 📚 Design References

- `@import(../design-questions.md#q51)` - Registry endpoint design
- `@import(../clarification-questions.md#2-trigger-registry-endpoint)` - Clarifications
- `@import(../implementation-refactor-plan.md#phase-2-api-endpoints-refactoring)` - Detailed plan
- `@import(../../api/endpoints.md#trigger-registry)` - API endpoint design
- `@import(../../features/trigger-registry.md)` - Trigger registry design

## ✅ Tasks

### 1. Update TriggerRegistryController
- [ ] Update `GET /triggers/registry`:
  - Return trigger configs from database (via TriggerService)
  - Format response to match expected structure
  - Support filtering by trigger type if needed
- [ ] Update `GET /triggers/registry/{id}`:
  - Get trigger config by ID from database
- [ ] Update `GET /triggers/registry/type/{type}`:
  - Filter trigger configs by type from database

### 2. Update TriggerRegistryService (if needed)
- [ ] Review if service needs update
- [ ] Consider keeping hardcoded types for reference (optional)
- [ ] Or remove if not needed

### 3. Response Format
- [ ] Ensure response format matches frontend expectations
- [ ] Include all required fields: id, name, type, config, status, metadata
- [ ] Maintain compatibility with existing frontend code

### 4. Testing
- [ ] Unit tests for updated endpoints
- [ ] Integration tests
- [ ] Verify response format

## 🔗 Related Sprints

- **Sprint 29**: TriggerService Refactoring (prerequisite)
- **Sprint 30**: TriggerController Refactoring (related)

## ⚠️ Breaking Changes

- Response now returns trigger configs from database instead of hardcoded definitions
- May need frontend update (see Frontend Sprint 28)

## 📝 Notes

- Trigger types (api-call, scheduler, event) are still hardcoded in code
- Registry returns user-created trigger configs for each type
- Consider showing both types and configs if needed for UI
