# Frontend Sprint 32: TriggerDialog and TriggerEditor Update

## 📋 Overview

Update TriggerDialog and TriggerEditor components để create/edit trigger configs (independent, không cần workflowId).

## 🎯 Objectives

- Refactor to create/edit trigger configs (not workflow-specific)
- Remove workflowId requirement
- Update API calls to new endpoints
- Support all trigger types in unified form

## 📚 Design References

- `@import(../implementation-refactor-plan.md#phase-7-component-updates)` - Detailed plan
- `@import(../../api/endpoints.md#trigger-configs)` - API endpoints
- `@import(../../features/triggers.md)` - Trigger configuration design

## ✅ Tasks

### 1. Update TriggerDialog Component
- [ ] Remove workflowId prop requirement
- [ ] Update to create trigger config (not workflow-specific trigger)
- [ ] Update API call: `POST /triggers` instead of `POST /triggers/api`, etc.
- [ ] Support all trigger types in single form
- [ ] Update form validation
- [ ] Update success handling (navigate to trigger config list or return config)

### 2. Update TriggerEditor Component
- [ ] Remove workflowId requirement
- [ ] Update to edit trigger config (not workflow-specific trigger)
- [ ] Update API call: `PUT /triggers/{id}` instead of workflow-specific endpoint
- [ ] Load trigger config by ID
- [ ] Update form with trigger config data
- [ ] Update save logic

### 3. Create Unified Trigger Config Form
- [ ] Support all trigger types: api-call, scheduler, event
- [ ] Type-specific fields based on selected trigger type
- [ ] Common fields: name, triggerType, status
- [ ] Type-specific config fields

### 4. Update Navigation
- [ ] Navigate to trigger config list after create/edit
- [ ] Or return trigger config for selection in workflow builder
- [ ] Update routing if needed

### 5. Testing
- [ ] Test trigger config creation
- [ ] Test trigger config editing
- [ ] Test form validation
- [ ] Test navigation

## 🔗 Related Sprints

- **Sprint 29**: Trigger Service Update (prerequisite)
- **Sprint 31**: PropertiesPanel Update (related)

## ⚠️ Breaking Changes

- Components no longer require workflowId
- API endpoints changed
- Form structure changed

## 📝 Notes

- Trigger configs are now independent
- Can be created from trigger management page
- Can be selected in workflow builder
