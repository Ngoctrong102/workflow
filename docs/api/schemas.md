# API Schemas

> **Note**: This file documents **request/response formats** for REST API endpoints only.
> 
> For **schema definitions** used in workflow nodes (triggers, actions), see:
> - [Schema Definition](../features/schema-definition.md) - Schema definitions for workflow nodes
> - Schema definitions are part of Trigger Registry and Action Registry, accessed via registry APIs

## Common Schemas

### Error Response
```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "details": {
      "field": "Additional error details"
    }
  }
}
```

### Pagination Response
```json
{
  "data": [...],
  "pagination": {
    "total": 100,
    "limit": 10,
    "offset": 0,
    "has_more": true
  }
}
```

### Timestamp
ISO 8601 format: `2024-01-01T00:00:00Z`

## Workflow Schemas

### Workflow Definition
```json
{
  "nodes": [
    {
      "id": "node-1",
      "type": "trigger",
      "subtype": "api",
      "position": {
        "x": 100,
        "y": 100
      },
      "data": {
        "config": {}
      }
    },
    {
      "id": "node-2",
      "type": "action",
      "subtype": "send_email",
      "position": {
        "x": 300,
        "y": 100
      },
      "data": {
        "template_id": "template-123",
        "recipients": "{{data.recipients}}"
      }
    }
  ],
  "edges": [
    {
      "id": "edge-1",
      "source": "node-1",
      "target": "node-2"
    }
  ]
}
```

### Node Types
- `trigger`: Trigger nodes (api, schedule, file, event)
- `action`: Action nodes (send_email, send_sms, send_push, etc.)
- `logic`: Logic nodes (condition, switch, loop, delay, merge)
- `data`: Data transformation nodes (map, filter, transform, read_file)

## Template Schemas

### Template
```json
{
  "id": "template-123",
  "name": "Welcome Email Template",
  "channel": "email",
  "subject": "Welcome {{user.name}}!",
  "body": "<html><body>...</body></html>",
  "variables": [
    {
      "name": "user.name",
      "type": "string",
      "required": true
    },
    {
      "name": "user.email",
      "type": "string",
      "required": true
    }
  ],
  "created_at": "2024-01-01T00:00:00Z",
  "updated_at": "2024-01-01T00:00:00Z"
}
```

## Trigger Schemas

### API Trigger
```json
{
  "id": "trigger-123",
  "type": "api",
  "workflow_id": "workflow-123",
  "path": "/trigger/welcome-email",
  "method": "POST",
  "authentication": {
    "type": "api_key",
    "key": "optional-api-key"
  },
  "request_schema": {
    "type": "object",
    "properties": {
      "data": {
        "type": "object"
      },
      "recipients": {
        "type": "array",
        "items": {
          "type": "object"
        }
      }
    }
  },
  "status": "active",
  "created_at": "2024-01-01T00:00:00Z"
}
```

### Schedule Trigger
```json
{
  "id": "trigger-456",
  "type": "schedule",
  "workflow_id": "workflow-123",
  "cron_expression": "0 9 * * *",
  "timezone": "UTC",
  "start_date": "2024-01-01T00:00:00Z",
  "end_date": null,
  "data": {
    "static_data": "value"
  },
  "status": "active",
  "created_at": "2024-01-01T00:00:00Z"
}
```

### Event Trigger
```json
{
  "id": "trigger-101",
  "type": "event",
  "workflow_id": "workflow-123",
  "queue_type": "kafka",
  "topic": "user.events",
  "consumer_group": "notification-service",
  "brokers": ["kafka1:9092", "kafka2:9092"],
  "filter": {
    "event_type": "user.created"
  },
  "status": "active",
  "created_at": "2024-01-01T00:00:00Z"
}
```

## Execution Schemas

### Execution Response
```json
{
  "id": "exec-123",
  "workflow_id": "workflow-456",
  "trigger_instance_id": "trigger-instance-789",
  "status": "COMPLETED",
  "started_at": "2024-01-01T00:00:00Z",
  "completed_at": "2024-01-01T00:05:00Z",
  "duration": 300,
  "nodes_executed": 5,
  "context": {
    "executionId": "exec-123",
    "workflowId": "workflow-456",
    "nodeOutputs": {...}
  },
  "trigger_data": {...},
  "workflow_metadata": {...}
}
```

### Node Execution Response
```json
{
  "id": "node-exec-123",
  "execution_id": "exec-456",
  "node_id": "node-abc",
  "node_type": "action",
  "node_sub_type": "api-call",
  "status": "COMPLETED",
  "input_data": {...},
  "output_data": {...},
  "retry_count": 0
}
```

## Channel Schemas

### Channel Configuration
```json
{
  "id": "channel-123",
  "type": "email",
  "name": "Primary Email",
  "provider": "smtp",
  "config": {
    "host": "smtp.example.com",
    "port": 587,
    "username": "user@example.com",
    "password": "encrypted-password",
    "encryption": "tls",
    "from_address": "noreply@example.com",
    "reply_to": "support@example.com"
  },
  "status": "active",
  "created_at": "2024-01-01T00:00:00Z"
}
```

