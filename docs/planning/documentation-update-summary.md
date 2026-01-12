# Documentation Update Summary

## ✅ Đã hoàn thành

### 1. Trigger Registry (`docs/features/trigger-registry.md`)
- ✅ Xóa phần về `trigger_definitions` table (legacy)
- ✅ Làm rõ trigger types hardcoded (3 loại: api-call, scheduler, event)
- ✅ Giải thích concept hierarchy: Trigger Type → Trigger Config → Trigger Instance → Runtime
- ✅ Update registry API: `GET /triggers/registry` trả về trigger configs từ database
- ✅ Thêm phần về trigger instance structure trong workflow definition
- ✅ Thêm phần về schema definition và override fields

### 2. Workflow Builder (`docs/features/workflow-builder.md`)
- ✅ Update Trigger Nodes section: Trigger-first flow
- ✅ Update Action Nodes section: Action-first flow
- ✅ Làm rõ trigger instance structure
- ✅ Update lifecycle management

### 3. User Flow (`docs/user-flows/workflow-creation.md`)
- ✅ Rewrite toàn bộ flow theo trigger-first approach
- ✅ Thêm steps: Create Trigger Config, Create Action Definition
- ✅ Update flow: Link trigger config to node, configure instance settings
- ✅ Thêm phần về sharing resources

### 4. Triggers Feature (`docs/features/triggers.md`)
- ✅ Update "Using Triggers in Workflows" section
- ✅ Thêm phần "Trigger Config vs Trigger Instance"
- ✅ Update "Trigger Instance Management": Lifecycle through workflow activation
- ✅ Xóa legacy API endpoints (init, start, pause, resume, stop, destroy)

### 5. Action Registry (`docs/features/action-registry.md`)
- ✅ Update "Workflow Node Configuration" section
- ✅ Thêm phần "Action-First Flow"
- ✅ Làm rõ Action Definition vs Action Config
- ✅ Update node structure example

## 🔍 Cần kiểm tra và xóa legacy

### Files cần review:

1. **`docs/api/endpoints.md`**
   - [ ] Xóa hoặc đánh dấu legacy endpoints:
     - `POST /triggers/api`
     - `POST /triggers/schedule`
     - `POST /triggers/event`
     - `POST /workflows/{workflowId}/triggers/{triggerId}/init`
     - `POST /workflows/{workflowId}/triggers/{triggerId}/start`
     - `POST /workflows/{workflowId}/triggers/{triggerId}/pause`
     - `POST /workflows/{workflowId}/triggers/{triggerId}/resume`
     - `POST /workflows/{workflowId}/triggers/{triggerId}/stop`
     - `DELETE /workflows/{workflowId}/triggers/{triggerId}`
   - [ ] Update `GET /triggers/registry` description
   - [ ] Update `GET /workflows/{id}/triggers` description

2. **`docs/database-schema/entities.md`**
   - [ ] Xóa mention về `trigger_definitions` table
   - [ ] Làm rõ trigger registry là hardcoded
   - [ ] Update trigger config description

3. **`docs/database-schema/relationships.md`**
   - [ ] Review và update relationships nếu cần
   - [ ] Xóa mention về trigger_definitions

4. **Các file khác có thể reference legacy:**
   - [ ] Search cho "trigger_definitions"
   - [ ] Search cho "trigger instance lifecycle endpoints"
   - [ ] Search cho old flow descriptions

## 📝 Design Summary

### Trigger System
- **Trigger Types**: 3 loại hardcoded (api-call, scheduler, event)
- **Trigger Configs**: Nhiều configs cho mỗi type, lưu trong bảng `triggers`
- **Trigger Instances**: Lưu trong workflow definition node data
- **Registry Endpoint**: `GET /triggers/registry` trả về trigger configs từ database

### Action System
- **Action Registry**: Bảng `actions` (registry definitions)
- **Action Config**: Lưu trong node data (không có bảng action_configs riêng)
- **Flow**: Chọn action từ registry → Configure trong workflow

### Workflow Definition Structure
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

### Override Fields
- Trước mắt: Consumer Group
- Cần cơ chế để dễ dàng define thêm

### Schema Definition
- Define trong Java (để dùng khi implement TriggerExecutor)
- Phân biệt: shared fields vs instance-specific fields

## 🚀 Next Steps

1. Review và update API endpoints documentation
2. Review và update database schema documentation
3. Search và xóa tất cả legacy references
4. Update technical documentation (frontend/backend) nếu cần

