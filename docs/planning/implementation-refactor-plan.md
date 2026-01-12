# Implementation Refactor Plan - Align với Design Mới

## 🎯 Mục tiêu

Refactor toàn bộ backend và frontend implementation để khớp với design mới:
- Trigger-first flow: Tạo trigger config trước → Thêm vào workflow
- Action-first flow: Tạo action definition trước → Thêm vào workflow
- Trigger configs độc lập, shareable
- Trigger instances lưu trong workflow definition

## 📋 Tổng quan các thay đổi

### Backend Changes
1. **Database Schema**: Update Trigger entity (xóa workflow_id, node_id, thêm name)
2. **API Endpoints**: Refactor TriggerController (xóa legacy endpoints, thêm trigger config management)
3. **Trigger Registry**: Update để trả về trigger configs từ database
4. **Workflow Definition**: Update node structure
5. **Services**: Refactor TriggerService và TriggerInstanceService

### Frontend Changes
1. **Types**: Update WorkflowNode structure
2. **Components**: Update PropertiesPanel, NodePalette, TriggerDialog
3. **Services**: Update trigger service calls
4. **Workflow Builder**: Update node creation flow

---

## 🔧 Backend Refactoring

### Phase 1: Database Schema Changes

#### 1.1 Update Trigger Entity

**File**: `backend/src/main/java/com/notificationplatform/entity/Trigger.java`

**Changes**:
- ❌ Xóa `@ManyToOne Workflow workflow`
- ❌ Xóa `@Column String nodeId`
- ✅ Thêm `@Column String name`
- ❌ Xóa `@OneToMany List<Execution> executions` (giữ relationship qua trigger_id trong Execution)
- ❌ Xóa `@OneToMany List<FileUpload> fileUploads` (nếu có)

**New Structure**:
```java
@Entity
@Table(name = "triggers")
public class Trigger {
    @Id
    private String id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "trigger_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TriggerType triggerType;
    
    @Column(name = "config", nullable = false, columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> config;
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TriggerStatus status = TriggerStatus.ACTIVE;
    
    // ... timestamps, deleted_at
}
```

#### 1.2 Update Workflow Entity

**File**: `backend/src/main/java/com/notificationplatform/entity/Workflow.java`

**Changes**:
- ❌ Xóa `@OneToMany List<Trigger> triggers` relationship

#### 1.3 Database Migration

**Create Migration Script**:
```sql
-- Remove foreign key constraint
ALTER TABLE triggers DROP CONSTRAINT IF EXISTS triggers_workflow_id_fkey;

-- Remove workflow_id and node_id columns
ALTER TABLE triggers DROP COLUMN IF EXISTS workflow_id;
ALTER TABLE triggers DROP COLUMN IF EXISTS node_id;

-- Add name column
ALTER TABLE triggers ADD COLUMN IF NOT EXISTS name VARCHAR(255) NOT NULL DEFAULT 'Untitled Trigger';

-- Update indexes
DROP INDEX IF EXISTS idx_triggers_workflow_id;
DROP INDEX IF EXISTS idx_triggers_node_id;
```

---

### Phase 2: API Endpoints Refactoring

#### 2.1 Refactor TriggerController

**File**: `backend/src/main/java/com/notificationplatform/controller/TriggerController.java`

**Remove Legacy Endpoints**:
- ❌ `POST /triggers/api`
- ❌ `POST /triggers/schedule`
- ❌ `POST /triggers/event`
- ❌ `POST /triggers/file`
- ❌ `POST /triggers/{id}/initialize`
- ❌ `POST /triggers/{id}/start`
- ❌ `POST /triggers/{id}/pause`
- ❌ `POST /triggers/{id}/resume`
- ❌ `POST /triggers/{id}/stop`
- ❌ `POST /triggers/{id}/activate`
- ❌ `POST /triggers/{id}/deactivate`

