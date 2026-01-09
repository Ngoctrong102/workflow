#!/bin/bash

# Restart Backend Application Only
# This script stops and restarts only the backend application (not Docker services)
# Fixes permission issues and cleans up before restart

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
echo -e "${BLUE}Restarting Backend Application${NC}"
echo -e "${BLUE}========================================${NC}"

# Step 1: Stop existing backend
echo -e "\n${BLUE}[1/4] Stopping existing backend...${NC}"

if [ -f "$BACKEND_DIR/.backend.pid" ]; then
    BACKEND_PID=$(cat "$BACKEND_DIR/.backend.pid")
    
    if ps -p $BACKEND_PID > /dev/null 2>&1; then
        echo -e "${YELLOW}Stopping backend process (PID: $BACKEND_PID)...${NC}"
        kill $BACKEND_PID 2>/dev/null || true
        sleep 3
        
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
    rm -f "$BACKEND_DIR/.backend.pid"
fi

# Also kill any process on port 8080
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

# Kill any Maven processes
MAVEN_PIDS=$(pgrep -f "spring-boot:run" 2>/dev/null || true)
if [ -n "$MAVEN_PIDS" ]; then
    echo -e "${YELLOW}Stopping Maven processes...${NC}"
    for maven_pid in $MAVEN_PIDS; do
        kill $maven_pid 2>/dev/null || true
    done
    sleep 2
    for maven_pid in $MAVEN_PIDS; do
        if ps -p $maven_pid > /dev/null 2>&1; then
            kill -9 $maven_pid 2>/dev/null || true
        fi
    done
fi

echo -e "${GREEN}Backend stopped${NC}"

# Step 2: Fix permissions and clean target directory
echo -e "\n${BLUE}[2/4] Fixing permissions and cleaning build directory...${NC}"

# Fix permissions on target directory (in case files were created by root)
if [ -d "$BACKEND_DIR/target" ]; then
    echo -e "${YELLOW}Fixing permissions on target directory...${NC}"
    sudo chown -R $USER:$USER "$BACKEND_DIR/target" 2>/dev/null || true
    chmod -R u+w "$BACKEND_DIR/target" 2>/dev/null || true
fi

# Clean target directory to avoid permission issues
echo -e "${YELLOW}Cleaning target directory...${NC}"
cd "$BACKEND_DIR"
rm -rf target/classes/* 2>/dev/null || true
rm -rf target/generated-sources 2>/dev/null || true

echo -e "${GREEN}Build directory cleaned${NC}"

# Step 3: Check prerequisites
echo -e "\n${BLUE}[3/4] Checking prerequisites...${NC}"

# Check Java
if ! command -v java &> /dev/null; then
    echo -e "${RED}Error: Java is not installed. Please install Java 17 or higher.${NC}"
    exit 1
fi

JAVA_VERSION_OUTPUT=$(java -version 2>&1 | head -n 1)
JAVA_MAJOR=$(echo "$JAVA_VERSION_OUTPUT" | sed -E 's/.*version "([0-9]+)(\.[0-9]+)?.*/\1/' 2>/dev/null || echo "0")

if [ "$JAVA_MAJOR" -lt 17 ] 2>/dev/null; then
    echo -e "${RED}Error: Java 17 or higher is required. Current: $JAVA_VERSION_OUTPUT${NC}"
    exit 1
fi

echo -e "${GREEN}Java: $JAVA_VERSION_OUTPUT${NC}"

# Check Maven
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Error: Maven is not installed. Please install Maven.${NC}"
    exit 1
fi

echo -e "${GREEN}Maven: $(mvn -version | head -n 1)${NC}"

# Check if Docker services are running
echo -e "${YELLOW}Checking Docker services...${NC}"
if ! docker ps | grep -q notification-platform-db; then
    echo -e "${YELLOW}Warning: PostgreSQL container is not running.${NC}"
    echo -e "${YELLOW}Run './scripts/start-local.sh' to start all services.${NC}"
fi

# Step 4: Start backend with clean log
echo -e "\n${BLUE}[4/4] Starting backend application...${NC}"

# Load environment variables if .env exists
PROJECT_ROOT="$(cd "$BACKEND_DIR/.." && pwd)"
if [ -f "$PROJECT_ROOT/.env" ]; then
    echo -e "${YELLOW}Loading environment variables from .env...${NC}"
    set -a
    source "$PROJECT_ROOT/.env"
    set +a
fi

# Clear old log file and create new one
if [ -f "$BACKEND_DIR/app.log" ]; then
    echo -e "${YELLOW}Clearing old log file...${NC}"
    > "$BACKEND_DIR/app.log"
fi

# Start backend with Maven (using --batch-mode to reduce ANSI codes)
# Redirect both stdout and stderr, strip ANSI codes
echo -e "${GREEN}Starting backend with hot reload enabled...${NC}"
echo -e "${YELLOW}Note: Code changes will automatically trigger reload${NC}"
echo ""

cd "$BACKEND_DIR"

# Use script to strip ANSI codes from Maven output
# Maven --batch-mode reduces color output, but we'll also strip any remaining codes
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
        echo -e "${YELLOW}Last 50 lines of log:${NC}"
        tail -50 "$BACKEND_DIR/app.log" | sed "s/\x1b\[[0-9;]*m//g"
        exit 1
    fi
    echo -n "."
    sleep 1
done

# Summary
echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}Backend restarted successfully!${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "\n${BLUE}Backend API:${NC}"
echo -e "  ${GREEN}✓${NC} API:    http://localhost:8080/api/v1"
echo -e "  ${GREEN}✓${NC} Health: http://localhost:8080/api/v1/actuator/health"
echo -e "  ${GREEN}✓${NC} Swagger: http://localhost:8080/api/v1/swagger-ui.html"
echo -e "\n${BLUE}Hot Reload:${NC}"
echo -e "  ${GREEN}✓${NC} Enabled - Code changes will automatically reload"
echo -e "\n${BLUE}Logs:${NC}"
echo -e "  Backend:  $BACKEND_DIR/app.log (ANSI codes removed)"
echo -e "  View:     tail -f $BACKEND_DIR/app.log"
echo -e "\n${YELLOW}Backend PID: $BACKEND_PID (saved to $BACKEND_DIR/.backend.pid)${NC}"

