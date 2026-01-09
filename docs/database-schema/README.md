# Database Schema Documentation

This directory contains database schema specifications for the No-Code Workflow Platform.

## Database Technology

- **Primary Database**: PostgreSQL (flexible, can be changed)
- **Data Retention**: 6 months for historical data
- **Not Stored**: User data and preferences (as per requirements)

## Schema Overview

### Core Entities
1. **[Workflows](./entities.md#workflows)** - Workflow definitions
2. **[Triggers](./entities.md#triggers)** - Trigger configurations for workflows (config only, not runtime state)
3. **[Actions](./entities.md#actions)** - Action registry definitions
4. **[Executions](./entities.md#executions)** - Workflow execution records
5. **[Node Executions](./entities.md#node-executions)** - Individual node execution records
6. **[Execution Wait States](./entities.md#execution-wait-states)** - States for nodes waiting for async events
7. **[Analytics Aggregates](./entities.md#analytics-aggregates)** - Daily aggregated analytics data
8. **[Workflow Reports](./entities.md#workflow-reports)** - Automated report configurations

**Note**: Trigger types (api-call, scheduler, event) are hardcoded in code, not stored in database. See [Trigger Registry](../features/trigger-registry.md) for details.

### Relationships
See [Entity Relationships](./relationships.md) for detailed relationship diagrams.

### Indexes
See [Indexes](./indexes.md) for performance optimization indexes.

## Data Retention Policy

- **Historical Data**: 6 months retention
- **Automatic Cleanup**: Remove data older than 6 months
- **Aggregated Data**: Keep aggregated analytics longer (optional)

## Related Documentation

- [API Specifications](../api/)
- [Architecture](../architecture/)


