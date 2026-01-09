# Sprint 05: Workflow Builder - Canvas & Nodes

## Goal
Implement workflow builder canvas with draggable nodes, ensuring compliance with workflow builder specifications.

## Phase
Core Features

## Complexity
Complex

## Dependencies
Sprint 02, Sprint 03

## Compliance Check

### Before Starting
1. ✅ Read `@import(features/workflow-builder.md)` - Understand workflow builder
2. ✅ Read `@import(features/trigger-registry.md)` - Understand trigger registry
3. ✅ Read `@import(features/action-registry.md)` - Understand action registry
4. ✅ Read `@import(features/node-types.md)` - Understand node types
5. ✅ Verify Sprint 02 and 03 are completed

## Tasks

### Canvas Setup
- [ ] Install and configure react-flow or similar library
- [ ] Create `WorkflowCanvas.tsx` component:
  - [ ] Canvas with zoom/pan controls
  - [ ] Grid background
  - [ ] Minimap (optional)
  - [ ] Controls toolbar
- [ ] Setup canvas state management

### Node Palette
- [ ] Create `NodePalette.tsx` component:
  - [ ] Trigger nodes section
  - [ ] Logic nodes section
  - [ ] Action nodes section
- [ ] Fetch triggers from registry: `GET /triggers/registry`
- [ ] Fetch actions from registry: `GET /actions/registry`
- [ ] Display nodes with icons and names
- [ ] Make nodes draggable

### Node Rendering
- [ ] Create node components:
  - [ ] `TriggerNode.tsx` - Trigger node
  - [ ] `LogicNode.tsx` - Logic node
  - [ ] `ActionNode.tsx` - Action node
- [ ] Implement node types:
  - [ ] Trigger nodes (API, Schedule, Event)
  - [ ] Logic nodes (Condition, Switch, Loop, Delay, Wait for Events, Merge)
  - [ ] Action nodes (API Call, Publish Event, Function, Custom Actions)
- [ ] Add node styling based on type
- [ ] Add node labels and icons

### Node Management
- [ ] Implement add node to canvas
- [ ] Implement delete node
- [ ] Implement node selection
- [ ] Implement node position updates
- [ ] Store nodes in state

## Deliverables

- ✅ Canvas implemented with zoom/pan
- ✅ Node palette working
- ✅ Nodes can be added to canvas
- ✅ Nodes can be selected and moved

## Technical Details

### Workflow Builder Structure
See `@import(features/workflow-builder.md)` for builder structure.

### Node Types
See `@import(features/node-types.md)` for node specifications.

### Registry APIs
- **Trigger Registry**: `@import(api/endpoints.md#trigger-registry)`
- **Action Registry**: `@import(api/endpoints.md#action-registry)`

## Compliance Verification

- [ ] Verify canvas matches `@import(features/workflow-builder.md)`
- [ ] Test node palette
- [ ] Test node dragging and placement

## Related Documentation

- `@import(features/workflow-builder.md)` ⚠️ **MUST MATCH**
- `@import(features/node-types.md)` ⚠️ **MUST MATCH**
- `@import(features/trigger-registry.md)`
- `@import(features/action-registry.md)`

