# Trigger Registry

## Overview

Triggers must be **defined and registered** before they can be used in workflows. The trigger registry provides a catalog of available triggers that users can select from when building workflows. Each registered trigger has a unique ID and name for easy selection.

## Design Principles

1. **Pre-Definition**: Triggers must be defined before use
2. **Registry-Based**: Central catalog of available triggers
3. **Instance Management**: Each trigger usage in a workflow creates an independent instance
4. **Lifecycle Control**: Each instance can be paused/resumed/initialized/destroyed independently

## Trigger Definition

A trigger definition specifies the type, configuration template, and metadata for a trigger type.

```json
{
  "id": "api-trigger-template-1",
  "name": "User API Trigger",
  "type": "api-call",
  "description": "Trigger workflow via HTTP API call",
  "category": "api",
  "configTemplate": {
    "endpointPath": "/api/v1/trigger/{workflowId}",
    "httpMethod": "POST",
    "authentication": {
      "type": "api-key",
      "header": "X-API-Key"
    },
    "requestSchema": {
      "fields": [
        {
          "name": "userId",
          "type": "string",
          "required": true
        },
        {
          "name": "action",
          "type": "string",
          "required": false
        }
      ]
    }
  },
  "metadata": {
    "icon": "api-trigger",
    "color": "#0ea5e9",
    "version": "1.0.0"
  }
}
```

## Trigger Types in Registry

### 1. API Call Trigger

```json
{
  "id": "api-trigger-standard",
  "name": "API Call Trigger",
  "type": "api-call",
  "description": "Receives HTTP request to start workflow",
  "configTemplate": {
    "endpointPath": "/api/v1/trigger/{workflowId}",
    "httpMethod": "POST",
    "authentication": {
      "type": "api-key|bearer-token|none"
    },
    "requestSchema": { ... }
  }
}
```

### 2. Scheduler Trigger

```json
{
  "id": "scheduler-trigger-standard",
  "name": "Scheduler Trigger",
  "type": "scheduler",
  "description": "Cron-based scheduled execution",
  "configTemplate": {
    "cronExpression": "0 9 * * *",
    "timezone": "UTC",
    "startDate": null,
    "endDate": null,
    "repeat": true,
    "data": {}
  }
}
```

### 3. Event Trigger (Kafka)

```json
{
  "id": "kafka-event-trigger-standard",
  "name": "Kafka Event Trigger",
  "type": "event",
  "description": "Listens to Kafka topic events",
  "configTemplate": {
    "kafka": {
      "brokers": ["localhost:9092"],
      "topic": "",
      "consumerGroup": "workflow-consumer-group",
      "offset": "latest"
    },
    "schemas": [],
    "kafkaConnect": {
      "enabled": false,
      "schemaRegistryUrl": null,
      "subject": null
    }
  }
}
```

## Registry Management

### Database Schema

```sql
CREATE TABLE trigger_definitions (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    description TEXT,
    config_template JSONB NOT NULL,
    metadata JSONB,
    version VARCHAR(50) NOT NULL DEFAULT '1.0.0',
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);
```

### Registry API

#### Get Available Triggers

```http
GET /api/v1/triggers/definitions
```

Response:
```json
{
  "triggers": [
    {
      "id": "api-trigger-standard",
      "name": "API Call Trigger",
      "type": "api-call",
      "description": "Receives HTTP request to start workflow",
      "metadata": {
        "icon": "api-trigger",
        "color": "#0ea5e9"
      }
    },
    {
      "id": "scheduler-trigger-standard",
      "name": "Scheduler Trigger",
      "type": "scheduler",
      "description": "Cron-based scheduled execution",
      "metadata": {
        "icon": "schedule-trigger",
        "color": "#0ea5e9"
      }
    },
    {
      "id": "kafka-event-trigger-standard",
      "name": "Kafka Event Trigger",
      "type": "event",
      "description": "Listens to Kafka topic events",
      "metadata": {
        "icon": "event-trigger",
        "color": "#0ea5e9"
      }
    }
  ]
}
```

## Workflow Node Configuration

### Using Registry in Workflow

When creating a trigger node in a workflow, users select from the registry:

