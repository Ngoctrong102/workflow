# Frontend Sprint 31: PropertiesPanel Update

## 📋 Overview

Update PropertiesPanel component để support trigger-first flow và new node structure. Support trigger config selection và instance-specific configuration.

## 🎯 Objectives

- Support trigger config selection in PropertiesPanel
- Display trigger config info and instance config fields
- Support instance-specific overrides (e.g., consumerGroup)
- Update form to save to new node structure

## 📚 Design References

- `@import(../design-questions.md#q72)` - Configuration flow
- `@import(../clarification-questions.md#4-trigger-instance-fields)` - Override fields
- `@import(../implementation-refactor-plan.md#phase-7-component-updates)` - Detailed plan
- `@import(../../features/workflow-builder.md#trigger-nodes)` - Trigger node design
- `@import(../../features/triggers.md#trigger-instance-management)` - Instance management

## ✅ Tasks

### 1. Update Trigger Node Configuration
- [ ] If trigger node doesn't have `triggerConfigId`:
  - Show trigger config selector
  - Allow user to select existing trigger config or create new
  - Load trigger configs from registry
- [ ] If trigger node has `triggerConfigId`:
  - Display trigger config information (name, type, config)
  - Show instance-specific config fields
  - Allow editing instance config only

### 2. Support Instance-Specific Fields
- [ ] Display fields that can be overridden at instance level
- [ ] Currently: `consumerGroup` for Event triggers
- [ ] Support schema-based field definition (for future extensibility)
- [ ] Validate instance config fields

### 3. Update Form Structure
- [ ] Save to `nodeConfig` structure:
  ```typescript
  {
    triggerConfigId: "trigger-config-123",
    triggerType: "event",
    instanceConfig: {
      consumerGroup: "workflow-456-consumer"
    }
  }
  ```
- [ ] Update form state management
- [ ] Update validation logic

### 4. Support Action Node Configuration
- [ ] Ensure action nodes use new structure
- [ ] Display action definition info
- [ ] Allow editing action config

### 5. Update Form Submission
- [ ] Save node config to workflow definition
- [ ] Don't create trigger in database (trigger config already exists)
- [ ] Update workflow definition on save

### 6. Testing
- [ ] Test trigger config selection
- [ ] Test instance config editing
- [ ] Test form validation
- [ ] Test save functionality

## 🔗 Related Sprints

- **Sprint 28**: Type Definitions Update (prerequisite)
- **Sprint 29**: Trigger Service Update (prerequisite)
- **Sprint 30**: NodePalette Update (related)

## ⚠️ Breaking Changes

- Form structure changed
- Save logic changed (no longer creates trigger in database)

## 📝 Notes

- Trigger config is selected/created separately
- Instance config is workflow-specific
- Support schema-based field rendering for extensibility
