# Setup Email Workflow Script

Script này giúp bạn tự động setup một workflow gửi email theo lịch (mỗi phút) đến địa chỉ email của bạn.

## Yêu cầu

1. Backend application đang chạy tại `http://localhost:8080/api/v1`
2. Gmail account với App Password đã được tạo

## Cách tạo Gmail App Password

1. Đăng nhập vào Google Account: https://myaccount.google.com/
2. Bật **2-Step Verification** (nếu chưa bật)
3. Tạo App Password:
   - Vào **Security** → **2-Step Verification**
   - Cuộn xuống và click **App passwords**
   - Chọn **Mail** và **Other (Custom name)**
   - Nhập tên: "Notification Platform"
   - Copy App Password (16 ký tự)

## Cách sử dụng

### Cách 1: Chạy script với prompts

```bash
cd backend
./scripts/setup-email-workflow.sh
```

Script sẽ hỏi bạn:
- Gmail address
- Gmail App Password

### Cách 2: Chạy script với environment variables

```bash
cd backend
export GMAIL_USER="your-email@gmail.com"
export GMAIL_APP_PASSWORD="your-app-password"
export EMAIL="trong04102000@gmail.com"  # Email nhận
./scripts/setup-email-workflow.sh
```

### Cách 3: Chạy script với custom API URL

```bash
cd backend
export API_BASE_URL="http://your-server:8080/api/v1"
export GMAIL_USER="your-email@gmail.com"
export GMAIL_APP_PASSWORD="your-app-password"
export EMAIL="trong04102000@gmail.com"
./scripts/setup-email-workflow.sh
```

## Script sẽ làm gì?

1. **Tạo Email Channel**: Tạo channel email với cấu hình Gmail SMTP
2. **Tạo Workflow**: Tạo workflow với:
   - Trigger node (schedule)
   - Action node (send_email) với nội dung: "Xin chào Trọng, bây giờ là {{currentTime}}"
3. **Tạo Schedule Trigger**: Tạo trigger chạy mỗi phút (cron: `0 * * * * *`)

## Kết quả

Sau khi chạy script thành công, bạn sẽ nhận được:
- Channel ID
- Workflow ID
- Trigger ID

Workflow sẽ tự động gửi email mỗi phút đến địa chỉ email bạn chỉ định.

## Kiểm tra executions

Bạn có thể kiểm tra các execution đã chạy:

```bash
curl "http://localhost:8080/api/v1/executions?workflow_id=<WORKFLOW_ID>"
```

Hoặc mở trong browser:
```
http://localhost:8080/api/v1/executions?workflow_id=<WORKFLOW_ID>
```

## Troubleshooting

### Lỗi: Cannot connect to API

- Kiểm tra backend đang chạy: `curl http://localhost:8080/api/v1/actuator/health`
- Kiểm tra `API_BASE_URL` có đúng không

### Lỗi: Failed to create channel

- Kiểm tra Gmail App Password có đúng không
- Đảm bảo đã bật 2-Step Verification
- Kiểm tra Gmail username có đúng không

### Email không được gửi

- Kiểm tra executions có chạy không
- Kiểm tra logs của backend
- Kiểm tra email có trong spam folder không
- Kiểm tra Gmail App Password còn hợp lệ không

### Workflow không chạy mỗi phút

- Kiểm tra trigger status: `curl "http://localhost:8080/api/v1/workflows/<WORKFLOW_ID>/triggers"`
- Kiểm tra workflow status phải là "active"
- Kiểm tra logs của backend để xem có lỗi gì không

## Dừng workflow

Để dừng workflow, bạn có thể:

1. **Pause workflow**:
```bash
curl -X PUT "http://localhost:8080/api/v1/workflows/<WORKFLOW_ID>" \
  -H "Content-Type: application/json" \
  -d '{"status": "paused"}'
```

2. **Delete trigger**:
```bash
curl -X DELETE "http://localhost:8080/api/v1/triggers/<TRIGGER_ID>"
```

3. **Delete workflow**:
```bash
curl -X DELETE "http://localhost:8080/api/v1/workflows/<WORKFLOW_ID>"
```

## Lưu ý

- Workflow sẽ gửi email **mỗi phút**, hãy đảm bảo bạn muốn điều này
- Gmail có giới hạn số lượng email gửi mỗi ngày (khoảng 500 emails/ngày cho free account)
- Nếu bạn muốn thay đổi tần suất, có thể cập nhật cron expression trong trigger

## Thay đổi tần suất gửi email

Để thay đổi tần suất, bạn có thể cập nhật cron expression:

- **Mỗi phút**: `0 * * * * *`
- **Mỗi 5 phút**: `0 */5 * * * *`
- **Mỗi giờ**: `0 0 * * * *`
- **Mỗi ngày lúc 9:00 AM**: `0 0 9 * * *`

Cập nhật trigger:
```bash
curl -X PUT "http://localhost:8080/api/v1/triggers/<TRIGGER_ID>" \
  -H "Content-Type: application/json" \
  -d '{
    "config": {
      "cronExpression": "0 */5 * * * *",
      "timezone": "Asia/Ho_Chi_Minh"
    }
  }'
```

