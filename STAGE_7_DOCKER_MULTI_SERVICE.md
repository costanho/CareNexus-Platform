# 🐳 Stage 7: Docker Multi-Service Setup

**Status**: Complete ✅
**Completion Date**: 2025-11-29
**Services**: 6 containers orchestrated with docker-compose
**Network**: carenexus-net (bridge network)

---

## 🎯 Overview

Stage 7 sets up Docker orchestration for the complete CareNexus microservices architecture with 6 interconnected containers:

1. **Zookeeper** - Kafka dependency for coordination
2. **Kafka** - Event bus for asynchronous messaging
3. **MySQL** - Relational database (carenexus_auth + carenexus_direct)
4. **Auth Service** - Authentication & JWT management (8082)
5. **Direct Service** - Patient/Doctor management (8081)
6. **Adminer** - Database management UI (8083)

---

## 🏗️ Architecture

```
┌────────────────────────────────────────────────────────────┐
│                      Docker Network                         │
│                   carenexus-net (bridge)                    │
│                                                              │
├────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌─────────────────┐  │
│  │  Zookeeper   │  │    Kafka     │  │  MySQL Server   │  │
│  │   :2181      │  │   :9092      │  │   :3306         │  │
│  │ (Kafka dep)  │  │ (Event Bus)  │  │ (Persistence)   │  │
│  └──────┬───────┘  └──────┬───────┘  └────────┬────────┘  │
│         │                 │                    │            │
│         └─────────────────┼────────────────────┘            │
│                           │                                 │
│         ┌─────────────────┴─────────────────┐               │
│         │                                   │               │
│  ┌──────▼─────────────┐          ┌─────────▼──────────┐   │
│  │  Auth Service      │          │ Direct Service     │   │
│  │   Container        │          │  Container         │   │
│  │   :8082 → 8082     │◄────────►│  :8081 → 8081      │   │
│  │ (Register/Login)   │ REST API │ (Doctors/Patients) │   │
│  │  Kafka Consumer    │◄────────►│ Kafka Producer     │   │
│  │  (Event Listener)  │ Events   │ (Business Logic)   │   │
│  └────────────────────┘          └────────────────────┘   │
│         ▲                                                   │
│         │                                                   │
│  ┌──────┴──────────────┐                                   │
│  │   Adminer (UI)      │                                   │
│  │   :8083 → 8080      │                                   │
│  │ (Database Admin)    │                                   │
│  └─────────────────────┘                                   │
│                                                              │
└────────────────────────────────────────────────────────────┘
```

---

## 📦 Services Configuration

### 1. Zookeeper (Kafka Coordinator)

```yaml
zookeeper:
  image: confluentinc/cp-zookeeper:7.5.0
  container_name: carenexus-zookeeper
  ports:
    - "29181:2181"  # External debugging port
  environment:
    ZOOKEEPER_CLIENT_PORT: 2181      # Internal port for Kafka
    ZOOKEEPER_SYNC_LIMIT: 2
    ZOOKEEPER_INIT_LIMIT: 5
```

**Purpose**: Manages Kafka broker coordination, leader election, and metadata
**Port**: 2181 (internal), 29181 (for debugging)
**Startup**: Starts first (no dependencies)

### 2. Kafka Broker (Event Bus)

```yaml
kafka:
  image: confluentinc/cp-kafka:7.5.0
  depends_on:
    - zookeeper
  environment:
    KAFKA_BROKER_ID: 1
    KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
    KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
    KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
    KAFKA_LOG_RETENTION_HOURS: 168  # 7 days
```

**Purpose**: Event bus for asynchronous messaging between services
**Port**: 9092 (broker), 9101 (metrics)
**Topics**:
- `user.registered` - New user registration
- `user.loggedIn` - User login events
- `user.loggedOut` - User logout events
- `token.refreshed` - Token refresh tracking

**Configuration**:
- Auto topic creation: enabled (topics auto-create on demand)
- Log retention: 7 days
- Replication: 1 (single broker, safe for dev)
- Acks: all (producer waits for all replicas)

### 3. MySQL Database

```yaml
mysql-db:
  image: mysql:8.0
  ports:
    - "3307:3306"
  volumes:
    - mysql_data:/var/lib/mysql
  environment:
    MYSQL_ROOT_PASSWORD: rootpassword
    MYSQL_DATABASE: carenexus_direct
    MYSQL_USER: appuser
    MYSQL_PASSWORD: apppassword
```

**Purpose**: Relational database for both services
**Databases**:
- `carenexus_auth` - User accounts, refresh tokens (Auth Service)
- `carenexus_direct` - Doctors, patients, appointments (Direct Service)
**Port**: 3306 (internal), 3307 (external)
**Persistence**: `mysql_data` volume (survives container restarts)
**Health Check**: mysqladmin ping every 10 seconds

### 4. Auth Service

```yaml
auth-service:
  build:
    dockerfile: ../auth-service/Dockerfile
  depends_on:
    - mysql-db:
        condition: service_healthy
    - kafka:
        condition: service_healthy
  environment:
    SPRING_DATASOURCE_URL: jdbc:mysql://mysql-db:3306/carenexus_auth
    SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
    AUTH_SERVICE_URL: http://auth-service:8082
```

