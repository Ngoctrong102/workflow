#!/bin/bash

# Start Backend Application Locally with Hot Reload
# This script starts infrastructure services in Docker and backend app on host with hot reload

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Starting Backend Services (Local Dev)${NC}"
echo -e "${BLUE}========================================${NC}"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}Error: Docker is not running. Please start Docker first.${NC}"
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo -e "${RED}Error: Docker Compose is not installed.${NC}"
    exit 1
fi

# Function to check if port is in use
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        return 0
    else
        return 1
    fi
}

# Check for port conflicts
echo -e "${YELLOW}Checking for port conflicts...${NC}"
PORTS=(5432 9092 5672 15672 2181 6379)
CONFLICTS=()

for port in "${PORTS[@]}"; do
    if check_port $port; then
        # Check if it's our container
        case $port in
            5432) container="notification-platform-db" ;;
            9092) container="notification-platform-kafka" ;;
            5672|15672) container="notification-platform-rabbitmq" ;;
            2181) container="notification-platform-zookeeper" ;;
            6379) container="notification-platform-redis" ;;
            *) container="" ;;
        esac
        
        if [ -z "$container" ] || ! docker ps --format '{{.Names}}' | grep -q "^${container}$"; then
            CONFLICTS+=($port)
        fi
    fi
done

