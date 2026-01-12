# Implementation vs Documentation Review

**Date**: 2024-12-19  
**Reviewer**: Backend Expert  
**Scope**: Trigger, Action, and Workflow Implementation

## Summary

Đã kiểm tra implementation của trigger, action và workflow so với documentation. Đã fix các vấn đề chính và tạo báo cáo chi tiết.

## ✅ Đã Fix

### 1. WorkflowValidator - Node Structure

**Vấn đề**:
- Code đang check `type` field thay vì `nodeType` (doc standard)
- Code đang check `registryId` ở root level cho trigger nodes, nhưng doc nói phải check `triggerConfigId` trong `nodeConfig`
- Code đang validate trigger với `TriggerRegistryService` (hardcoded), nhưng doc nói phải validate với `TriggerService` (database configs)

**Đã fix**:
- ✅ Support cả `nodeType` (new structure) và `type` (old structure) cho backward compatibility
- ✅ Check `triggerConfigId` trong `nodeConfig` cho trigger nodes (hoặc `data` cho old structure)
- ✅ Validate trigger config với `TriggerService.getTriggerConfigById()` thay vì `TriggerRegistryService`
- ✅ Check `registryId` trong `nodeConfig` cho action nodes (hoặc `data` cho old structure)

**Files changed**:
- `backend/src/main/java/com/notificationplatform/service/workflow/WorkflowValidator.java`

## ✅ Đã Verify - Đúng với Documentation

### 2. Trigger Node Structure

**Documentation**:
```json
{
  "id": "node-1",
  "nodeType": "trigger",
  "nodeConfig": {
    "triggerConfigId": "trigger-config-123",
    "triggerType": "event",
    "instanceConfig": {
      "consumerGroup": "workflow-456-consumer"
    }
  }
}
```

**Implementation**:
- ✅ `TriggerNodeExecutor` đã support cả `triggerConfigId` (new) và `registryId` (old) - backward compatibility
- ✅ `WorkflowServiceImpl.getWorkflowTriggers()` đã đọc `triggerConfigId` từ `nodeData`
- ✅ Runtime state được lưu trong workflow definition node data
- ✅ Instance config được merge với trigger config

**Files verified**:
- `backend/src/main/java/com/notificationplatform/engine/nodes/TriggerNodeExecutor.java`
- `backend/src/main/java/com/notificationplatform/service/workflow/WorkflowServiceImpl.java`

### 3. Action Node Structure

**Documentation**:
```json
{
  "id": "node-uuid",
  "nodeType": "action",
  "nodeConfig": {
    "registryId": "send-email-action",
    "actionType": "custom-action",
    "config": {
      "recipient": "@{user.email}",
      "subject": "Welcome!",
      "body": "Welcome to our platform!"
    }
  }
}
```

**Implementation**:
- ✅ `ActionNodeExecutor` đã đọc `registryId` từ `nodeData`
- ✅ Action config được lưu trực tiếp trong workflow definition node data
- ✅ Actions được load từ `ActionRegistryService` (database registry)

**Files verified**:
- `backend/src/main/java/com/notificationplatform/engine/nodes/ActionNodeExecutor.java`
- `backend/src/main/java/com/notificationplatform/engine/nodes/CustomActionNodeExecutor.java`
- `backend/src/main/java/com/notificationplatform/engine/nodes/ApiCallNodeExecutor.java`
- `backend/src/main/java/com/notificationplatform/engine/nodes/FunctionNodeExecutor.java`
- `backend/src/main/java/com/notificationplatform/engine/nodes/PublishEventNodeExecutor.java`

### 4. Workflow Executor - Node Structure Support

**Implementation**:
- ✅ `WorkflowExecutor` đã support cả old structure (`type`, `data`) và new structure (`nodeType`, `nodeConfig`)
- ✅ `extractNodeData()` method đã handle cả hai structures

**Files verified**:
- `backend/src/main/java/com/notificationplatform/engine/WorkflowExecutor.java`

## ⚠️ Cần Implement (TODO)

### 5. Find Workflows by Trigger Config ID

**Vấn đề**:
Các trigger services cần tìm workflows sử dụng trigger config để execute workflows khi trigger được kích hoạt. Hiện tại các services chỉ log warning và return.

**Các chỗ cần implement**:
1. `ScheduleTriggerService.executeScheduledWorkflow()` - Tìm workflows sử dụng scheduler trigger
2. `KafkaEventProcessor.processTriggerEvent()` - Tìm workflows sử dụng event trigger
3. `RabbitMQEventProcessor.processTriggerEvent()` - Tìm workflows sử dụng event trigger
4. `ApiTriggerHandler.handleApiTrigger()` - Tìm workflows sử dụng API trigger

**Cần implement**:
```java
/**
 * Find all active workflows that use a specific trigger config.
 * Searches workflow definitions for nodes with triggerConfigId matching the given trigger config ID.
 */
public List<Workflow> findWorkflowsByTriggerConfigId(String triggerConfigId) {
    // 1. Query all active workflows
    // 2. Parse workflow definitions to find nodes with triggerConfigId matching triggerConfigId
    // 3. Return list of workflows
}
```

**Files cần update**:
- `backend/src/main/java/com/notificationplatform/service/workflow/WorkflowService.java` (add method)
- `backend/src/main/java/com/notificationplatform/service/workflow/WorkflowServiceImpl.java` (implement method)
- `backend/src/main/java/com/notificationplatform/service/trigger/schedule/ScheduleTriggerService.java` (use method)
- `backend/src/main/java/com/notificationplatform/service/trigger/event/KafkaEventProcessor.java` (use method)
- `backend/src/main/java/com/notificationplatform/service/trigger/event/RabbitMQEventProcessor.java` (use method)
- `backend/src/main/java/com/notificationplatform/service/trigger/api/ApiTriggerHandler.java` (use method)

## 📋 Checklist

- [x] WorkflowValidator support nodeType (new structure)
- [x] WorkflowValidator support triggerConfigId in nodeConfig
- [x] WorkflowValidator validate trigger config with TriggerService
- [x] WorkflowValidator support registryId in nodeConfig for actions
- [x] TriggerNodeExecutor support triggerConfigId
- [x] ActionNodeExecutor support registryId
- [x] WorkflowExecutor support both old and new node structures
- [ ] Implement findWorkflowsByTriggerConfigId() method
- [ ] Update ScheduleTriggerService to use findWorkflowsByTriggerConfigId()
- [ ] Update KafkaEventProcessor to use findWorkflowsByTriggerConfigId()
- [ ] Update RabbitMQEventProcessor to use findWorkflowsByTriggerConfigId()
- [ ] Update ApiTriggerHandler to use findWorkflowsByTriggerConfigId()

## Notes

1. **Backward Compatibility**: Code đã support cả old structure (`type`, `data`) và new structure (`nodeType`, `nodeConfig`) để đảm bảo compatibility với workflows đã tồn tại.

2. **Trigger Config vs Trigger Registry**: 
   - `TriggerRegistryService` = Hardcoded trigger definitions (templates)
   - `TriggerService` = Database trigger configs (user-created configurations)
   - Workflow nodes reference trigger configs (database), không phải registry definitions (hardcoded)

3. **Action Registry**: Actions được lưu trong `actions` table (database registry), không phải hardcoded. Action config được lưu trực tiếp trong workflow definition node data.

4. **Runtime State**: Trigger instance runtime state (ACTIVE, PAUSED, STOPPED) được lưu trong workflow definition node data, không phải trong trigger config table.