```json
{
  "id": "node-uuid",
  "label": "kafkaTrigger",
  "type": "trigger",
  "subType": "event",
  "registryId": "kafka-event-trigger-standard",
  "config": {
    "kafka": {
      "brokers": ["localhost:9092"],
      "topic": "user-events",
      "consumerGroup": "workflow-123-consumer",
      "offset": "latest"
    },
    "schemas": [ ... ]
  }
}
```

**Key Points**:
- `registryId`: References the trigger definition from registry
- `config`: Instance-specific configuration (overrides template defaults)
- Each trigger instance has its own configuration

## Trigger Instance Lifecycle

### Instance Creation

When a trigger is used in a workflow, a **separate consumer/scheduler instance** is created:

```java
// Pseudo-code for instance creation
public TriggerInstance createInstance(String workflowId, String registryId, Map<String, Object> config) {
    // Get trigger definition from registry
    TriggerDefinition definition = triggerRegistry.get(registryId);
    
    // Create instance-specific configuration
    TriggerInstance instance = new TriggerInstance();
    instance.setWorkflowId(workflowId);
    instance.setRegistryId(registryId);
    instance.setConfig(mergeConfig(definition.getConfigTemplate(), config));
    instance.setStatus(TriggerInstanceStatus.INITIALIZED);
    
    // Create consumer/scheduler based on type
    if (definition.getType() == TriggerType.EVENT) {
        KafkaConsumer consumer = createKafkaConsumer(instance);
        instance.setConsumer(consumer);
    } else if (definition.getType() == TriggerType.SCHEDULER) {
        Scheduler scheduler = createScheduler(instance);
        instance.setScheduler(scheduler);
    }
    
    return instance;
}
```

### Instance States

```java
public enum TriggerInstanceStatus {
    INITIALIZED,  // Instance created but not started
    ACTIVE,       // Instance is running
    PAUSED,       // Instance is paused
    STOPPED,      // Instance is stopped
    ERROR         // Instance has error
}
```

### Lifecycle Operations

#### Initialize

```http
POST /api/v1/workflows/{workflowId}/triggers/{triggerId}/init
```

Creates the consumer/scheduler instance but doesn't start it.

#### Start/Resume

```http
POST /api/v1/workflows/{workflowId}/triggers/{triggerId}/start
POST /api/v1/workflows/{workflowId}/triggers/{triggerId}/resume
```

Starts or resumes the consumer/scheduler.

#### Pause

```http
POST /api/v1/workflows/{workflowId}/triggers/{triggerId}/pause
```

Pauses the consumer/scheduler (stops processing but keeps connection).

#### Stop

```http
POST /api/v1/workflows/{workflowId}/triggers/{triggerId}/stop
```

Stops the consumer/scheduler completely.

#### Destroy

```http
DELETE /api/v1/workflows/{workflowId}/triggers/{triggerId}
```

Destroys the instance and releases all resources.

### Instance Management Example

```java
@Service
public class TriggerInstanceManager {
    
    private final Map<String, TriggerInstance> instances = new ConcurrentHashMap<>();
    
    public void initialize(String workflowId, String triggerId, TriggerDefinition definition, Map<String, Object> config) {
        TriggerInstance instance = createInstance(workflowId, triggerId, definition, config);
        instances.put(getInstanceKey(workflowId, triggerId), instance);
        instance.setStatus(TriggerInstanceStatus.INITIALIZED);
    }
    
    public void start(String workflowId, String triggerId) {
        TriggerInstance instance = getInstance(workflowId, triggerId);
        if (instance.getStatus() == TriggerInstanceStatus.INITIALIZED || 
            instance.getStatus() == TriggerInstanceStatus.PAUSED) {
            instance.getConsumer().start(); // or scheduler.start()
            instance.setStatus(TriggerInstanceStatus.ACTIVE);
        }
    }
    
    public void pause(String workflowId, String triggerId) {
        TriggerInstance instance = getInstance(workflowId, triggerId);
        if (instance.getStatus() == TriggerInstanceStatus.ACTIVE) {
            instance.getConsumer().pause(); // or scheduler.pause()
            instance.setStatus(TriggerInstanceStatus.PAUSED);
        }
    }
    
    public void stop(String workflowId, String triggerId) {
        TriggerInstance instance = getInstance(workflowId, triggerId);
        instance.getConsumer().stop(); // or scheduler.stop()
        instance.setStatus(TriggerInstanceStatus.STOPPED);
    }
    
    public void destroy(String workflowId, String triggerId) {
        TriggerInstance instance = getInstance(workflowId, triggerId);
        stop(workflowId, triggerId);
        instance.getConsumer().close(); // or scheduler.destroy()
        instances.remove(getInstanceKey(workflowId, triggerId));
    }
}
```

