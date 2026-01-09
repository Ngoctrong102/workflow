# Backend Project Structure

## Directory Organization

### src/main/java/com/yourorg/notificationplatform/
Main application package with all source code.

```
src/main/java/com/yourorg/notificationplatform/
├── NotificationPlatformApplication.java  # Spring Boot application entry point
├── controller/          # REST Controllers
│   ├── WorkflowController.java
│   ├── TemplateController.java
│   ├── TriggerController.java
│   ├── NotificationController.java
│   ├── AnalyticsController.java
│   └── ChannelController.java
```

### controller/
REST Controllers - handle HTTP requests and responses.

```
controller/
├── WorkflowController.java      # Workflow endpoints
├── TemplateController.java       # Template endpoints
├── TriggerController.java        # Trigger endpoints
├── NotificationController.java   # Notification endpoints
├── AnalyticsController.java      # Analytics endpoints
└── ChannelController.java        # Channel endpoints
```

### service/
Business logic layer - core services.

```
service/
├── workflow/            # Workflow service
│   ├── WorkflowService.java
│   ├── WorkflowExecutor.java
│   └── WorkflowValidator.java
├── notification/        # Notification service
│   ├── NotificationService.java
│   ├── NotificationSender.java
│   └── TemplateRenderer.java
├── trigger/            # Trigger service
│   ├── TriggerService.java
│   ├── ApiTriggerHandler.java
│   ├── ScheduleTriggerHandler.java
│   ├── FileTriggerHandler.java
│   └── EventTriggerHandler.java
├── channel/            # Channel service
│   ├── ChannelService.java
│   ├── EmailChannel.java
│   ├── SmsChannel.java
│   ├── PushChannel.java
│   └── ...
├── template/           # Template service
│   ├── TemplateService.java
│   └── TemplateRenderer.java
├── analytics/          # Analytics service
│   ├── AnalyticsService.java
│   └── AnalyticsAggregator.java
└── scheduler/          # Scheduler service
    ├── SchedulerService.java
    └── CronScheduler.java
```

### repository/
Data access layer - Spring Data JPA repositories.

```
repository/
├── WorkflowRepository.java      # Workflow repository
├── TemplateRepository.java       # Template repository
├── NotificationRepository.java   # Notification repository
├── ExecutionRepository.java      # Execution repository
├── DeliveryRepository.java       # Delivery repository
└── AnalyticsRepository.java      # Analytics repository
```

### entity/
JPA Entities - database entities matching schema.

```
entity/
├── Workflow.java          # Workflow entity
├── Template.java          # Template entity
├── Trigger.java           # Trigger entity
├── Notification.java      # Notification entity
├── Execution.java         # Execution entity
├── Delivery.java          # Delivery entity
└── Channel.java          # Channel entity
```

### dto/
Data Transfer Objects - request/response DTOs.

```
dto/
├── request/              # Request DTOs
│   ├── CreateWorkflowRequest.java
│   ├── UpdateWorkflowRequest.java
│   └── ...
├── response/            # Response DTOs
│   ├── WorkflowResponse.java
│   ├── TemplateResponse.java
│   └── ...
└── mapper/              # DTO mappers
    └── WorkflowMapper.java
```

### config/
Configuration classes - Spring configuration.

```
config/
├── DatabaseConfig.java      # Database configuration
├── KafkaConfig.java         # Kafka configuration
├── RabbitMQConfig.java      # RabbitMQ configuration
├── SecurityConfig.java       # Security configuration
└── WebConfig.java           # Web configuration (CORS, etc.)
```

### security/
Security configuration - Spring Security setup.

```
security/
├── SecurityConfig.java           # Security configuration
├── JwtAuthenticationFilter.java # JWT filter
└── JwtTokenProvider.java         # JWT token provider
```

### exception/
Exception handling - global exception handler.

```
exception/
├── GlobalExceptionHandler.java   # @ControllerAdvice
├── ErrorResponse.java            # Error response DTO
└── CustomException.java          # Custom exceptions
```

### engine/
Workflow execution engine.

```
engine/
├── WorkflowExecutor.java     # Workflow executor
├── NodeExecutor.java         # Node execution
├── ExecutionContext.java      # Execution context
└── nodes/                     # Node implementations
    ├── TriggerNode.java
    ├── ActionNode.java
    ├── LogicNode.java
    └── DataNode.java
```

### channel/
Notification channel implementations.

```
channel/
├── email/               # Email channel
│   ├── EmailChannel.java
│   ├── SmtpEmailProvider.java
│   └── SendGridEmailProvider.java
├── sms/                 # SMS channel
│   ├── SmsChannel.java
│   ├── TwilioSmsProvider.java
│   └── AwsSnsProvider.java
├── push/                # Push channel
│   ├── PushChannel.java
│   ├── FcmProvider.java
│   └── ApnsProvider.java
└── ...
```

### trigger/
Trigger implementations.

```
trigger/
├── api/                 # API trigger
│   └── ApiTriggerHandler.java
├── schedule/           # Schedule trigger
│   └── ScheduleTriggerHandler.java
└── event/              # Event trigger
    ├── KafkaEventConsumer.java
    └── RabbitMqEventConsumer.java
```

### util/
Utility classes.

```
util/
├── LoggerUtil.java      # Logging utilities
├── ErrorUtil.java       # Error handling utilities
└── DateUtil.java        # Date utilities
```

## Package Dependencies

### Internal Dependencies
```
controller → service → repository → entity
controller → dto
service → engine
service → channel
service → trigger
service → queue (Kafka/RabbitMQ)
```

### External Dependencies
- **Database**: PostgreSQL driver (via Spring Data JPA)
- **HTTP**: Spring Boot Web (embedded Tomcat)
- **Message Queue**: Spring Kafka, Spring AMQP
- **Validation**: Jakarta Validation (Bean Validation)
- **Configuration**: Spring Boot Configuration Properties

## Code Organization Principles

### Separation of Concerns
- **Controller Layer**: HTTP handling only, DTO conversion
- **Service Layer**: Business logic, orchestration
- **Repository Layer**: Data access only (Spring Data JPA)
- **Entity Layer**: Database entities (JPA)

### Dependency Injection
- **Spring DI**: Use @Autowired or constructor injection
- **Interfaces**: Define interfaces for services and repositories
- **Testing**: Easy to mock with @MockBean

### Error Handling
- **Exception Types**: Define custom exception types
- **Global Handler**: @ControllerAdvice for global exception handling
- **Error Responses**: Standardized error response format

## Related Documentation

- [Backend Overview](./overview.md) - Technology stack
- [Service Interfaces](./service-interfaces.md) - Service specs
- [Implementation Guide](./implementation-guide.md) - Implementation details
