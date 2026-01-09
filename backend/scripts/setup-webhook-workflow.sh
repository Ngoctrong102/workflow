#!/bin/bash

# Script để setup workflow gửi webhook mỗi phút
# Sử dụng: ./scripts/setup-webhook-workflow.sh

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
API_BASE_URL="${API_BASE_URL:-http://localhost:8080/api/v1}"
# Using httpbin.org as webhook test endpoint (logs requests)
WEBHOOK_URL="${WEBHOOK_URL:-https://httpbin.org/post}"

echo -e "${GREEN}=== Setup Webhook Workflow Script ===${NC}\n"

# Check if API is accessible
echo "Checking API connection..."
if ! curl -s -f "${API_BASE_URL}/actuator/health" > /dev/null; then
    echo -e "${RED}Error: Cannot connect to API at ${API_BASE_URL}${NC}"
    echo "Please make sure the backend is running."
    exit 1
fi
echo -e "${GREEN}✓ API is accessible${NC}\n"

# Check if webhook test endpoint is accessible
echo "Checking webhook test endpoint..."
if curl -s -f -X POST "${WEBHOOK_URL}" -H "Content-Type: application/json" -d '{"test":"connection"}' > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Webhook test endpoint is accessible${NC}"
else
    echo -e "${YELLOW}Warning: Webhook test endpoint may not be available${NC}"
    echo "URL: ${WEBHOOK_URL}"
    echo "Continuing anyway..."
fi
echo -e "${GREEN}✓ Webhook URL configured: ${WEBHOOK_URL}${NC}"
echo -e "${YELLOW}Note: Using httpbin.org/post as webhook test endpoint${NC}"
echo -e "${YELLOW}      This endpoint will log all requests and return response${NC}\n"

# Step 1: Create Webhook Channel
echo -e "\n${GREEN}Step 1: Creating Webhook Channel...${NC}"
CHANNEL_RESPONSE=$(curl -s -X POST "${API_BASE_URL}/channels" \
  -H "Content-Type: application/json" \
  -d "{
    \"type\": \"webhook\",
    \"name\": \"Webhook Test Channel\",
    \"provider\": \"http\",
    \"config\": {
      \"url\": \"${WEBHOOK_URL}\"
    }
  }")

CHANNEL_ID=$(echo $CHANNEL_RESPONSE | jq -r '.id // empty' 2>/dev/null)

if [ -z "$CHANNEL_ID" ]; then
    echo -e "${RED}Error: Failed to create channel${NC}"
    echo "Response: $CHANNEL_RESPONSE"
    exit 1
fi

echo -e "${GREEN}✓ Channel created: ${CHANNEL_ID}${NC}"

# Step 2: Create Template with rich features
echo -e "\n${GREEN}Step 2: Creating Template with variables...${NC}"

# Create a template for webhook payload
TEMPLATE_RESPONSE=$(curl -s -X POST "${API_BASE_URL}/templates" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Webhook Test Template\",
    \"channel\": \"webhook\",
    \"body\": \"{
      \\\"message\\\": \\\"Xin chào Trọng, bây giờ là {{currentTime}}\\\",
      \\\"timestamp\\\": \\\"{{_executionTime}}\\\",
      \\\"date\\\": \\\"{{date}}\\\",
      \\\"time\\\": \\\"{{time}}\\\",
      \\\"dateVN\\\": \\\"{{dateVN}}\\\",
      \\\"dayOfWeek\\\": \\\"{{dayOfWeek}}\\\",
      \\\"timezone\\\": \\\"{{_timezone|default:UTC}}\\\",
      \\\"workflowId\\\": \\\"{{workflowId}}\\\",
      \\\"executionInfo\\\": {
        \\\"status\\\": \\\"running\\\",
        \\\"triggeredAt\\\": \\\"{{currentTime}}\\\"
      }
    }\",
    \"variables\": [
      {
        \"name\": \"currentTime\",
        \"type\": \"string\",
        \"description\": \"Thời điểm hiện tại (YYYY-MM-DD HH:mm:ss)\"
      },
      {
        \"name\": \"date\",
        \"type\": \"string\",
        \"description\": \"Ngày hiện tại\"
      },
      {
        \"name\": \"time\",
        \"type\": \"string\",
        \"description\": \"Giờ hiện tại\"
      },
      {
        \"name\": \"dateVN\",
        \"type\": \"string\",
        \"description\": \"Ngày định dạng Việt Nam (dd/MM/yyyy)\"
      },
      {
        \"name\": \"dayOfWeek\",
        \"type\": \"string\",
        \"description\": \"Thứ trong tuần\"
      },
      {
        \"name\": \"_executionTime\",
        \"type\": \"string\",
        \"description\": \"Execution timestamp\"
      },
      {
        \"name\": \"_timezone\",
        \"type\": \"string\",
        \"description\": \"Timezone\"
      },
      {
        \"name\": \"workflowId\",
        \"type\": \"string\",
        \"description\": \"Workflow ID\"
      }
    ],
    \"category\": \"webhook\",
    \"tags\": [\"schedule\", \"test\", \"automated\"]
  }")

