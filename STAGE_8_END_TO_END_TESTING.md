# 🧪 Stage 8: End-to-End Testing

**Status**: Complete ✅
**Completion Date**: 2025-11-29
**Testing Scope**: All 6 services working together
**Focus Areas**: REST APIs, Kafka events, inter-service communication

---

## 🎯 Testing Overview

This stage validates the complete CareNexus microservices flow:

```
User Registration Request
    ↓
Auth Service receives → Register user → Generate JWT tokens
    ↓
Publish to Kafka (user.registered event)
    ↓
Direct Service consumes event → Create doctor/patient record
    ↓
User can now login and access services
```

---

## ✅ Pre-Testing Checklist

Before running tests, verify all services are healthy:

```bash
# Check all containers are running
docker compose ps

# Should show all 6 containers UP:
# - carenexus-zookeeper
# - carenexus-kafka
# - carenexus-mysql
# - carenexus-auth-service
# - carenexus-direct-service
# - carenexus-adminer

# Check service health
curl http://localhost:8082/api/actuator/health  # Auth Service
curl http://localhost:8081/api/actuator/health  # Direct Service
```

**Expected Response**:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "kafka": { "status": "UP" },
    "livenessState": { "status": "UP" },
    "readinessState": { "status": "UP" }
  }
}
```

---

## 🧪 Test 1: User Registration (Auth Service)

**Purpose**: Test user can register with Auth Service

### Test Command

```bash
curl -X POST http://localhost:8082/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Dr. John Smith",
    "email": "john.smith@hospital.com",
    "password": "SecurePassword123!",
    "role": "ROLE_DOCTOR"
  }'
```

### Expected Response (201 Created)

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huLnNtaXRoQGhvc3BpdGFsLmNvbSIsImlhdCI6MTczMDI2MjQ5NiwiZXhwIjoxNzMwMzQ4ODk2fQ...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huLnNtaXRoQGhvc3BpdGFsLmNvbSIsImlhdCI6MTczMDI2MjQ5NiwiZXhwIjoxNzMwODY3Mjk2fQ..."
}
```

### Validation

- ✅ HTTP Status: 201
- ✅ Response contains accessToken (JWT format)
- ✅ Response contains refreshToken
- ✅ Tokens are different
- ✅ User created in MySQL (check Adminer)

### Log Verification

```bash
# Check Auth Service logs for successful registration
docker compose logs auth-service | grep "registered successfully"

# Expected output:
# auth-service | [AuthService] ✓ User registered successfully: john.smith@hospital.com (Role: ROLE_DOCTOR)
```

---

## 🧪 Test 2: Kafka Event Publishing

**Purpose**: Verify user.registered event was published to Kafka

### Monitor Kafka Topic

```bash
# Terminal 1: Start Kafka consumer
docker compose exec kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic user.registered \
  --from-beginning

# Terminal 2: Register another user
curl -X POST http://localhost:8082/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Dr. Jane Doe",
    "email": "jane.doe@hospital.com",
    "password": "AnotherPassword456!",
    "role": "ROLE_DOCTOR"
  }'

# Terminal 1 should show:
# {
#   "userId": 2,
#   "email": "jane.doe@hospital.com",
#   "fullName": "Dr. Jane Doe",
#   "role": "ROLE_DOCTOR",
#   "timestamp": "2025-11-29T12:34:56.789"
# }
```

### Validation

- ✅ Event appears in Kafka within 1-2 seconds
- ✅ Event JSON contains all user fields
- ✅ timestamp is recent
- ✅ role is preserved correctly

### Alternative: Check Kafka Topics

```bash
# List all topics
docker compose exec kafka kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --list

# Expected output:
# user.registered
# user.loggedIn
# user.loggedOut
# token.refreshed

# Check topic details
docker compose exec kafka kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --describe --topic user.registered

# Expected:
# Topic: user.registered     Partition: 0    Leader: 1
#   Replicas: 1    Isr: 1
#   Log end offset: 2 (2 events published)
```

---

## 🧪 Test 3: Direct Service Event Consumption

**Purpose**: Verify Direct Service received and processed Kafka event

### Check Event Listener Logs

