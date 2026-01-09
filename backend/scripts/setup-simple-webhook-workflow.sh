#!/bin/bash

# Script đơn giản để setup workflow test với webhook (không cần channel)
# Sử dụng: ./scripts/setup-simple-webhook-workflow.sh

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
API_BASE_URL="${API_BASE_URL:-http://localhost:8080/api/v1}"
WEBHOOK_URL="${WEBHOOK_URL:-https://httpbin.org/post}"

echo -e "${GREEN}=== Setup Simple Webhook Workflow Script ===${NC}\n"

# Check if API is accessible
echo "Checking API connection..."
if ! curl -s -f "${API_BASE_URL}/actuator/health" > /dev/null; then
    echo -e "${RED}Error: Cannot connect to API at ${API_BASE_URL}${NC}"
    exit 1
fi
echo -e "${GREEN}✓ API is accessible${NC}\n"

# Step 1: Create Workflow (using direct body, no template needed)
echo -e "\n${GREEN}Step 1: Creating Workflow...${NC}"

WORKFLOW_RESPONSE=$(curl -s -X POST "${API_BASE_URL}/workflows" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Webhook Test mỗi phút\",
    \"description\": \"Workflow test gửi webhook mỗi phút\",
    \"status\": \"active\",
    \"definition\": {
      \"nodes\": [
        {
          \"id\": \"trigger-1\",
          \"type\": \"trigger\",
          \"subtype\": \"schedule\",
          \"position\": {\"x\": 100, \"y\": 200}
        },
        {
          \"id\": \"action-1\",
          \"type\": \"action\",
          \"subtype\": \"send_webhook\",
          \"position\": {\"x\": 300, \"y\": 200},
          \"data\": {
            \"url\": \"${WEBHOOK_URL}\",
            \"method\": \"POST\",
            \"headers\": {
              \"Content-Type\": \"application/json\"
            },
            \"body\": \"{\\\"message\\\":\\\"Xin chào Trọng, bây giờ là {{currentTime}}\\\",\\\"timestamp\\\":\\\"{{_executionTime}}\\\",\\\"date\\\":\\\"{{date}}\\\",\\\"time\\\":\\\"{{time}}\\\",\\\"dateVN\\\":\\\"{{dateVN}}\\\",\\\"dayOfWeek\\\":\\\"{{dayOfWeek}}\\\",\\\"timezone\\\":\\\"{{_timezone|default:UTC}}\\\",\\\"workflowId\\\":\\\"{{workflowId}}\\\"}\"
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

WORKFLOW_ID=$(echo $WORKFLOW_RESPONSE | jq -r '.id // empty' 2>/dev/null)

if [ -z "$WORKFLOW_ID" ]; then
    echo -e "${RED}Error: Failed to create workflow${NC}"
    echo "Response: $WORKFLOW_RESPONSE"
    exit 1
fi

echo -e "${GREEN}✓ Workflow created: ${WORKFLOW_ID}${NC}"

# Step 2: Create Schedule Trigger
echo -e "\n${GREEN}Step 2: Creating Schedule Trigger (every minute)...${NC}"

# Note: Need to provide workflowId in body for validation, controller will use path variable
TRIGGER_RESPONSE=$(curl -s -X POST "${API_BASE_URL}/workflows/${WORKFLOW_ID}/triggers/schedule" \
  -H "Content-Type: application/json" \
  -d "{
    \"workflowId\": \"${WORKFLOW_ID}\",
    \"cronExpression\": \"0 * * * * *\",
    \"timezone\": \"UTC\"
  }")

TRIGGER_ID=$(echo $TRIGGER_RESPONSE | jq -r '.id // empty' 2>/dev/null)

if [ -z "$TRIGGER_ID" ]; then
    echo -e "${RED}Error: Failed to create schedule trigger${NC}"
    echo "Response: $TRIGGER_RESPONSE"
    exit 1
fi

echo -e "${GREEN}✓ Schedule trigger created: ${TRIGGER_ID}${NC}"

# Summary
echo -e "\n${GREEN}=== Setup Complete ===${NC}"
echo -e "Workflow ID: ${WORKFLOW_ID}"
echo -e "Trigger ID: ${TRIGGER_ID}"
echo -e "\n${GREEN}Workflow Features Used:${NC}"
echo -e "  ✓ Direct body with template variables (currentTime, date, time, dateVN, dayOfWeek, etc.)"
echo -e "  ✓ Schedule Trigger (cron: every minute)"
echo -e "  ✓ Webhook action node"
echo -e "  ✓ Variable substitution in JSON body"
echo -e "\nWorkflow will send webhook to ${WEBHOOK_URL} every minute."
echo -e "You can check executions at: ${API_BASE_URL}/executions?workflow_id=${WORKFLOW_ID}"
echo -e "\n${YELLOW}To view webhook responses:${NC}"
echo -e "  Visit: https://httpbin.org/anything (shows last request)"