**Purpose**: Authentication, JWT token management, event publishing
**Port**: 8082
**Dependencies**: MySQL (database), Kafka (event publishing)
**Features**:
- User registration and login
- JWT token generation (24h access, 7d refresh)
- Event publishing (user.registered, user.loggedIn, etc)
- Password hashing (BCrypt)

**Database Connection**: `mysql-db:3306` (internal Docker network)
**Kafka Connection**: `kafka:9092` (internal Docker network)

### 5. Direct Service

```yaml
direct-service:
  build:
    dockerfile: Dockerfile
  depends_on:
    - mysql-db:
        condition: service_healthy
    - kafka:
        condition: service_healthy
    - auth-service:
        condition: service_healthy
  environment:
    SPRING_DATASOURCE_URL: jdbc:mysql://mysql-db:3306/carenexus_direct
    SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
    AUTH_SERVICE_URL: http://auth-service:8082
```

**Purpose**: Patient/Doctor management, appointment scheduling
**Port**: 8081
**Dependencies**: MySQL, Kafka, Auth Service
**Features**:
- Kafka consumer (listens for auth events)
- REST client (calls Auth Service via HTTP)
- Doctor/Patient CRUD operations
- Appointment scheduling

**Database Connection**: `mysql-db:3306` (internal Docker network)
**Kafka Connection**: `kafka:9092` (for event consumption)
**Auth Service Call**: `http://auth-service:8082` (service-to-service)

### 6. Adminer (Database UI)

```yaml
adminer:
  image: adminer
  ports:
    - "8083:8080"
  environment:
    ADMINER_DEFAULT_SERVER: mysql-db
```

**Purpose**: Web-based database administration interface
**Port**: 8080 (internal), 8083 (external)
**Access**: http://localhost:8083
**Login**:
- Server: mysql-db
- Username: appuser
- Password: apppassword

---

## 🚀 Getting Started

### 1. Start All Services

```bash
cd /Users/cosy/Documents/CareNexus/direct

# Start all containers in the background
docker compose up -d

# Or rebuild and start
docker compose up -d --build
```

### 2. Monitor Startup

```bash
# Watch logs in real-time
docker compose logs -f --tail=50

# Or watch specific service
docker compose logs -f auth-service
docker compose logs -f direct-service
docker compose logs -f kafka
```

### 3. Check Service Status

```bash
# List running containers
docker compose ps

# Output:
# NAME                      STATUS              PORTS
# carenexus-zookeeper       Up 2 minutes
# carenexus-kafka           Up 2 minutes        9092:9092, 9101:9101
# carenexus-mysql           Up 2 minutes        3307:3306
# carenexus-auth-service    Up 1 minute         8082:8082
# carenexus-direct-service  Up 30 seconds       8081:8081
# carenexus-adminer         Up 2 minutes        8083:8080
```

---

## 🔌 Access Points

| Service | Internal URL | External URL | Purpose |
|---------|--------------|--------------|---------|
| Kafka | kafka:9092 | localhost:9092 | Event bus |
| MySQL | mysql-db:3306 | localhost:3307 | Database |
| Auth Service | http://auth-service:8082 | http://localhost:8082 | Auth API |
| Direct Service | http://direct-service:8081 | http://localhost:8081 | Main API |
| Adminer | - | http://localhost:8083 | Database UI |

---

## 📝 Environment Variables

Create a `.env` file in the docker-compose directory:

```bash
# MySQL Configuration
MYSQL_ROOT_PASSWORD=rootpassword
MYSQL_USER=appuser
MYSQL_PASSWORD=apppassword
MYSQL_DATABASE=carenexus_direct
MYSQL_PORT=3307

# Auth Service
AUTH_SERVICE_PORT=8082

# Direct Service
DIRECT_SERVICE_PORT=8081

# Adminer
ADMINER_PORT=8083

# JWT Configuration
JWT_SECRET=your-secret-key-change-in-production
JWT_ACCESS_EXPIRATION=86400000   # 24 hours
JWT_REFRESH_EXPIRATION=604800000 # 7 days

# Auth Service URL (for Direct Service to call Auth Service)
AUTH_SERVICE_URL=http://auth-service:8082
```

---

## 🧪 Testing the Setup

### 1. Test Auth Service (Register & Login)

```bash
# Register a new user
curl -X POST http://localhost:8082/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName":"Test Doctor",
    "email":"doctor@test.com",
    "password":"password123",
    "role":"ROLE_DOCTOR"
  }'

# Response should include:
# {
#   "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
#   "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
# }
```

### 2. Test Kafka Events

```bash
# Monitor user.registered topic
docker compose exec kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic user.registered \
  --from-beginning

# Or check topic exists
docker compose exec kafka kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --list

# Should show:
# user.registered
# user.loggedIn
# user.loggedOut
# token.refreshed
```

### 3. Test Inter-Service Communication