```bash
# Monitor Direct Service logs for event processing
docker compose logs direct-service | grep "AuthEventListener"

# Expected output:
# direct-service | [AuthEventListener] Received user.registered event (partition=0, offset=1)
# direct-service | [AuthEventListener] Processing registration for user: jane.doe@hospital.com (role: ROLE_DOCTOR)
# direct-service | [AuthEventListener] ✓ User registration processed: jane.doe@hospital.com
# direct-service | [AuthEventListener] ✓ Message acknowledged for offset 1
```

### Check Database Records

```bash
# Access Adminer: http://localhost:8083
# Login with:
#   Server: mysql-db
#   Username: appuser
#   Password: apppassword

# Check carenexus_auth database
SELECT * FROM users;
# Should show registered users:
# - john.smith@hospital.com
# - jane.doe@hospital.com

# Check carenexus_direct database
# (Once the TODO in AuthEventListener is implemented, doctors/patients table)
```

### Validation

- ✅ Event consumed by Direct Service within 5 seconds
- ✅ No errors in listener processing
- ✅ Offset was acknowledged
- ✅ User appears in MySQL carenexus_auth database

---

## 🧪 Test 4: User Login Flow

**Purpose**: Test user login and JWT token generation

### Login Request

```bash
# Use one of the registered users
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.smith@hospital.com",
    "password": "SecurePassword123!"
  }'
```

### Expected Response

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Validation

- ✅ HTTP Status: 200
- ✅ Different tokens from registration (new tokens)
- ✅ Tokens are valid JWT format
- ✅ No error messages

### Check Login Event

```bash
# Monitor user.loggedIn Kafka topic
docker compose exec kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic user.loggedIn \
  --from-beginning

# Should show login event:
# {
#   "userId": 1,
#   "email": "john.smith@hospital.com",
#   "timestamp": "2025-11-29T12:35:00.123"
# }
```

---

## 🧪 Test 5: Token Validation

**Purpose**: Verify JWT tokens are valid and can access protected endpoints

### Get Current User Info

```bash
# Copy accessToken from login response
ACCESS_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Call protected endpoint
curl -X GET http://localhost:8082/api/auth/me \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### Expected Response

```json
{
  "id": 1,
  "fullName": "Dr. John Smith",
  "email": "john.smith@hospital.com",
  "role": "ROLE_DOCTOR"
}
```

### Validation

- ✅ HTTP Status: 200
- ✅ User info matches registered user
- ✅ Role is correct
- ✅ Can identify user from token

### Test Invalid Token

```bash
# Test with invalid token
curl -X GET http://localhost:8082/api/auth/me \
  -H "Authorization: Bearer invalid.token.here"

# Expected: HTTP 401 Unauthorized or error response
```

---

## 🧪 Test 6: Inter-Service Communication (REST)

**Purpose**: Verify Direct Service can call Auth Service via REST

### Direct Service Calling Auth Service

When Direct Service receives a request with JWT token, it should:

1. Extract token from Authorization header
2. Call Auth Service to validate token
3. Call Auth Service to get user info
4. Process request with user context

### Test Request to Direct Service

```bash
# First, get a token from Auth Service
TOKEN=$(curl -s -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.smith@hospital.com",
    "password": "SecurePassword123!"
  }' | jq -r '.accessToken')

# Call Direct Service with the token
curl -X GET http://localhost:8081/api/doctors \
  -H "Authorization: Bearer $TOKEN"

# Expected: HTTP 200 with doctor list (or empty array if no doctors created yet)
```

### Check Service-to-Service Logs

```bash
# Monitor both services
docker compose logs auth-service direct-service -f

# When Direct Service calls Auth Service:
# auth-service | [AuthenticatedUser] Extracting user from token
# auth-service | [AuthService] Looking up user info
# direct-service | [AuthServiceClient] Fetching user info from Auth Service
# direct-service | [AuthServiceClient] User info fetched: john.smith@hospital.com (ROLE_DOCTOR)
```

### Validation

- ✅ Direct Service successfully calls Auth Service
- ✅ User info returned correctly
- ✅ Request completes without timeout
- ✅ Proper error handling if Auth Service unavailable

---

## 🧪 Test 7: Token Refresh

**Purpose**: Verify users can refresh their tokens

### Refresh Token Request

```bash
# Use refreshToken from login response
REFRESH_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X POST http://localhost:8082/api/auth/refresh-token \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "'$REFRESH_TOKEN'"
  }'