**New Endpoints**:
```java
@RestController
@RequestMapping("/triggers")
public class TriggerController {
    
    // Create trigger config (independent, no workflow_id)
    @PostMapping
    public ResponseEntity<TriggerResponse> createTriggerConfig(
        @Valid @RequestBody CreateTriggerConfigRequest request) {
        // ...
    }
    
    // Get trigger config
    @GetMapping("/{id}")
    public ResponseEntity<TriggerResponse> getTriggerConfig(@PathVariable String id) {
        // ...
    }
    
    // List trigger configs (with filters)
    @GetMapping
    public ResponseEntity<PagedResponse<TriggerResponse>> listTriggerConfigs(
        @RequestParam(required = false) String triggerType,
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "20") int limit,
        @RequestParam(defaultValue = "0") int offset) {
        // ...
    }
    
    // Update trigger config
    @PutMapping("/{id}")
    public ResponseEntity<TriggerResponse> updateTriggerConfig(
        @PathVariable String id,
        @Valid @RequestBody UpdateTriggerConfigRequest request) {
        // ...
    }
    
    // Delete trigger config
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTriggerConfig(@PathVariable String id) {
        // ...
    }
}
```

#### 2.2 Update TriggerRegistryController

**File**: `backend/src/main/java/com/notificationplatform/controller/TriggerRegistryController.java`

**Changes**:
- Update `GET /triggers/registry` để trả về trigger configs từ database thay vì hardcoded definitions
- Có thể giữ hardcoded trigger types nhưng trả về trigger configs đã tạo

**New Implementation**:
```java
@GetMapping
public ResponseEntity<Map<String, Object>> getAllTriggers() {
    // Option 1: Trả về trigger configs từ database
    List<Trigger> triggerConfigs = triggerService.listTriggerConfigs();
    
    // Option 2: Trả về cả hardcoded types + trigger configs
    // (nếu cần hiển thị types để user tạo config mới)
    
    // Convert to response format
    List<Map<String, Object>> triggerResponses = triggerConfigs.stream()
        .map(this::convertToResponse)
        .collect(Collectors.toList());
    
    Map<String, Object> response = new HashMap<>();
    response.put("triggers", triggerResponses);
    return ResponseEntity.ok(response);
}
```

#### 2.3 Add Workflow Triggers Endpoint

**File**: `backend/src/main/java/com/notificationplatform/controller/WorkflowController.java`

**Add New Endpoint**:
```java
@GetMapping("/{workflowId}/triggers")
public ResponseEntity<List<WorkflowTriggerResponse>> getWorkflowTriggers(
    @PathVariable String workflowId) {
    // Read workflow definition
    // Extract trigger nodes
    // Load trigger configs from database
    // Merge with instance configs
    // Return trigger instances with configs and runtime states
}
```

---

### Phase 3: DTOs và Request/Response Updates

#### 3.1 Create New DTOs

**New Files**:
- `CreateTriggerConfigRequest.java`
- `UpdateTriggerConfigRequest.java`
- `WorkflowTriggerResponse.java` (trigger instance với config)

**Remove Legacy DTOs**:
- `CreateApiTriggerRequest.java` (hoặc refactor)
- `CreateScheduleTriggerRequest.java` (hoặc refactor)
- `CreateEventTriggerRequest.java` (hoặc refactor)
- `CreateFileTriggerRequest.java` (hoặc refactor)

#### 3.2 Update TriggerResponse

**File**: `backend/src/main/java/com/notificationplatform/dto/response/TriggerResponse.java`

**Changes**:
- ❌ Xóa `workflowId`
- ❌ Xóa `nodeId`
- ✅ Thêm `name`
- ✅ Giữ `triggerType`, `config`, `status`

---

### Phase 4: Service Layer Refactoring

#### 4.1 Refactor TriggerService

**File**: `backend/src/main/java/com/notificationplatform/service/trigger/TriggerService.java`

