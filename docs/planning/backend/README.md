# Backend Implementation Planning - Detailed Guide

## Overview

This directory contains **extremely detailed** sprint planning for backend implementation. Each sprint assumes **ZERO existing implementation** and provides step-by-step instructions with code examples, exact file paths, class names, method signatures, and test cases.

## Critical Requirements

### Design Compliance
- **MUST** follow architecture specifications in `docs/architecture/`
- **MUST** follow database schema in `docs/database-schema/`
- **MUST** follow API specifications in `docs/api/`
- **MUST** follow feature specifications in `docs/features/`
- **MUST** verify existing code compliance before starting work
- **MUST** fix any violations immediately

### Sprint Execution
- Each sprint should be completable in **1 prompt**
- Each task includes:
  - Exact file paths
  - Exact class names
  - Exact method signatures
  - Code examples
  - Test cases
  - Verification steps
- If code violates design, **fix it immediately** before proceeding

## Sprint Index

### Phase 1: Foundation (Sprints 01-04)
Foundation setup: project structure, database, repositories, and registries.

- [Sprint 01](./sprint-01.md) - Project Setup & Infrastructure
- [Sprint 02](./sprint-02.md) - Database Schema & JPA Entities
- [Sprint 03](./sprint-03.md) - Core Repositories
- [Sprint 04](./sprint-04.md) - Action Registry & Trigger Registry

### Phase 2: Core Services (Sprints 05-10)
Core business logic: workflow engine, execution management, and node executors.

- [Sprint 05](./sprint-05.md) - Workflow Service - CRUD Operations
- [Sprint 06](./sprint-06.md) - Workflow Engine - Core Execution Framework
- [Sprint 07](./sprint-07.md) - Workflow Engine - Logic Node Executors
- [Sprint 08](./sprint-08.md) - Workflow Engine - Action Node Executors
- [Sprint 09](./sprint-09.md) - Execution State Management & Pause/Resume
- [Sprint 10](./sprint-10.md) - Retry Mechanism & Error Handling

### Phase 3: Triggers (Sprints 11-14)
Trigger implementations: API, scheduler, event triggers.

- [Sprint 11](./sprint-11.md) - Trigger Service - API Trigger
- [Sprint 12](./sprint-12.md) - Trigger Service - Schedule Trigger
- [Sprint 13](./sprint-13.md) - Trigger Service - Event Trigger (Kafka)
- [Sprint 14](./sprint-14.md) - Trigger Registry & Instance Management

### Phase 4: API Layer (Sprints 15-18)
REST API controllers and exception handling.

- [Sprint 15](./sprint-15.md) - API Controllers - Workflow & Execution
- [Sprint 16](./sprint-16.md) - API Controllers - Trigger & Action Registry
- [Sprint 17](./sprint-17.md) - Exception Handling & Validation
- [Sprint 18](./sprint-18.md) - API Documentation & Testing

### Phase 5: Analytics & Reporting (Sprints 19-21)
Analytics service and workflow dashboard/report APIs.

- [Sprint 19](./sprint-19.md) - Analytics Service
- [Sprint 20](./sprint-20.md) - Workflow Dashboard API
- [Sprint 21](./sprint-21.md) - Workflow Report Service

### Phase 6: Extended Features (Sprints 22-25)
Advanced features: async event aggregation, A/B testing, execution visualization.

- [Sprint 22](./sprint-22.md) - Async Event Aggregation - Database & Entities
- [Sprint 23](./sprint-23.md) - Async Event Aggregation - Service & Node Executor
- [Sprint 24](./sprint-24.md) - A/B Testing Service
- [Sprint 25](./sprint-25.md) - Execution Visualization API

### Phase 7: MVEL Expression System (Sprint 26-34)
MVEL expression evaluation, output mapping, và integration.

- [Sprint 26](./sprint-26.md) - Action Registry - Config Template Validation
- [Sprint 27](./sprint-27.md) - Backend - MVEL Expression Evaluation (Planning only - chưa implement)
- [Sprint 28](./sprint-28.md) - MVEL Infrastructure (MvelEvaluator, ExecutionContextBuilder) [NEW]
- [Sprint 29](./sprint-29.md) - Action Schema Structure (DTOs, Entity Update) [NEW]
- [Sprint 30](./sprint-30.md) - NodeConfig Structure (DTOs, Workflow Definition Update) [NEW]
- [Sprint 31](./sprint-31.md) - Action Executors - MVEL Integration [NEW]
- [Sprint 32](./sprint-32.md) - Output Mapping (OutputMappingApplier, ConfigValidator) [NEW]
- [Sprint 33](./sprint-33.md) - Logic & Data Nodes - MVEL Support [NEW]
- [Sprint 34](./sprint-34.md) - Template Migration (TemplateRenderer → MVEL) [NEW]

