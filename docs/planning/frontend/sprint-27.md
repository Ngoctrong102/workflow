# Sprint 27: Trigger Registry Management Pages

## Goal
Create Trigger Registry management pages: List page to view all trigger definitions, and Editor page to create/edit trigger definitions with schema.

## Phase
Registry Integration - Management Pages

## Complexity
Medium

## Dependencies
Sprint 26

## Compliance Check

### Before Starting
1. ✅ Read `@import(features/trigger-registry.md)` - Understand trigger registry structure
2. ✅ Read `@import(features/schema-definition.md)` - Understand schema definition
3. ✅ Verify Sprint 26 is completed (Schema Editor component exists)
4. ✅ Check existing ActionList and ActionEditor pages for reference

## Tasks

### Trigger Registry List Page
- [ ] Create `frontend/src/pages/TriggerRegistryList.tsx`
- [ ] Display list of trigger definitions from registry
- [ ] Features:
  - Search/filter triggers
  - Filter by type (api-call, scheduler, event)
  - Table view with columns: Name, Type, Description, Actions
  - Actions: Edit, Delete, View
  - "Create Trigger Definition" button
- [ ] Use `useTriggerRegistry()` hook
- [ ] Handle delete with confirmation dialog
- [ ] Navigate to editor on create/edit

### Trigger Registry Editor Page
- [ ] Create `frontend/src/pages/TriggerRegistryEditor.tsx`
- [ ] Form to create/edit trigger definition:
  - Basic info section:
    - ID (required, disabled on edit)
    - Name (required)
    - Type (required, select: api-call, scheduler, event)
    - Description (optional)
  - Config template section:
    - Type-specific configuration fields
    - For event trigger: Kafka brokers, topic, consumerGroup, offset
    - For scheduler: Cron expression, timezone, etc.
    - For API: Endpoint path, HTTP method, authentication
  - Schema section (for event triggers):
    - Use SchemaEditor component
    - Allow multiple schemas
    - Schema selector/preview
- [ ] Handle create/update via service methods
- [ ] Form validation
- [ ] Success/error toast notifications
- [ ] Navigate back to list on save/cancel

### Router Updates
- [ ] Update `frontend/src/router/index.tsx`
- [ ] Add routes:
  - `/trigger-registry` → TriggerRegistryList
  - `/trigger-registry/new` → TriggerRegistryEditor
  - `/trigger-registry/:id` → TriggerRegistryEditor (edit mode)

### Navigation Updates
- [ ] Update sidebar navigation (if exists)
- [ ] Add "Trigger Registry" menu item
- [ ] Link to `/trigger-registry`

## Deliverables

- ✅ Trigger Registry List page functional
- ✅ Trigger Registry Editor page functional
- ✅ Create/edit/delete trigger definitions works
- ✅ Schema editor integrated in editor page
- ✅ Routes configured correctly
- ✅ Navigation updated

## Technical Details

### Trigger Registry List Page Structure
```typescript
export default function TriggerRegistryListPage() {
  const { data, isLoading } = useTriggerRegistry()
  // Search, filter, table display
  // Delete with confirmation
  // Navigate to editor
}
```

### Trigger Registry Editor Page Structure
```typescript
export default function TriggerRegistryEditorPage() {
  const { id } = useParams()
  const isEditMode = id !== "new"
  const { data: trigger } = useTriggerRegistryById(id)
  
  // Form with sections:
  // 1. Basic info
  // 2. Config template (type-specific)
  // 3. Schema editor (for event triggers)
}
```

### Form Sections
1. **Basic Info**
   - ID, Name, Type, Description

2. **Config Template** (type-specific)
   - Event: Kafka config (brokers, topic, consumerGroup, offset)
   - Scheduler: Cron, timezone, dates
   - API: Path, method, authentication

3. **Schema** (for event triggers)
   - Use SchemaEditor component
   - Multiple schemas support

## Compliance Verification

- [ ] Verify list page displays all trigger definitions
- [ ] Verify search/filter works correctly
- [ ] Verify create new trigger works
- [ ] Verify edit existing trigger works
- [ ] Verify delete with confirmation works
- [ ] Verify schema editor integrated correctly
- [ ] Verify form validation works
- [ ] Verify routes work correctly
- [ ] Verify navigation updated
- [ ] Verify components follow design system

## Related Documentation

- `@import(features/trigger-registry.md)`
- `@import(features/schema-definition.md)`
- `@import(api/endpoints.md)`

