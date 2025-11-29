# 🔌 Stage 5: Inter-Service Communication

**Status**: Complete ✅
**Completion Date**: 2025-11-29
**Duration**: Part of Phase 7 Microservice Extraction

---

## 🎯 Overview

Stage 5 establishes HTTP communication between the Direct Service (port 8081) and the Auth Service (port 8082). This allows the Direct Service to:

1. **Validate JWT tokens** with the Auth Service
2. **Fetch user information** from the Auth Service
3. **Handle service-to-service authentication** via JWT bearer tokens

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Direct Service (8081)                    │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Controller/Service Layer                                    │
│        ↓                                                     │
│  AuthServiceClient (HTTP calls)                             │
│        ↓                                                     │
│  RestTemplate (Spring Bean)                                 │
│        ↓                                                     │
│  HTTP Request over Network                                  │
│                                                              │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       │ HTTP GET/POST
                       │
┌──────────────────────▼──────────────────────────────────────┐
│                    Auth Service (8082)                      │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  AuthController                                             │
│    ├── POST /api/auth/register                              │
│    ├── POST /api/auth/login                                 │
│    ├── POST /api/auth/refresh-token                         │
│    ├── GET /api/auth/me                                     │
│    └── GET /api/auth/validate                               │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 📝 Components Created

### 1. RestTemplateConfig.java
**Location**: `src/main/java/com/carenexus/direct/config/RestTemplateConfig.java`

Creates a Spring Bean for HTTP communication with timeouts:
- **Connection Timeout**: 5 seconds
- **Read Timeout**: 10 seconds
- **Buffering**: Enabled for retry logic and logging

```java
@Bean
public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
    return restTemplateBuilder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(10))
            .requestFactory(this::clientHttpRequestFactory)
            .build();
}
```

### 2. AuthServiceClient.java
**Location**: `src/main/java/com/carenexus/direct/client/AuthServiceClient.java`

Encapsulates all HTTP communication with Auth Service:

#### Methods:

**getUserInfo(String jwtToken)**
```java
UserInfoResponse user = authServiceClient.getUserInfo(jwtToken);
// Returns: { id, fullName, email, role }
```

**validateToken(String jwtToken)**
```java
TokenValidationResponse validation = authServiceClient.validateToken(jwtToken);
if (validation.isValid()) {
    // Token is valid
}
```

### 3. Configuration in application.yml
**Locations**:
- Default: `src/main/resources/application.yml` (local dev)
- Docker: Same file, docker profile

```yaml
# Local development
auth-service:
  url: http://localhost:8082

# Docker deployment
auth-service:
  url: http://auth-service:8082
```

---

## 📡 Usage Examples

### Example 1: In a Service Class
```java
@Service
@RequiredArgsConstructor
public class DoctorService {

    private final AuthServiceClient authServiceClient;

    public DoctorDTO getDoctorInfo(String jwtToken) {
        // Verify user with Auth Service
        UserInfoResponse user = authServiceClient.getUserInfo(jwtToken);

        // Process based on user info
        if ("ROLE_DOCTOR".equals(user.getRole())) {
            return getDoctorById(user.getId());
        }
        throw new ForbiddenException("Only doctors can access this");
    }
}
```

### Example 2: In a Controller
```java
@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final AuthServiceClient authServiceClient;
    private final DoctorService doctorService;

    @GetMapping("/{id}")
    public ResponseEntity<DoctorDTO> getDoctor(
            @PathVariable Long id,
            @RequestHeader("Authorization") String bearerToken) {

        // Extract token (remove "Bearer " prefix)
        String token = bearerToken.substring(7);

        // Validate with Auth Service
        TokenValidationResponse validation = authServiceClient.validateToken(token);
        if (!validation.isValid()) {
            return ResponseEntity.status(401).build();
        }

        // Get doctor info
        DoctorDTO doctor = doctorService.getDoctorById(id);
        return ResponseEntity.ok(doctor);
    }
}
```

### Example 3: Error Handling
```java
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AuthServiceClient authServiceClient;

    public AppointmentDTO scheduleAppointment(AppointmentRequest request, String jwtToken) {
        try {
            UserInfoResponse user = authServiceClient.getUserInfo(jwtToken);
            // Process appointment
            return createAppointment(request, user.getId());
        } catch (RuntimeException e) {
            // Auth Service is down
            log.error("Auth Service unavailable: {}", e.getMessage());
            throw new ServiceUnavailableException("Authentication service unavailable");
        }
    }
}
```

---

## 🔐 Security Flow

### Token Validation Flow

```
1. Client sends request to Direct Service
   Authorization: Bearer <jwt-token>
           ↓
2. Direct Service extracts token from header
           ↓
3. AuthServiceClient.validateToken(token)
           ↓
4. HTTP GET http://auth-service:8082/api/auth/validate
   Authorization: Bearer <jwt-token>
           ↓
5. Auth Service validates token
   - Checks signature
   - Verifies expiration
   - Extracts claims
           ↓
6. Returns TokenValidationResponse { valid: true/false }
           ↓
7. Direct Service allows/denies request based on validation
```

### User Info Fetching Flow

```
1. Direct Service needs user details (email, role, etc)
           ↓
2. AuthServiceClient.getUserInfo(jwtToken)
           ↓
3. HTTP GET http://auth-service:8082/api/auth/me
   Authorization: Bearer <jwt-token>
           ↓
4. Auth Service returns UserInfoResponse
   {
     "id": 1,
     "fullName": "John Doe",
     "email": "john@example.com",
     "role": "ROLE_DOCTOR"
   }
           ↓
5. Direct Service uses user info for business logic
```

