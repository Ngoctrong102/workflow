# Documentation Index - Import System Reference

This file provides a reference for the `@import()` system used to load documentation context dynamically.

## Import Syntax

- `@import('path/to/file.md')` - Import entire file
- `@if(condition) then @import('path/to/file.md')` - Conditional import
- `@import('category/*')` - Import all files in category (use sparingly)

## Documentation Categories

### Features
```
@import('docs/features/workflow-builder.md')
@import('docs/features/triggers.md')
@import('docs/features/analytics.md')
@import('docs/features/workflow-dashboard.md')
@import('docs/features/workflow-report.md')
@import('docs/features/ab-testing.md')
@import('docs/features/schema-definition.md')
```

### API
```
@import('docs/api/endpoints.md')
@import('docs/api/schemas.md')
@import('docs/api/error-handling.md')
@import('docs/api/authentication.md')
```

### Database Schema
```
@import('docs/database-schema/entities.md')
@import('docs/database-schema/relationships.md')
@import('docs/database-schema/indexes.md')
```

### Architecture
```
@import('docs/architecture/overview.md')
@import('docs/architecture/components.md')
@import('docs/architecture/integrations.md')
@import('docs/architecture/scalability.md')
```

### User Flows
```
@import('docs/user-flows/workflow-creation.md')
@import('docs/user-flows/notification-delivery.md')
@import('docs/user-flows/analytics-viewing.md')
@import('docs/user-flows/workflow-dashboard-viewing.md')
@import('docs/user-flows/workflow-report-configuration.md')
```

### Technical - Frontend
```
@import('docs/technical/frontend/overview.md')
@import('docs/technical/frontend/design-system.json')
@import('docs/technical/frontend/components.md')
@import('docs/technical/frontend/routing.md')
@import('docs/technical/frontend/implementation-guide.md')
@import('docs/technical/frontend/api-integration.md')
@import('docs/technical/frontend/state-management.md')
@import('docs/technical/frontend/development-guide.md')
```

### Technical - Backend
```
@import('docs/technical/backend/overview.md')
@import('docs/technical/backend/project-structure.md')
@import('docs/technical/backend/service-interfaces.md')
@import('docs/technical/backend/implementation-guide.md')
@import('docs/technical/backend/workflow-context-management.md')
```

### Technical - Integration
```
@import('docs/technical/integration/api-contract.md')
@import('docs/technical/integration/integration-requirements.md')
@import('docs/technical/integration/integration-checklist.md')
@import('docs/technical/integration/async-event-aggregation.md')
```

## Pattern-Based Imports

### Load All Features
```
@import('docs/features/*')
```

### Load All API Documentation
```
@import('docs/api/*')
```

### Load Complete Technical Specs
```
@import('docs/technical/frontend/*')
@import('docs/technical/backend/*')
@import('docs/technical/integration/*')
```

## Conditional Imports

### Load Security Docs Only If Needed
```
@if("security-required") then @import('docs/security/authentication.md')
```

### Load MVP-Specific Documentation
```
@if("mvp-scope") then @import('docs/mvp/scope.md')
```

## Best Practices

1. **Be Specific**: Import specific files rather than entire categories when possible
2. **Avoid Duplication**: Use imports to reference, not duplicate content
3. **Logical Grouping**: Group related imports together
4. **Documentation First**: Always check this index before creating new documentation

## Adding New Documentation

When adding new documentation:

1. Create the file in the appropriate category directory
2. Update this index with the import path
3. Update `docs/README.md` if adding a new category
4. Use consistent naming conventions


