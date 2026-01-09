# Notification Delivery User Flow

## Overview

This document describes the flow of notification delivery from trigger to recipient.

## Delivery Flow

### 1. Trigger Activation
- **Trigger Types**:
  - **API Trigger**: HTTP request received
  - **Schedule Trigger**: Cron schedule fires
  - **Event Trigger**: Message received from Kafka topic
- **System Actions**:
  - Validate trigger
  - Create execution record
  - Load workflow definition
  - Create execution context

### 2. Workflow Execution Start
- **System Actions**:
  - Initialize workflow engine
  - Load workflow definition
  - Create execution context with trigger data
  - Start processing nodes

### 3. Node Processing
- **System Actions**:
  - Process nodes sequentially
  - Execute each node:
    - **Trigger Node**: Extract trigger data
    - **Logic Nodes**: Evaluate conditions, branch
    - **Data Nodes**: Transform data
    - **Action Nodes**: Prepare notifications
  - Log node executions
  - Pass data between nodes

### 4. Notification Preparation
- **System Actions**:
  - Action node triggers notification
  - Load template (if used)
  - Render template with data:
    - Replace variables
    - Format content
    - Validate rendered content
  - Determine recipients
  - Select channel

### 5. Channel Selection
- **System Actions**:
  - Determine notification channel
  - Load channel configuration
  - Validate channel status
  - Check rate limits
  - Queue notification if needed

### 6. Notification Sending
- **System Actions**:
  - Create notification record
  - Create delivery records (one per recipient)
  - Send notification via channel:
    - **Email**: SMTP or email provider API
    - **SMS**: SMS provider API
    - **Push**: Push provider API
    - **Slack/Discord/Teams**: Platform API
    - **Webhook**: HTTP request
  - Track sending status

### 7. Delivery Status Tracking
- **System Actions**:
  - Update delivery status:
    - **Pending**: Queued
    - **Sending**: Currently sending
    - **Delivered**: Successfully delivered
    - **Failed**: Delivery failed
  - Receive status updates from providers:
    - Webhook callbacks
    - Polling provider APIs
  - Update delivery records
  - Update analytics

### 8. Completion
- **System Actions**:
  - Mark execution as completed
  - Update execution metrics
  - Update analytics aggregates
  - Log completion

## Error Handling Flow

### Node Execution Error
1. Node execution fails
2. Log error
3. Check error handling strategy:
   - **Retry**: Retry node execution
   - **Continue**: Continue to next node
   - **Fail**: Mark execution as failed
4. Update execution status

### Notification Delivery Error
1. Delivery fails
2. Log error
3. Check retry strategy:
   - **Retry**: Retry delivery
   - **Dead Letter**: Move to dead letter queue
   - **Fail**: Mark delivery as failed
4. Update delivery status
5. Alert on persistent failures

### Channel Error
1. Channel connection fails
2. Log error
3. Check fallback:
   - **Fallback Channel**: Use alternative channel
   - **Retry**: Retry connection
   - **Fail**: Mark as failed
4. Update channel status

## Status Updates

### Real-time Updates (Not Required for MVP)
- WebSocket updates
- Real-time status changes

### MVP Alternative
- Periodic status polling
- Manual refresh
- Scheduled status updates

## Delivery Metrics

### Per Notification
- Sent timestamp
- Delivered timestamp
- Opened timestamp (email/push)
- Clicked timestamp (email)
- Error message (if failed)

### Aggregated Metrics
- Total sent
- Total delivered
- Delivery rate
- Average delivery time
- Error rate

## Related Documentation

- [Workflow Creation Flow](./workflow-creation.md)
- [Analytics Viewing Flow](./analytics-viewing.md)
- [Notification Channels Feature](../features/notification-channels.md)


