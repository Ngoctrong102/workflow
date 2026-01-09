# Sprint 06: Workflow Builder - Connections & Properties

## Goal
Implement node connections (edges) and properties panel for node configuration, ensuring compliance with workflow builder specifications.

## Phase
Core Features

## Complexity
Complex

## Dependencies
Sprint 05

## Compliance Check

### Before Starting
1. ✅ Read `@import(features/workflow-builder.md)` - Understand workflow builder
2. ✅ Read `@import(features/schema-definition.md)` - Understand schema-based configuration
3. ✅ Verify Sprint 05 is completed

## Tasks

### Edge Connections
- [ ] Implement edge creation between nodes
- [ ] Create `Edge` components for react-flow
- [ ] Validate edge connections:
  - [ ] Only one trigger node allowed
  - [ ] Logic nodes can have multiple inputs/outputs
  - [ ] Action nodes can have single input
- [ ] Implement edge deletion
- [ ] Store edges in state

### Properties Panel
- [ ] Create `PropertiesPanel.tsx` component:
  - [ ] Show when node is selected
  - [ ] Display node type and name
  - [ ] Display configuration form
- [ ] Implement schema-based configuration:
  - [ ] Load schema from registry based on `registryId`
  - [ ] Render form fields from schema
  - [ ] Implement field source selection (dropdown):
    - [ ] From Previous Node → Select node → Select field
    - [ ] From Trigger Data → Select field
    - [ ] From Variables → Select variable
    - [ ] Static Value → Enter value
- [ ] Save field mappings to node config

### Node Configuration Forms
- [ ] Create configuration forms for each node type:
  - [ ] Trigger configuration (API, Schedule, Event)
  - [ ] Logic configuration (Condition, Switch, Loop, Delay, Wait for Events)
  - [ ] Action configuration (API Call, Publish Event, Function, Custom Actions)
- [ ] Implement form validation
- [ ] Implement field type validation

### Workflow Definition
- [ ] Create workflow definition structure:
  - [ ] Nodes array with positions and configs
  - [ ] Edges array with source/target
- [ ] Implement save workflow definition
- [ ] Implement load workflow definition

## Deliverables

- ✅ Edge connections working
- ✅ Properties panel implemented
- ✅ Schema-based configuration working
- ✅ Workflow definition structure complete

## Technical Details

### Schema-Based Configuration
See `@import(features/schema-definition.md)` for schema-based configuration flow.

### Workflow Definition
See `@import(features/workflow-builder.md)` for workflow definition structure.

## Compliance Verification

- [ ] Verify connections match specifications
- [ ] Verify properties panel matches `@import(features/workflow-builder.md)`
- [ ] Test schema-based configuration

## Related Documentation

- `@import(features/workflow-builder.md)` ⚠️ **MUST MATCH**
- `@import(features/schema-definition.md)` ⚠️ **MUST MATCH**

