# Backend Implementation Gap Analysis

> **Tài liệu này phân tích những gì còn thiếu trong backend implementation so với design specifications.**

## Tổng quan

Sau khi kiểm tra codebase và so sánh với design documents, có nhiều components quan trọng chưa được implement, đặc biệt là **MVEL Expression System** và **Action Schema Structure**.

## Critical Missing Components

### 1. MVEL Expression System (HOÀN TOÀN THIẾU)

**Status**: ❌ **CHƯA IMPLEMENT**

**Design Requirements** (từ `docs/features/mvel-expression-system.md`):
- MVEL 2.x library integration
- MVEL evaluator utility class
- Execution context builder
- Support cho tất cả use cases: config values, templates, expressions, conditions, mappings, output mapping

**Current State**:
- ❌ Không có MVEL dependency trong `pom.xml`
- ❌ Không có `MvelEvaluator` utility class
- ❌ Không có `ExecutionContextBuilder` cho MVEL context
- ❌ Action executors vẫn dùng `TemplateRenderer` với `${}` syntax (old syntax)
- ❌ Chưa có support cho `@{expression}` syntax

**Required Implementation**:
- [ ] Add MVEL dependency: `org.mvel:mvel2:2.4.14.Final`
- [ ] Create `MvelEvaluator.java` utility class
- [ ] Create `ExecutionContextBuilder.java` utility class
- [ ] Update tất cả action executors để sử dụng MVEL thay vì TemplateRenderer
- [ ] Support recursive evaluation trong nested objects và arrays

**Files to Create**:
- `backend/src/main/java/com/notificationplatform/util/MvelEvaluator.java`
- `backend/src/main/java/com/notificationplatform/util/ExecutionContextBuilder.java`

**Files to Update**:
- `backend/src/main/java/com/notificationplatform/engine/nodes/ApiCallNodeExecutor.java`
- `backend/src/main/java/com/notificationplatform/engine/nodes/PublishEventNodeExecutor.java`
- `backend/src/main/java/com/notificationplatform/engine/nodes/FunctionNodeExecutor.java`
- `backend/src/main/java/com/notificationplatform/engine/nodes/ActionNodeExecutor.java`
- `backend/src/main/java/com/notificationplatform/engine/nodes/LogicNodeExecutor.java`
- `backend/src/main/java/com/notificationplatform/engine/nodes/ConditionNodeExecutor.java`

### 2. Action Schema Structure (THIẾU PHẦN LỚN)

**Status**: ⚠️ **PARTIALLY IMPLEMENTED**

**Design Requirements** (từ `docs/features/action-registry.md`):
- Input Schema (Optional)
- Config Template Schema (Required)
- Output Schema (Required)
- Output Mapping (Required)

**Current State**:
- ✅ Action entity có `configTemplate` field (JSONB)
- ❌ Chưa có structure rõ ràng cho Input Schema, Config Template Schema, Output Schema, Output Mapping
- ❌ Chưa có validation cho schema structure
- ❌ Chưa có DTO classes cho schema definitions

**Required Implementation**:
- [ ] Update Action entity để có structure rõ ràng cho 4 components
- [ ] Create `SchemaDefinition.java` DTO class
- [ ] Create `ActionConfigTemplate.java` DTO class với đầy đủ fields
- [ ] Add validation cho schema structure (Sprint 26 đã có nhưng cần check)
- [ ] Update ActionRegistryService để handle schema structure

**Files to Create/Update**:
- `backend/src/main/java/com/notificationplatform/dto/SchemaDefinition.java`
- `backend/src/main/java/com/notificationplatform/dto/ActionConfigTemplate.java`
- `backend/src/main/java/com/notificationplatform/entity/Action.java` (update configTemplate structure)

### 3. Output Mapping Mechanism (HOÀN TOÀN THIẾU)

**Status**: ❌ **CHƯA IMPLEMENT**

**Design Requirements** (từ `docs/technical/backend/action-execution.md`):
- Apply output mapping với MVEL expressions
- Map từ raw response vào output schema structure
- Support nested fields và arrays
- Validate mapped output

**Current State**:
- ❌ Không có `OutputMappingApplier` utility class
- ❌ Action executors không apply output mapping
- ❌ Output được return trực tiếp từ action execution, chưa được map vào output schema

