#!/bin/bash

# Script test để kiểm tra workflow creation (không cần Gmail thật)
# Sử dụng: ./scripts/test-workflow-creation.sh

set -e

API_BASE_URL="${API_BASE_URL:-http://localhost:8080/api/v1}"

echo "=== Testing Workflow Creation (Demo) ==="
echo "API Base URL: $API_BASE_URL"
echo ""

# Test 1: Create a simple template
echo "1. Creating Test Template..."
TEMPLATE_RESPONSE=$(curl -s -X POST "$API_BASE_URL/templates" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Template",
    "channel": "email",
    "subject": "Test Subject",
    "body": "Test body with {{currentTime}}",
    "variables": [
      {
        "name": "currentTime",
        "type": "string",
        "description": "Current time"
      }
    ]
  }')

TEMPLATE_ID=$(echo "$TEMPLATE_RESPONSE" | jq -r '.id // empty' 2>/dev/null)

if [ -z "$TEMPLATE_ID" ]; then
    echo "   ✗ Failed to create template"
    echo "   Response: $TEMPLATE_RESPONSE"
    exit 1
fi

echo "   ✓ Template created: $TEMPLATE_ID"

# Test 2: Create a simple workflow
echo "2. Creating Test Workflow..."
WORKFLOW_RESPONSE=$(curl -s -X POST "$API_BASE_URL/workflows" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Test Workflow\",
    \"description\": \"Test workflow for verification\",
    \"status\": \"draft\",
    \"definition\": {
      \"nodes\": [
        {
          \"id\": \"trigger-1\",
          \"type\": \"trigger\",
          \"subtype\": \"schedule\",
          \"position\": {\"x\": 100, \"y\": 100}
        },
        {
          \"id\": \"action-1\",
          \"type\": \"action\",
          \"subtype\": \"send_email\",
          \"position\": {\"x\": 300, \"y\": 100},
          \"data\": {
            \"templateId\": \"$TEMPLATE_ID\",
            \"recipients\": [
              {
                \"email\": \"test@example.com\",
                \"name\": \"Test User\"
              }
            ]
          }
        }
      ],
      \"edges\": [
        {
          \"source\": \"trigger-1\",
          \"target\": \"action-1\"
        }
      ]
    }
  }")

WORKFLOW_ID=$(echo "$WORKFLOW_RESPONSE" | jq -r '.id // empty' 2>/dev/null)

if [ -z "$WORKFLOW_ID" ]; then
    echo "   ✗ Failed to create workflow"
    echo "   Response: $WORKFLOW_RESPONSE"
    exit 1
fi

echo "   ✓ Workflow created: $WORKFLOW_ID"

# Test 3: Get workflow
echo "3. Getting Workflow..."
GET_RESPONSE=$(curl -s "$API_BASE_URL/workflows/$WORKFLOW_ID")
GET_NAME=$(echo "$GET_RESPONSE" | jq -r '.name // empty' 2>/dev/null)

if [ "$GET_NAME" = "Test Workflow" ]; then
    echo "   ✓ Workflow retrieved successfully"
else
    echo "   ✗ Failed to retrieve workflow"
    exit 1
fi

# Test 4: Create schedule trigger
echo "4. Creating Schedule Trigger..."
TRIGGER_RESPONSE=$(curl -s -X POST "$API_BASE_URL/workflows/$WORKFLOW_ID/triggers/schedule" \
  -H "Content-Type: application/json" \
  -d '{
    "cronExpression": "0 */5 * * * *",
    "timezone": "UTC"
  }')

TRIGGER_ID=$(echo "$TRIGGER_RESPONSE" | jq -r '.id // empty' 2>/dev/null)

if [ -z "$TRIGGER_ID" ]; then
    echo "   ✗ Failed to create trigger"
    echo "   Response: $TRIGGER_RESPONSE"
    exit 1
fi

echo "   ✓ Schedule trigger created: $TRIGGER_ID"

# Cleanup
echo ""
echo "5. Cleaning up test data..."
curl -s -X DELETE "$API_BASE_URL/workflows/$WORKFLOW_ID" > /dev/null
curl -s -X DELETE "$API_BASE_URL/templates/$TEMPLATE_ID" > /dev/null
echo "   ✓ Cleanup completed"

echo ""
echo "=== All Tests Passed ==="
echo "Workflow creation and management APIs are working correctly!"

