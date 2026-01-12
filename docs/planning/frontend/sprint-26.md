# Sprint 26: Trigger/Action Registry Integration - Foundation

## Goal
Create foundation components and services for Trigger/Action Registry integration: Schema Editor component, hooks, and service methods.

## Phase
Registry Integration - Foundation

## Complexity
Medium

## Dependencies
None (can work independently)

## Compliance Check

### Before Starting
1. ✅ Read `@import(features/trigger-registry.md)` - Understand trigger registry system
2. ✅ Read `@import(features/action-registry.md)` - Understand action registry system
3. ✅ Read `@import(features/schema-definition.md)` - Understand schema definition system
4. ✅ Verify existing trigger/action services exist

## Tasks

### Schema Editor Component
- [ ] Create `frontend/src/components/registry/SchemaEditor.tsx`
- [ ] Implement schema field management:
  - Add/remove/edit fields
  - Field properties: name, type, required, description
  - Support field types: string, number, boolean, date, datetime, email, phone, url, json, array, object
  - Support nested objects/arrays
  - Field validation UI
- [ ] Implement schema preview
- [ ] Support multiple schemas (for event triggers)
- [ ] UI should be user-friendly (not JSON editor)

### Trigger Registry Hook
- [ ] Update `frontend/src/hooks/use-trigger-registry.ts`
- [ ] Add `useTriggerRegistryById(id: string | undefined)` hook
- [ ] Similar to `useActionRegistryById` pattern
- [ ] Use `triggerService.getRegistryById()`

### Trigger Service Methods
- [ ] Update `frontend/src/services/trigger-service.ts`
- [ ] Add `createRegistryDefinition(data: TriggerRegistryItem): Promise<TriggerRegistryItem>`
- [ ] Add `updateRegistryDefinition(id: string, data: Partial<TriggerRegistryItem>): Promise<TriggerRegistryItem>`
- [ ] Add `deleteRegistryDefinition(id: string): Promise<void>`
- [ ] API endpoints:
  - `POST /api/v1/triggers/registry`
  - `PUT /api/v1/triggers/registry/{id}`
  - `DELETE /api/v1/triggers/registry/{id}`

### Action Registry Service Methods
- [ ] Update `frontend/src/services/action-registry-service.ts`
- [ ] Implement `create(data: ActionRegistryItem): Promise<ActionRegistryItem>` (currently TODO)
- [ ] Implement `update(id: string, data: Partial<ActionRegistryItem>): Promise<ActionRegistryItem>`
- [ ] Implement `delete(id: string): Promise<void>`
- [ ] API endpoints:
  - `POST /api/v1/actions/registry`
  - `PUT /api/v1/actions/registry/{id}`
  - `DELETE /api/v1/actions/registry/{id}`

## Deliverables

- ✅ Schema Editor component created and functional
- ✅ `useTriggerRegistryById` hook working
- ✅ Trigger service CRUD methods implemented
- ✅ Action registry service CRUD methods implemented
- ✅ All components follow design system

## Technical Details

### Schema Editor Component Structure
```typescript
interface SchemaEditorProps {
  schemas: SchemaDefinition[]
  onChange: (schemas: SchemaDefinition[]) => void
  allowMultiple?: boolean // For event triggers
}

interface SchemaDefinition {
  schemaId: string
  eventType?: string // For event triggers
  description?: string
  fields: FieldDefinition[]
  filter?: FilterDefinition
  mapping?: MappingDefinition
}

interface FieldDefinition {
  name: string
  type: 'string' | 'number' | 'boolean' | 'date' | 'datetime' | 'email' | 'phone' | 'url' | 'json' | 'array' | 'object'
  required: boolean
  description?: string
  defaultValue?: any
  validation?: ValidationRules
  fields?: FieldDefinition[] // For nested objects/arrays
}
```

### Hook Pattern
```typescript
export function useTriggerRegistryById(id: string | undefined) {
  return useQuery({
    queryKey: ['trigger-registry', id],
    queryFn: () => triggerService.getRegistryById(id!),
    enabled: !!id,
    staleTime: 10 * 60 * 1000,
    gcTime: 30 * 60 * 1000,
  })
}
```

## Compliance Verification

- [ ] Verify Schema Editor component renders correctly
- [ ] Verify schema field management works (add/remove/edit)
- [ ] Verify nested objects/arrays are supported
- [ ] Verify hook loads trigger registry by ID correctly
- [ ] Verify service methods call correct API endpoints
- [ ] Verify error handling in services
- [ ] Verify components follow design system

## Related Documentation

- `@import(features/trigger-registry.md)`
- `@import(features/action-registry.md)`
- `@import(features/schema-definition.md)`
- `@import(api/endpoints.md)`

