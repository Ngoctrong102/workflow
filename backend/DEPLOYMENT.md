# Deployment Guide

This guide covers deployment of the No-Code Notification Platform backend.

## Prerequisites

- Java 17 or higher
- PostgreSQL 12+
- Kafka (for event triggers)
- Docker and Docker Compose (recommended)

## Environment Setup

### 1. Database Configuration

Create a PostgreSQL database:

```bash
createdb notification_platform
```

Or using Docker:
```bash
docker run -d --name postgres \
  -e POSTGRES_DB=notification_platform \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15
```

### 2. Environment Variables

Set the following environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/notification_platform
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export SERVER_PORT=8080
```

### 3. Kafka Setup (Optional)

For event triggers, set up Kafka:

```bash
docker-compose up -d kafka zookeeper
```

## Build

### Build JAR

```bash
mvn clean package -DskipTests
```

The JAR file will be created at: `target/notification-platform-1.0.0-SNAPSHOT.jar`

### Build Docker Image

```bash
docker build -t notification-platform-backend:latest .
```

## Deployment Options

### Option 1: Docker Compose (Recommended)

1. Update `docker-compose.yml` with your configuration
2. Start all services:
```bash
docker-compose up -d
```

This will start:
- PostgreSQL database
- Kafka and Zookeeper
- Backend application

### Option 2: Standalone JAR

1. Build the JAR:
```bash
mvn clean package -DskipTests
```

2. Run the application:
```bash
java -jar target/notification-platform-1.0.0-SNAPSHOT.jar
```

3. With environment variables:
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/notification_platform
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=prod_password
java -jar target/notification-platform-1.0.0-SNAPSHOT.jar
```

### Option 3: Production Server

1. **Copy files to server**:
```bash
scp target/notification-platform-1.0.0-SNAPSHOT.jar user@server:/opt/notification-platform/
scp application.yml user@server:/opt/notification-platform/config/
```

2. **Create systemd service** (`/etc/systemd/system/notification-platform.service`):
```ini
[Unit]
Description=No-Code Notification Platform Backend
After=network.target postgresql.service

[Service]
Type=simple
User=notification-platform
WorkingDirectory=/opt/notification-platform
ExecStart=/usr/bin/java -jar /opt/notification-platform/notification-platform-1.0.0-SNAPSHOT.jar
Restart=always
RestartSec=10
Environment="SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/notification_platform"
Environment="SPRING_DATASOURCE_USERNAME=postgres"
Environment="SPRING_DATASOURCE_PASSWORD=postgres"

[Install]
WantedBy=multi-user.target
```

3. **Start service**:
```bash
sudo systemctl daemon-reload
sudo systemctl enable notification-platform
sudo systemctl start notification-platform
sudo systemctl status notification-platform
```

## Health Checks

After deployment, verify the application is running:

```bash
# Health check
curl http://localhost:8080/api/v1/actuator/health

# Expected response:
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "kafka": {
      "status": "UP"
    }
  }
}
```

## Monitoring

### Actuator Endpoints

- Health: `GET /api/v1/actuator/health`
- Info: `GET /api/v1/actuator/info`
- Metrics: `GET /api/v1/actuator/metrics`
- Prometheus: `GET /api/v1/actuator/prometheus`

### Logs

Application logs are written to console and can be redirected to files:

```bash
java -jar notification-platform-1.0.0-SNAPSHOT.jar > app.log 2>&1
```

Or configure logging in `application.yml` for file output.

## Database Schema

The database schema is managed automatically by Hibernate/JPA. To verify:

1. Check application logs for schema initialization
2. Verify tables exist in database:
```sql
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public';
```

## Troubleshooting

### Application Won't Start

1. Check Java version: `java -version` (should be 17+)
2. Check database connection
3. Check port availability: `netstat -tuln | grep 8080`
4. Check logs for errors

### Database Connection Issues

1. Verify PostgreSQL is running
2. Check database credentials
3. Verify network connectivity
4. Check firewall rules

### Kafka Connection Issues

1. Verify Kafka is running: `docker ps | grep kafka`
2. Check Kafka bootstrap servers configuration
3. Verify network connectivity
4. Note: Kafka is optional for MVP

## Performance Tuning

### Database Connection Pool

Adjust in `application.yml`:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
```

### JVM Options

For production, use appropriate JVM options:

```bash
java -Xms512m -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -jar notification-platform-1.0.0-SNAPSHOT.jar
```

## Security Considerations

1. **Change default passwords** in production
2. **Use environment variables** for sensitive data
3. **Enable HTTPS** in production (configure reverse proxy)
4. **Restrict database access** to application server only
5. **Enable firewall** rules
6. **Regular security updates**

## Backup

### Database Backup

```bash
pg_dump -U postgres notification_platform > backup.sql
```

### Restore

```bash
psql -U postgres notification_platform < backup.sql
```

## Scaling

### Horizontal Scaling

1. Deploy multiple instances behind a load balancer
2. Use shared database
3. Configure session affinity if needed
4. Use external cache (Redis) for shared state

### Vertical Scaling

1. Increase JVM heap size
2. Increase database connection pool
3. Add more CPU/memory to server

## Related Documentation

- [README.md](./README.md) - Setup and configuration
- [API Documentation](./docs/api/) - API endpoints
- [Architecture](./docs/architecture/) - System architecture

