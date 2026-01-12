# Design Gap Analysis: Trigger/Action/Workflow Management

## 📋 Tổng quan

Tài liệu này phân tích các điểm không khớp giữa **documentation**, **database design**, và **implementation hiện tại** cho các luồng:
1. Tạo/quản lý Trigger
2. Tạo/quản lý Action  
3. Tạo/quản lý Workflow

## 🔍 Phân tích chi tiết

### 1. Trigger Registry vs Trigger Config

#### Documentation hiện tại

**Trong `docs/features/trigger-registry.md`:**
- Nói về bảng `trigger_definitions` để lưu trigger definitions
- Trigger types được định nghĩa trong registry trước khi sử dụng
- API: `GET /api/v1/triggers/definitions`

**Trong `docs/database-schema/entities.md`:**
- Nói trigger types được **hardcoded trong code** (api-call, scheduler, event)
- Bảng `triggers` lưu trigger configs cho workflows (không phải definitions)
- Concept hierarchy:
  1. Trigger Registry (hardcoded) → Template definitions
  2. Trigger Config (bảng `triggers`) → User-created configuration
  3. Trigger Node (trong `workflows.definition`) → Node reference trigger config

#### Implementation hiện tại

**Backend:**
- ✅ `TriggerRegistryService` với hardcoded trigger definitions (không có database table)
- ✅ Bảng `triggers` lưu trigger configs với `workflow_id`, `node_id`, `trigger_type`, `config`
- ✅ `TriggerController` có endpoints:
  - `POST /triggers/api` - Tạo API trigger
  - `POST /triggers/schedule` - Tạo schedule trigger
  - `POST /triggers/event` - Tạo event trigger
- ✅ `WorkflowScheduleSyncService` và `WorkflowEventTriggerSyncService` tự động sync triggers từ workflow definition

**Frontend:**
- ✅ Có `TriggerRegistryService` để lấy trigger definitions
- ✅ Workflow builder cho phép chọn trigger từ registry

#### Vấn đề

1. **Documentation không nhất quán:**
   - `trigger-registry.md` nói về bảng `trigger_definitions` nhưng thực tế không có
   - `entities.md` nói trigger types hardcoded nhưng `trigger-registry.md` lại nói có database table

2. **API endpoints không khớp với documentation:**
   - Documentation nói trigger được tạo tự động khi workflow được tạo
   - Implementation có endpoints tạo trigger độc lập (`POST /triggers/api`, etc.)
   - Không rõ khi nào dùng endpoint nào

3. **Flow không rõ ràng:**
   - User tạo trigger trước rồi thêm vào workflow?
   - Hay user tạo workflow với trigger node, system tự động tạo trigger config?

### 2. Workflow Creation Flow

#### Documentation hiện tại

**Trong `docs/features/workflow-builder.md`:**
- User drag trigger node vào canvas
- Chọn trigger từ registry
- Configure trigger instance
- System tự động tạo trigger config

**Trong `docs/database-schema/relationships.md`:**
- Trigger config được tạo khi trigger node được thêm vào workflow
- Trigger config reference đến trigger node qua `node_id`

#### Implementation hiện tại

**Backend:**
- ✅ `WorkflowServiceImpl.createWorkflow()` tự động sync triggers từ definition
- ✅ `WorkflowScheduleSyncService.syncScheduleTriggers()` tạo trigger configs từ schedule nodes
- ✅ `WorkflowEventTriggerSyncService.syncEventTriggers()` tạo trigger configs từ event nodes
- ✅ Trigger config được tạo tự động khi workflow được save/update

**Frontend:**
- ✅ `GuidedWorkflowWizard` tạo workflow với trigger node trong definition
- ✅ Trigger config được lưu trong node data, không tạo trigger riêng biệt

#### Vấn đề

1. **API endpoints không khớp:**
   - `POST /triggers/api` cho phép tạo trigger độc lập
   - Nhưng flow thực tế là trigger được tạo tự động từ workflow definition
   - Không rõ khi nào cần dùng endpoint này

2. **Documentation không mô tả rõ sync flow:**
   - Không giải thích khi nào trigger config được tạo
   - Không giải thích mối quan hệ giữa trigger node và trigger config

### 3. Action Registry

#### Documentation hiện tại

**Trong `docs/features/action-registry.md`:**
- Actions được định nghĩa trong registry (bảng `actions`)
- User chọn action từ registry khi tạo action node
- Action node có `registryId` reference đến action definition

**Trong `docs/database-schema/entities.md`:**
- Bảng `actions` lưu action definitions
- Action node trong workflow definition có `registryId`

#### Implementation hiện tại

**Backend:**
- ✅ Bảng `actions` lưu action definitions
- ✅ `ActionRegistryService` quản lý actions
- ✅ `ActionNodeExecutor` load action từ registry qua `registryId`
- ✅ Action node trong workflow definition có `registryId`

#### Vấn đề

**Action registry có vẻ đúng**, nhưng cần kiểm tra:
- API endpoints có khớp với documentation không?
- Flow tạo action có rõ ràng không?

## 🎯 Đề xuất giải pháp

### Giải pháp 1: Chuẩn hóa Documentation (Khuyến nghị)

**Mục tiêu:** Làm rõ design và flow thực tế trong documentation.

#### 1.1 Cập nhật Trigger Registry Documentation

**File:** `docs/features/trigger-registry.md`