if [ ${#CONFLICTS[@]} -gt 0 ]; then
    echo -e "${YELLOW}Warning: The following ports are already in use: ${CONFLICTS[*]}${NC}"
    echo -e "${YELLOW}This might cause conflicts. Continuing anyway...${NC}"
fi

# Step 1: Start infrastructure services in Docker (without backend app)
echo -e "\n${BLUE}[1/2] Starting infrastructure services in Docker (PostgreSQL, Kafka, RabbitMQ, Redis)...${NC}"
cd "$BACKEND_DIR"

# Use docker compose (v2) or docker-compose (v1)
if docker compose version > /dev/null 2>&1; then
    COMPOSE_CMD="docker compose"
elif docker-compose version > /dev/null 2>&1; then
    COMPOSE_CMD="docker-compose"
else
    echo -e "${RED}Error: Docker Compose is not installed.${NC}"
    exit 1
fi

# Start only infrastructure services (exclude backend if it exists in compose)
if $COMPOSE_CMD ps 2>/dev/null | grep -q "Up"; then
    echo -e "${YELLOW}Some services are already running. Restarting...${NC}"
    $COMPOSE_CMD down
fi

# Start infrastructure services
$COMPOSE_CMD up -d postgres zookeeper kafka rabbitmq redis

# Wait for services to be healthy
echo -e "${YELLOW}Waiting for services to be ready...${NC}"

# Wait for PostgreSQL
echo -n "Waiting for PostgreSQL..."
for i in {1..30}; do
    if docker exec notification-platform-db pg_isready -U postgres > /dev/null 2>&1; then
        echo -e " ${GREEN}✓${NC}"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e " ${RED}✗${NC}"
        echo -e "${RED}PostgreSQL failed to start${NC}"
        exit 1
    fi
    echo -n "."
    sleep 1
done

# Wait for Kafka
echo -n "Waiting for Kafka..."
for i in {1..60}; do
    # Check if container is running
    if ! docker ps | grep -q notification-platform-kafka; then
        if [ $i -eq 60 ]; then
            echo -e " ${RED}✗${NC}"
            echo -e "${RED}Kafka container is not running${NC}"
            $COMPOSE_CMD logs kafka | tail -20
            exit 1
        fi
        echo -n "."
        sleep 1
        continue
    fi
    
    # Check if Kafka is ready
    if docker exec notification-platform-kafka kafka-broker-api-versions --bootstrap-server localhost:9092 > /dev/null 2>&1; then
        echo -e " ${GREEN}✓${NC}"
        break
    fi
    if [ $i -eq 60 ]; then
        echo -e " ${RED}✗${NC}"
        echo -e "${RED}Kafka failed to start. Checking logs...${NC}"
        $COMPOSE_CMD logs kafka | tail -30
        exit 1
    fi
    echo -n "."
    sleep 2
done

# Wait for RabbitMQ
echo -n "Waiting for RabbitMQ..."
for i in {1..30}; do
    if docker exec notification-platform-rabbitmq rabbitmq-diagnostics ping > /dev/null 2>&1; then
        echo -e " ${GREEN}✓${NC}"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e " ${RED}✗${NC}"
        echo -e "${RED}RabbitMQ failed to start${NC}"
        exit 1
    fi
    echo -n "."
    sleep 1
done

# Wait for Redis
echo -n "Waiting for Redis..."
for i in {1..30}; do
    if docker exec notification-platform-redis redis-cli ping > /dev/null 2>&1; then
        echo -e " ${GREEN}✓${NC}"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e " ${RED}✗${NC}"
        echo -e "${RED}Redis failed to start${NC}"
        exit 1
    fi
    echo -n "."
    sleep 1
done

# Step 2: Start backend application on host with hot reload
echo -e "\n${BLUE}[2/2] Starting backend application on host with hot reload...${NC}"
cd "$BACKEND_DIR"

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo -e "${RED}Error: Java is not installed. Please install Java 17 or higher.${NC}"
    exit 1
fi

# Check Java version
JAVA_VERSION_OUTPUT=$(java -version 2>&1 | head -n 1)
JAVA_MAJOR=$(echo "$JAVA_VERSION_OUTPUT" | sed -E 's/.*version "([0-9]+)(\.[0-9]+)?.*/\1/' 2>/dev/null || echo "0")

if [ "$JAVA_MAJOR" -lt 17 ] 2>/dev/null; then
    echo -e "${RED}Error: Java 17 or higher is required. Current: $JAVA_VERSION_OUTPUT${NC}"
    exit 1
fi

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Error: Maven is not installed. Please install Maven.${NC}"
    exit 1
fi

# Check if backend is already running
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo -e "${YELLOW}Port 8080 is already in use. Stopping existing process...${NC}"
    kill $(lsof -t -i:8080) 2>/dev/null || true
    sleep 2
fi

# Check if there's a Maven process already running
if [ -f "$BACKEND_DIR/.backend.pid" ]; then
    OLD_PID=$(cat "$BACKEND_DIR/.backend.pid")
    if ps -p $OLD_PID > /dev/null 2>&1; then
        echo -e "${YELLOW}Stopping existing backend process (PID: $OLD_PID)...${NC}"
        kill $OLD_PID 2>/dev/null || true
        sleep 2
    fi
    rm -f "$BACKEND_DIR/.backend.pid"
fi

# Load environment variables if .env exists
PROJECT_ROOT="$(cd "$BACKEND_DIR/.." && pwd)"
if [ -f "$PROJECT_ROOT/.env" ]; then
    echo -e "${YELLOW}Loading environment variables from .env...${NC}"
    set -a
    source "$PROJECT_ROOT/.env"
    set +a
fi

# Start backend with Maven spring-boot:run (enables hot reload via DevTools)
echo -e "${GREEN}Starting backend with hot reload enabled...${NC}"
echo -e "${YELLOW}Note: Code changes will automatically trigger reload (no restart needed)${NC}"
echo -e "${YELLOW}Press Ctrl+C to stop the backend application${NC}"
echo ""

# Run Maven spring-boot:run in background and save PID
# Use --batch-mode to reduce ANSI color codes, and strip any remaining codes
cd "$BACKEND_DIR"
nohup bash -c 'mvn spring-boot:run --batch-mode 2>&1 | sed "s/\x1b\[[0-9;]*m//g" > app.log 2>&1' &
BACKEND_PID=$!
echo $BACKEND_PID > "$BACKEND_DIR/.backend.pid"

# Wait for backend to start
echo -n "Waiting for backend to start..."
for i in {1..90}; do
    if curl -s http://localhost:8080/api/v1/actuator/health > /dev/null 2>&1; then
        echo -e " ${GREEN}✓${NC}"
        break
    fi
    if [ $i -eq 90 ]; then
        echo -e " ${RED}✗${NC}"
        echo -e "${RED}Backend failed to start. Check logs: $BACKEND_DIR/app.log${NC}"
        tail -50 "$BACKEND_DIR/app.log" | sed "s/\x1b\[[0-9;]*m//g"
        exit 1
    fi
    echo -n "."
    sleep 1
done

# Summary
echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}All services started successfully!${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "\n${BLUE}Services:${NC}"
echo -e "  ${GREEN}✓${NC} PostgreSQL:     localhost:5432 (Docker)"
echo -e "  ${GREEN}✓${NC} Kafka:          localhost:9092 (Docker)"
echo -e "  ${GREEN}✓${NC} RabbitMQ:       localhost:5672 (Docker)"
echo -e "  ${GREEN}✓${NC} RabbitMQ Mgmt:  http://localhost:15672 (Docker)"
echo -e "  ${GREEN}✓${NC} Redis:          localhost:6379 (Docker)"
echo -e "  ${GREEN}✓${NC} Backend API:    http://localhost:8080/api/v1 (Host - Hot Reload Enabled)"
echo -e "  ${GREEN}✓${NC} Backend Health: http://localhost:8080/api/v1/actuator/health"
echo -e "  ${GREEN}✓${NC} Swagger UI:     http://localhost:8080/api/v1/swagger-ui.html"
echo -e "\n${BLUE}Hot Reload:${NC}"
echo -e "  ${GREEN}✓${NC} Enabled via Spring Boot DevTools"
echo -e "  ${GREEN}✓${NC} Code changes will automatically reload (no restart needed)"
echo -e "  ${GREEN}✓${NC} Watch the console for reload messages"
echo -e "\n${BLUE}Logs:${NC}"
echo -e "  Backend:  $BACKEND_DIR/app.log"
echo -e "  Docker:   docker compose logs -f [service-name]"
echo -e "\n${YELLOW}To stop backend, run:${NC}"
echo -e "  ${BLUE}./backend/scripts/stop-local.sh${NC}"
echo -e "\n${YELLOW}To view backend logs in real-time:${NC}"
echo -e "  ${BLUE}tail -f $BACKEND_DIR/app.log${NC}"
echo -e "\n${YELLOW}Backend PID: $BACKEND_PID (saved to $BACKEND_DIR/.backend.pid)${NC}"