**Required Implementation**:
- [ ] Create `OutputMappingApplier.java` utility class
- [ ] Update action executors để apply output mapping sau khi execute
- [ ] Build output context với `_response` chứa raw response
- [ ] Validate mapped output against output schema

**Files to Create**:
- `backend/src/main/java/com/notificationplatform/util/OutputMappingApplier.java`

**Files to Update**:
- Tất cả action executors (ApiCallNodeExecutor, PublishEventNodeExecutor, FunctionNodeExecutor, etc.)

### 4. Config Validator (THIẾU)

**Status**: ⚠️ **PARTIALLY IMPLEMENTED**

**Design Requirements**:
- Validate resolved config against config template schema
- Validate mapped output against output schema
- Check required fields, types, enum values, validation rules

**Current State**:
- ✅ Có `ActionConfigTemplateValidator.java` (Sprint 26)
- ❌ Chưa có generic `ConfigValidator` utility class
- ❌ Chưa validate resolved config trước khi execute action
- ❌ Chưa validate mapped output sau khi apply output mapping

**Required Implementation**:
- [ ] Create `ConfigValidator.java` utility class (generic, reusable)
- [ ] Validate resolved config trong action executors
- [ ] Validate mapped output sau khi apply output mapping

**Files to Create**:
- `backend/src/main/java/com/notificationplatform/util/ConfigValidator.java`

### 5. NodeConfig Structure (THIẾU)

**Status**: ⚠️ **PARTIALLY IMPLEMENTED**

**Design Requirements** (từ `docs/technical/backend/action-execution.md`):
```java
public class NodeConfig {
    private Map<String, Object> configValues; // Config values với MVEL expressions
    private Map<String, FieldMapping> inputMappings; // Optional: input data mappings
    private Map<String, String> outputMapping; // Optional: custom output mapping (override registry)
}
```

**Current State**:
- ✅ `NodeExecution.nodeConfig` là `Map<String, Object>` (JSONB)
- ❌ Chưa có structure rõ ràng cho `configValues`, `inputMappings`, `outputMapping`
- ❌ Action executors đọc config trực tiếp từ `nodeData`, chưa sử dụng `configValues` structure

**Required Implementation**:
- [ ] Create `NodeConfig.java` DTO class với đầy đủ fields
- [ ] Update action executors để đọc từ `nodeConfig.configValues`
- [ ] Support `inputMappings` và `outputMapping` trong node config

**Files to Create**:
- `backend/src/main/java/com/notificationplatform/dto/NodeConfig.java`
- `backend/src/main/java/com/notificationplatform/dto/FieldMapping.java`

### 6. Action Executors Update (CẦN UPDATE LỚN)

**Status**: ⚠️ **CẦN REFACTOR**

**Current Implementation**:
- `ApiCallNodeExecutor` - Dùng `TemplateRenderer` với `${}` syntax
- `PublishEventNodeExecutor` - Chưa check implementation
- `FunctionNodeExecutor` - Dùng `TemplateRenderer`
- `ActionNodeExecutor` - Dùng `TemplateRenderer`

**Required Changes**:
- [ ] Replace `TemplateRenderer` với `MvelEvaluator`
- [ ] Load action registry để get schemas và outputMapping
- [ ] Build execution context với `ExecutionContextBuilder`
- [ ] Evaluate MVEL expressions trong config values
- [ ] Validate resolved config
- [ ] Execute action → get raw response
- [ ] Apply output mapping với MVEL
- [ ] Validate mapped output
- [ ] Return mapped output

**Files to Update**:
- `backend/src/main/java/com/notificationplatform/engine/nodes/ApiCallNodeExecutor.java`
- `backend/src/main/java/com/notificationplatform/engine/nodes/PublishEventNodeExecutor.java`
- `backend/src/main/java/com/notificationplatform/engine/nodes/FunctionNodeExecutor.java`
- `backend/src/main/java/com/notificationplatform/engine/nodes/ActionNodeExecutor.java`

### 7. ExecutionContext Enhancement (CẦN UPDATE)

**Status**: ⚠️ **CẦN ENHANCE**

