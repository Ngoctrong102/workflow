#!/bin/bash

# Script để verify webhook workflow setup
# Sử dụng: ./scripts/verify-webhook-setup.sh [WORKFLOW_ID]

set -e

API_BASE_URL="${API_BASE_URL:-http://localhost:8080/api/v1}"
WORKFLOW_ID="${1:-}"

if [ -z "$WORKFLOW_ID" ]; then
    echo "Usage: ./scripts/verify-webhook-setup.sh <WORKFLOW_ID>"
    echo ""
    echo "Or get latest workflow:"
    curl -s "$API_BASE_URL/workflows" | jq -r '.data[-1].id // empty' 2>/dev/null
    exit 1
fi

echo "=== Verifying Webhook Workflow ==="
echo "Workflow ID: $WORKFLOW_ID"
echo ""

# Check workflow
echo "1. Checking Workflow..."
WORKFLOW=$(curl -s "$API_BASE_URL/workflows/$WORKFLOW_ID")
if echo "$WORKFLOW" | grep -q '"id"'; then
    NAME=$(echo "$WORKFLOW" | jq -r '.name // "N/A"')
    STATUS=$(echo "$WORKFLOW" | jq -r '.status // "N/A"')
    echo "   ✓ Workflow found: $NAME (Status: $STATUS)"
else
    echo "   ✗ Workflow not found"
    exit 1
fi

# Check triggers
echo "2. Checking Triggers..."
TRIGGERS=$(curl -s "$API_BASE_URL/workflows/$WORKFLOW_ID/triggers")
TRIGGER_COUNT=$(echo "$TRIGGERS" | jq '.data | length' 2>/dev/null || echo "0")
echo "   Found $TRIGGER_COUNT trigger(s)"

# Check executions
echo "3. Checking Executions..."
EXECUTIONS=$(curl -s "$API_BASE_URL/executions?workflow_id=$WORKFLOW_ID&limit=5")
EXECUTION_COUNT=$(echo "$EXECUTIONS" | jq '.data | length' 2>/dev/null || echo "0")
echo "   Found $EXECUTION_COUNT execution(s)"

if [ "$EXECUTION_COUNT" -gt 0 ]; then
    echo ""
    echo "Recent executions:"
    echo "$EXECUTIONS" | jq -r '.data[] | "  - \(.executionId): \(.status) at \(.startedAt)"' 2>/dev/null
fi

echo ""
echo "=== Verification Complete ==="

