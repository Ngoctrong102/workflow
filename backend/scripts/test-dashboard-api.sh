#!/bin/bash

# Test script for Dashboard API endpoints
# This script tests the dashboard endpoints and reports errors

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080/api/v1"
WORKFLOW_ID="0956fc34-b49a-4547-bf0d-1669827fa225"

# Test results
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0
ERRORS=()

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Testing Dashboard API Endpoints${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Function to test an endpoint
test_endpoint() {
    local endpoint=$1
    local name=$2
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    echo -e "${YELLOW}Testing: ${name}${NC}"
    echo -e "  ${BLUE}GET ${endpoint}${NC}"
    
    response=$(curl -s -w "\n%{http_code}" "${endpoint}" 2>&1)
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" -eq 200 ]; then
        # Check if response contains error
        if echo "$body" | grep -qi "error\|exception"; then
            echo -e "  ${RED}✗ FAILED${NC} - Response contains error"
            echo -e "  ${RED}Response: ${body:0:200}...${NC}"
            FAILED_TESTS=$((FAILED_TESTS + 1))
            ERRORS+=("${name}: Response contains error")
        else
            echo -e "  ${GREEN}✓ PASSED${NC} (HTTP ${http_code})"
            PASSED_TESTS=$((PASSED_TESTS + 1))
        fi
    else
        echo -e "  ${RED}✗ FAILED${NC} - HTTP ${http_code}"
        echo -e "  ${RED}Response: ${body:0:200}...${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        ERRORS+=("${name}: HTTP ${http_code}")
    fi
    echo ""
}

# Check if backend is running
echo -e "${BLUE}Checking if backend is running...${NC}"
if ! curl -s "${BASE_URL}/actuator/health" > /dev/null 2>&1; then
    echo -e "${RED}✗ Backend is not running at ${BASE_URL}${NC}"
    echo -e "${YELLOW}Please start the backend first:${NC}"
    echo -e "  ${BLUE}./scripts/start-local.sh${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Backend is running${NC}"
echo ""

# Test endpoints
test_endpoint "${BASE_URL}/workflows/${WORKFLOW_ID}/dashboard" "Dashboard Overview"
test_endpoint "${BASE_URL}/workflows/${WORKFLOW_ID}/dashboard/channels" "Channel Performance"
test_endpoint "${BASE_URL}/workflows/${WORKFLOW_ID}/dashboard/nodes" "Node Performance"
test_endpoint "${BASE_URL}/workflows/${WORKFLOW_ID}/dashboard/trends" "Execution Trends"
test_endpoint "${BASE_URL}/workflows/${WORKFLOW_ID}/dashboard/executions" "Execution History"
test_endpoint "${BASE_URL}/workflows/${WORKFLOW_ID}/dashboard/errors" "Error Analysis"

# Test with date parameters
test_endpoint "${BASE_URL}/workflows/${WORKFLOW_ID}/dashboard?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59" "Dashboard Overview (with dates)"
test_endpoint "${BASE_URL}/workflows/${WORKFLOW_ID}/dashboard/channels?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59" "Channel Performance (with dates)"
test_endpoint "${BASE_URL}/workflows/${WORKFLOW_ID}/dashboard/nodes?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59" "Node Performance (with dates)"

# Summary
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Test Summary${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "Total:  ${TOTAL_TESTS}"
echo -e "${GREEN}Passed: ${PASSED_TESTS}${NC}"
echo -e "${RED}Failed: ${FAILED_TESTS}${NC}"
echo ""

if [ ${FAILED_TESTS} -gt 0 ]; then
    echo -e "${RED}Failed Tests:${NC}"
    for error in "${ERRORS[@]}"; do
        echo -e "  ${RED}✗ ${error}${NC}"
    done
    echo ""
    exit 1
else
    echo -e "${GREEN}All tests passed!${NC}"
    exit 0
fi

