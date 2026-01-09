# API Documentation

This directory contains REST API specifications for the No-Code Workflow Platform.

## API Overview

- **Base URL**: `/api/v1`
- **Protocol**: HTTP/HTTPS
- **Format**: JSON
- **Authentication**: API Key (optional for MVP, can be deferred)

## API Categories

1. **[Workflows](./endpoints.md#workflows)** - Workflow management
2. **[Trigger Registry](./endpoints.md#trigger-registry)** - Trigger definition management
3. **[Action Registry](./endpoints.md#action-registry)** - Action definition management
4. **[Triggers](./endpoints.md#triggers)** - Trigger instance management
5. **[Executions](./endpoints.md#executions)** - Workflow execution management
6. **[Execution Visualization](./endpoints.md#execution-visualization)** - Step-by-step execution visualization and debugging
7. **[Analytics](./endpoints.md#analytics)** - Analytics and reporting

## API Standards

- **Request Format**: JSON
- **Response Format**: JSON
- **Error Format**: Standardized error responses
- **Status Codes**: HTTP status codes
- **Pagination**: Offset/limit or cursor-based

## Related Documentation

- [API Schemas](./schemas.md) - Request/response schemas (documentation only, not API endpoints)
- [Error Handling](./error-handling.md) - Error response format
- [Backend Technical Specs](../technical/backend/)

## Note on Schema Definitions

**Schema definitions** (for workflow nodes) are **NOT** managed via separate API endpoints. They are:
- Part of **Trigger Registry** and **Action Registry** definitions
- Defined when creating trigger/action definitions in registry
- Accessed through registry APIs: `GET /triggers/registry/{id}` and `GET /actions/registry/{id}`
- See [Schema Definition](../features/schema-definition.md) for details

The `api/schemas.md` file only documents **request/response formats** for REST API endpoints, not schema definitions for workflow nodes.