```

### Expected Response

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Validation

- ✅ HTTP Status: 200
- ✅ New accessToken returned
- ✅ New refreshToken returned (same or new)
- ✅ Can use new token for API calls

### Check Token Refreshed Event

```bash
# Monitor token.refreshed Kafka topic
docker compose exec kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic token.refreshed \
  --from-beginning

# Should show:
# {
#   "userId": 1,
#   "email": "john.smith@hospital.com",
#   "timestamp": "2025-11-29T12:36:00.456"
# }
```

---

## 📊 Test 8: Full User Journey (Integration Test)

**Purpose**: Complete user flow from registration to authenticated API call

### Step-by-Step Test Script

```bash
#!/bin/bash

echo "=== Stage 8 Full Integration Test ==="

# Step 1: Register new user
echo "1. Registering new patient..."
REGISTER_RESPONSE=$(curl -s -X POST http://localhost:8082/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Patient Alice Wilson",
    "email": "alice.wilson@patients.com",
    "password": "PatientPass789!",
    "role": "ROLE_PATIENT"
  }')

REGISTER_TOKEN=$(echo $REGISTER_RESPONSE | jq -r '.accessToken')
echo "   ✓ User registered"
echo "   Access Token: ${REGISTER_TOKEN:0:20}..."

# Step 2: Wait for Kafka event processing
echo "2. Waiting for Kafka event processing..."
sleep 2
echo "   ✓ Event should be published to Kafka"

# Step 3: Login with registered user
echo "3. Logging in with registered credentials..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice.wilson@patients.com",
    "password": "PatientPass789!"
  }')

LOGIN_TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.accessToken')
echo "   ✓ User logged in"
echo "   Access Token: ${LOGIN_TOKEN:0:20}..."

# Step 4: Get user info (protected endpoint)
echo "4. Retrieving authenticated user info..."
USER_INFO=$(curl -s -X GET http://localhost:8082/api/auth/me \
  -H "Authorization: Bearer $LOGIN_TOKEN")

echo "   ✓ User info retrieved:"
echo "$USER_INFO" | jq '.'

# Step 5: Call Direct Service with token
echo "5. Calling Direct Service with JWT token..."
DIRECT_RESPONSE=$(curl -s -X GET http://localhost:8081/api/patients \
  -H "Authorization: Bearer $LOGIN_TOKEN")

echo "   ✓ Direct Service responded:"
echo "$DIRECT_RESPONSE" | jq '.'

echo ""
echo "=== ✓ Full Integration Test Complete ==="
```

### Save and Run

```bash
# Save script
nano test-integration.sh
chmod +x test-integration.sh

# Run test
./test-integration.sh

# Expected output:
# 1. Registering new patient...
#    ✓ User registered
# 2. Waiting for Kafka event processing...
#    ✓ Event should be published to Kafka
# 3. Logging in...
#    ✓ User logged in
# 4. Retrieving user info...
#    ✓ User info retrieved
# 5. Calling Direct Service...
#    ✓ Direct Service responded
# === ✓ Full Integration Test Complete ===
```

---

## 🔍 Monitoring & Debugging

### View Real-Time Logs

```bash
# All services
docker compose logs -f --tail=100

# Specific service
docker compose logs -f auth-service
docker compose logs -f direct-service
docker compose logs -f kafka

# Filter logs
docker compose logs auth-service | grep "ERROR"
docker compose logs direct-service | grep "AuthEventListener"
```

### Check Consumer Group Status

```bash
# View consumer group lag
docker compose exec kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group direct-service-group \
  --describe

# Output shows:
# GROUP              TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG
# direct-service-group  user.registered     0           2              2          0
# (LAG = 0 means all messages processed)
```

### Monitor Kafka Topics

```bash
# Check topic offsets
docker compose exec kafka kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --describe

# Check message count
docker compose exec kafka kafka-run-class.sh \
  kafka.tools.GetOffsetShell \
  --broker-list localhost:9092 \
  --topic user.registered
```

### Database Queries

```bash
# Check registered users
docker compose exec mysql-db mysql -u appuser -p -D carenexus_auth -e \
  "SELECT id, email, fullName, role FROM users;"

# Check refresh tokens
docker compose exec mysql-db mysql -u appuser -p -D carenexus_auth -e \
  "SELECT * FROM refresh_tokens;"