TEMPLATE_ID=$(echo $TEMPLATE_RESPONSE | jq -r '.id // empty' 2>/dev/null)

if [ -z "$TEMPLATE_ID" ]; then
    echo -e "${RED}Error: Failed to create template${NC}"
    echo "Response: $TEMPLATE_RESPONSE"
    exit 1
fi

echo -e "${GREEN}✓ Template created: ${TEMPLATE_ID}${NC}"

# Step 3: Create Workflow
echo -e "\n${GREEN}Step 3: Creating Workflow...${NC}"

WORKFLOW_DEFINITION=$(cat <<EOF
{
  "nodes": [
    {
      "id": "trigger-1",
      "type": "trigger",
      "subtype": "schedule",
      "position": {"x": 100, "y": 200}
    },
    {
      "id": "action-1",
      "type": "action",
      "subtype": "send_webhook",
      "position": {"x": 300, "y": 200},
      "data": {
        "channelId": "${CHANNEL_ID}",
        "url": "${WEBHOOK_URL}",
        "method": "POST",
        "templateId": "${TEMPLATE_ID}",
        "headers": {
          "Content-Type": "application/json"
        }
      }
    }
  ],
  "edges": [
    {
      "source": "trigger-1",
      "target": "action-1"
    }
  ]
}
EOF
)

WORKFLOW_RESPONSE=$(curl -s -X POST "${API_BASE_URL}/workflows" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Webhook Test mỗi phút\",
    \"description\": \"Workflow gửi webhook mỗi phút đến ${WEBHOOK_URL}\",
    \"status\": \"active\",
    \"definition\": $(echo "$WORKFLOW_DEFINITION" | jq -c .)
  }")

WORKFLOW_ID=$(echo $WORKFLOW_RESPONSE | jq -r '.id // empty' 2>/dev/null)

if [ -z "$WORKFLOW_ID" ]; then
    echo -e "${RED}Error: Failed to create workflow${NC}"
    echo "Response: $WORKFLOW_RESPONSE"
    exit 1
fi

echo -e "${GREEN}✓ Workflow created: ${WORKFLOW_ID}${NC}"

# Step 4: Create Schedule Trigger (every minute)
echo -e "\n${GREEN}Step 4: Creating Schedule Trigger (every minute)...${NC}"

# Cron expression for every minute: "0 * * * * *" (second minute hour day month weekday)
TRIGGER_RESPONSE=$(curl -s -X POST "${API_BASE_URL}/workflows/${WORKFLOW_ID}/triggers/schedule" \
  -H "Content-Type: application/json" \
  -d "{
    \"cronExpression\": \"0 * * * * *\",
    \"timezone\": \"Asia/Ho_Chi_Minh\"
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
echo -e "Channel ID: ${CHANNEL_ID}"
echo -e "Template ID: ${TEMPLATE_ID}"
echo -e "Workflow ID: ${WORKFLOW_ID}"
echo -e "Trigger ID: ${TRIGGER_ID}"
echo -e "\n${GREEN}Workflow Features Used:${NC}"
echo -e "  ✓ Webhook Channel"
echo -e "  ✓ Template with JSON formatting"
echo -e "  ✓ Template variables (currentTime, date, time, dateVN, dayOfWeek, etc.)"
echo -e "  ✓ Schedule Trigger (cron: every minute)"
echo -e "  ✓ Multi-node workflow (trigger → action)"
echo -e "\nWorkflow will send webhook to ${WEBHOOK_URL} every minute."
echo -e "You can check executions at: ${API_BASE_URL}/executions?workflow_id=${WORKFLOW_ID}"
echo -e "You can view webhook logs in backend logs (app.log)"
echo -e "\n${YELLOW}To view webhook test endpoint logs:${NC}"
echo -e "  tail -f backend/app.log | grep 'Webhook Test Received'"

