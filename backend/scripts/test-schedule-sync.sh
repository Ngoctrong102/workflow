#!/bin/bash

set -e

API_BASE_URL="http://localhost:8080/api/v1"
WORKFLOW_ID="0956fc34-b49a-4547-bf0d-1669827fa225"

echo "Testing schedule trigger sync for workflow: $WORKFLOW_ID"
echo ""

# Get current workflow
echo "1. Getting current workflow..."
WORKFLOW_RESPONSE=$(curl -s "${API_BASE_URL}/workflows/${WORKFLOW_ID}")
echo "Status: $(echo "$WORKFLOW_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('status', 'unknown'))" 2>/dev/null || echo "unknown")"
echo ""

# Update workflow to trigger sync
echo "2. Updating workflow to trigger sync..."
UPDATE_RESPONSE=$(curl -s -X PUT "${API_BASE_URL}/workflows/${WORKFLOW_ID}" \
  -H "Content-Type: application/json" \
  -d '{"status": "active"}')
echo "Update response: $(echo "$UPDATE_RESPONSE" | head -c 200)"
echo ""

# Wait a bit for sync to complete
sleep 3

# Check triggers
echo "3. Checking schedule triggers..."
TRIGGERS=$(curl -s "${API_BASE_URL}/workflows/${WORKFLOW_ID}/triggers")
TRIGGER_COUNT=$(echo "$TRIGGERS" | python3 -c "import sys, json; print(len(json.load(sys.stdin)))" 2>/dev/null || echo "0")
echo "Found $TRIGGER_COUNT trigger(s)"
echo ""

if [ "$TRIGGER_COUNT" -gt 0 ]; then
    echo "$TRIGGERS" | python3 -m json.tool 2>/dev/null || echo "$TRIGGERS"
else
    echo "No triggers found. Checking database directly..."
    docker-compose exec -T postgres psql -U postgres -d notification_platform -c \
      "SELECT id, type, status, config->>'cronExpression' as cron FROM triggers WHERE workflow_id = '${WORKFLOW_ID}' AND deleted_at IS NULL;" 2>&1
fi

echo ""
echo "Done!"