## Kafka Consumer Instance

### Consumer Configuration

Each Kafka event trigger instance has its own consumer:

```java
public class KafkaTriggerInstance {
    private String workflowId;
    private String triggerId;
    private KafkaConsumer<String, Object> consumer;
    private String topic;
    private String consumerGroup;
    private TriggerInstanceStatus status;
    
    public void start() {
        if (status == TriggerInstanceStatus.INITIALIZED || 
            status == TriggerInstanceStatus.PAUSED) {
            consumer.subscribe(Collections.singletonList(topic));
            // Start consuming in background thread
            executorService.submit(() -> {
                while (status == TriggerInstanceStatus.ACTIVE) {
                    ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(100));
                    for (ConsumerRecord<String, Object> record : records) {
                        triggerWorkflow(record.value());
                    }
                }
            });
            status = TriggerInstanceStatus.ACTIVE;
        }
    }
    
    public void pause() {
        if (status == TriggerInstanceStatus.ACTIVE) {
            consumer.pause(consumer.assignment());
            status = TriggerInstanceStatus.PAUSED;
        }
    }
    
    public void resume() {
        if (status == TriggerInstanceStatus.PAUSED) {
            consumer.resume(consumer.assignment());
            status = TriggerInstanceStatus.ACTIVE;
        }
    }
    
    public void stop() {
        status = TriggerInstanceStatus.STOPPED;
        consumer.unsubscribe();
    }
    
    public void destroy() {
        stop();
        consumer.close();
    }
}
```

## Scheduler Instance

### Scheduler Configuration

Each scheduler trigger instance has its own scheduler:

```java
public class SchedulerTriggerInstance {
    private String workflowId;
    private String triggerId;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduledTask;
    private String cronExpression;
    private TriggerInstanceStatus status;
    
    public void start() {
        if (status == TriggerInstanceStatus.INITIALIZED || 
            status == TriggerInstanceStatus.PAUSED) {
            CronExpression cron = CronExpression.parse(cronExpression);
            scheduledTask = scheduler.scheduleAtFixedRate(
                () -> triggerWorkflow(),
                calculateNextExecution(cron),
                Duration.between(calculateNextExecution(cron), calculateNextExecution(cron))
            );
            status = TriggerInstanceStatus.ACTIVE;
        }
    }
    
    public void pause() {
        if (status == TriggerInstanceStatus.ACTIVE) {
            scheduledTask.cancel(false);
            status = TriggerInstanceStatus.PAUSED;
        }
    }
    
    public void resume() {
        if (status == TriggerInstanceStatus.PAUSED) {
            start();
        }
    }
    
    public void stop() {
        if (scheduledTask != null) {
            scheduledTask.cancel(true);
        }
        status = TriggerInstanceStatus.STOPPED;
    }
    
    public void destroy() {
        stop();
        scheduler.shutdown();
    }
}
```

## Benefits

1. **Centralized Management**: All triggers defined in one place
2. **Easy Selection**: Users select from predefined catalog
3. **Independent Instances**: Each workflow trigger is independent
4. **Lifecycle Control**: Fine-grained control over each instance
5. **Resource Management**: Proper cleanup and resource management
6. **Scalability**: Each instance can be scaled independently

## Related Documentation

- [Action Registry](./action-registry.md) - Action registry system
- [Workflow Builder](./workflow-builder.md) - Workflow builder feature
- [Node Types](./node-types.md) - Node type specifications
- [Triggers](./triggers.md) - Trigger details
- [Schema Definition](./schema-definition.md) - Schema definitions

