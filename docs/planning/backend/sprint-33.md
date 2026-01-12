# Backend Sprint 33: WorkflowExecutor Node Structure Support

## 📋 Overview

Update WorkflowExecutor để support node structure mới với `nodeType` và `nodeConfig`. Extract trigger configs từ trigger nodes.

## 🎯 Objectives

- Support new node structure in workflow definition
- Extract trigger configs from trigger nodes
- Support both old and new node structures (backward compatibility)
- Update node execution logic

## 📚 Design References

- `@import(../design-questions.md)` - Design decisions
- `@import(../clarification-questions.md#6-workflow-definition-structure)` - Node structure
- `@import(../implementation-refactor-plan.md#phase-5-workflow-definition-structure)` - Detailed plan
- `@import(../../features/workflow-builder.md#trigger-nodes)` - Node structure design

## ✅ Tasks

### 1. Update Node Structure Parsing
- [ ] Support new node structure:
  ```json
  {
    "id": "node-1",
    "nodeType": "trigger",
    "nodeConfig": {
      "triggerConfigId": "trigger-config-123",
      "triggerType": "event",
      "instanceConfig": {...}
    }
  }
  ```
- [ ] Support backward compatibility with old structure (if needed)
- [ ] Extract `triggerConfigId` from trigger nodes
- [ ] Extract `instanceConfig` from trigger nodes

### 2. Update Trigger Data Extraction
- [ ] Load trigger config from database using `triggerConfigId`
- [ ] Merge trigger config with `instanceConfig` overrides
- [ ] Pass merged config to trigger executors

### 3. Update Action Node Processing
- [ ] Support new action node structure:
  ```json
  {
    "nodeType": "action",
    "nodeConfig": {
      "registryId": "send-email-action",
      "actionType": "custom-action",
      "config": {...}
    }
  }
  ```
- [ ] Extract `registryId` from action nodes
- [ ] Load action definition from registry if needed

### 4. Update Node Execution Logic
- [ ] Update node type detection (use `nodeType` field)
- [ ] Update config extraction (use `nodeConfig` field)
- [ ] Ensure backward compatibility

### 5. Testing
- [ ] Unit tests for node structure parsing
- [ ] Integration tests for workflow execution
- [ ] Test with both old and new node structures

## 🔗 Related Sprints

- **Sprint 31**: WorkflowService Lifecycle Management (prerequisite)
- **Sprint 28-32**: All previous sprints (foundation)

## ⚠️ Breaking Changes

- Node structure change in workflow definition
- May need migration for existing workflows

## 📝 Notes

- Support both old and new structures for backward compatibility
- Consider migration utility for existing workflows
- Ensure all node types are supported (trigger, action, logic, data)