**Current State**:
- ✅ `ExecutionContext` có `nodeOutputs`, `variables`, `triggerDataMap`
- ❌ Chưa có method để build MVEL context structure
- ❌ Chưa support `_trigger`, `_vars` prefixes đúng cách
- ❌ Chưa có built-in functions trong context

**Required Changes**:
- [ ] Add method `buildMvelContext()` để build context cho MVEL evaluation
- [ ] Ensure `_trigger` và `_vars` prefixes được support đúng
- [ ] Add built-in functions: `_now()`, `_uuid()`, `_date()`, `_timestamp()`, etc.

**Files to Update**:
- `backend/src/main/java/com/notificationplatform/engine/ExecutionContext.java`

### 8. Logic Node Executors (CẦN UPDATE)

**Status**: ⚠️ **CẦN UPDATE**

**Current State**:
- `LogicNodeExecutor` - Basic condition evaluation
- `ConditionNodeExecutor` - Chưa check implementation
- Chưa sử dụng MVEL cho condition expressions

**Required Changes**:
- [ ] Update condition evaluation để sử dụng MVEL
- [ ] Support MVEL expressions trong condition: `@{user.age} >= 18 && @{user.status} == 'active'`
- [ ] Support MVEL trong switch case expressions

**Files to Update**:
- `backend/src/main/java/com/notificationplatform/engine/nodes/LogicNodeExecutor.java`
- `backend/src/main/java/com/notificationplatform/engine/nodes/ConditionNodeExecutor.java`
- `backend/src/main/java/com/notificationplatform/engine/nodes/SwitchNodeExecutor.java`

### 9. Data Node Executors (CẦN UPDATE)

**Status**: ⚠️ **CẦN UPDATE**

**Current State**:
- `DataNodeExecutor` - Map, Transform, Filter nodes
- Chưa sử dụng MVEL cho field mappings và transformations

**Required Changes**:
- [ ] Update field mappings để sử dụng MVEL expressions
- [ ] Support MVEL trong transform expressions: `@{source.email.toLowerCase()}`
- [ ] Support MVEL trong filter conditions

**Files to Update**:
- `backend/src/main/java/com/notificationplatform/engine/nodes/DataNodeExecutor.java`

### 10. Notification Templates (CẦN UPDATE)

**Status**: ⚠️ **CẦN UPDATE**

**Current State**:
- `TemplateRenderer` được sử dụng cho notification templates
- Dùng `${}` syntax (old syntax)

**Required Changes**:
- [ ] Update `TemplateRenderer` để support MVEL `@{expression}` syntax
- [ ] Hoặc tạo `MvelTemplateRenderer` mới
- [ ] Migrate từ `${}` sang `@{expression}`

**Files to Update**:
- `backend/src/main/java/com/notificationplatform/service/template/TemplateRenderer.java`
- Hoặc create new `MvelTemplateRenderer.java`

## Implementation Priority

### Phase 1: Core MVEL Infrastructure (CRITICAL)
1. ✅ Add MVEL dependency
2. ✅ Create `MvelEvaluator` utility
3. ✅ Create `ExecutionContextBuilder` utility
4. ✅ Update `ExecutionContext` để support MVEL context

### Phase 2: Action Schema Structure (CRITICAL)
1. ✅ Create `SchemaDefinition` DTO
2. ✅ Create `ActionConfigTemplate` DTO
3. ✅ Update Action entity structure
4. ✅ Update ActionRegistryService

### Phase 3: Action Executors với MVEL (CRITICAL)
1. ✅ Update `ApiCallNodeExecutor` với MVEL
2. ✅ Update `PublishEventNodeExecutor` với MVEL
3. ✅ Update `FunctionNodeExecutor` với MVEL
4. ✅ Update `ActionNodeExecutor` với MVEL

### Phase 4: Output Mapping (CRITICAL)
1. ✅ Create `OutputMappingApplier` utility
2. ✅ Create `ConfigValidator` utility
3. ✅ Integrate output mapping vào action executors

### Phase 5: NodeConfig Structure (HIGH)
1. ✅ Create `NodeConfig` DTO
2. ✅ Create `FieldMapping` DTO
3. ✅ Update action executors để sử dụng NodeConfig structure

### Phase 6: Logic & Data Nodes (HIGH)
1. ✅ Update `LogicNodeExecutor` với MVEL
2. ✅ Update `ConditionNodeExecutor` với MVEL
3. ✅ Update `DataNodeExecutor` với MVEL

