# System Architecture Overview

## High-Level Architecture

```mermaid
graph TB
    subgraph "Frontend Layer - React"
        WB[Workflow Builder]
        DASH[Dashboard]
        ANAL[Analytics]
    end
    
    subgraph "Backend API Layer - Spring Boot"
        WS[Workflow Service]
        AS[Action Service]
        AS_ANALYTICS[Analytics Service]
        TS[Trigger Service]
        SS[Scheduler Service]
        RS[Registry Service]
    end
    
    subgraph "Data Layer"
        PG[(PostgreSQL<br/>Primary Database)]
        REDIS[(Redis<br/>Cache & Locks)]
    end
    
    subgraph "Message Queue Layer"
        KF[Kafka<br/>Event Streaming]
    end
    
    subgraph "External Services"
        SMTP[SMTP Servers]
        SMS[SMS Providers]
        PUSH[Push Providers]
        SLACK[Slack/Discord/Teams]
        WEBHOOK[Webhooks]
    end
    
    WB -->|HTTP/REST| WS
    DASH -->|HTTP/REST| WS
    ANAL -->|HTTP/REST| AS
    
    WS --> PG
    WS --> REDIS
    NS --> PG
    AS --> PG
    TS --> PG
    SS --> PG
    RS --> PG
    
    TS --> KF
    
    NS --> SMTP
    NS --> SMS
    NS --> PUSH
    NS --> SLACK
    NS --> WEBHOOK
    
    style WB fill:#e1f5ff
    style WS fill:#fff4e1
    style PG fill:#e8f5e9
    style REDIS fill:#fff9c4
    style KF fill:#f3e5f5
```

## System Components

### 1. Frontend Layer
- **Workflow Builder**: Drag-and-drop interface for creating workflows
- **Dashboard**: Overview and management interface
- **Analytics**: Reporting and insights interface

### 2. Backend API Layer
- **Workflow Service**: Manages workflow definitions and execution
- **Action Service**: Executes actions (API calls, event publishing, custom actions)
- **Trigger Service**: Manages workflow triggers and trigger instances
- **Analytics Service**: Collects and aggregates analytics data
- **Scheduler Service**: Handles scheduled triggers
- **Registry Service**: Manages trigger and action definitions in the registry

### 3. Data Layer
- **PostgreSQL**: Primary database for all data
- **Redis**: Cache for active execution contexts and distributed locks

### 4. Message Queue Layer
- **Kafka**: Event streaming for event triggers

### 5. External Services
- **External APIs**: Various REST APIs for integration
- **Webhooks**: HTTP webhook endpoints
- **Message Brokers**: Kafka for event publishing
- **Providers**: Email, SMS, Push, Slack, Discord, Teams providers (via custom actions)

## Data Flow

### Workflow Creation Flow

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant API as Backend API
    participant WS as Workflow Service
    participant DB as PostgreSQL
    
    User->>Frontend: Create workflow in builder
    Frontend->>Frontend: Validate workflow structure
    Frontend->>API: POST /workflows
    API->>WS: createWorkflow(definition)
    WS->>WS: Validate workflow
    WS->>DB: INSERT workflow
    DB-->>WS: Workflow saved
    WS-->>API: WorkflowResponse
    API-->>Frontend: Success response
    Frontend-->>User: Workflow created
    
    User->>Frontend: Configure triggers
    Frontend->>API: POST /triggers
    API->>WS: createTrigger(triggerConfig)
    WS->>DB: INSERT trigger
    DB-->>WS: Trigger saved
    WS-->>API: TriggerResponse
    API-->>Frontend: Success response
```

### Workflow Execution Flow

```mermaid
sequenceDiagram
    participant Trigger
    participant TS as Trigger Service
    participant WE as Workflow Engine
    participant AS as Action Service
    participant Provider as External Provider
    participant AS_ANALYTICS as Analytics Service
    participant DB as PostgreSQL
    participant REDIS as Redis Cache
    
    alt API Trigger
        Trigger->>TS: HTTP POST /trigger/{trigger_path}
    else Schedule Trigger
        Trigger->>TS: Cron fires (from scheduler)
    else Event Trigger
        Trigger->>TS: Kafka message (from consumer)
    end
    
    TS->>DB: Create execution record
    TS->>WE: Start workflow execution
    WE->>DB: Load workflow definition
    WE->>REDIS: Cache execution context
    
    loop For each node
        WE->>WE: Execute node
        alt Action Node
            WE->>AS: Execute action request
            alt API Call Action
                AS->>Provider: HTTP request
                Provider-->>AS: Response
            else Publish Event Action
                AS->>Provider: Publish to Kafka
                Provider-->>AS: Published
            else Custom Action
                AS->>AS: Execute custom logic
            end
            AS->>AS_ANALYTICS: Record action execution
        else Delay Node
            WE->>DB: Persist execution state
            WE->>REDIS: Store context for resume
            WE->>WE: Pause execution
            Note over WE,REDIS: Can resume on any instance
        end
        WE->>DB: Save node execution
        WE->>REDIS: Update cached context
    end
    
    WE->>DB: Update execution status
    WE->>REDIS: Remove cached context
    WE-->>TS: Execution completed
    AS_ANALYTICS->>DB: Aggregate analytics data
