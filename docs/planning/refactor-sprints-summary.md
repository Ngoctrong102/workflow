# Refactor Sprints Summary

## 📋 Overview

Các sprint để refactor implementation backend và frontend để align với design mới. Mỗi sprint có mô tả ngắn gọn và references đến design/feature docs cần thiết.

## 🎯 Design Principles

- **Trigger-First Flow**: Tạo trigger config trước → Thêm vào workflow
- **Action-First Flow**: Tạo action definition trước → Thêm vào workflow
- **Trigger Configs**: Độc lập, shareable, không có workflow_id
- **Trigger Instances**: Lưu trong workflow definition node data
- **Lifecycle**: Qua workflow activation/deactivation

## 📚 Core Design References

- `@import(design-questions.md)` - Design decisions
- `@import(clarification-questions.md)` - Clarifications
- `@import(implementation-refactor-plan.md)` - Detailed implementation plan
- `@import(../api/endpoints.md)` - API endpoints design
- `@import(../database-schema/entities.md)` - Database schema
- `@import(../features/trigger-registry.md)` - Trigger registry design
- `@import(../features/workflow-builder.md)` - Workflow builder design

## 🔧 Backend Sprints

### Sprint 28: Database Migration - Trigger Entity Refactoring
**File**: `backend/sprint-28.md`

**Objective**: Update Trigger entity structure - xóa `workflow_id` và `node_id`, thêm `name` field.

**Key Tasks**:
- Database migration script
- Update Trigger entity
- Remove Workflow-Trigger relationship

**Dependencies**: None (foundation)

---

### Sprint 29: TriggerService Refactoring
**File**: `backend/sprint-29.md`

**Objective**: Refactor TriggerService để support trigger config management (independent, shareable).

**Key Tasks**:
- Remove workflow-specific methods
- Add trigger config CRUD methods
- Create new DTOs

**Dependencies**: Sprint 28

---

### Sprint 30: TriggerController Refactoring
**File**: `backend/sprint-30.md`

**Objective**: Refactor TriggerController để expose trigger config management APIs.

**Key Tasks**:
- Remove 11 legacy endpoints
- Add 5 new trigger config endpoints
- Update API documentation

**Dependencies**: Sprint 29

---

### Sprint 31: WorkflowService Lifecycle Management
**File**: `backend/sprint-31.md`

**Objective**: Add trigger instance lifecycle management to WorkflowService.

**Key Tasks**:
- Add workflow trigger methods
- Implement lifecycle in workflow activation/deactivation
- Store runtime state in workflow definition

**Dependencies**: Sprint 30

---

### Sprint 32: TriggerRegistryController Update
**File**: `backend/sprint-32.md`

**Objective**: Update TriggerRegistryController để trả về trigger configs từ database.

**Key Tasks**:
- Update registry endpoints
- Return trigger configs from database
- Support filtering by type

**Dependencies**: Sprint 29

---

### Sprint 33: WorkflowExecutor Node Structure Support
**File**: `backend/sprint-33.md`

**Objective**: Update WorkflowExecutor để support node structure mới với `nodeType` và `nodeConfig`.

**Key Tasks**:
- Support new node structure
- Extract trigger configs from nodes
- Update node execution logic

**Dependencies**: Sprint 31

---

## 🎨 Frontend Sprints

### Sprint 28: Type Definitions Update
**File**: `frontend/sprint-28.md`

**Objective**: Update TypeScript type definitions để support node structure mới.

**Key Tasks**:
- Update WorkflowNode type
- Add TriggerNodeConfig and ActionNodeConfig types
- Update type helpers

**Dependencies**: None (foundation)

---

### Sprint 29: Trigger Service Update
**File**: `frontend/sprint-29.md`

**Objective**: Update trigger service để call new trigger config management APIs.

**Key Tasks**:
- Remove legacy methods
- Add trigger config CRUD methods
- Update hooks

**Dependencies**: Sprint 28

---

### Sprint 30: NodePalette Update
**File**: `frontend/sprint-30.md`

**Objective**: Update NodePalette để support trigger-first flow và new node structure.

**Key Tasks**:
- Load trigger configs from database
- Update node creation
- Support trigger config selection

**Dependencies**: Sprint 28, 29

---

### Sprint 31: PropertiesPanel Update
**File**: `frontend/sprint-31.md`

**Objective**: Update PropertiesPanel để support trigger config selection và instance-specific configuration.

**Key Tasks**:
- Support trigger config selection
- Display instance config fields
- Update form structure

**Dependencies**: Sprint 28, 29

---

### Sprint 32: TriggerDialog and TriggerEditor Update
**File**: `frontend/sprint-32.md`

**Objective**: Update TriggerDialog và TriggerEditor để create/edit trigger configs (independent).

**Key Tasks**:
- Remove workflowId requirement
- Update API calls
- Create unified form

**Dependencies**: Sprint 29

---

### Sprint 33: Workflow Builder Integration
**File**: `frontend/sprint-33.md`

**Objective**: Update workflow builder components để fully support new node structure.

**Key Tasks**:
- Update GuidedWorkflowWizard
- Update WorkflowCanvas
- End-to-end testing

**Dependencies**: Sprint 28-32

---

## 📊 Sprint Dependencies

### Backend Dependencies
```
Sprint 28 (Database Migration)
  ↓
Sprint 29 (TriggerService)
  ↓
Sprint 30 (TriggerController) + Sprint 32 (TriggerRegistryController)
  ↓
Sprint 31 (WorkflowService)
  ↓
Sprint 33 (WorkflowExecutor)
```

### Frontend Dependencies
```
Sprint 28 (Types)
  ↓
Sprint 29 (Service)
  ↓
Sprint 30 (NodePalette) + Sprint 31 (PropertiesPanel) + Sprint 32 (Dialog/Editor)
  ↓
Sprint 33 (Integration)
```

## ⚠️ Breaking Changes Summary

### Backend
- Database: `triggers.workflow_id` và `triggers.node_id` removed
- API: 11 endpoints removed, 5 new endpoints added
- Service: Methods removed/renamed
- Entity: Workflow-Trigger relationship removed

### Frontend
- Types: WorkflowNode structure completely changed
- Service: All trigger service methods changed
- Components: All workflow builder components need update

## ✅ Implementation Checklist

### Backend
- [ ] Sprint 28: Database Migration
- [ ] Sprint 29: TriggerService Refactoring
- [ ] Sprint 30: TriggerController Refactoring
- [ ] Sprint 31: WorkflowService Lifecycle
- [ ] Sprint 32: TriggerRegistryController Update
- [ ] Sprint 33: WorkflowExecutor Update

### Frontend
- [ ] Sprint 28: Type Definitions
- [ ] Sprint 29: Trigger Service
- [ ] Sprint 30: NodePalette
- [ ] Sprint 31: PropertiesPanel
- [ ] Sprint 32: TriggerDialog/Editor
- [ ] Sprint 33: Workflow Builder Integration

## 📝 Notes

- Mỗi sprint có mô tả ngắn gọn và references đến design docs
- Không tự implement, chỉ mô tả yêu cầu
- Experts sẽ đọc design docs và implement theo yêu cầu
- Test sau mỗi sprint để đảm bảo không break existing functionality

---

**Last Updated**: [Date]
**Status**: Ready for Expert Implementation

