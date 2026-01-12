# Backend Sprint 28: Database Migration - Trigger Entity Refactoring

## 📋 Overview

Refactor Trigger entity để align với design mới: xóa `workflow_id` và `node_id`, thêm `name` field. Trigger configs giờ độc lập và có thể share giữa nhiều workflows.

## 🎯 Objectives

- Update Trigger entity structure
- Create database migration script
- Update existing trigger records
- Remove Workflow-Trigger relationship

## 📚 Design References

- `@import(../design-questions.md)` - Design decisions
- `@import(../clarification-questions.md)` - Clarifications
- `@import(../implementation-refactor-plan.md#phase-1-database-schema-changes)` - Detailed plan
- `@import(../../database-schema/entities.md#triggers)` - Target schema
- `@import(../../database-schema/relationships.md)` - Updated relationships

## ✅ Tasks

### 1. Database Migration Script
- [ ] Create migration script to:
  - Remove `workflow_id` column and foreign key constraint
  - Remove `node_id` column
  - Add `name` column (VARCHAR(255) NOT NULL)
  - Update existing records: set `name` from config or generate default
  - Remove indexes on `workflow_id` and `node_id`
  - Add index on `name` if needed

### 2. Update Trigger Entity
- [ ] Remove `@ManyToOne Workflow workflow` field
- [ ] Remove `@Column String nodeId` field
- [ ] Add `@Column String name` field
- [ ] Remove `@OneToMany List<Trigger> triggers` from Workflow entity
- [ ] Update entity annotations and validations

### 3. Update Related Entities
- [ ] Update Workflow entity: remove `@OneToMany List<Trigger> triggers`
- [ ] Verify Execution entity: ensure `trigger_id` foreign key still works
- [ ] Update any other entities that reference Trigger

### 4. Testing
- [ ] Test migration script on test database
- [ ] Verify existing triggers are migrated correctly
- [ ] Verify no data loss
- [ ] Test entity operations (create, read, update, delete)

## 🔗 Related Sprints

- **Sprint 29**: TriggerService Refactoring (depends on this)
- **Sprint 30**: TriggerController Refactoring (depends on Sprint 29)

## ⚠️ Breaking Changes

- Database schema change: `triggers.workflow_id` and `triggers.node_id` removed
- Entity relationship change: Workflow-Trigger relationship removed

## 📝 Notes

- Backup database before migration
- Migration should handle existing data gracefully
- Consider data migration strategy for `name` field (use config data or generate)

