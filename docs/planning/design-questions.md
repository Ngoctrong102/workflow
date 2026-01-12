# Design Questions - Trigger/Action/Workflow Management

**Mục đích:** File này chứa các câu hỏi để làm rõ design decisions cho việc chuẩn hóa documentation và implementation.

**Hướng dẫn:** 
- Đánh dấu [x] cho option bạn chọn
- Thêm ghi chú nếu cần giải thích
- Nếu có option khác, viết vào phần "Other"

---

## 1. Trigger Registry Design

### Q1.1: Trigger Registry nên như thế nào?

- [X] **Option A:** Hardcoded trong code (như hiện tại) - trigger types cố định, không thể thêm mới
- [ ] **Option B:** Database table `trigger_definitions` - cho phép dynamic trigger definitions
- [ ] **Option C:** Hybrid - trigger types cơ bản hardcoded, custom triggers trong database

**Ghi chú:**
```
Trigger types là cố định, nhưng chúng ta có thể define được trigger mới với type là một trong các type đã được define sẵn trong code. trigger_definitions là một legacy table, xóa hết đi.
```

### Q1.2: Nếu chọn Option A (hardcoded), có cần hỗ trợ custom trigger types trong tương lai không?

- [X] Có, cần thiết kế để mở rộng
- [ ] Không, chỉ cần 3 types cơ bản (api-call, scheduler, event)

**Ghi chú:**
```
Chỉ cần mở rộng được là được, trước mắt không cần quá detail.
```

---

## 2. Workflow Creation Flow

### Q2.1: Flow tạo trigger trong workflow nên như thế nào?

- [ ] **Option A:** Tự động sync từ workflow definition (như hiện tại)
  - User tạo workflow với trigger node → System tự động tạo trigger config
- [X] **Option B:** Manual creation trước
  - User tạo trigger config trước → Thêm trigger node vào workflow → Link trigger config
- [ ] **Option C:** Cả hai (tự động + manual cho advanced cases)

**Ghi chú:**
```
Chúng ta sẽ tạo trước trigger config (Chưa thực sự chạy, chỉ chứa các field có thể sử dụng chung), khi trigger đó được kéo thả vào worflow thì nó mới thực sự được chạy. Có thể có các field của trigger được config riêng ở workflow, ví dụ như consumer group, mỗi workflow phải có group khác nhau để hoạt động độc lập.
```

### Q2.2: Nếu chọn Option A hoặc C, các endpoints `POST /triggers/api`, `POST /triggers/schedule`, `POST /triggers/event` nên:

- [ ] **Option A:** Xóa hoàn toàn (chỉ dùng workflow builder)
- [ ] **Option B:** Giữ lại nhưng đánh dấu legacy/deprecated
- [ ] **Option C:** Giữ lại và document như advanced API cho programmatic access

**Ghi chú:**
```
Nếu implement không khớp với design tôi mong muốn thì xóa thẳng tay.
```

### Q2.3: Khi nào trigger config được tạo?

- [ ] Khi workflow được tạo (create)
- [ ] Khi workflow được cập nhật (update)
- [ ] Khi workflow được activate
- [ ] Cả 3 trường hợp trên

**Ghi chú:**
```
Phải làm rõ là chúng ta có 2 khái niệm là trigger config và trigger instance, trigger config thì được lưu trong bảng trigger và nó chỉ chứa thông tin config chưa chạy thực tế. Nó được tạo khi người dùng vào mục quản lý trigger và tạo một config mới. Khi nó được gắn vào workflow thì nó sẽ thực sự được chạy. Lúc này trigger instance được tạo.
```

---

## 3. Trigger Node vs Trigger Config Relationship

### Q3.1: Mối quan hệ giữa trigger node (trong workflow definition) và trigger config (bảng `triggers`) nên như thế nào?

- [ ] **Option A:** 1 trigger node = 1 trigger config (1:1)
- [ ] **Option B:** 1 trigger node có thể reference nhiều trigger configs (1:N)
- [X] **Option C:** Nhiều trigger nodes có thể share 1 trigger config (N:1)

