#!/bin/bash

# Start Local Development Environment
# This script starts all required services for local development

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../" && pwd)"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Starting Notification Platform (Local)${NC}"
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
PORTS=(5432 9092 5672 15672 8080 5173)
CONFLICTS=()

for port in "${PORTS[@]}"; do
    if check_port $port; then
        # Check if it's our container
        case $port in
            5432) container="notification-platform-db" ;;
            9092) container="notification-platform-kafka" ;;
            5672) container="notification-platform-rabbitmq" ;;
            15672) container="notification-platform-rabbitmq" ;;
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

# Step 1: Start infrastructure services
echo -e "\n${BLUE}[1/4] Starting infrastructure services (PostgreSQL, Kafka, RabbitMQ)...${NC}"
cd "$PROJECT_ROOT/backend"

# Use docker compose (v2) or docker-compose (v1)
if docker compose version > /dev/null 2>&1; then
    COMPOSE_CMD="docker compose"
elif docker-compose version > /dev/null 2>&1; then
    COMPOSE_CMD="docker-compose"
else
    echo -e "${RED}Error: Docker Compose is not installed.${NC}"
    exit 1
fi

if $COMPOSE_CMD ps 2>/dev/null | grep -q "Up"; then
    echo -e "${YELLOW}Some services are already running. Restarting...${NC}"
    $COMPOSE_CMD down
fi

$COMPOSE_CMD up -d

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
            docker compose logs kafka | tail -20
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

# Step 2: Build backend
echo -e "\n${BLUE}[2/4] Building backend application...${NC}"
cd "$PROJECT_ROOT/backend"

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo -e "${YELLOW}Maven not found. Checking for existing JAR file...${NC}"
    JAR_FILE=$(find target -name "notification-platform-*.jar" -type f 2>/dev/null | head -n 1)
    if [ -z "$JAR_FILE" ]; then
        echo -e "${YELLOW}No JAR file found. Attempting to build with Docker...${NC}"
        
        # Try to build with Docker using Maven image
        if docker info > /dev/null 2>&1; then
            echo -e "${YELLOW}Building backend with Docker (Maven image)...${NC}"
            if docker run --rm \
                -v "$PROJECT_ROOT/backend:/app" \
                -w /app \
                maven:3.9-eclipse-temurin-17 \
                mvn clean package -DskipTests; then
                
                JAR_FILE=$(find target -name "notification-platform-*.jar" -type f 2>/dev/null | head -n 1)
                if [ -n "$JAR_FILE" ] && [ -f "$JAR_FILE" ]; then
                    echo -e "${GREEN}Backend built successfully with Docker: $JAR_FILE${NC}"
                    BACKEND_SKIP=false
                else
                    echo -e "${RED}Docker build completed but JAR file not found.${NC}"
                    BACKEND_SKIP=true
                fi
            else
                echo -e "${RED}Docker build failed.${NC}"
                echo -e "${YELLOW}Backend will not be started.${NC}"
                echo -e "${YELLOW}To build backend manually, install Maven and run: cd backend && mvn clean package -DskipTests${NC}"
                BACKEND_SKIP=true
            fi
        else
            echo -e "${YELLOW}Docker is not running. Backend will not be started.${NC}"
            echo -e "${YELLOW}To build backend, install Maven and run: cd backend && mvn clean package -DskipTests${NC}"
            BACKEND_SKIP=true
        fi
    else
        echo -e "${GREEN}Found existing JAR: $JAR_FILE${NC}"
        BACKEND_SKIP=false
    fi
else
    # Always build backend to ensure it's up to date
    echo -e "${YELLOW}Building backend JAR...${NC}"
    if mvn clean package -DskipTests; then
        JAR_FILE=$(find target -name "notification-platform-*.jar" -type f 2>/dev/null | head -n 1)
        if [ -n "$JAR_FILE" ] && [ -f "$JAR_FILE" ]; then
            echo -e "${GREEN}Backend built successfully: $JAR_FILE${NC}"
            BACKEND_SKIP=false
        else
            echo -e "${RED}Build completed but JAR file not found.${NC}"
            BACKEND_SKIP=true
        fi
    else
        echo -e "${RED}Backend build failed.${NC}"
        echo -e "${YELLOW}Checking for existing JAR file...${NC}"
        JAR_FILE=$(find target -name "notification-platform-*.jar" -type f 2>/dev/null | head -n 1)
        if [ -n "$JAR_FILE" ] && [ -f "$JAR_FILE" ]; then
            echo -e "${YELLOW}Using existing JAR: $JAR_FILE${NC}"
            BACKEND_SKIP=false
        else
            echo -e "${YELLOW}No JAR file available. Backend will not be started.${NC}"
            BACKEND_SKIP=true
        fi
    fi
fi

# Step 3: Start backend
echo -e "\n${BLUE}[3/4] Starting backend application...${NC}"

if [ "$BACKEND_SKIP" = true ]; then
    echo -e "${YELLOW}Backend JAR not available. Skipping backend startup.${NC}"
    echo -e "${YELLOW}To build and start backend later, run:${NC}"
    echo -e "${YELLOW}  cd backend && mvn clean package -DskipTests && java -jar target/notification-platform-*.jar${NC}"
