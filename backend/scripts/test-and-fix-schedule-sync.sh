#!/bin/bash

set -e

API_BASE_URL="http://localhost:8080/api/v1"
WORKFLOW_ID="0956fc34-b49a-4547-bf0d-1669827fa225"

echo "=========================================="
echo "Testing Schedule Trigger Sync"
echo "=========================================="
echo ""

# Wait for backend to be ready
echo "1. Waiting for backend to be ready..."
MAX_WAIT=60
WAITED=0
while [ $WAITED -lt $MAX_WAIT ]; do
    if curl -s "${API_BASE_URL}/actuator/health" > /dev/null 2>&1; then
        echo "   ✓ Backend is ready"
        break
    fi
    sleep 2
    WAITED=$((WAITED + 2))
    echo "   Waiting... (${WAITED}s/${MAX_WAIT}s)"
done

if [ $WAITED -ge $MAX_WAIT ]; then
    echo "   ✗ Backend not ready after ${MAX_WAIT}s"
    echo "   Please check backend logs: tail -f app.log"
    exit 1
fi

echo ""
echo "2. Getting current workflow..."
WORKFLOW_RESPONSE=$(curl -s "${API_BASE_URL}/workflows/${WORKFLOW_ID}")
if [ $? -ne 0 ] || [ -z "$WORKFLOW_RESPONSE" ]; then
    echo "   ✗ Failed to get workflow"
    exit 1
fi

CURRENT_STATUS=$(echo "$WORKFLOW_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('status', 'unknown'))" 2>/dev/null || echo "unknown")
echo "   Current status: $CURRENT_STATUS"

# Check if workflow has schedule-trigger nodes
SCHEDULE_NODES=$(echo "$WORKFLOW_RESPONSE" | python3 -c "
import sys, json
try:
    w = json.load(sys.stdin)
    d = w.get('definition', {})
    nodes = d.get('nodes', [])
    schedule_nodes = [n for n in nodes if n.get('type') == 'schedule-trigger']
    print(len(schedule_nodes))
except:
    print('0')
" 2>/dev/null || echo "0")

echo "   Schedule-trigger nodes: $SCHEDULE_NODES"
echo ""

# Update workflow to trigger sync
echo "3. Updating workflow to trigger sync..."
UPDATE_RESPONSE=$(curl -s -X PUT "${API_BASE_URL}/workflows/${WORKFLOW_ID}" \
  -H "Content-Type: application/json" \
  -d '{"status": "active"}')

if echo "$UPDATE_RESPONSE" | grep -q "error"; then
    echo "   ✗ Failed to update workflow"
    echo "   Response: $UPDATE_RESPONSE"
    exit 1
fi

echo "   ✓ Workflow updated"
echo ""

# Wait for sync to complete
echo "4. Waiting for sync to complete..."
sleep 5

# Check triggers
echo "5. Checking schedule triggers..."
TRIGGERS_RESPONSE=$(curl -s "${API_BASE_URL}/workflows/${WORKFLOW_ID}/triggers")
TRIGGER_COUNT=$(echo "$TRIGGERS_RESPONSE" | python3 -c "
import sys, json
try:
    triggers = json.load(sys.stdin)
    if isinstance(triggers, list):
        print(len(triggers))
    else:
        print('0')
except:
    print('0')
" 2>/dev/null || echo "0")

echo "   Found $TRIGGER_COUNT trigger(s) via API"

# Also check database directly
DB_TRIGGERS=$(docker-compose exec -T postgres psql -U postgres -d notification_platform -t -c \
  "SELECT COUNT(*) FROM triggers WHERE workflow_id = '${WORKFLOW_ID}' AND deleted_at IS NULL AND type = 'schedule';" 2>&1 | tr -d ' \n')

echo "   Found $DB_TRIGGERS trigger(s) in database"
echo ""

if [ "$TRIGGER_COUNT" -gt 0 ] || [ "$DB_TRIGGERS" -gt 0 ]; then
    echo "=========================================="
    echo "✓ SUCCESS: Schedule triggers synced!"
    echo "=========================================="
    echo ""
    echo "Trigger details:"
    docker-compose exec -T postgres psql -U postgres -d notification_platform -c \
      "SELECT id, type, status, config->>'cronExpression' as cron, config->>'nodeId' as node_id FROM triggers WHERE workflow_id = '${WORKFLOW_ID}' AND deleted_at IS NULL;" 2>&1
else
    echo "=========================================="
    echo "✗ FAILED: No triggers found"
    echo "=========================================="
    echo ""
    echo "Checking logs for errors..."
    tail -100 app.log | grep -i "sync\|schedule.*trigger\|Failed to sync\|error\|exception" | tail -20
    exit 1
fi

echo ""
echo "Done!"