**Ghi chú:**
```
Trigger node chính là trigger instance tôi nói ở trên.
```

### Q3.2: Khi user xóa trigger node khỏi workflow definition:

- [ ] Xóa trigger config luôn (cascade delete)
- [X] Giữ trigger config nhưng đánh dấu inactive
- [ ] Soft delete trigger config

**Ghi chú:**
```
Giữ config và nó bị không ảnh hưởng gì hết.
```

### Q3.3: Khi user update trigger node config trong workflow definition:

- [ ] Tự động update trigger config trong database
- [ ] Tạo trigger config mới (versioning)
- [ ] Cảnh báo user về conflict

**Ghi chú:**
```
Update trigger node thì chỉ update các thông tin riêng thôi, ví dụ như consumer group, hoàn toàn không ảnh hưởng trigger config. Nhưng khi update trigger config thì phải apply cho toàn bộ các trigger node. Phải có cơ chế define field nào sẽ cần được set up ở bước edit workflow để ui render được form.
```

---

## 4. Action Registry

### Q4.1: Action Registry hiện tại có vấn đề gì không?

- [X] Không, hoạt động đúng
- [ ] Có, cần review và sửa

**Ghi chú:**
```
[Viết ghi chú của bạn ở đây]
```

### Q4.2: Nếu cần review, vấn đề cụ thể là gì?

**Ghi chú:**
```
[Viết mô tả vấn đề cụ thể ở đây]
```

### Q4.3: Flow tạo action node trong workflow:

- [ ] User chọn action từ registry → Configure → Lưu vào workflow definition
- [X] User tạo action definition trước → Thêm action node vào workflow
- [ ] Cả hai cách đều được

**Ghi chú:**
```
[Viết ghi chú của bạn ở đây]
```

---

## 5. API Endpoints Design

### Q5.1: Endpoint `GET /triggers/registry` nên trả về gì?

- [ ] **Option A:** Hardcoded trigger definitions (metadata only)
- [X] **Option B:** Trigger definitions từ database (nếu có table)
- [ ] **Option C:** Cả trigger definitions + trigger configs đã tạo

**Ghi chú:**
```
[Viết ghi chú của bạn ở đây]
```

### Q5.2: Endpoint `GET /workflows/{id}/triggers` nên trả về gì?

- [ ] **Option A:** Chỉ trigger configs của workflow đó
- [ ] **Option B:** Trigger configs + trigger definitions (merged)
- [X] **Option C:** Trigger nodes từ workflow definition + trigger configs

**Ghi chú:**
```
[Viết ghi chú của bạn ở đây]
```

### Q5.3: Endpoint `POST /trigger/{trigger_path}` (trigger workflow) nên:

- [ ] **Option A:** Tự động tìm workflow từ trigger path
- [ ] **Option B:** Cần specify workflow_id trong request
- [X] **Option C:** Cả hai (auto-detect hoặc manual)

**Ghi chú:**
```
[Viết ghi chú của bạn ở đây]
```

---

## 6. Database Schema

### Q6.1: Bảng `triggers` hiện tại có đủ không?

- [X] Đủ, không cần thay đổi
- [ ] Thiếu fields, cần thêm (ví dụ: `registry_id`, `instance_state`, etc.)

**Nếu thiếu, cần thêm fields gì:**
```
[Liệt kê các fields cần thêm]
```

### Q6.2: Có cần bảng `trigger_definitions` không?

- [X] Không, giữ hardcoded
- [ ] Có, để hỗ trợ dynamic trigger definitions

**Ghi chú:**
```
[Viết ghi chú của bạn ở đây]
```

### Q6.3: Field `status` trong bảng `triggers` nên lưu gì?

- [X] **Option A:** Chỉ metadata status (active/inactive) - runtime state lưu riêng
- [ ] **Option B:** Runtime state (INITIALIZED, ACTIVE, PAUSED, STOPPED, ERROR)
- [ ] **Option C:** Cả hai (metadata + runtime state)

**Ghi chú:**
```
Chỉ đơn giản là nó không được chọn trong lúc tạo workflow nếu inactive
```

---

