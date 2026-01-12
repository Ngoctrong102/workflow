# Clarification Questions - Cần làm rõ trước khi update documentation

## 🔍 Các điểm cần làm rõ

### 1. Trigger Instance Storage

**Câu hỏi:** Trigger instance được lưu ở đâu?

Từ câu trả lời, tôi hiểu:
- Trigger Config: Lưu trong bảng `triggers` (chưa chạy)
- Trigger Instance: Khi trigger config được gắn vào workflow thì tạo instance

**Cần làm rõ:**
- [ ] Trigger instance có bảng riêng không? (ví dụ: `trigger_instances`)
- [ ] Hay trigger instance chỉ là runtime state (trong memory/Redis)?
- [X] Hay trigger instance được lưu trong workflow definition node data?

**Ghi chú của bạn:**
```
Được lưu trong workflow config, không có bảng riêng.
```

---

### 2. Trigger Registry Endpoint (Q5.1 vs Q6.2)

**Mâu thuẫn phát hiện:**
- Q5.1: Chọn Option B - "Trigger definitions từ database"
- Q6.2: Chọn "Không, giữ hardcoded" - không cần bảng `trigger_definitions`

**Cần làm rõ:**
- [X] `GET /triggers/registry` nên trả về trigger configs đã tạo (từ bảng `triggers`)?
- [ ] Hay vẫn trả về hardcoded trigger types?
- [ ] Hay cả hai (hardcoded types + trigger configs có thể chọn)?

**Ghi chú của bạn:**
```
Ý tôi là chúng ta chỉ có giới hạn các loại trigger cứng là API call, Event, Scheduler. Nhưng sẽ có thể có nhiều trigger configs loại API call, Event và Scheduler. Ví dụ 10 API Call, 25 trigger configs loại Scheduler, mỗi config là mọt row trong table `triggers`.
```

---

### 3. Action Config Table

**Câu hỏi:** Action có cần bảng action configs riêng không?

Từ câu trả lời Q4.3 và Q7.3:
- Action giống trigger (tạo action definition trước → thêm vào workflow)
- Action nodes có flow tương tự trigger nodes

**Cần làm rõ:**
- [ ] Có cần bảng `action_configs` tương tự bảng `triggers` không?
- [X] Hay action chỉ có bảng `actions` (registry) và config lưu trong node data?
- [ ] Nếu có action configs, có thể share giữa nhiều action nodes không?

**Ghi chú của bạn:**
```
[Viết câu trả lời ở đây]
```

---

### 4. Trigger Instance Fields (Override Fields)

**Câu hỏi:** Field nào có thể override ở trigger instance level?

Từ câu trả lời Q3.3 và Q7.2:
- Trigger node lưu ref đến trigger config + thông tin riêng (ví dụ: consumer group)
- Update trigger node chỉ update thông tin riêng, không ảnh hưởng trigger config

**Cần làm rõ:**
- [ ] List các fields có thể override ở trigger instance level:
  - Consumer Group (cho Kafka event trigger)
  - Endpoint Path (cho API trigger)?
  - Cron Expression (cho Scheduler trigger)?
  - Các fields khác?

**Ghi chú của bạn:**
```
Trước mắt thì chỉ có Consumer Group, nhưng phải có cơ chế đề dễ dàng define thêm.
```

---

### 5. Trigger Config Schema Definition

**Câu hỏi:** Làm sao UI biết field nào có thể config ở workflow level?

Từ câu trả lời Q3.3:
- "Phải có cơ chế define field nào sẽ cần được set up ở bước edit workflow để ui render được form"

**Cần làm rõ:**
- [ ] Trigger config có schema definition không? (ví dụ: JSON schema)
- [ ] Schema này định nghĩa:
  - Field nào là shared (từ trigger config)
  - Field nào là instance-specific (override ở workflow level)
  - Field nào là required/optional
- [ ] Schema này lưu ở đâu? (trong trigger config JSONB? trong hardcoded trigger type definition?)

**Ghi chú của bạn:**
```
Có schema definition. Nên define rõ các thông tin bạn nhắc tới. Schema này cần định nghĩa bằng java rõ ràng để có thể sử dụng được khi implement TriggerExecutor.
```

---

### 6. Workflow Definition Structure

**Câu hỏi:** Trigger node trong workflow definition có cấu trúc như thế nào?

Từ câu trả lời Q7.2:
- Trigger node lưu ref đến trigger config + thông tin riêng

**Cần làm rõ:**
- [ ] Trigger node có structure như sau không?
  ```json
  {
    "id": "node-1",
    "type": "trigger",
    "subType": "event",
    "triggerConfigId": "trigger-config-123",  // Reference
    "instanceConfig": {
      "consumerGroup": "workflow-456-consumer"  // Override fields
    }
  }
  ```
- [ ] Hay structure khác?

**Ghi chú của bạn:**
```
Nó nên như thế này:
  ```json
  {
    "id": "node-1",
    "nodeType": "trigger",
    "nodeConfig": {
        "triggerConfigId": "trigger-config-123",  // Reference
        "triggerType": "event",
        "instanceConfig": {
            "consumerGroup": "workflow-456-consumer"  // Override fields
        }
    }
  }
  ```
```

---

### 7. Trigger Instance Lifecycle

**Câu hỏi:** Trigger instance lifecycle được quản lý như thế nào?

Từ câu trả lời Q2.3:
- Trigger instance được tạo khi trigger config được gắn vào workflow

**Cần làm rõ:**
- [ ] Khi nào trigger instance được start/stop/pause?
  - Khi workflow được activate?
  - Khi trigger node được enable/disable?
- [ ] Runtime state (ACTIVE, PAUSED, STOPPED) được lưu ở đâu?
  - Trong workflow definition?
  - Trong bảng riêng?
  - Trong memory/Redis?

**Ghi chú của bạn:**
```
- Khi nào trigger instance được start/stop/pause? -> Khi workflow được activate?
- Runtime state (ACTIVE, PAUSED, STOPPED) được lưu ở đâu? -> Trong workflow definition.
```

---

## ✅ Sau khi trả lời

Tôi sẽ:
1. Tổng hợp design hoàn chỉnh
2. Update documentation theo design mới
3. Xóa toàn bộ legacy documentation
4. Bắt đầu với Feature và User Flow documentation

