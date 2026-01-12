# No-Code Notification Platform - Backend

Backend API for the No-Code Notification Platform built with Spring Boot.

## Overview

This backend service provides a RESTful API for managing workflows, templates, channels, triggers, notifications, and analytics for a no-code notification platform.

## Features

- **Workflow Management**: Create, update, and execute workflows with visual node-based definitions
- **Template Management**: Create and manage notification templates with variable substitution
- **Channel Management**: Configure and manage notification channels (Email, SMS, Push)
- **Trigger Management**: Support for API, Schedule, File, and Event (Kafka) triggers
- **Notification Service**: Send notifications through configured channels
- **Analytics**: Track and analyze workflow executions and notification deliveries
- **Execution Tracking**: Monitor workflow executions with detailed node execution status

## Technology Stack

- **Java 17+**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **PostgreSQL** (Database)
- **Kafka** (Event Streaming)
- **Maven** (Build Tool)
- **Docker** (Containerization)

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- Docker and Docker Compose (for local development)
- Kafka (for event triggers)

## Quick Start

### Using Docker Compose

1. Clone the repository:
```bash
git clone <repository-url>
cd workflow/backend
```

2. Start all services:
```bash
docker-compose up -d
```

This will start:
- PostgreSQL database
- Kafka and Zookeeper
- Backend application

3. Access the API:
- API Base URL: `http://localhost:8080`
- Health Check: `http://localhost:8080/actuator/health`

### Manual Setup

1. **Database Setup**:
```bash
# Create PostgreSQL database
createdb notification_platform

# Or use Docker
docker run -d --name postgres \
  -e POSTGRES_DB=notification_platform \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15
```

2. **Kafka Setup** (for event triggers):
```bash
# Using Docker Compose
docker-compose up -d kafka zookeeper
```

3. **Configure Application**:
   - Copy `.env.example` to `.env` (if exists)
   - Update `application.yml` with your database and Kafka settings

4. **Build and Run**:
```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

## Configuration

### Application Properties

Key configuration in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/notification_platform
    username: postgres
    password: postgres
  
  kafka:
    bootstrap-servers: localhost:9092

server:
  port: 8080
```

### Environment Variables

- `SPRING_DATASOURCE_URL`: Database connection URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka bootstrap servers
- `SMTP_HOST`: SMTP server host (for email channel)
- `SMTP_PORT`: SMTP server port
- `SMTP_USERNAME`: SMTP username
- `SMTP_PASSWORD`: SMTP password

## API Endpoints

### Workflows
- `GET /workflows` - List workflows
- `GET /workflows/{id}` - Get workflow
- `POST /workflows` - Create workflow
- `PUT /workflows/{id}` - Update workflow
- `DELETE /workflows/{id}` - Delete workflow
- `POST /workflows/{id}/execute` - Execute workflow

### Templates
- `GET /templates` - List templates
- `GET /templates/{id}` - Get template
- `POST /templates` - Create template
- `PUT /templates/{id}` - Update template
- `DELETE /templates/{id}` - Delete template
- `POST /templates/render` - Render template

### Channels
- `GET /channels` - List channels
- `GET /channels/{id}` - Get channel
- `POST /channels` - Create channel
- `PUT /channels/{id}` - Update channel
- `DELETE /channels/{id}` - Delete channel
- `POST /channels/{id}/test` - Test channel connection
- `POST /channels/email/send` - Send email
- `POST /channels/sms/send` - Send SMS
- `POST /channels/push/send` - Send push notification

### Triggers
- `GET /triggers` - List triggers
- `GET /triggers/{id}` - Get trigger
- `POST /triggers/api` - Create API trigger
- `POST /triggers/schedule` - Create schedule trigger
- `POST /triggers/file` - Create file trigger
- `POST /triggers/event` - Create event trigger
- `PUT /triggers/{id}` - Update trigger
- `DELETE /triggers/{id}` - Delete trigger

### Notifications
- `GET /notifications` - List notifications
- `GET /notifications/{id}` - Get notification
- `GET /notifications/{id}/status` - Get notification status
- `POST /notifications/send` - Send notification

### Executions
- `GET /executions` - List executions
- `GET /executions/{id}` - Get execution status

### Analytics
- `GET /analytics/workflows/{id}` - Get workflow analytics
- `GET /analytics/workflows` - Get all workflows analytics
- `GET /analytics/delivery` - Get delivery analytics
- `GET /analytics/channels/{id}` - Get channel analytics
- `GET /analytics/channels` - Get all channels analytics

### File Uploads
- `POST /triggers/file/{triggerId}/upload` - Upload file
- `GET /triggers/file/{triggerId}/uploads/{fileId}` - Get file upload status

## Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test
```bash
mvn test -Dtest=WorkflowServiceTest
```

### Test Coverage
```bash
mvn clean test jacoco:report
# View report at: target/site/jacoco/index.html
```

## Database Schema

The database schema is managed automatically by Hibernate/JPA using `spring.jpa.hibernate.ddl-auto` setting.

### Schema Management
- **Development**: Set `JPA_DDL_AUTO=update` to automatically update schema on startup
- **Production**: Set `JPA_DDL_AUTO=none` and manage schema manually or use a separate migration tool

### Schema Configuration
See `application.yml` for JPA configuration:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: ${JPA_DDL_AUTO:update}
```

## Building

### Build JAR
```bash
mvn clean package
```

### Build Docker Image
```bash
docker build -t notification-platform-backend .
```

## Deployment

### Docker Deployment
```bash
docker-compose up -d
```

### Production Deployment

1. **Build the application**:
```bash
mvn clean package -DskipTests
```

2. **Run the application**:
```bash
java -jar target/notification-platform-1.0.0-SNAPSHOT.jar
```

3. **With environment variables**:
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/notification_platform
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=prod_password
java -jar target/notification-platform-1.0.0-SNAPSHOT.jar
```

## Monitoring

### Actuator Endpoints

- Health: `GET /actuator/health`
- Info: `GET /actuator/info`
- Metrics: `GET /actuator/metrics`

## Troubleshooting

### Database Connection Issues
- Verify PostgreSQL is running
- Check database credentials in `application.yml`
- Ensure database exists

### Kafka Connection Issues
- Verify Kafka is running: `docker ps | grep kafka`
- Check Kafka bootstrap servers configuration
- Verify network connectivity

### Port Already in Use
- Change port in `application.yml`: `server.port: 8081`
- Or stop the process using port 8080

## Development

### Project Structure
```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/notificationplatform/
│   │   │   ├── controller/     # REST controllers
│   │   │   ├── service/        # Business logic
│   │   │   ├── repository/     # Data access
│   │   │   ├── entity/         # JPA entities
│   │   │   ├── dto/            # Data transfer objects
│   │   │   ├── exception/      # Exception handling
│   │   │   ├── engine/         # Workflow engine
│   │   │   └── config/         # Configuration
│   │   └── resources/
│   │       └── application.yml # Application config
│   └── test/                   # Tests
├── pom.xml                     # Maven configuration
├── Dockerfile                  # Docker image
└── docker-compose.yml          # Docker Compose config
```

## Contributing

1. Create a feature branch
2. Make your changes
3. Add tests
4. Ensure all tests pass
5. Submit a pull request

## License

[Your License Here]
