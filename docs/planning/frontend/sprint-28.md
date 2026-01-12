# Frontend Sprint 28: Type Definitions Update

## 📋 Overview

Update TypeScript type definitions để support node structure mới với `nodeType` và `nodeConfig`. Align với design mới.

## 🎯 Objectives

- Update WorkflowNode type structure
- Add TriggerNodeConfig and ActionNodeConfig types
- Update WorkflowDefinition type
- Ensure type safety

## 📚 Design References

- `@import(../design-questions.md)` - Design decisions
- `@import(../clarification-questions.md#6-workflow-definition-structure)` - Node structure
- `@import(../implementation-refactor-plan.md#phase-6-type-definitions)` - Detailed plan
- `@import(../../features/workflow-builder.md#trigger-nodes)` - Node structure design

## ✅ Tasks

### 1. Update WorkflowNode Type
- [ ] Change structure from:
  ```typescript
  {
    id: string
    type: WorkflowNodeType
    data: { label, config }
  }
  ```
  To:
  ```typescript
  {
    id: string
    nodeType: "trigger" | "action" | "logic" | "data"
    nodeConfig: TriggerNodeConfig | ActionNodeConfig | LogicNodeConfig | DataNodeConfig
    position: { x, y }
  }
  ```

### 2. Add Node Config Types
- [ ] `TriggerNodeConfig`:
  ```typescript
  {
    triggerConfigId: string
    triggerType: "api-call" | "scheduler" | "event"
    instanceConfig?: {
      consumerGroup?: string
      // Other instance-specific overrides
    }
  }
  ```
- [ ] `ActionNodeConfig`:
  ```typescript
  {
    registryId: string
    actionType: string
    config: Record<string, unknown>
  }
  ```
- [ ] `LogicNodeConfig` and `DataNodeConfig` (as needed)

### 3. Update WorkflowDefinition Type
- [ ] Ensure nodes array uses new WorkflowNode structure
- [ ] Keep edges structure unchanged
- [ ] Update any related types

### 4. Update Type Helpers
- [ ] Add type guards for node types
- [ ] Add helper functions to extract configs
- [ ] Update type conversion utilities

### 5. Testing
- [ ] TypeScript compilation check
- [ ] Type safety verification
- [ ] Update existing code to use new types (if needed)

## 🔗 Related Sprints

- **Sprint 29**: Trigger Service Update (depends on this)
- **Sprint 30**: NodePalette Update (depends on this)
- **Sprint 31**: PropertiesPanel Update (depends on this)

## ⚠️ Breaking Changes

- WorkflowNode structure completely changed
- All components using WorkflowNode need update

## 📝 Notes

- Maintain backward compatibility helpers if needed
- Consider migration utilities for existing workflows
- Ensure all node categories are covered
