# Backend Service Interfaces

## Service Interface Definitions

This document defines the service interfaces for the backend services using Java interfaces.

## Workflow Service

### Interface
```java
public interface WorkflowService {
    WorkflowDTO createWorkflow(CreateWorkflowRequest request);
    WorkflowDTO getWorkflow(String id);
    ListWorkflowsResponse listWorkflows(ListWorkflowsRequest request);
    WorkflowDTO updateWorkflow(String id, UpdateWorkflowRequest request);
    void deleteWorkflow(String id);
    ExecutionDTO executeWorkflow(String id, Map<String, Object> data);
    void validateWorkflow(WorkflowDTO workflow);
}
```

### Methods
- **createWorkflow**: Create a new workflow
- **getWorkflow**: Get workflow by ID
- **listWorkflows**: List workflows with filters
- **updateWorkflow**: Update workflow
- **deleteWorkflow**: Delete workflow (soft delete)
- **executeWorkflow**: Execute workflow with data
- **validateWorkflow**: Validate workflow definition

## Notification Service

### Interface
```java
public interface NotificationService {
    NotificationDTO sendNotification(SendNotificationRequest request);
    NotificationDTO getNotification(String id);
    NotificationStatusDTO getNotificationStatus(String id);
    RenderedTemplate renderTemplate(String templateId, Map<String, Object> data);
}
```

### Methods
- **sendNotification**: Send notification via channel
- **getNotification**: Get notification by ID
- **getNotificationStatus**: Get notification delivery status
- **renderTemplate**: Render template with data

## Trigger Service

### Interface
```java
public interface TriggerService {
    TriggerDTO createApiTrigger(String workflowId, CreateApiTriggerRequest request);
    TriggerDTO createScheduleTrigger(String workflowId, CreateScheduleTriggerRequest request);
    TriggerDTO createFileTrigger(String workflowId, CreateFileTriggerRequest request);
    TriggerDTO createEventTrigger(String workflowId, CreateEventTriggerRequest request);
    TriggerDTO getTrigger(String id);
    List<TriggerDTO> listTriggers(String workflowId);
    TriggerDTO updateTrigger(String id, UpdateTriggerRequest request);
    void deleteTrigger(String id);
    void activateTrigger(String id);
    void deactivateTrigger(String id);
}
```

### Methods
- **createApiTrigger**: Create API trigger
- **createScheduleTrigger**: Create schedule trigger
- **createEventTrigger**: Create event trigger
- **getTrigger**: Get trigger by ID
- **listTriggers**: List triggers for workflow
- **updateTrigger**: Update trigger
- **deleteTrigger**: Delete trigger
- **activateTrigger**: Activate trigger
- **deactivateTrigger**: Deactivate trigger

## Channel Service

### Interface
```java
public interface ChannelService {
    ChannelDTO createChannel(CreateChannelRequest request);
    ChannelDTO getChannel(String id);
    List<ChannelDTO> listChannels();
    ChannelDTO updateChannel(String id, UpdateChannelRequest request);
    void deleteChannel(String id);
    void testConnection(String id);
    DeliveryResult send(String channelId, Message message);
}
```

### Methods
- **createChannel**: Create notification channel
- **getChannel**: Get channel by ID
- **listChannels**: List all channels
- **updateChannel**: Update channel configuration
- **deleteChannel**: Delete channel
- **testConnection**: Test channel connection
- **send**: Send notification via channel

## Template Service

### Interface
```java
public interface TemplateService {
    TemplateDTO createTemplate(CreateTemplateRequest request);
    TemplateDTO getTemplate(String id);
    ListTemplatesResponse listTemplates(ListTemplatesRequest request);
    TemplateDTO updateTemplate(String id, UpdateTemplateRequest request);
    void deleteTemplate(String id);
    String renderTemplate(String templateId, Map<String, Object> data);
}
```

### Methods
- **createTemplate**: Create notification template
- **getTemplate**: Get template by ID
- **listTemplates**: List templates with filters
- **updateTemplate**: Update template
- **deleteTemplate**: Delete template
- **renderTemplate**: Render template with data

## Analytics Service

### Interface
```java
public interface AnalyticsService {
    WorkflowAnalyticsDTO getWorkflowAnalytics(String workflowId, AnalyticsRequest request);
    DeliveryAnalyticsDTO getDeliveryAnalytics(AnalyticsRequest request);
    ChannelAnalyticsDTO getChannelAnalytics(AnalyticsRequest request);
    ErrorAnalyticsDTO getErrorAnalytics(AnalyticsRequest request);
    void aggregateAnalytics(LocalDate date);
}
```

### Methods
- **getWorkflowAnalytics**: Get workflow analytics
- **getDeliveryAnalytics**: Get delivery analytics
- **getChannelAnalytics**: Get channel analytics
- **getErrorAnalytics**: Get error analytics
- **aggregateAnalytics**: Aggregate analytics data

## Scheduler Service

### Interface
```java
public interface SchedulerService {
    void scheduleWorkflow(String triggerId, String cronExpr, Map<String, Object> data);
    void unscheduleWorkflow(String triggerId);
    List<ScheduledWorkflowDTO> listScheduledWorkflows();
    void executeScheduledWorkflow(String triggerId);
}
```

### Methods
- **scheduleWorkflow**: Schedule workflow execution
- **unscheduleWorkflow**: Unschedule workflow
- **listScheduledWorkflows**: List scheduled workflows
- **executeScheduledWorkflow**: Execute scheduled workflow

- **parseJson**: Parse JSON file
- **parseExcel**: Parse Excel file

## Related Documentation

- [Backend Overview](./overview.md) - Technology stack
- [Project Structure](./project-structure.md) - Package organization
- [Implementation Guide](./implementation-guide.md) - Implementation details