---

## ⚙️ Configuration

### Environment Variables

Set these in your deployment environment:

```bash
# Local development (localhost)
AUTH_SERVICE_URL=http://localhost:8082

# Docker deployment (service name)
AUTH_SERVICE_URL=http://auth-service:8082

# Kubernetes deployment (DNS)
AUTH_SERVICE_URL=http://auth-service.default.svc.cluster.local:8082
```

### application.yml Configuration

Default values (if env var not set):

```yaml
# Local
auth-service:
  url: http://localhost:8082

# Docker
auth-service:
  url: http://auth-service:8082
```

---

## 🧪 Testing Inter-Service Communication

### Test 1: Validate Token
```bash
# Get a token from Auth Service
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"password123"}'

# Response:
# {
#   "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
#   "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
# }

# Validate token with Direct Service
curl -X GET http://localhost:8081/api/some-protected-endpoint \
  -H "Authorization: Bearer <accessToken>"

# Direct Service will:
# 1. Receive the request
# 2. Extract the token
# 3. Call Auth Service: GET /api/auth/validate
# 4. Auth Service validates and returns response
# 5. Direct Service allows/denies access
```

### Test 2: Get User Info
```bash
# Direct Service can fetch user info
AuthServiceClient.getUserInfo(jwtToken)
# This calls: GET http://auth-service:8082/api/auth/me
# Returns user details for the token owner
```

### Test 3: Service-to-Service Failure Handling
```bash
# Stop Auth Service
docker stop carenexus-auth-service

# Try to use Direct Service
curl -X GET http://localhost:8081/api/doctors \
  -H "Authorization: Bearer <token>"

# Response: 503 Service Unavailable
# or error message: "Auth Service unavailable"
```

---

## 🔄 Fallback & Retry Logic (Optional for Stage 6+)

Currently, if Auth Service is unreachable, Direct Service returns an error. Future improvements:

1. **Caching tokens** in Redis (Stage 6)
2. **Circuit breaker pattern** for resilience
3. **Retry logic** with exponential backoff
4. **Local token validation cache** for short outages

---

## 🚀 Integration Points

### Where AuthServiceClient is Used

1. **JwtAuthenticationFilter** (Optional enhancement)
   - Instead of local validation, call Auth Service

2. **Service Classes**
   - DoctorService, PatientService, AppointmentService
   - Validate user role and permissions

3. **Controllers**
   - Validate tokens before processing requests
   - Fetch user info for business logic

4. **Custom Annotations**
   - Create @ValidateWithAuthService annotation
   - Automatically inject and validate tokens

---

## 📋 Checklist for Stage 5 Completion

- [x] Created RestTemplateConfig.java
- [x] Created AuthServiceClient.java with:
  - [x] getUserInfo() method
  - [x] validateToken() method
  - [x] Error handling
  - [x] Logging
- [x] Updated application.yml with auth-service.url
- [x] Added configuration for both local and docker profiles
- [x] Created documentation with usage examples
- [ ] (Stage 6) Add event bus configuration
- [ ] (Stage 7) Update docker-compose.yml with auth-service

---

## 🔗 Related Files

- **RestTemplateConfig**: `src/main/java/com/carenexus/direct/config/RestTemplateConfig.java`
- **AuthServiceClient**: `src/main/java/com/carenexus/direct/client/AuthServiceClient.java`
- **Configuration**: `src/main/resources/application.yml`
- **Auth Service**: `/Users/cosy/Documents/CareNexus/auth-service`

---

## 🎓 What's Next (Stage 6+)

### Stage 6: Event Bus Foundation (Kafka)
- Configure Kafka in both services
- Create event publishers in Auth Service
- Create event listeners in Direct Service
- Publish events like: user.registered, user.loggedIn, etc.

### Stage 7: Docker Multi-Service Setup
- Update docker-compose.yml
- Add auth-service container
- Add Kafka and Zookeeper containers
- Update health checks

### Stage 8: End-to-End Testing
- Test all three services together
- Verify JWT validation flow
- Verify Kafka events
- Load testing

### Stage 9: Documentation & Deployment
- Update GitHub documentation
- Create deployment guide
- Push to production setup

---

## 📞 Troubleshooting

### Auth Service is unreachable

**Symptom**: "Auth Service unavailable" error

**Solution**:
1. Check if Auth Service is running: `docker ps | grep auth-service`
2. Check logs: `docker logs carenexus-auth-service`
3. Verify URL in application.yml matches Auth Service port
4. Check network connectivity: `curl http://auth-service:8082/api/actuator/health`

### Token validation fails

**Symptom**: Valid token returns "invalid" from Auth Service

**Solution**:
1. Check JWT secret matches in both services
2. Verify token hasn't expired (24 hour limit)
3. Check user still exists in Auth Service database
4. Review logs in both services

### Timeout errors

**Symptom**: "Connection timeout" or "Read timeout"

**Solution**:
1. Increase timeouts in RestTemplateConfig (default: 5s connection, 10s read)
2. Check if Auth Service is slow or under load
3. Monitor network latency
4. Add retry logic with exponential backoff

---

**Status**: Ready for Stage 6 (Event Bus Configuration) ✅