## 7. User Experience & Frontend Flow

### Q7.1: Trong Workflow Builder, user tạo trigger như thế nào?

- [ ] **Option A:** Drag trigger node → Chọn trigger type từ registry → Configure → Done
- [ ] **Option B:** Drag trigger node → Chọn trigger type → Tạo trigger config riêng → Link
- [x] **Option C:** Tạo trigger config trước → Drag trigger node → Link trigger config

**Ghi chú:**
```
[Viết ghi chú của bạn ở đây]
```

### Q7.2: Khi user configure trigger node trong PropertiesPanel:

- [X] Config được lưu trực tiếp vào node data trong workflow definition
- [ ] Config được lưu vào trigger config table, node chỉ reference
- [ ] Cả hai (sync giữa node data và trigger config)

**Ghi chú:**
```
Trigger node sẽ lưu ref đến trigger config và kèm theo thông tin riêng của nó, ví dụ consumer group.
```

### Q7.3: Action nodes có flow tương tự trigger nodes không?

- [X] Có, giống hệt
- [ ] Không, action nodes chỉ lưu config trong node data, không có action config table riêng

**Ghi chú:**
```
[Viết ghi chú của bạn ở đây]
```

---

## 8. Documentation Structure

### Q8.1: Documentation nên tập trung vào flow nào?

- [ ] **Option A:** Workflow-first (tạo workflow → triggers tự động)
- [X] **Option B:** Trigger-first (tạo trigger → thêm vào workflow)
- [ ] **Option C:** Cả hai, document rõ khi nào dùng cách nào

**Ghi chú:**
```
Tạo trigger và action trước
```

### Q8.2: Có cần tách documentation thành "Basic Flow" và "Advanced Flow" không?

- [ ] Có, tách rõ ràng
- [X] Không, một flow duy nhất

**Ghi chú:**
```
[Viết ghi chú của bạn ở đây]
```

### Q8.3: Có cần migration guide từ legacy flow sang flow mới không?

- [ ] Có
- [X] Không

**Ghi chú:**
```
Yêu cầu xóa toàn bộ legacy
```

---

## 9. Implementation Priority

### Q9.1: Ưu tiên sửa gì trước?

- [X] **Priority 1:** Documentation (chuẩn hóa docs trước)
- [ ] **Priority 2:** Implementation (refactor code để khớp docs)
- [ ] **Priority 3:** Cả hai song song

**Ghi chú:**
```
[Viết ghi chú của bạn ở đây]
```

### Q9.2: Có breaking changes được chấp nhận không?

- [X] Có, sẵn sàng breaking changes để có design đúng
- [ ] Không, phải backward compatible

**Ghi chú:**
```
[Viết ghi chú của bạn ở đây]
```

---

## 10. Edge Cases & Special Scenarios

### Q10.1: Khi workflow có nhiều trigger nodes:

- [ ] Mỗi trigger node tạo 1 trigger config riêng
- [X] Có thể share trigger config giữa các nodes
- [ ] Chỉ cho phép 1 trigger node per workflow

**Ghi chú:**
```
Cũng có thể có nhìêu trigger với các trigger config khác nhau.
```

### Q10.2: Khi user duplicate workflow:

- [ ] Duplicate trigger configs luôn
- [X] Share trigger configs (reference)
- [ ] User chọn duplicate hoặc share

**Ghi chú:**
```
[Viết ghi chú của bạn ở đây]
```

### Q10.3: Khi workflow versioning:

- [X] Trigger configs versioned cùng workflow
- [ ] Trigger configs độc lập với version
- [ ] User chọn strategy

**Ghi chú:**
```
[Viết ghi chú của bạn ở đây]
```

---

## 11. Additional Questions / Concerns

**Nếu có câu hỏi hoặc concerns khác, viết ở đây:**

```
[Viết các câu hỏi/concerns của bạn ở đây]
```

---

## Notes

**Sau khi điền xong:**
1. Lưu file này
2. Thông báo cho Requirements Analyst
3. Analyst sẽ tổng hợp và cập nhật documentation

**Last Updated:** [Điền ngày bạn hoàn thành]

