# Features Documentation

This directory contains detailed feature specifications for the No-Code Workflow Platform.

## Feature List

1. **[Workflow Builder](./workflow-builder.md)** - Drag-and-drop interface for creating workflows
2. **[Workflow Execution State](./workflow-execution-state.md)** - Execution state management, context recovery, and pause/resume support
3. **[Distributed Execution Management](./distributed-execution-management.md)** - Distributed pause/resume with Redis cache and distributed locks
4. **[Execution Data Structure](./execution-data-structure.md)** - Comprehensive execution data structure for troubleshooting, reporting, and analytics
5. **[Execution Query Capabilities](./execution-query-capabilities.md)** - Query capabilities using PostgreSQL JSON functions for field-level queries
6. **[Execution Visualization](./execution-visualization.md)** - Step-by-step execution visualization and debugging
7. **[Trigger Registry](./trigger-registry.md)** - Registry system for managing trigger definitions
8. **[Action Registry](./action-registry.md)** - Registry system for managing action definitions
9. **[Node Types](./node-types.md)** - Detailed specifications for all workflow node types (Trigger, Logic, Action)
10. **[Triggers](./triggers.md)** - Event-based, scheduled, and API-based triggers
11. **[Analytics](./analytics.md)** - Execution tracking, logs, and reporting
12. **[Workflow Dashboard](./workflow-dashboard.md)** - Per-workflow monitoring and analytics dashboard
13. **[Workflow Report](./workflow-report.md)** - Automated report generation and scheduling for workflows
14. **[A/B Testing](./ab-testing.md)** - Testing different workflow variants
15. **[Schema Definition](./schema-definition.md)** - Define schemas and fields for field selection in workflows

## Feature Dependencies

```
Workflow Builder
  └── Triggers

Analytics
  └── Workflow Builder (depends on workflows being executed)

Workflow Dashboard
  ├── Workflow Builder (depends on workflows)
  └── Analytics (uses analytics data)

Workflow Report
  ├── Workflow Builder (depends on workflows)
  ├── Workflow Dashboard (uses dashboard data)
  └── Analytics (uses analytics data)

A/B Testing
  └── Workflow Builder (uses workflows for variants)

Schema Definition
  └── Workflow Builder (provides field selection for workflow nodes)
```

## MVP Feature Priority

### Phase 1 (Core MVP)
- ✅ Workflow Builder (basic drag-and-drop)
- ✅ Triggers (API, Scheduler, Event)
- ✅ Basic Analytics

### Phase 2 (Enhanced MVP)
- ✅ A/B Testing
- ✅ Advanced Analytics
- ✅ Workflow Dashboard

## Related Documentation

- [API Specifications](../api/)
- [User Flows](../user-flows/)
- [Technical Specifications](../technical/)


