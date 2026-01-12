# Backend Scripts

Scripts để quản lý backend application trong môi trường local development.

## Scripts

### `start-local.sh`

Khởi động backend application và tất cả các services cần thiết cho local development.

**Chức năng:**
1. Kiểm tra Docker và Docker Compose
2. Khởi động infrastructure services (PostgreSQL, Kafka, RabbitMQ) trong Docker từ `docker-compose.yml`
3. Đợi các services sẵn sàng
4. Khởi động Spring Boot application **trên host** với hot reload (sử dụng `mvn spring-boot:run`)

**Cách sử dụng:**
```bash
cd backend
./scripts/start-local.sh
```

**Yêu cầu:**
- Docker và Docker Compose đã được cài đặt và đang chạy
- Java 17+ (bắt buộc - backend chạy trên host)
- Maven 3.6+ (bắt buộc - để chạy với hot reload)

**Hot Reload:**
- Backend chạy với Spring Boot DevTools, tự động reload khi code thay đổi
- Không cần restart app khi sửa code Java
- Chỉ cần save file, DevTools sẽ tự động reload

**Services được khởi động:**
- PostgreSQL: `localhost:5432` (Docker)
- Kafka: `localhost:9092` (Docker)
- RabbitMQ: `localhost:5672` (Docker) - Management UI: `http://localhost:15672`
- Backend API: `http://localhost:8080/api/v1` (Host - Hot Reload Enabled)
- Health Check: `http://localhost:8080/api/v1/actuator/health`
- Swagger UI: `http://localhost:8080/api/v1/swagger-ui.html`

**Logs:**
- Backend logs: `backend/app.log` (xem real-time: `tail -f backend/app.log`)
- Process ID: `backend/.backend.pid`
- Docker logs: `docker compose logs -f [service-name]`

**Lưu ý:**
- Infrastructure services (PostgreSQL, Kafka, RabbitMQ) chạy trong Docker
- Backend application chạy trên host với hot reload
- Code changes sẽ tự động reload (không cần restart)

### `restart-backend.sh`

Chỉ restart backend application (không restart Docker services). Script này hữu ích khi:
- Backend bị crash hoặc cần restart
- Fix permission issues
- Clean build directory và restart

**Chức năng:**
1. Dừng backend application đang chạy
2. Fix permissions trên target directory (nếu có vấn đề)
3. Clean target directory để tránh permission issues
4. Khởi động lại backend với hot reload
5. Strip ANSI color codes từ log để dễ đọc hơn

**Cách sử dụng:**
```bash
cd backend
./scripts/restart-backend.sh
```

**Lưu ý:**
- Script chỉ restart backend, không restart Docker services
- Script tự động fix permission issues
- Log sẽ không có ANSI color codes (dễ đọc hơn)
- Yêu cầu Docker services đã chạy (PostgreSQL, Kafka, RabbitMQ)

### `stop-local.sh`

Dừng backend application và tùy chọn dừng infrastructure services.

**Chức năng:**
1. Dừng backend application đang chạy trên host (tìm process từ PID file hoặc port 8080)
2. Dừng Maven processes nếu có
3. Hỏi người dùng có muốn dừng infrastructure services không

**Cách sử dụng:**
```bash
cd backend
./scripts/stop-local.sh
```

**Lưu ý:**
- Script sẽ tự động dừng backend application (Maven + Java process)
- Script sẽ hỏi bạn có muốn dừng infrastructure services (PostgreSQL, Kafka, RabbitMQ) không
- Nếu chọn không, các services sẽ tiếp tục chạy trong Docker

## Troubleshooting

### Port đã được sử dụng

Nếu gặp lỗi port đã được sử dụng:
```bash
# Kiểm tra process đang sử dụng port
lsof -i :8080

# Dừng process
kill <PID>
```

### Backend không khởi động được

1. Kiểm tra logs: `cat backend/app.log`
2. Kiểm tra các services đã sẵn sàng chưa:
   ```bash
   docker ps
   docker logs notification-platform-db
   docker logs notification-platform-kafka
   docker logs notification-platform-rabbitmq
   ```
3. Kiểm tra database connection:
   ```bash
   docker exec -it notification-platform-db psql -U postgres -d notification_platform
   ```

### Build thất bại

Nếu build thất bại:
1. Kiểm tra Java version: `java -version` (cần Java 17+)
2. Kiểm tra Maven: `mvn -version`
3. Thử build thủ công:
   ```bash
   cd backend
   mvn clean compile
   ```

**Lưu ý:** Script sử dụng `mvn spring-boot:run` để chạy với hot reload, không cần build JAR.

### Services không khởi động

1. Kiểm tra Docker đang chạy: `docker ps`
2. Kiểm tra logs của services:
   ```bash
   cd backend
   docker compose logs
   ```
3. Restart services:
   ```bash
   cd backend
   docker compose down
   docker compose up -d
   ```

## Environment Variables

Script sẽ tự động load environment variables từ `.env` file ở project root (nếu có).

Các biến môi trường quan trọng:
- `DB_USERNAME`: Database username (default: postgres)
- `DB_PASSWORD`: Database password (default: postgres)
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka bootstrap servers (default: localhost:9092)
- `RABBITMQ_HOST`: RabbitMQ host (default: localhost)
- `RABBITMQ_PORT`: RabbitMQ port (default: 5672)
- `RABBITMQ_USERNAME`: RabbitMQ username (default: guest)
- `RABBITMQ_PASSWORD`: RabbitMQ password (default: guest)
- `SERVER_PORT`: Backend server port (default: 8080)

Xem `application.yml` để biết thêm chi tiết về cấu hình.

## Hot Reload

Backend sử dụng Spring Boot DevTools để tự động reload khi code thay đổi:

- **Tự động reload**: Khi bạn save file Java, DevTools sẽ tự động reload application
- **Không cần restart**: Không cần dừng và khởi động lại app
- **Xem reload status**: Theo dõi console output để thấy khi nào reload xảy ra
- **Logs**: Xem `backend/app.log` để theo dõi reload messages

**Lưu ý:**
- Hot reload hoạt động tốt nhất với các thay đổi trong code Java
- Một số thay đổi (như thay đổi cấu hình application.yml) có thể cần restart
- Thay đổi trong database schema (JPA entities) sẽ được tự động cập nhật khi restart với `ddl-auto=update`