```

### Action Execution Flow

```mermaid
sequenceDiagram
    participant Node as Action Node
    participant AS as Action Service
    participant Provider as External Provider
    participant DB as PostgreSQL
    participant AS_ANALYTICS as Analytics Service
    
    Node->>AS: Execute action request<br/>(action type, config, input data)
    
    alt API Call Action
        AS->>AS: Prepare HTTP request<br/>(from node config and input data)
        AS->>Provider: HTTP request
        Provider-->>AS: Response
        AS->>AS: Process response
    else Publish Event Action (Kafka)
        AS->>AS: Prepare event message<br/>(from node config and input data)
        AS->>Provider: Publish to Kafka topic
        Provider-->>AS: Published confirmation
    else Function Action
        AS->>AS: Execute calculation logic<br/>(from node config)
    else Custom Action
        AS->>AS: Execute custom action logic<br/>(send-email, send-sms, etc.)
        alt Custom Action requires external call
            AS->>Provider: Call external service
            Provider-->>AS: Response
        end
    end
    
    AS->>DB: Save node execution record
    AS->>AS_ANALYTICS: Record action metrics
    AS_ANALYTICS->>DB: Update analytics
    AS-->>Node: Action result
```

## Communication Patterns

### Synchronous Communication
- **Frontend ↔ Backend API**: HTTP/REST
- **Backend API ↔ Database**: Direct connection
- **Backend API ↔ External Providers**: HTTP/REST

### Asynchronous Communication
- **Event Triggers**: Kafka consumers
- **Event Publishing**: Kafka producers for publishing events
- **Analytics Processing**: Async aggregation

## Scalability Considerations

### Horizontal Scaling
- **Frontend**: Stateless, can scale horizontally
- **Backend API**: Stateless, can scale horizontally
- **Database**: Read replicas for read scaling
- **Message Queue**: Partitioned topics/queues

### Vertical Scaling
- **Database**: Increase resources for larger datasets
- **Message Queue**: Increase broker resources

### Caching Strategy
- **Workflow Definitions**: Cache frequently accessed workflows
- **Trigger/Action Registry**: Cache registry definitions
- **Execution Context**: Cache active execution contexts in Redis for distributed pause/resume
- **Distributed Locks**: Use Redis for distributed locks during execution state updates

### Load Balancing
- **Frontend**: Load balancer for multiple instances
- **Backend API**: Load balancer for API instances
- **Database**: Connection pooling

## Deployment Architecture

### On-Premise Deployment

```mermaid
graph TB
    subgraph "Load Balancer Layer"
        LB[Load Balancer<br/>Reverse Proxy]
    end
    
    subgraph "Frontend Layer"
        FE1[Frontend Instance 1]
        FE2[Frontend Instance 2]
        FE3[Frontend Instance N]
    end
    
    subgraph "Backend API Layer"
        BE1[Backend API Instance 1]
        BE2[Backend API Instance 2]
        BE3[Backend API Instance N]
    end
    
    subgraph "Data Layer"
        PG_PRIMARY[(PostgreSQL<br/>Primary)]
        PG_REPLICA[(PostgreSQL<br/>Read Replica)]
        REDIS_CLUSTER[(Redis<br/>Cluster)]
        KF[Kafka Cluster]
    end
    
    LB --> FE1
    LB --> FE2
    LB --> FE3
    
    FE1 --> BE1
    FE1 --> BE2
    FE2 --> BE1
    FE2 --> BE2
    FE3 --> BE3
    
    BE1 --> PG_PRIMARY
    BE2 --> PG_PRIMARY
    BE3 --> PG_PRIMARY
    
    BE1 --> PG_REPLICA
    BE2 --> PG_REPLICA
    BE3 --> PG_REPLICA
    
    BE1 --> REDIS_CLUSTER
    BE2 --> REDIS_CLUSTER
    BE3 --> REDIS_CLUSTER
    
    BE1 --> KF
    BE2 --> KF
    BE3 --> KF
    
    PG_PRIMARY -->|Replication| PG_REPLICA
    
    style LB fill:#e1f5ff
    style FE1 fill:#fff4e1
    style BE1 fill:#e8f5e9
    style PG_PRIMARY fill:#f3e5f5
```

## Security Considerations (Future)

### Authentication
- API key authentication (optional for MVP)
- OAuth 2.0 (future)
- SSO integration (future)

### Authorization
- Role-based access control (future)
- Resource-level permissions (future)

### Data Protection
- Encryption at rest (future)
- Encryption in transit (TLS/SSL)
- Secure credential storage (future)

## Monitoring and Observability

### Logging
- Application logs
- Access logs
- Error logs
- Execution logs

### Metrics
- Request rates
- Error rates
- Execution times
- Delivery rates
- Queue depths

### Health Checks
- API health endpoints
- Database connectivity
- Message queue connectivity
- External provider status

## Related Documentation

- [Components](./components.md) - Detailed component descriptions
- [Integrations](./integrations.md) - External integrations
- [Scalability](./scalability.md) - Scalability details


