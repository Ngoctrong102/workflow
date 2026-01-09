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

# Function to stop process gracefully
stop_process() {
    local name=$1
    local pid_file=$2
    local port=$3
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file" 2>/dev/null)
        if [ -n "$pid" ] && ps -p $pid > /dev/null 2>&1; then
            echo -n "Stopping $name (PID: $pid)..."
            # Try graceful shutdown first
            kill $pid 2>/dev/null || true
            # Wait up to 5 seconds for graceful shutdown
            for i in {1..5}; do
                if ! ps -p $pid > /dev/null 2>&1; then
                    echo -e " ${GREEN}✓${NC}"
                    rm -f "$pid_file"
                    return 0
                fi
                sleep 1
            done
            # Force kill if still running
            if ps -p $pid > /dev/null 2>&1; then
                kill -9 $pid 2>/dev/null || true
                sleep 1
                echo -e " ${GREEN}✓ (force killed)${NC}"
            fi
            rm -f "$pid_file"
            return 0
        else
            echo -e "${YELLOW}$name process not found${NC}"
            rm -f "$pid_file"
        fi
    fi
    
    # Try to kill by port if PID file doesn't exist or process not found
    if [ -n "$port" ] && lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        local pids=$(lsof -t -i:$port 2>/dev/null)
        if [ -n "$pids" ]; then
            echo -n "Stopping $name (port $port)..."
            for pid in $pids; do
                kill $pid 2>/dev/null || true
            done
            sleep 2
            # Force kill if still running
            pids=$(lsof -t -i:$port 2>/dev/null)
            if [ -n "$pids" ]; then
                for pid in $pids; do
                    kill -9 $pid 2>/dev/null || true
                done
            fi
            echo -e " ${GREEN}✓${NC}"
            return 0
        fi
    fi
    
    echo -e "${YELLOW}$name not running${NC}"
    return 0
}

# Stop frontend
echo -e "\n${BLUE}[1/3] Stopping frontend...${NC}"
stop_process "Frontend" "$PROJECT_ROOT/frontend/.frontend.pid" "5173"

# Stop backend
echo -e "\n${BLUE}[2/3] Stopping backend...${NC}"
stop_process "Backend" "$PROJECT_ROOT/backend/.backend.pid" "8080"

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

# Check if any containers are running
if $COMPOSE_CMD ps 2>/dev/null | grep -q "Up"; then
    echo -n "Stopping infrastructure services..."
    $COMPOSE_CMD down > /dev/null 2>&1
    echo -e " ${GREEN}✓${NC}"
else
    echo -e "${YELLOW}Infrastructure services not running${NC}"
fi

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}All services stopped${NC}"
echo -e "${GREEN}========================================${NC}"

