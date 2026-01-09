#!/bin/bash

# Script test để kiểm tra các API endpoints
# Sử dụng: ./scripts/test-workflow-api.sh

set -e

API_BASE_URL="${API_BASE_URL:-http://localhost:8080/api/v1}"

echo "=== Testing Workflow API ==="
echo "API Base URL: $API_BASE_URL"
echo ""

# Test 1: Health check
echo "1. Testing Health Check..."
HEALTH=$(curl -s "$API_BASE_URL/actuator/health")
if echo "$HEALTH" | grep -q '"status":"UP"'; then
    echo "   ✓ Health check passed"
else
    echo "   ✗ Health check failed"
    exit 1
fi

# Test 2: List channels
echo "2. Testing List Channels..."
CHANNELS=$(curl -s "$API_BASE_URL/channels")
if echo "$CHANNELS" | grep -q '"data"'; then
    CHANNEL_COUNT=$(echo "$CHANNELS" | jq '.data | length' 2>/dev/null || echo "0")
    echo "   ✓ Channels API works (found $CHANNEL_COUNT channels)"
else
    echo "   ✗ Channels API failed"
    exit 1
fi

# Test 3: List templates
echo "3. Testing List Templates..."
TEMPLATES=$(curl -s "$API_BASE_URL/templates")
if echo "$TEMPLATES" | grep -q '"data"'; then
    TEMPLATE_COUNT=$(echo "$TEMPLATES" | jq '.data | length' 2>/dev/null || echo "0")
    echo "   ✓ Templates API works (found $TEMPLATE_COUNT templates)"
else
    echo "   ✗ Templates API failed"
    exit 1
fi

# Test 4: List workflows
echo "4. Testing List Workflows..."
WORKFLOWS=$(curl -s "$API_BASE_URL/workflows")
if echo "$WORKFLOWS" | grep -q '"workflows"\|"data"'; then
    WORKFLOW_COUNT=$(echo "$WORKFLOWS" | jq '.workflows // .data | length' 2>/dev/null || echo "0")
    echo "   ✓ Workflows API works (found $WORKFLOW_COUNT workflows)"
else
    echo "   ✗ Workflows API failed"
    exit 1
fi

# Test 5: List executions
echo "5. Testing List Executions..."
EXECUTIONS=$(curl -s "$API_BASE_URL/executions")
if echo "$EXECUTIONS" | grep -q '"data"'; then
    EXECUTION_COUNT=$(echo "$EXECUTIONS" | jq '.data | length' 2>/dev/null || echo "0")
    echo "   ✓ Executions API works (found $EXECUTION_COUNT executions)"
else
    echo "   ✗ Executions API failed"
    exit 1
fi

echo ""
echo "=== All API Tests Passed ==="

