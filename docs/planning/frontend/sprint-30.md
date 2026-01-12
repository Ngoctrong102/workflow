# Frontend Sprint 30: NodePalette Update

## 📋 Overview

Update NodePalette component để support trigger-first flow. Load trigger configs từ database và tạo nodes với structure mới.

## 🎯 Objectives

- Load trigger configs from database (via registry endpoint)
- Update node creation to use new structure
- Support trigger config selection
- Update node drag and drop

## 📚 Design References

- `@import(../design-questions.md#q71)` - User experience flow
- `@import(../implementation-refactor-plan.md#phase-7-component-updates)` - Detailed plan
- `@import(../../features/workflow-builder.md#trigger-selection-process)` - Trigger selection design
- `@import(../../user-flows/workflow-creation.md)` - User flow

## ✅ Tasks

### 1. Update Trigger Loading
- [ ] Load trigger configs from `GET /triggers/registry` (database)
- [ ] Map trigger configs to node definitions
- [ ] Support filtering by trigger type
- [ ] Display trigger configs in palette

### 2. Update Node Creation
- [ ] When dragging trigger node, create node with new structure:
  ```typescript
  {
    id: "node-1",
    nodeType: "trigger",
    nodeConfig: {
      triggerConfigId: "trigger-config-123",
      triggerType: "event",
      instanceConfig: {}
    }
  }
  ```
- [ ] Store `triggerConfigId` instead of creating trigger immediately
- [ ] Allow user to select trigger config or create new one

### 3. Update Node Display
- [ ] Display trigger config name in node
- [ ] Show trigger type icon
- [ ] Update node styling if needed

### 4. Support Action Nodes
- [ ] Keep action node loading from registry
- [ ] Ensure action nodes use new structure
- [ ] Update action node creation

### 5. Testing
- [ ] Test node creation with new structure
- [ ] Test trigger config selection
- [ ] Test node drag and drop
- [ ] Verify node data structure

## 🔗 Related Sprints

- **Sprint 28**: Type Definitions Update (prerequisite)
- **Sprint 29**: Trigger Service Update (prerequisite)
- **Sprint 31**: PropertiesPanel Update (related)

## ⚠️ Breaking Changes

- Node creation structure changed
- Trigger loading from database instead of hardcoded

## 📝 Notes

- Trigger configs are now user-created, not hardcoded
- Support creating new trigger config from palette if needed
- Consider showing trigger config details in tooltip
