# Architecture Documentation

This directory contains system architecture and design documentation for the No-Code Notification Platform.

## Architecture Overview

The platform is designed as a scalable, on-premise solution supporting:
- 10 million users
- 10,000 messages per second
- Multiple notification channels
- Workflow-based notification delivery

## Architecture Documents

1. **[System Overview](./overview.md)** - High-level system architecture
2. **[Components](./components.md)** - Detailed component descriptions
3. **[Integrations](./integrations.md)** - External system integrations
4. **[Scalability](./scalability.md)** - Scalability considerations

## Technology Stack

### Frontend
- **Framework**: React
- **UI Library**: Shadcn/ui
- **State Management**: To be determined
- **Build Tool**: To be determined

### Backend
- **Language**: Java
- **Framework**: Spring Boot
- **Database**: PostgreSQL
- **Cache**: Redis (for execution context and distributed locks)
- **Message Queue**: Kafka (for event triggers)

### Infrastructure
- **Deployment**: On-premise
- **Containerization**: Docker (optional)
- **Orchestration**: To be determined

## Related Documentation

- [API Specifications](../api/)
- [Database Schema](../database-schema/)
- [Technical Specifications](../technical/)


