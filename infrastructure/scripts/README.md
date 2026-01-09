# Local Development Scripts

Scripts để khởi động và dừng ứng dụng trong môi trường local development.

## Scripts

### `start-local.sh`

Khởi động tất cả services cần thiết cho local development:

1. **Infrastructure Services** (Docker Compose):
   - PostgreSQL (port 5432)
   - Kafka + Zookeeper (port 9092)
   - RabbitMQ (ports 5672, 15672)

2. **Backend Application**:
   - Build JAR file (nếu cần)
   - Start Spring Boot application (port 8080)

3. **Frontend Application**:
   - Install dependencies (nếu cần)
   - Start Vite dev server (port 5173)

### `stop-local.sh`

Dừng tất cả services đang chạy:
- Frontend application
- Backend application
- Infrastructure services (Docker Compose)

## Sử dụng

### Khởi động ứng dụng

```bash
./infrastructure/scripts/start-local.sh
```

Hoặc từ root directory:

```bash
bash infrastructure/scripts/start-local.sh
```

### Dừng ứng dụng

```bash
./infrastructure/scripts/stop-local.sh
```

Hoặc từ root directory:

```bash
bash infrastructure/scripts/stop-local.sh
```

## Yêu cầu

- Docker và Docker Compose đã được cài đặt và đang chạy
- Java 17+ (cho backend)
- Maven (cho backend build)
- Node.js 20+ và npm (cho frontend)

## Cấu hình

Script sẽ tự động:
- Tạo file `.env` từ `.env.example` nếu chưa có
- Kiểm tra và chờ các services sẵn sàng
- Build backend JAR nếu cần
- Install frontend dependencies nếu cần

## Logs

- Backend logs: `backend/app.log`
- Frontend logs: `frontend/app.log`
- Docker services logs: `docker compose logs` (trong thư mục `backend/`)

## Troubleshooting

### Port đã được sử dụng

Nếu gặp lỗi port đã được sử dụng:
1. Chạy `stop-local.sh` để dừng tất cả services
2. Hoặc kill process thủ công:
   ```bash
   # Backend (port 8080)
   kill $(lsof -t -i:8080)
   
   # Frontend (port 5173)
   kill $(lsof -t -i:5173)
   ```

### Services không khởi động

1. Kiểm tra Docker đang chạy: `docker info`
2. Kiểm tra logs: `docker compose logs` (trong `backend/`)
3. Kiểm tra backend logs: `tail -f backend/app.log`
4. Kiểm tra frontend logs: `tail -f frontend/app.log`

### Backend không build được

1. Kiểm tra Java version: `java -version` (cần Java 17+)
2. Kiểm tra Maven: `mvn -version`
3. Build thủ công: `cd backend && mvn clean package -DskipTests`

### Frontend không chạy được

1. Kiểm tra Node.js version: `node -v` (cần Node 20+)
2. Xóa node_modules và cài lại: `cd frontend && rm -rf node_modules && npm install`

## URLs sau khi khởi động

- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080/api/v1
- **Backend Health**: http://localhost:8080/api/v1/actuator/health
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)