### SMS Channel Config
```json
{
  "type": "sms",
  "provider": "twilio",
  "config": {
    "account_sid": "account-sid",
    "auth_token": "auth-token",
    "from_number": "+1234567890"
  }
}
```

### Push Channel Config
```json
{
  "type": "push",
  "provider": "fcm",
  "config": {
    "server_key": "server-key",
    "project_id": "project-id"
  }
}
```

## Execution Schemas

### Execution
```json
{
  "id": "exec-456",
  "workflow_id": "workflow-123",
  "trigger_id": "trigger-123",
  "status": "completed",
  "started_at": "2024-01-01T00:00:00Z",
  "completed_at": "2024-01-01T00:00:30Z",
  "duration": 30,
  "nodes_executed": 5,
  "notifications_sent": 1,
  "errors": [],
  "context": {
    "data": {}
  }
}
```

### Node Execution
```json
{
  "id": "node-exec-123",
  "execution_id": "exec-456",
  "node_id": "node-1",
  "status": "completed",
  "started_at": "2024-01-01T00:00:00Z",
  "completed_at": "2024-01-01T00:00:05Z",
  "duration": 5,
  "input": {},
  "output": {},
  "error": null
}
```

## Execution Visualization Schemas

### Execution Visualization Response
```json
{
  "execution": {
    "id": "exec-123",
    "workflow_id": "workflow-456",
    "status": "COMPLETED",
    "started_at": "2024-01-01T00:00:00Z",
    "completed_at": "2024-01-01T00:05:00Z"
  },
  "workflow": {
    "id": "workflow-456",
    "name": "User Onboarding Workflow",
    "definition": {
      "nodes": [
        {
          "id": "node-1",
          "type": "trigger",
          "subType": "api-call",
          "label": "API Trigger",
          "position": {"x": 100, "y": 100}
        }
      ],
      "edges": [
        {
          "id": "edge-1",
          "source": "node-1",
          "target": "node-2"
        }
      ]
    }
  },
  "trigger": {
    "type": "api",
    "instance_id": "trigger-instance-789",
    "data": {
      "userId": "user-123",
      "eventType": "user.created"
    }
  },
  "current_step": 0,
  "total_steps": 5,
  "nodes": [
    {
      "id": "node-1",
      "type": "trigger",
      "subType": "api-call",
      "label": "API Trigger",
      "status": "completed",
      "execution": {
        "id": "node-exec-1",
        "input_data": {},
        "output_data": {
          "userId": "user-123",
          "eventType": "user.created"
        },
        "started_at": "2024-01-01T00:00:00Z",
        "completed_at": "2024-01-01T00:00:01Z"
      }
    }
  ],
  "context": {
    "executionId": "exec-123",
    "workflowId": "workflow-456",
    "nodeOutputs": {
      "node-1": {
        "userId": "user-123",
        "eventType": "user.created"
      }
    },
    "variables": {}
  }
}
```

### Step Execution Request
```json
{
  "direction": "forward"  // "forward" or "backward"
}
```

### Step Execution Response
```json
{
  "step_number": 2,
  "node_id": "node-2",
  "node_type": "action",
  "node_sub_type": "api-call",
  "node_label": "Call User Service",
  "status": "completed",
  "execution": {
    "id": "node-exec-2",
    "input_data": {
      "userId": "user-123"
    },
    "output_data": {
      "user": {
        "id": "user-123",
        "name": "John Doe",
        "email": "john@example.com"
      }
    },
    "started_at": "2024-01-01T00:00:02Z",
    "completed_at": "2024-01-01T00:00:03Z"
  },
  "context": {
    "executionId": "exec-123",
    "workflowId": "workflow-456",
    "nodeOutputs": {
      "node-1": {...},
      "node-2": {...}
    },
    "variables": {}
  },
  "next_node": "node-3",
  "has_next": true,
  "has_previous": true
}
```

## Analytics Schemas

### Workflow Analytics
```json
{
  "workflow_id": "workflow-123",
  "period": {
    "start": "2024-01-01T00:00:00Z",
    "end": "2024-01-31T23:59:59Z"
  },
  "metrics": {
    "total_executions": 1000,
    "successful_executions": 950,
    "failed_executions": 50,
    "average_execution_time": 2.5,
    "success_rate": 0.95
  },
  "channel_breakdown": {
    "email": {
      "sent": 800,
      "delivered": 780,
      "failed": 20,
      "delivery_rate": 0.975
    }
  },
  "daily_breakdown": [
    {
      "date": "2024-01-01",
      "executions": 30,
      "successful": 28,
      "failed": 2
    }
  ]
}
```

### Delivery Analytics
```json
{
  "period": {
    "start": "2024-01-01T00:00:00Z",
    "end": "2024-01-31T23:59:59Z"
  },
  "metrics": {
    "total_sent": 10000,
    "delivered": 9500,
    "failed": 500,
    "delivery_rate": 0.95
  },
  "channel_breakdown": {
    "email": {
      "sent": 5000,
      "delivered": 4750,
      "failed": 250
    },
    "sms": {
      "sent": 3000,
      "delivered": 2850,
      "failed": 150
    }
  }
}
```