**⚠️ IMPORTANT**: Xem [Implementation Gap Analysis](./implementation-gap-analysis.md) để biết những gì còn thiếu.

## Sprint Template Structure

Each sprint file follows this structure:

1. **Goal**: Clear objective
2. **Phase**: Development phase
3. **Complexity**: Simple/Medium/Complex
4. **Dependencies**: Prerequisite sprints
5. **Compliance Check**: Verify existing code compliance
6. **Tasks**: Extremely detailed task list with:
   - Exact file paths
   - Exact class names
   - Exact method signatures
   - Code examples
   - Test cases
   - Verification steps
7. **Deliverables**: Expected outcomes
8. **Technical Details**: Implementation specifics with code
9. **Compliance Verification**: Post-implementation checks
10. **Related Documentation**: Reference docs

## Implementation Guidelines

### File Naming Conventions
- Entities: `{EntityName}.java` (e.g., `Workflow.java`)
- Repositories: `{EntityName}Repository.java` (e.g., `WorkflowRepository.java`)
- Services: `{ServiceName}Service.java` and `{ServiceName}ServiceImpl.java`
- DTOs: `{EntityName}DTO.java`, `{EntityName}RequestDTO.java`, `{EntityName}ResponseDTO.java`
- Controllers: `{EntityName}Controller.java`
- Config: `{ConfigName}Config.java`

### Package Structure (MUST FOLLOW)
```
com.notificationplatform
├── NotificationPlatformApplication.java
├── controller/
│   ├── workflow/
│   ├── execution/
│   ├── trigger/
│   └── analytics/
├── service/
│   ├── workflow/
│   ├── execution/
│   ├── trigger/
│   └── analytics/
├── repository/
│   ├── workflow/
│   ├── execution/
│   └── trigger/
├── entity/
│   ├── workflow/
│   ├── execution/
│   └── trigger/
├── dto/
│   ├── request/
│   └── response/
├── config/
├── exception/
├── util/
└── enums/
```

### Code Quality Standards
- Use Lombok for reducing boilerplate (@Data, @Getter, @Setter, etc.)
- Use constructor injection (not @Autowired on fields)
- Add JavaDoc for public methods
- Follow Java naming conventions
- Use Optional for nullable returns
- Handle exceptions properly
- Add logging with SLF4J

## Implementation Status

**⚠️ CRITICAL**: Backend implementation còn thiếu nhiều components quan trọng so với design.

Xem [Implementation Gap Analysis](./implementation-gap-analysis.md) để biết chi tiết những gì còn thiếu.

### Quick Summary

**Missing Critical Components**:
- ❌ MVEL Expression System (hoàn toàn thiếu)
- ❌ Output Mapping Mechanism (hoàn toàn thiếu)
- ⚠️ Action Schema Structure (thiếu phần lớn)
- ⚠️ NodeConfig Structure (chưa đúng design)
- ⚠️ Action Executors (cần refactor lớn để dùng MVEL)

**Current State**:
- ✅ Basic workflow execution engine
- ✅ Node executors (nhưng dùng old `${}` syntax)
- ✅ Action Registry CRUD
- ⚠️ Chưa có MVEL evaluation
- ⚠️ Chưa có output mapping

**Required Sprints** (28-34):
- Sprint 28: MVEL Infrastructure
- Sprint 29: Action Schema Structure
- Sprint 30: NodeConfig Structure
- Sprint 31: Action Executors - MVEL Integration
- Sprint 32: Output Mapping
- Sprint 33: Logic & Data Nodes - MVEL
- Sprint 34: Template Migration

## Related Documentation

- [Implementation Gap Analysis](./implementation-gap-analysis.md) - **ĐỌC NÀY TRƯỚC** - Chi tiết những gì còn thiếu
- [Architecture Overview](../../architecture/overview.md)
- [Database Schema](../../database-schema/)
- [API Specifications](../../api/)
- [Feature Specifications](../../features/)
- [Backend Technical Guide](../../technical/backend/)
- [MVEL Expression System](../../features/mvel-expression-system.md) - Design specification
- [Action Execution](../../technical/backend/action-execution.md) - Implementation guide
