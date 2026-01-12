# Frontend Sprint 33: Workflow Builder Integration

## 📋 Overview

Update workflow builder components để fully support new node structure và trigger-first flow. Ensure all components work together.

## 🎯 Objectives

- Update GuidedWorkflowWizard for new structure
- Update WorkflowCanvas node rendering
- Update WorkflowNode component
- Ensure end-to-end flow works

## 📚 Design References

- `@import(../implementation-refactor-plan.md#phase-7-component-updates)` - Detailed plan
- `@import(../../features/workflow-builder.md)` - Workflow builder design
- `@import(../../user-flows/workflow-creation.md)` - User flow

## ✅ Tasks

### 1. Update GuidedWorkflowWizard
- [ ] Update trigger node creation to use new structure
- [ ] Link trigger config instead of creating trigger with workflowId
- [ ] Update action node creation
- [ ] Update workflow definition building

### 2. Update WorkflowCanvas
- [ ] Support rendering nodes with new structure
- [ ] Update node selection logic
- [ ] Update node connection logic
- [ ] Ensure backward compatibility if needed

### 3. Update WorkflowNode Component
- [ ] Support new node structure
- [ ] Display nodeType and nodeConfig correctly
- [ ] Update node styling if needed
- [ ] Support all node types (trigger, action, logic, data)

### 4. Update Workflow Save Logic
- [ ] Ensure workflow definition uses new node structure
- [ ] Validate node structure before save
- [ ] Handle errors appropriately

### 5. End-to-End Testing
- [ ] Test complete workflow creation flow
- [ ] Test trigger config selection
- [ ] Test instance config editing
- [ ] Test workflow save and activation
- [ ] Test workflow execution

## 🔗 Related Sprints

- **Sprint 28-32**: All previous sprints (prerequisites)
- **Backend Sprint 33**: WorkflowExecutor Node Structure Support (related)

## ⚠️ Breaking Changes

- Workflow definition structure changed
- All workflow builder components need update

## 📝 Notes

- Ensure all components work together
- Test complete user flow
- Consider migration for existing workflows
- Support backward compatibility if needed