**Remove Methods**:
- ❌ `createApiTrigger(CreateApiTriggerRequest)`
- ❌ `createScheduleTrigger(CreateScheduleTriggerRequest)`
- ❌ `createEventTrigger(CreateEventTriggerRequest)`
- ❌ `createFileTrigger(CreateFileTriggerRequest)`
- ❌ `listTriggers(String workflowId)` - filter by workflow
- ❌ `activateTrigger(String id)`
- ❌ `deactivateTrigger(String id)`

**New Methods**:
```java
public interface TriggerService {
    // Create trigger config (independent)
    TriggerResponse createTriggerConfig(CreateTriggerConfigRequest request);
    
    // Get trigger config
    TriggerResponse getTriggerConfigById(String id);
    
    // List trigger configs (no workflow filter)
    PagedResponse<TriggerResponse> listTriggerConfigs(
        String triggerType, String status, String search, int limit, int offset);
    
    // Update trigger config
    TriggerResponse updateTriggerConfig(String id, UpdateTriggerConfigRequest request);
    
    // Delete trigger config
    void deleteTriggerConfig(String id);
    
    // Keep for backward compatibility (trigger workflow execution)
    TriggerActivationResponse activateApiTrigger(
        String path, String method, Map<String, Object> requestData, String apiKey);
    
    TriggerResponse getTriggerByPath(String path, String method);
}
```

#### 4.2 Remove/Refactor TriggerInstanceService

**File**: `backend/src/main/java/com/notificationplatform/service/trigger/api/TriggerInstanceService.java`

**Decision**: 
- Option A: Xóa hoàn toàn (lifecycle qua workflow activation)
- Option B: Giữ lại nhưng chỉ dùng internal (không expose qua API)

**Recommendation**: Option A - Xóa hoàn toàn, lifecycle qua WorkflowService

#### 4.3 Update WorkflowService

**File**: `backend/src/main/java/com/notificationplatform/service/workflow/WorkflowService.java`

**Add Methods**:
```java
// Get trigger instances for workflow
List<WorkflowTriggerResponse> getWorkflowTriggers(String workflowId);

// Activate workflow (starts trigger instances)
void activateWorkflow(String workflowId);

// Deactivate workflow (stops trigger instances)
void deactivateWorkflow(String workflowId);
```

**Update Methods**:
- `activateWorkflow()`: 
  - Read trigger instances from workflow definition
  - Load trigger configs
  - Merge configs with instance overrides
  - Create consumers/schedulers
  - Store runtime state in workflow definition

---

### Phase 5: Workflow Definition Structure

#### 5.1 Update Node Structure

**Trigger Node Structure** (trong workflow definition):
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
  },
  "position": { "x": 100, "y": 100 }
}
```

**Action Node Structure**:
```json
{
  "id": "node-2",
  "nodeType": "action",
  "nodeConfig": {
    "registryId": "send-email-action",
    "actionType": "custom-action",
    "config": {
      "recipients": ["@{user.email}"],
      "subject": "Welcome!",
      "body": "Hello @{user.name}"
    }
  },
  "position": { "x": 300, "y": 100 }
}
```

#### 5.2 Update WorkflowExecutor

**File**: `backend/src/main/java/com/notificationplatform/engine/WorkflowExecutor.java`

**Changes**:
- Update để đọc node structure mới
- Support `nodeType` và `nodeConfig`
- Extract `triggerConfigId` từ trigger nodes

---

## 🎨 Frontend Refactoring

### Phase 6: Type Definitions

#### 6.1 Update WorkflowNode Type

**File**: `frontend/src/types/workflow.ts`

**Changes**:
```typescript
export interface WorkflowNode {
  id: string
  nodeType: "trigger" | "action" | "logic" | "data"
  nodeConfig: TriggerNodeConfig | ActionNodeConfig | LogicNodeConfig | DataNodeConfig
  position: { x: number; y: number }
}

export interface TriggerNodeConfig {
  triggerConfigId: string
  triggerType: "api-call" | "scheduler" | "event"
  instanceConfig?: {
    consumerGroup?: string
    // Other instance-specific overrides
  }
}