### Phase 7: Templates & Other (MEDIUM)
1. ✅ Update `TemplateRenderer` hoặc create `MvelTemplateRenderer`
2. ✅ Migrate old `${}` syntax sang `@{expression}`

## Missing Sprint Planning

**Current Sprints**: 1-27
**Missing Sprints**: Cần thêm sprints cho các components còn thiếu

### Suggested Additional Sprints

**Sprint 28**: MVEL Infrastructure
- Add MVEL dependency
- Create MvelEvaluator
- Create ExecutionContextBuilder
- Update ExecutionContext

**Sprint 29**: Action Schema Structure
- Create SchemaDefinition DTO
- Create ActionConfigTemplate DTO
- Update Action entity
- Update ActionRegistryService

**Sprint 30**: NodeConfig Structure
- Create NodeConfig DTO
- Create FieldMapping DTO
- Update workflow definition structure

**Sprint 31**: Action Executors - MVEL Integration
- Update ApiCallNodeExecutor
- Update PublishEventNodeExecutor
- Update FunctionNodeExecutor

**Sprint 32**: Output Mapping
- Create OutputMappingApplier
- Create ConfigValidator
- Integrate vào action executors

**Sprint 33**: Logic & Data Nodes - MVEL
- Update LogicNodeExecutor
- Update ConditionNodeExecutor
- Update DataNodeExecutor

**Sprint 34**: Template Migration
- Update TemplateRenderer
- Migrate old syntax

## Comparison với Design

### MVEL Expression System Design vs Implementation

| Component | Design | Implementation | Status |
|-----------|--------|----------------|--------|
| MVEL Library | Required | ❌ Missing | **CRITICAL** |
| MvelEvaluator | Required | ❌ Missing | **CRITICAL** |
| ExecutionContextBuilder | Required | ❌ Missing | **CRITICAL** |
| Config Evaluation | Required | ❌ Missing | **CRITICAL** |
| Output Mapping | Required | ❌ Missing | **CRITICAL** |
| Built-in Functions | Required | ❌ Missing | **CRITICAL** |

### Action Schema Structure Design vs Implementation

| Component | Design | Implementation | Status |
|-----------|--------|----------------|--------|
| Input Schema | Optional | ⚠️ Partial | **HIGH** |
| Config Template Schema | Required | ⚠️ Partial | **CRITICAL** |
| Output Schema | Required | ⚠️ Partial | **CRITICAL** |
| Output Mapping | Required | ❌ Missing | **CRITICAL** |
| Schema Validation | Required | ⚠️ Partial | **HIGH** |

### Action Execution Flow Design vs Implementation

| Step | Design | Implementation | Status |
|------|--------|----------------|--------|
| Load Action Registry | Required | ✅ Done | OK |
| Build Execution Context | Required | ⚠️ Partial | **HIGH** |
| Evaluate MVEL Config | Required | ❌ Missing | **CRITICAL** |
| Validate Config | Required | ⚠️ Partial | **HIGH** |
| Execute Action | Required | ✅ Done | OK |
| Build Output Context | Required | ❌ Missing | **CRITICAL** |
| Apply Output Mapping | Required | ❌ Missing | **CRITICAL** |
| Validate Output | Required | ❌ Missing | **CRITICAL** |
| Add to Context | Required | ✅ Done | OK |

## Recommendations

1. **IMMEDIATE**: Implement MVEL infrastructure (Sprint 28)
2. **IMMEDIATE**: Implement Action Schema Structure (Sprint 29)
3. **HIGH**: Implement Output Mapping (Sprint 32)
4. **HIGH**: Update Action Executors (Sprint 31)
5. **MEDIUM**: Update Logic/Data Nodes (Sprint 33)
6. **MEDIUM**: Template Migration (Sprint 34)

## Related Documentation

- [MVEL Expression System](../../features/mvel-expression-system.md) - Design specification
- [Action Registry](../../features/action-registry.md) - Action schema structure
- [Action Execution](../technical/backend/action-execution.md) - Implementation guide
- [Planning: Sprint 27](./sprint-27.md) - Backend MVEL Evaluation (chưa implement)

