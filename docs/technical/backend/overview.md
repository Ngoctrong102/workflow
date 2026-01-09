# Backend Technical Overview

## Technology Stack

### Core Framework
- **Language**: Java
- **Framework**: Spring Boot
- **Version**: Java 17+ (or latest LTS)
- **Spring Boot Version**: 3.x

### Database
- **Primary Database**: PostgreSQL
- **ORM**: Spring Data JPA
- **Migration Tool**: Flyway or Liquibase

### Message Queue
- **Kafka**: Spring Kafka
- **RabbitMQ**: Spring AMQP

### HTTP Server
- **Framework**: Spring Boot (embedded Tomcat)
- **Middleware**: Spring Security, CORS, logging
- **Validation**: Bean Validation (Jakarta Validation)

### Configuration
- **Config Management**: Spring Boot Configuration Properties
- **Environment Variables**: application.yml or application.properties
- **Profiles**: Spring Profiles for different environments

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/yourorg/notificationplatform/
│   │   │   ├── NotificationPlatformApplication.java  # Application entry point
│   │   │   ├── controller/          # REST Controllers
│   │   │   │   ├── WorkflowController.java
│   │   │   │   ├── TemplateController.java
│   │   │   │   ├── TriggerController.java
│   │   │   │   ├── NotificationController.java
│   │   │   │   ├── AnalyticsController.java
│   │   │   │   └── ChannelController.java
│   │   │   ├── service/             # Business logic
│   │   │   │   ├── workflow/        # Workflow service
│   │   │   │   ├── notification/    # Notification service
│   │   │   │   ├── trigger/         # Trigger service
│   │   │   │   ├── channel/         # Channel service
│   │   │   │   ├── template/        # Template service
│   │   │   │   ├── analytics/       # Analytics service
│   │   │   │   └── scheduler/       # Scheduler service
│   │   │   ├── repository/          # Data access layer
│   │   │   │   ├── WorkflowRepository.java
│   │   │   │   ├── TemplateRepository.java
│   │   │   │   ├── NotificationRepository.java
│   │   │   │   ├── ExecutionRepository.java
│   │   │   │   └── AnalyticsRepository.java
│   │   │   ├── entity/              # JPA Entities
│   │   │   │   ├── Workflow.java
│   │   │   │   ├── Template.java
│   │   │   │   ├── Trigger.java
│   │   │   │   ├── Notification.java
│   │   │   │   ├── Execution.java
│   │   │   │   └── Delivery.java
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   │   ├── WorkflowDTO.java
│   │   │   │   ├── TemplateDTO.java
│   │   │   │   └── ...
│   │   │   ├── config/              # Configuration classes
│   │   │   │   ├── DatabaseConfig.java
│   │   │   │   ├── KafkaConfig.java
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── security/            # Security configuration
│   │   │   │   └── JwtAuthenticationFilter.java
│   │   │   ├── exception/           # Exception handling
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── engine/              # Workflow engine
│   │   │   │   ├── WorkflowExecutor.java
│   │   │   │   ├── NodeExecutor.java
│   │   │   │   └── ExecutionContext.java
│   │   │   ├── channel/              # Channel implementations
│   │   │   │   ├── email/          # Email channel
│   │   │   │   ├── sms/             # SMS channel
│   │   │   │   ├── push/            # Push channel
│   │   │   │   └── ...
│   │   │   ├── trigger/             # Trigger implementations
│   │   │   │   ├── api/             # API trigger
│   │   │   │   ├── schedule/        # Schedule trigger
│   │   │   │   ├── file/            # File trigger
│   │   │   │   └── event/           # Event trigger
│   │   │   └── util/                # Utility classes
│   │   └── resources/
│   │       ├── application.yml      # Application configuration
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/        # Flyway migrations
│   │           └── V1__Initial_schema.sql
│   └── test/
│       ├── java/                    # Test source code
│       └── resources/               # Test resources
├── pom.xml                          # Maven build file
└── Dockerfile
```

## Key Components

### API Layer
- **Controllers**: REST controllers (@RestController)
- **DTOs**: Data Transfer Objects for request/response
- **Validation**: Bean Validation annotations
- **Exception Handling**: @ControllerAdvice for global exception handling

### Service Layer
- **Business Logic**: Core business logic
- **Orchestration**: Service orchestration
- **Validation**: Business validation
- **Transactions**: @Transactional for transaction management

### Repository Layer
- **JPA Repositories**: Spring Data JPA repositories
- **Custom Queries**: @Query annotations or QueryDSL
- **Transactions**: Spring transaction management

### Engine Layer
- **Workflow Engine**: Workflow execution engine
- **Node Executor**: Node execution logic
- **Context Manager**: Execution context management

### Channel Layer
- **Channel Implementations**: Notification channel implementations
- **Provider Integrations**: Third-party provider integrations
- **Delivery Tracking**: Delivery status tracking

### Trigger Layer
- **Trigger Implementations**: Trigger type implementations
- **Event Consumers**: Spring Kafka/AMQP consumers
- **Scheduler**: Spring @Scheduled for scheduled triggers

## API Structure

### REST API
- **Base Path**: `/api/v1`
- **Endpoints**: See [API Documentation](../../api/endpoints.md)
- **Format**: JSON
- **Authentication**: API key (optional for MVP)

### Error Handling
- **Error Format**: Standardized error responses via @ControllerAdvice
- **Status Codes**: HTTP status codes
- **Error Logging**: Comprehensive error logging

## Database Access

### Connection Management
- **Connection Pool**: HikariCP (default Spring Boot connection pool)
- **Connection String**: application.yml configuration
- **Migrations**: Flyway or Liquibase for database migrations

### Query Patterns
- **JPA Repositories**: Use Spring Data JPA repositories
- **Custom Queries**: @Query annotations or QueryDSL
- **Transactions**: @Transactional annotation
- **Error Handling**: Spring Data exception handling

## Message Queue Integration

### Kafka
- **Spring Kafka**: Use Spring Kafka for consumer/producer
- **Consumer Groups**: @KafkaListener for consumers
- **Partitioning**: Partition handling via Spring Kafka
- **Offset Management**: Automatic offset management

### RabbitMQ
- **Spring AMQP**: Use Spring AMQP for RabbitMQ
- **Connection Management**: Automatic connection management
- **Queue Management**: @RabbitListener for consumers
- **Message Acknowledgment**: Automatic acknowledgment

## Configuration Management

### Environment Variables
- **Database**: Database connection in application.yml
- **Message Queue**: Kafka/RabbitMQ configuration
- **External Services**: Provider credentials
- **Server**: Server configuration (port, context path)

### Configuration Loading
- **YAML/Properties**: application.yml or application.properties
- **Profiles**: Spring Profiles for different environments
- **Validation**: @ConfigurationProperties with validation

## Logging

### Log Levels
- **Debug**: Debug messages
- **Info**: Informational messages
- **Warn**: Warning messages
- **Error**: Error messages

### Log Format
- **Structured Logging**: Logback with JSON layout (optional)
- **Context**: MDC (Mapped Diagnostic Context) for request context
- **Error Stack Traces**: Stack traces for errors

## Testing

### Unit Tests
- **Test Framework**: JUnit 5
- **Mocking**: Mockito
- **Coverage**: JaCoCo for test coverage

### Integration Tests
- **Database Tests**: @DataJpaTest for repository tests
- **API Tests**: @WebMvcTest for controller tests
- **Service Tests**: @SpringBootTest for full integration tests
- **TestContainers**: Optional for database integration tests

## Deployment

### Build
- **Maven/Gradle**: Build JAR file
- **Docker**: Docker image build
- **Optimization**: Spring Boot layered JAR for optimization

### Runtime
- **Configuration**: Runtime configuration via environment variables
- **Health Checks**: Spring Boot Actuator health endpoints
- **Graceful Shutdown**: Spring Boot graceful shutdown

## Related Documentation

- [Project Structure](./project-structure.md) - Package organization
- [Service Interfaces](./service-interfaces.md) - Service specifications
- [Implementation Guide](./implementation-guide.md) - Step-by-step guide
- [Workflow Context Management](./workflow-context-management.md) - Context management and data flow between nodes