export interface ActionNodeConfig {
  registryId: string
  actionType: string
  config: Record<string, unknown>
}

export interface LogicNodeConfig {
  // Logic-specific config
  [key: string]: unknown
}
```

#### 6.2 Update WorkflowDefinition

**File**: `frontend/src/types/workflow.ts`

**Keep structure but ensure nodes use new format**

---

### Phase 7: Component Updates

#### 7.1 Update NodePalette

**File**: `frontend/src/components/workflow/NodePalette.tsx`

**Changes**:
- Load trigger configs từ `GET /triggers/registry` (trigger configs từ DB)
- Khi drag trigger node, store `triggerConfigId` thay vì tạo trigger ngay
- Update node creation để sử dụng structure mới

**New Flow**:
1. User drags trigger node từ palette
2. Node được tạo với `nodeType: "trigger"` và `nodeConfig: { triggerConfigId: "..." }`
3. PropertiesPanel hiển thị trigger config selection
4. User chọn trigger config hoặc tạo mới

#### 7.2 Update PropertiesPanel

**File**: `frontend/src/components/workflow/PropertiesPanel.tsx`

**Changes**:
- Support trigger-first flow:
  - Nếu trigger node chưa có `triggerConfigId` → hiển thị trigger config selector
  - Nếu đã có → hiển thị trigger config info + instance config fields
- Support instance-specific fields (consumerGroup, etc.)
- Update form để save vào `nodeConfig` structure

**New Structure**:
```typescript
// Trigger node config
{
  triggerConfigId: "trigger-config-123",
  triggerType: "event",
  instanceConfig: {
    consumerGroup: "workflow-456-consumer"
  }
}
```

#### 7.3 Update TriggerDialog

**File**: `frontend/src/components/trigger/TriggerDialog.tsx`

**Changes**:
- Refactor để tạo trigger config (không cần workflowId)
- Update API calls:
  - `POST /triggers` thay vì `POST /triggers/api`, etc.
  - Remove workflowId từ request

#### 7.4 Update TriggerEditor

**File**: `frontend/src/pages/TriggerEditor.tsx`

**Changes**:
- Refactor để edit trigger config (không cần workflowId)
- Update API calls:
  - `PUT /triggers/{id}` thay vì workflow-specific endpoints
  - Remove workflowId từ request

#### 7.5 Update GuidedWorkflowWizard

**File**: `frontend/src/components/workflow/GuidedWorkflowWizard.tsx`

**Changes**:
- Update trigger node creation để sử dụng structure mới
- Link trigger config thay vì tạo trigger với workflowId

---

### Phase 8: Service Layer Updates

#### 8.1 Update Trigger Service

**File**: `frontend/src/services/trigger-service.ts`

**Remove Methods**:
- ❌ `createApiTrigger()`
- ❌ `createScheduleTrigger()`
- ❌ `createEventTrigger()`
- ❌ `createFileTrigger()`
- ❌ `initializeTrigger()`
- ❌ `startTrigger()`
- ❌ `pauseTrigger()`
- ❌ `resumeTrigger()`
- ❌ `stopTrigger()`

**New Methods**:
```typescript
// Create trigger config
createTriggerConfig(request: CreateTriggerConfigRequest): Promise<TriggerResponse>

// Get trigger config
getTriggerConfig(id: string): Promise<TriggerResponse>

// List trigger configs
listTriggerConfigs(params: {
  triggerType?: string
  status?: string
  search?: string
  limit?: number
  offset?: number
}): Promise<PagedResponse<TriggerResponse>>

// Update trigger config
updateTriggerConfig(id: string, request: UpdateTriggerConfigRequest): Promise<TriggerResponse>

// Delete trigger config
deleteTriggerConfig(id: string): Promise<void>