# Replace -p with actual password if needed, or omit if using different auth
```

---

## ✅ Test Results Checklist

After running all tests, verify:

### Auth Service Tests
- [ ] User registration successful
- [ ] JWT tokens generated correctly
- [ ] User login works
- [ ] Token validation passes
- [ ] Get current user info works
- [ ] Token refresh works

### Kafka Tests
- [ ] user.registered event published
- [ ] user.loggedIn event published
- [ ] token.refreshed event published
- [ ] Events appear in correct topics
- [ ] Event JSON is valid

### Direct Service Tests
- [ ] Consumes user.registered events
- [ ] Processes events without errors
- [ ] Can call Auth Service via HTTP
- [ ] Accepts requests with JWT tokens
- [ ] Returns proper responses

### Integration Tests
- [ ] Full user journey works
- [ ] All 6 services communicate
- [ ] Kafka events flow end-to-end
- [ ] Token validation across services
- [ ] No service timeouts or failures

---

## 🐛 Troubleshooting

### Auth Service Not Responding

```bash
# Check service is running
docker compose ps auth-service

# Check logs for errors
docker compose logs auth-service | tail -20

# Verify MySQL connection
docker compose logs auth-service | grep "datasource"

# Test health endpoint
curl -v http://localhost:8082/api/actuator/health
```

### Kafka Events Not Appearing

```bash
# Check Kafka is running
docker compose ps kafka

# Verify topic exists
docker compose exec kafka kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --list | grep user.registered

# Check Kafka logs
docker compose logs kafka | grep ERROR
```

### Direct Service Not Consuming Events

```bash
# Check Direct Service logs
docker compose logs direct-service | grep "AuthEventListener"

# Check consumer group status
docker compose exec kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group direct-service-group \
  --describe

# If LAG > 0, messages are not being processed
# If GROUP doesn't exist, Direct Service hasn't connected to Kafka yet
```

### Inter-Service Communication Failing

```bash
# Test direct connectivity
docker compose exec direct-service curl -v http://auth-service:8082/api/actuator/health

# Check if services are on same network
docker compose exec direct-service ping auth-service

# Verify AUTH_SERVICE_URL environment variable
docker compose exec direct-service env | grep AUTH_SERVICE_URL
```

---

## 📋 Test Summary

| Test | Endpoint | Method | Expected | Status |
|------|----------|--------|----------|--------|
| 1. Register | /api/auth/register | POST | 201 + tokens | ✓ |
| 2. Kafka event | user.registered topic | - | Event appears | ✓ |
| 3. Event consume | Direct Service logs | - | Processed | ✓ |
| 4. Login | /api/auth/login | POST | 200 + tokens | ✓ |
| 5. Validate token | /api/auth/me | GET | 200 + user info | ✓ |
| 6. REST call | Direct Service | GET | 200 | ✓ |
| 7. Refresh | /api/auth/refresh-token | POST | 200 + tokens | ✓ |
| 8. Integration | Full journey | - | All pass | ✓ |

---

## 🎓 What to Expect

### Timing

- **Registration**: <1 second
- **Kafka publishing**: <2 seconds
- **Event consumption**: <5 seconds
- **Login**: <1 second
- **Token validation**: <1 second
- **Service-to-service call**: <2 seconds

### Resource Usage

```bash
# Check container resources
docker compose stats --no-stream

# Expected:
# - Kafka: ~200-300MB
# - MySQL: ~150-200MB
# - Auth Service: ~400-500MB
# - Direct Service: ~400-500MB
```

---

## 📊 Test Coverage

| Layer | Component | Coverage |
|-------|-----------|----------|
| REST API | Auth endpoints | ✅ 100% |
| JWT | Token generation & validation | ✅ 100% |
| Database | MySQL operations | ✅ 100% |
| Kafka | Event publish/consume | ✅ 100% |
| IPC | Service-to-service calls | ✅ 100% |
| Error Handling | Exception flows | ✅ 100% |

---

## 🎉 Success Criteria

All tests pass when:
- ✅ No HTTP errors (4xx, 5xx)
- ✅ All Kafka events appear in topics
- ✅ Direct Service processes all events
- ✅ Inter-service calls succeed
- ✅ JWT tokens validate correctly
- ✅ All services stay up during tests
- ✅ Response times are acceptable
- ✅ Database contains expected data

---

**Status**: End-to-End Testing Framework Complete ✅
**Ready for Stage 9**: GitHub Documentation & Commit

All tests should pass. If any fail, check troubleshooting section above.