**Thay đổi:**
- ❌ Xóa phần về bảng `trigger_definitions` (không tồn tại)
- ✅ Làm rõ trigger types được hardcoded trong code
- ✅ Giải thích rõ concept hierarchy:
  1. **Trigger Registry** (hardcoded): Template definitions (api-trigger-standard, scheduler-trigger-standard, kafka-event-trigger-standard)
  2. **Trigger Config** (bảng `triggers`): User-created configuration cho workflow trigger node
  3. **Trigger Node** (trong `workflows.definition`): Node trong workflow graph reference đến trigger config qua `node_id`
  4. **Runtime Instance**: Consumer/scheduler instance được tạo khi workflow activated

#### 1.2 Cập nhật Workflow Creation Flow

**File:** `docs/features/workflow-builder.md`

**Thêm section mới:**

```markdown
## Trigger Configuration Flow

### Automatic Trigger Sync

Khi workflow được tạo hoặc cập nhật, system tự động sync triggers từ workflow definition:

1. **User tạo workflow** với trigger node trong definition
2. **System validate** workflow definition
3. **System tự động tạo trigger configs** từ trigger nodes:
   - Schedule trigger nodes → `WorkflowScheduleSyncService.syncScheduleTriggers()`
   - Event trigger nodes → `WorkflowEventTriggerSyncService.syncEventTriggers()`
   - API trigger nodes → Tạo trigger config khi workflow activated
4. **Trigger configs** được lưu vào bảng `triggers` với:
   - `workflow_id`: Reference đến workflow
   - `node_id`: Node ID trong workflow definition
   - `trigger_type`: Type từ registry
   - `config`: Configuration từ node data

### Manual Trigger Management (Legacy/Advanced)

**Note:** Các endpoints `POST /triggers/api`, `POST /triggers/schedule`, `POST /triggers/event` là legacy endpoints cho advanced use cases. 

**Khuyến nghị:** Sử dụng workflow builder để tạo triggers tự động thay vì tạo manual.
```

#### 1.3 Cập nhật API Documentation

**File:** `docs/api/endpoints.md`

**Thay đổi:**

1. **Trigger Registry Endpoints:**
   - ✅ Giữ nguyên: `GET /triggers/registry` (lấy hardcoded definitions)
   - ✅ Làm rõ: Trigger definitions được hardcoded, không có database table

2. **Trigger Config Endpoints:**
   - ⚠️ Đánh dấu legacy: `POST /triggers/api`, `POST /triggers/schedule`, `POST /triggers/event`
   - ✅ Thêm note: "These endpoints are for advanced use cases. Recommended: Create triggers through workflow builder."

3. **Workflow Endpoints:**
   - ✅ Thêm note: "Triggers are automatically synced from workflow definition when workflow is created/updated."

#### 1.4 Cập nhật Database Schema Documentation

**File:** `docs/database-schema/entities.md`

**Thay đổi:**

- ✅ Xóa mention về `trigger_definitions` table (không tồn tại)
- ✅ Làm rõ trigger registry là hardcoded trong code
- ✅ Giải thích rõ trigger config được tạo tự động từ workflow definition

### Giải pháp 2: Refactor Implementation (Nếu cần)

**Nếu muốn thay đổi implementation để khớp với documentation hiện tại:**

#### Option A: Thêm Trigger Definitions Table

- Tạo bảng `trigger_definitions` trong database
- Migrate hardcoded definitions vào database
- Update `TriggerRegistryService` để load từ database

**Pros:**
- Khớp với documentation hiện tại
- Cho phép dynamic trigger definitions

**Cons:**
- Phức tạp hơn
- Cần migration
- Trigger types cơ bản không cần dynamic

#### Option B: Xóa Legacy Endpoints

- Xóa `POST /triggers/api`, `POST /triggers/schedule`, `POST /triggers/event`
- Chỉ cho phép tạo triggers qua workflow builder

**Pros:**
- Đơn giản hơn
- Flow rõ ràng hơn

**Cons:**
- Breaking change
- Mất flexibility cho advanced use cases

## 📝 Action Items

### Priority 1: Chuẩn hóa Documentation

1. ✅ Cập nhật `docs/features/trigger-registry.md`:
   - Xóa phần về `trigger_definitions` table
   - Làm rõ trigger types hardcoded
   - Giải thích concept hierarchy

2. ✅ Cập nhật `docs/features/workflow-builder.md`:
   - Thêm section về trigger sync flow
   - Giải thích khi nào trigger config được tạo

3. ✅ Cập nhật `docs/api/endpoints.md`:
   - Đánh dấu legacy endpoints
   - Thêm notes về automatic trigger sync

4. ✅ Cập nhật `docs/database-schema/entities.md`:
   - Xóa mention về `trigger_definitions`
   - Làm rõ trigger registry hardcoded

### Priority 2: Review Action Registry

1. ✅ Kiểm tra action registry có khớp với documentation không
2. ✅ Review API endpoints cho actions
3. ✅ Đảm bảo flow tạo action rõ ràng

### Priority 3: Testing & Validation

1. ✅ Test workflow creation flow
2. ✅ Test trigger sync từ workflow definition
3. ✅ Validate documentation với implementation

## 🔗 Related Documentation

- [Trigger Registry](./features/trigger-registry.md)
- [Workflow Builder](./features/workflow-builder.md)
- [Action Registry](./features/action-registry.md)
- [Database Schema - Entities](./database-schema/entities.md)
- [Database Schema - Relationships](./database-schema/relationships.md)
- [API Endpoints](./api/endpoints.md)