// Get workflow triggers (trigger instances)
getWorkflowTriggers(workflowId: string): Promise<WorkflowTriggerResponse[]>
```

#### 8.2 Update Trigger Registry Service

**File**: `frontend/src/services/trigger-registry-service.ts`

**Changes**:
- Update `getTriggerRegistry()` để load trigger configs từ database
- Có thể giữ hardcoded types để hiển thị options tạo config mới

---

## 📝 Migration Strategy

### Step 1: Database Migration
1. Create migration script
2. Run migration (backup data trước)
3. Update existing trigger records:
   - Set `name` từ config hoặc generate
   - Remove workflow_id và node_id

### Step 2: Backend Refactoring
1. Update Trigger entity
2. Refactor TriggerService
3. Update TriggerController
4. Update TriggerRegistryController
5. Update WorkflowService (lifecycle management)
6. Update WorkflowExecutor (node structure)

### Step 3: Frontend Refactoring
1. Update types
2. Update services
3. Update components
4. Update workflow builder

### Step 4: Testing
1. Unit tests
2. Integration tests
3. E2E tests
4. Manual testing

### Step 5: Documentation
1. Update API documentation
2. Update developer guides
3. Update migration guide

---

## ⚠️ Breaking Changes

### API Breaking Changes
- ❌ `POST /triggers/api` → ✅ `POST /triggers`
- ❌ `POST /triggers/schedule` → ✅ `POST /triggers`
- ❌ `POST /triggers/event` → ✅ `POST /triggers`
- ❌ `POST /triggers/{id}/initialize` → ✅ Removed (via workflow activation)
- ❌ `POST /triggers/{id}/start/pause/resume/stop` → ✅ Removed (via workflow activation)

### Database Breaking Changes
- ❌ `triggers.workflow_id` → ✅ Removed
- ❌ `triggers.node_id` → ✅ Removed
- ✅ `triggers.name` → ✅ Added

### Frontend Breaking Changes
- ❌ Old node structure → ✅ New node structure with `nodeType` and `nodeConfig`
- ❌ Trigger creation with workflowId → ✅ Trigger config creation (independent)

---

## 🎯 Implementation Priority

### High Priority (Core Functionality)
1. ✅ Database migration
2. ✅ Trigger entity update
3. ✅ TriggerService refactoring
4. ✅ TriggerController refactoring
5. ✅ WorkflowService lifecycle management
6. ✅ Frontend types update
7. ✅ PropertiesPanel update
8. ✅ NodePalette update

### Medium Priority (Enhancements)
1. TriggerRegistryController update
2. WorkflowExecutor node structure support
3. Service layer updates
4. Component updates

### Low Priority (Nice to Have)
1. Migration utilities
2. Backward compatibility layer (nếu cần)
3. Enhanced error handling
4. Performance optimizations

---

## 📚 Related Documentation

- [Design Questions](./design-questions.md)
- [Clarification Questions](./clarification-questions.md)
- [Design Gap Analysis](./design-gap-analysis.md)
- [API Endpoints](../api/endpoints.md)
- [Database Schema](../database-schema/entities.md)
- [Trigger Registry](../features/trigger-registry.md)
- [Workflow Builder](../features/workflow-builder.md)

---

## ✅ Checklist

### Backend
- [ ] Database migration script
- [ ] Trigger entity update
- [ ] Workflow entity update
- [ ] TriggerService refactoring
- [ ] TriggerController refactoring
- [ ] TriggerRegistryController update
- [ ] WorkflowService lifecycle methods
- [ ] DTOs update
- [ ] WorkflowExecutor node structure support
- [ ] Unit tests
- [ ] Integration tests

### Frontend
- [ ] Types update
- [ ] Trigger service update
- [ ] Trigger registry service update
- [ ] NodePalette update
- [ ] PropertiesPanel update
- [ ] TriggerDialog update
- [ ] TriggerEditor update
- [ ] GuidedWorkflowWizard update
- [ ] Workflow builder update
- [ ] Unit tests
- [ ] E2E tests

### Documentation
- [ ] API documentation update
- [ ] Migration guide
- [ ] Developer guide update

---

**Last Updated**: [Date]
**Status**: Draft - Ready for Review

