#!/bin/bash

# Stop Local Development Environment
# This script stops all running services

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
echo -e "${BLUE}Stopping Notification Platform${NC}"
echo -e "${BLUE}========================================${NC}"

# Stop frontend
echo -e "\n${BLUE}[1/3] Stopping frontend...${NC}"
if [ -f "$PROJECT_ROOT/frontend/.frontend.pid" ]; then
    FRONTEND_PID=$(cat "$PROJECT_ROOT/frontend/.frontend.pid")
    if ps -p $FRONTEND_PID > /dev/null 2>&1; then
        kill $FRONTEND_PID 2>/dev/null || true
        echo -e "${GREEN}Frontend stopped${NC}"
    else
        echo -e "${YELLOW}Frontend process not found${NC}"
    fi
    rm -f "$PROJECT_ROOT/frontend/.frontend.pid"
else
    # Try to kill by port
    if lsof -Pi :5173 -sTCP:LISTEN -t >/dev/null 2>&1; then
        kill $(lsof -t -i:5173) 2>/dev/null || true
        echo -e "${GREEN}Frontend stopped (by port)${NC}"
    else
        echo -e "${YELLOW}Frontend not running${NC}"
    fi
fi

# Stop backend
echo -e "\n${BLUE}[2/3] Stopping backend...${NC}"
if [ -f "$PROJECT_ROOT/backend/.backend.pid" ]; then
    BACKEND_PID=$(cat "$PROJECT_ROOT/backend/.backend.pid")
    if ps -p $BACKEND_PID > /dev/null 2>&1; then
        kill $BACKEND_PID 2>/dev/null || true
        echo -e "${GREEN}Backend stopped${NC}"
    else
        echo -e "${YELLOW}Backend process not found${NC}"
    fi
    rm -f "$PROJECT_ROOT/backend/.backend.pid"
else
    # Try to kill by port
    if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1; then
        kill $(lsof -t -i:8080) 2>/dev/null || true
        echo -e "${GREEN}Backend stopped (by port)${NC}"
    else
        echo -e "${YELLOW}Backend not running${NC}"
    fi
fi

# Stop infrastructure services
echo -e "\n${BLUE}[3/3] Stopping infrastructure services...${NC}"
cd "$PROJECT_ROOT/backend"

# Use docker compose (v2) or docker-compose (v1)
if docker compose version > /dev/null 2>&1; then
    COMPOSE_CMD="docker compose"
elif docker-compose version > /dev/null 2>&1; then
    COMPOSE_CMD="docker-compose"
else
    COMPOSE_CMD="docker compose"  # Default to v2
fi

if $COMPOSE_CMD ps 2>/dev/null | grep -q "Up"; then
    $COMPOSE_CMD down
    echo -e "${GREEN}Infrastructure services stopped${NC}"
else
    echo -e "${YELLOW}Infrastructure services not running${NC}"
fi

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}All services stopped${NC}"
echo -e "${GREEN}========================================${NC}"

