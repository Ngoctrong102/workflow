#!/bin/bash

# Stop Backend Application Locally
# This script stops the backend application running on host and optionally infrastructure services

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
echo -e "${BLUE}Stopping Backend Application (Local)${NC}"
echo -e "${BLUE}========================================${NC}"

# Stop backend application
if [ -f "$BACKEND_DIR/.backend.pid" ]; then
    BACKEND_PID=$(cat "$BACKEND_DIR/.backend.pid")
    
    if ps -p $BACKEND_PID > /dev/null 2>&1; then
        echo -e "${YELLOW}Stopping backend application (PID: $BACKEND_PID)...${NC}"
        
        # Try graceful shutdown first
        kill $BACKEND_PID 2>/dev/null || true
        sleep 3
        
        # Check if it's still running (might be Maven process)
        if ps -p $BACKEND_PID > /dev/null 2>&1; then
            # Find child processes (Java process started by Maven)
            CHILD_PIDS=$(pgrep -P $BACKEND_PID 2>/dev/null || true)
            if [ -n "$CHILD_PIDS" ]; then
                echo -e "${YELLOW}Stopping child processes...${NC}"
                for child_pid in $CHILD_PIDS; do
                    kill $child_pid 2>/dev/null || true
                done
                sleep 2
            fi
            
            # Force kill if still running
            if ps -p $BACKEND_PID > /dev/null 2>&1; then
                echo -e "${YELLOW}Force killing backend process...${NC}"
                kill -9 $BACKEND_PID 2>/dev/null || true
            fi
        fi
        
        # Also kill any Java process on port 8080
        if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1; then
            PORT_PID=$(lsof -t -i:8080)
            if [ -n "$PORT_PID" ]; then
                echo -e "${YELLOW}Stopping process on port 8080 (PID: $PORT_PID)...${NC}"
                kill $PORT_PID 2>/dev/null || true
                sleep 2
                if ps -p $PORT_PID > /dev/null 2>&1; then
                    kill -9 $PORT_PID 2>/dev/null || true
                fi
            fi
        fi
        
        echo -e "${GREEN}Backend application stopped${NC}"
    else
        echo -e "${YELLOW}Backend process (PID: $BACKEND_PID) is not running${NC}"
    fi
    rm -f "$BACKEND_DIR/.backend.pid"
else
    # Try to find and kill process on port 8080
    if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo -e "${YELLOW}Found process on port 8080, stopping...${NC}"
        PORT_PID=$(lsof -t -i:8080)
        kill $PORT_PID 2>/dev/null || true
        sleep 2
        if ps -p $PORT_PID > /dev/null 2>&1; then
            kill -9 $PORT_PID 2>/dev/null || true
        fi
        echo -e "${GREEN}Backend application stopped${NC}"
    else
        echo -e "${YELLOW}Backend application is not running${NC}"
    fi
fi

# Also check for any Maven processes that might be running spring-boot:run
MAVEN_PIDS=$(pgrep -f "spring-boot:run" 2>/dev/null || true)
if [ -n "$MAVEN_PIDS" ]; then
    echo -e "${YELLOW}Found Maven spring-boot processes, stopping...${NC}"
    for maven_pid in $MAVEN_PIDS; do
        kill $maven_pid 2>/dev/null || true
    done
    sleep 2
    # Force kill if still running
    for maven_pid in $MAVEN_PIDS; do
        if ps -p $maven_pid > /dev/null 2>&1; then
            kill -9 $maven_pid 2>/dev/null || true
        fi
    done
fi

# Ask if user wants to stop infrastructure services
echo -e "\n${YELLOW}Do you want to stop infrastructure services (PostgreSQL, Kafka, RabbitMQ)? [y/N]${NC}"
read -t 5 -r STOP_INFRA || STOP_INFRA="n"

if [[ "$STOP_INFRA" =~ ^[Yy]$ ]]; then
    echo -e "\n${BLUE}Stopping infrastructure services...${NC}"
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
    
    $COMPOSE_CMD down
    echo -e "${GREEN}Infrastructure services stopped${NC}"
else
    echo -e "${YELLOW}Infrastructure services are still running${NC}"
    echo -e "${YELLOW}To stop them manually, run: cd backend && docker compose down${NC}"
fi

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}Backend stopped successfully!${NC}"
echo -e "${GREEN}========================================${NC}"