else
    cd "$PROJECT_ROOT/backend"
    
    # Check if .env file exists, if not use .env.example
    if [ ! -f "$PROJECT_ROOT/.env" ]; then
        if [ -f "$PROJECT_ROOT/.env.example" ]; then
            echo -e "${YELLOW}No .env file found. Copying from .env.example...${NC}"
            cp "$PROJECT_ROOT/.env.example" "$PROJECT_ROOT/.env"
            echo -e "${YELLOW}Please update .env file with your configuration.${NC}"
        fi
    fi

    # Load environment variables
    if [ -f "$PROJECT_ROOT/.env" ]; then
        set -a
        source "$PROJECT_ROOT/.env"
        set +a
    fi

    # Start backend in background
    echo -e "${GREEN}Starting backend on port 8080...${NC}"
fi

if [ "$BACKEND_SKIP" = false ]; then
    # Kill existing backend process if running
    if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo -e "${YELLOW}Port 8080 is in use. Killing existing process...${NC}"
        kill $(lsof -t -i:8080) 2>/dev/null || true
        sleep 2
    fi

    # Start backend
    nohup java -jar "$JAR_FILE" > "$PROJECT_ROOT/backend/app.log" 2>&1 &
    BACKEND_PID=$!
    echo $BACKEND_PID > "$PROJECT_ROOT/backend/.backend.pid"

    # Wait for backend to start
    echo -n "Waiting for backend to start..."
    for i in {1..60}; do
        if curl -s http://localhost:8080/api/v1/actuator/health > /dev/null 2>&1; then
            echo -e " ${GREEN}✓${NC}"
            break
        fi
        if [ $i -eq 60 ]; then
            echo -e " ${RED}✗${NC}"
            echo -e "${RED}Backend failed to start. Check logs: $PROJECT_ROOT/backend/app.log${NC}"
            exit 1
        fi
        echo -n "."
        sleep 1
    done
fi

# Step 4: Start frontend
echo -e "\n${BLUE}[4/4] Starting frontend application...${NC}"
cd "$PROJECT_ROOT/frontend"

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    echo -e "${YELLOW}Installing frontend dependencies...${NC}"
    npm install
fi

# Kill existing frontend process if running
if lsof -Pi :5173 -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo -e "${YELLOW}Port 5173 is in use. Killing existing process...${NC}"
    kill $(lsof -t -i:5173) 2>/dev/null || true
    sleep 2
fi

# Start frontend in background
echo -e "${GREEN}Starting frontend on port 5173...${NC}"
nohup npm run dev > "$PROJECT_ROOT/frontend/app.log" 2>&1 &
FRONTEND_PID=$!
echo $FRONTEND_PID > "$PROJECT_ROOT/frontend/.frontend.pid"

# Wait for frontend to start
echo -n "Waiting for frontend to start..."
for i in {1..30}; do
    if curl -s http://localhost:5173 > /dev/null 2>&1; then
        echo -e " ${GREEN}✓${NC}"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e " ${RED}✗${NC}"
        echo -e "${YELLOW}Frontend may still be starting. Check logs: $PROJECT_ROOT/frontend/app.log${NC}"
        break
    fi
    echo -n "."
    sleep 1
done

# Summary
echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}All services started successfully!${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "\n${BLUE}Services:${NC}"
echo -e "  ${GREEN}✓${NC} PostgreSQL:     localhost:5432"
echo -e "  ${GREEN}✓${NC} Kafka:          localhost:9092"
echo -e "  ${GREEN}✓${NC} RabbitMQ:       localhost:5672 (Management: http://localhost:15672)"
if [ "$BACKEND_SKIP" = false ]; then
    echo -e "  ${GREEN}✓${NC} Backend API:    http://localhost:8080/api/v1"
    echo -e "  ${GREEN}✓${NC} Backend Health: http://localhost:8080/api/v1/actuator/health"
else
    echo -e "  ${YELLOW}⚠${NC} Backend:        Not started (JAR not found)"
fi
echo -e "  ${GREEN}✓${NC} Frontend:       http://localhost:5173"
echo -e "\n${BLUE}Logs:${NC}"
if [ "$BACKEND_SKIP" = false ]; then
    echo -e "  Backend:  $PROJECT_ROOT/backend/app.log"
fi
echo -e "  Frontend: $PROJECT_ROOT/frontend/app.log"
echo -e "\n${YELLOW}To stop all services, run:${NC}"
echo -e "  ${BLUE}./infrastructure/scripts/stop-local.sh${NC}"
if [ "$BACKEND_SKIP" = false ]; then
    echo -e "\n${YELLOW}Process IDs saved to:${NC}"
    echo -e "  Backend:  $PROJECT_ROOT/backend/.backend.pid"
    echo -e "  Frontend: $PROJECT_ROOT/frontend/.frontend.pid"
else
    echo -e "\n${YELLOW}Process IDs saved to:${NC}"
    echo -e "  Frontend: $PROJECT_ROOT/frontend/.frontend.pid"
fi