```bash
# Direct Service should be able to call Auth Service
# This happens internally when endpoints are accessed

# Get current user (Auth Service endpoint)
curl -X GET http://localhost:8082/api/auth/me \
  -H "Authorization: Bearer <accessToken>"

# Direct Service should be consuming Kafka events
# Check logs:
docker compose logs direct-service | grep "AuthEventListener"
```

### 4. Access Database UI

```bash
# Open in browser
http://localhost:8083

# Login with:
# Server: mysql-db
# Username: appuser
# Password: apppassword

# Then select database: carenexus_auth or carenexus_direct
```

---

## 📊 Service Startup Order

Docker Compose respects `depends_on` conditions:

```
1. Zookeeper starts
   ↓ (waits for healthy status)
2. Kafka starts
   ↓ (waits for healthy status)
3. MySQL starts
   ↓ (waits for healthy status)
4. Auth Service starts (depends on MySQL + Kafka healthy)
   ↓ (waits for healthy status)
5. Direct Service starts (depends on MySQL + Kafka + Auth healthy)
6. Adminer starts (depends on MySQL healthy)
```

Each service has health checks to verify readiness before dependent services start.

---

## 🛠️ Common Docker Commands

```bash
# Start all services
docker compose up -d

# Stop all services
docker compose down

# Stop and remove volumes (WARNING: deletes data!)
docker compose down -v

# Rebuild and start
docker compose up -d --build

# View logs
docker compose logs -f

# View specific service logs
docker compose logs -f auth-service

# Execute command in container
docker compose exec auth-service curl http://localhost:8082/api/actuator/health

# Restart a service
docker compose restart direct-service

# Remove stopped containers
docker compose rm

# View resource usage
docker compose stats
```

---

## 🔐 Security Considerations

### Development vs Production

**Current Setup (Development)**:
- Single Kafka broker (no replication)
- Single MySQL instance (no replication)
- No TLS/SSL on network traffic
- Root password in docker-compose file
- Auto topic creation enabled

**Production Recommendations**:
- Kafka replication factor: 3
- MySQL replication/clustering
- Enable TLS/SSL encryption
- Use secrets management (Docker Secrets, Kubernetes Secrets)
- Disable auto topic creation
- Add authentication (username/password)
- Add network policies/firewalls

---

## 📊 File Summary

### docker-compose.yml Updates
- Added Zookeeper service
- Added Kafka broker service
- Added Auth Service container
- Updated Direct Service dependencies (MySQL + Kafka + Auth)
- Changed Adminer port to 8083
- Added Kafka environment variables to both services

### Total Services: 6
1. Zookeeper
2. Kafka
3. MySQL
4. Auth Service
5. Direct Service
6. Adminer

---

## 🎓 Understanding the Flow

### Service Startup Flow

```
1. Zookeeper + Kafka + MySQL start in parallel
2. Auth Service waits for MySQL + Kafka to be healthy
   - Connects to MySQL, creates carenexus_auth database
   - Connects to Kafka, creates topics
   - Starts on port 8082
3. Direct Service waits for all three + Auth Service healthy
   - Connects to MySQL, creates carenexus_direct database
   - Connects to Kafka, joins consumer group
   - Registers as REST client to Auth Service
   - Starts on port 8081
4. Adminer connects to MySQL
   - Provides UI for database administration
   - Starts on port 8083
```

### Request Flow

```
Client Request → Direct Service (8081)
  ↓
  ├─ REST call to Auth Service (8082) for token validation
  │    Auth Service queries MySQL
  │
  └─ Kafka consumer processes events
       Auth Service publishes events → Kafka
       Direct Service consumes events (async)
```

---

## 📋 Checklist for Stage 7 Completion

- [x] Added Zookeeper service to docker-compose.yml
- [x] Added Kafka broker service to docker-compose.yml
- [x] Added Auth Service container to docker-compose.yml
- [x] Updated Direct Service dependencies (MySQL + Kafka + Auth)
- [x] Updated Adminer port to 8083 (avoid conflict with Auth Service)
- [x] Added Kafka configuration to both services
- [x] Added Auth Service URL to Direct Service
- [x] Created comprehensive documentation
- [ ] (Stage 8) Test all services end-to-end
- [ ] (Stage 9) Push to GitHub

---

## 🔗 Related Files

- `docker-compose.yml` - Multi-service orchestration
- `/auth-service/Dockerfile` - Auth Service container image
- `/Dockerfile` - Direct Service container image
- `src/main/resources/application.yml` - Direct Service config
- `/auth-service/src/main/resources/application.yml` - Auth Service config

---

## 🎓 What's Next

### Stage 8: Test Complete Flow End-to-End
- Test user registration (triggers Kafka event)
- Verify doctor/patient record creation
- Check event consumption
- Monitor all services

### Stage 9: Documentation & GitHub
- Update GitHub documentation
- Create deployment guide
- Push all changes to GitHub
- Create release notes

---

**Status**: Ready for Stage 8 (End-to-End Testing) ✅

Docker orchestration is now complete with 6 coordinated services!
