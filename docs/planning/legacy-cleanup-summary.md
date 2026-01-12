# Legacy Documentation Cleanup Summary

## ✅ Đã xóa/update

### 1. Database Schema

#### `docs/database-schema/indexes.md`
- ✅ Xóa `idx_trigger_definitions_*` indexes
- ✅ Xóa `idx_trigger_instances_*` indexes  
- ✅ Update `action_definitions` → `actions`
- ✅ Xóa `idx_triggers_workflow_id` (trigger configs không có workflow_id)

#### `docs/database-schema/entities.md`
- ✅ Update trigger table description: Làm rõ trigger configs độc lập, không có workflow_id và node_id
- ✅ Update concept hierarchy: Trigger Type → Trigger Config → Trigger Instance → Runtime

#### `docs/database-schema/relationships.md`
- ✅ Update Workflows → Triggers relationship: Không còn foreign key, trigger instances lưu trong workflow definition
- ✅ Update Triggers → Executions: Làm rõ là trigger configs → executions
- ✅ Update data flow: Làm rõ trigger instance được đọc từ workflow definition

### 2. API Endpoints

#### `docs/api/endpoints.md`
- ✅ Xóa legacy endpoints:
  - `POST /workflows/{workflow_id}/triggers/api`
  - `POST /workflows/{workflow_id}/triggers/schedule`
  - `POST /workflows/{workflow_id}/triggers/event`
  - `POST /triggers/{trigger_id}/initialize`
  - `POST /triggers/{trigger_id}/start`
  - `POST /triggers/{trigger_id}/pause`
  - `POST /triggers/{trigger_id}/resume`
  - `POST /triggers/{trigger_id}/stop`
- ✅ Thêm trigger config management endpoints:
  - `POST /triggers` - Create trigger config
  - `GET /triggers` - List trigger configs
  - `GET /triggers/{id}` - Get trigger config
  - `PUT /triggers/{id}` - Update trigger config
  - `DELETE /triggers/{id}` - Delete trigger config
- ✅ Update `GET /triggers/registry`: Trả về trigger configs từ database
- ✅ Update `GET /workflows/{id}/triggers`: Trả về trigger nodes + trigger configs + runtime states
- ✅ Update workflow creation example: Node structure mới với `nodeType`, `nodeConfig`, `triggerConfigId`
- ✅ Update API diagram: Xóa legacy trigger endpoints, thêm trigger config endpoints

#### `docs/technical/integration/api-contract.md`
- ✅ Xóa legacy trigger endpoints
- ✅ Thêm trigger config management endpoints
- ✅ Thêm note về trigger config independence

### 3. Architecture

#### `docs/architecture/overview.md`
- ✅ Update trigger flow diagram: `POST /trigger/{trigger_path}` thay vì `POST /triggers/api/{id}`

### 4. Feature Documentation

#### `docs/features/trigger-registry.md`
- ✅ Xóa phần về `trigger_definitions` table
- ✅ Làm rõ trigger types hardcoded
- ✅ Update registry API: Trả về trigger configs từ database

#### `docs/features/triggers.md`
- ✅ Update "Using Triggers in Workflows": Trigger-first flow
- ✅ Thêm "Trigger Config vs Trigger Instance"
- ✅ Xóa legacy lifecycle endpoints
- ✅ Update lifecycle: Qua workflow activation/deactivation

#### `docs/features/workflow-builder.md`
- ✅ Update Trigger Nodes: Trigger-first flow
- ✅ Update Action Nodes: Action-first flow
- ✅ Update node structure examples

#### `docs/features/action-registry.md`
- ✅ Update database schema: `action_definitions` → `actions`
- ✅ Update "Workflow Node Configuration": Action-first flow
- ✅ Làm rõ Action Definition vs Action Config

### 5. User Flows

#### `docs/user-flows/workflow-creation.md`
- ✅ Rewrite toàn bộ flow theo trigger-first approach
- ✅ Thêm steps: Create Trigger Config, Create Action Definition
- ✅ Update flow: Link trigger config to node

## 📋 Legacy Concepts Đã Xóa

### Database Tables (Không tồn tại)
- ❌ `trigger_definitions` - Xóa tất cả references
- ❌ `trigger_instances` - Xóa tất cả references (instances lưu trong workflow definition)

### API Endpoints (Legacy - Đã xóa)
- ❌ `POST /workflows/{workflow_id}/triggers/api`
- ❌ `POST /workflows/{workflow_id}/triggers/schedule`
- ❌ `POST /workflows/{workflow_id}/triggers/event`
- ❌ `POST /triggers/{trigger_id}/initialize`
- ❌ `POST /triggers/{trigger_id}/start`
- ❌ `POST /triggers/{trigger_id}/pause`
- ❌ `POST /triggers/{trigger_id}/resume`
- ❌ `POST /triggers/{trigger_id}/stop`

### Concepts (Legacy - Đã thay thế)
- ❌ Automatic trigger sync từ workflow definition
- ❌ Trigger configs gắn với workflow (có workflow_id)
- ❌ Trigger instances có bảng riêng
- ❌ Separate lifecycle endpoints cho trigger instances

## ✅ Design Mới

### Trigger System
- **Trigger Types**: 3 loại hardcoded (api-call, scheduler, event)
- **Trigger Configs**: Độc lập, lưu trong bảng `triggers`, có thể share
- **Trigger Instances**: Lưu trong workflow definition node data
- **Lifecycle**: Qua workflow activation/deactivation

### Action System
- **Action Definitions**: Lưu trong bảng `actions` (registry)
- **Action Configs**: Lưu trong workflow definition node data

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

## 🔍 Files Cần Review (Planning Documents)

Các file trong `docs/planning/` có thể vẫn có legacy references, nhưng đây là historical records nên có thể giữ lại:
- `docs/planning/design-gap-analysis.md`
- `docs/planning/design-questions.md`
- `docs/planning/clarification-questions.md`
- `docs/planning/frontend/sprint-*.md`
- `docs/planning/backend/sprint-*.md`

**Note**: Planning documents là historical records, không cần xóa legacy references.

## ✅ Summary

Tất cả legacy documentation đã được xóa hoặc update trong:
- ✅ Feature documentation
- ✅ User flows
- ✅ API endpoints
- ✅ Database schema
- ✅ Architecture documentation

Documentation hiện tại phản ánh đúng design mới:
- Trigger-first flow
- Action-first flow
- Trigger configs độc lập, có thể share
- Trigger instances lưu trong workflow definition
- Lifecycle qua workflow activation/deactivation

