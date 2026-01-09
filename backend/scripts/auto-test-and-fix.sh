#!/bin/bash

# Auto test and fix script for Dashboard API
# This script will test the API, check for errors, and attempt to fix them

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080/api/v1"
WORKFLOW_ID="0956fc34-b49a-4547-bf0d-1669827fa225"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
MAX_ITERATIONS=10
ITERATION=0

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Auto Test and Fix Dashboard API${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Function to check if backend is running
check_backend() {
    if curl -s "${BASE_URL}/actuator/health" > /dev/null 2>&1; then
        return 0
    else
        return 1
    fi
}

# Function to test an endpoint
test_endpoint() {
    local endpoint=$1
    local name=$2
    
    response=$(curl -s -w "\n%{http_code}" "${endpoint}" 2>&1)
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" -eq 200 ]; then
        # Check if response contains error
        if echo "$body" | grep -qi "error\|exception\|SQLGrammarException\|InvalidDataAccessResourceUsageException"; then
            echo -e "  ${RED}✗ FAILED${NC} - Response contains error"
            echo -e "  ${RED}Error: ${body:0:300}...${NC}"
            return 1
        else
            echo -e "  ${GREEN}✓ PASSED${NC}"
            return 0
        fi
    else
        echo -e "  ${RED}✗ FAILED${NC} - HTTP ${http_code}"
        echo -e "  ${RED}Response: ${body:0:200}...${NC}"
        return 1
    fi
}

# Function to check log for errors
check_log_errors() {
    local log_file="${BACKEND_DIR}/app.log"
    if [ -f "$log_file" ]; then
        # Check for SQL errors in the last 100 lines
        local errors=$(tail -100 "$log_file" | grep -i "SQLGrammarException\|InvalidDataAccessResourceUsageException\|could not determine data type\|syntax error" | wc -l)
        if [ "$errors" -gt 0 ]; then
            echo -e "${YELLOW}Found $errors SQL errors in log${NC}"
            tail -100 "$log_file" | grep -i "SQLGrammarException\|InvalidDataAccessResourceUsageException\|could not determine data type\|syntax error" | head -5
            return 1
        fi
    fi
    return 0
}

# Main loop
while [ $ITERATION -lt $MAX_ITERATIONS ]; do
    ITERATION=$((ITERATION + 1))
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}Iteration $ITERATION/$MAX_ITERATIONS${NC}"
    echo -e "${BLUE}========================================${NC}"
    
    # Check if backend is running
    echo -e "${YELLOW}Checking backend status...${NC}"
    if ! check_backend; then
        echo -e "${RED}Backend is not running. Starting backend...${NC}"
        cd "$BACKEND_DIR"
        # Start backend in background
        nohup mvn --batch-mode spring-boot:run > app.log 2>&1 &
        BACKEND_PID=$!
        echo $BACKEND_PID > .backend.pid
        echo -e "${YELLOW}Waiting for backend to start...${NC}"
        
        # Wait for backend to start (max 60 seconds)
        for i in {1..60}; do
            if check_backend; then
                echo -e "${GREEN}Backend started successfully${NC}"
                break
            fi
            if [ $i -eq 60 ]; then
                echo -e "${RED}Backend failed to start after 60 seconds${NC}"
                exit 1
            fi
            sleep 1
        done
    else
        echo -e "${GREEN}Backend is running${NC}"
    fi
    
    # Wait a bit for backend to be ready
    sleep 2
    
    # Test endpoints
    echo -e "\n${YELLOW}Testing endpoints...${NC}"
    FAILED=0
    
    echo -e "${BLUE}1. Testing Dashboard Overview${NC}"
    if ! test_endpoint "${BASE_URL}/workflows/${WORKFLOW_ID}/dashboard" "Dashboard Overview"; then
        FAILED=$((FAILED + 1))
    fi
    
    echo -e "${BLUE}2. Testing Channel Performance${NC}"
    if ! test_endpoint "${BASE_URL}/workflows/${WORKFLOW_ID}/dashboard/channels" "Channel Performance"; then
        FAILED=$((FAILED + 1))
    fi
    
    echo -e "${BLUE}3. Testing Node Performance${NC}"
    if ! test_endpoint "${BASE_URL}/workflows/${WORKFLOW_ID}/dashboard/nodes" "Node Performance"; then
        FAILED=$((FAILED + 1))
    fi
    
    # Check log for errors
    echo -e "\n${YELLOW}Checking log for errors...${NC}"
    if check_log_errors; then
        echo -e "${GREEN}No SQL errors found in log${NC}"
    else
        FAILED=$((FAILED + 1))
    fi
    
    # Summary
    echo -e "\n${BLUE}========================================${NC}"
    if [ $FAILED -eq 0 ]; then
        echo -e "${GREEN}✓ All tests passed!${NC}"
        echo -e "${GREEN}No errors found. Exiting.${NC}"
        exit 0
    else
        echo -e "${RED}✗ Found $FAILED failures${NC}"
        if [ $ITERATION -lt $MAX_ITERATIONS ]; then
            echo -e "${YELLOW}Waiting 5 seconds before next iteration...${NC}"
            sleep 5
        fi
    fi
done

echo -e "\n${RED}========================================${NC}"
echo -e "${RED}Max iterations reached. Some tests failed.${NC}"
echo -e "${RED}========================================${NC}"
exit 1

