# Fix Triggers Table Schema

## Vấn đề

Database có column `node_id` với constraint NOT NULL, nhưng theo design mới, trigger configs là độc lập và không cần `node_id` hoặc `workflow_id`.

**Lỗi:**
```
ERROR: null value in column "node_id" of relation "triggers" violates not-null constraint
```

## Giải pháp

Chạy script SQL để:
1. Xóa column `node_id`
2. Xóa column `workflow_id` (nếu có)
3. Thêm column `name` (nếu chưa có)
4. Xóa foreign key constraints và indexes liên quan

## Cách chạy

### Option 1: Chạy trực tiếp với psql

```bash
psql -U postgres -d notification_platform -f scripts/fix_triggers_table.sql
```

### Option 2: Chạy từ Docker

```bash
docker exec -i <postgres_container> psql -U postgres -d notification_platform < backend/scripts/fix_triggers_table.sql
```

### Option 3: Copy và paste vào psql console

1. Mở psql:
```bash
psql -U postgres -d notification_platform
```

2. Copy nội dung file `scripts/fix_triggers_table.sql` và paste vào console

### Option 4: Sử dụng pgAdmin hoặc DBeaver

1. Mở pgAdmin/DBeaver
2. Connect đến database `notification_platform`
3. Mở file `scripts/fix_triggers_table.sql`
4. Execute script

## Kiểm tra sau khi chạy

Sau khi chạy script, kiểm tra schema:

```sql
SELECT 
    column_name, 
    data_type, 
    is_nullable
FROM information_schema.columns 
WHERE table_name = 'triggers' 
ORDER BY ordinal_position;
```

Kết quả mong đợi:
- ✅ Có `id`, `name`, `trigger_type`, `config`, `status`, `error_message`, `created_at`, `updated_at`, `deleted_at`
- ❌ KHÔNG có `node_id`
- ❌ KHÔNG có `workflow_id`

## Lưu ý

- **Backup database trước khi chạy**: Script này sẽ xóa columns, nên backup trước
- **Script an toàn**: Script sử dụng `IF EXISTS` và `IF NOT EXISTS` nên có thể chạy nhiều lần
- **Không mất data**: Chỉ xóa columns không cần thiết, không xóa data

## Sau khi fix

Sau khi chạy script thành công, restart application và thử tạo trigger config lại.

