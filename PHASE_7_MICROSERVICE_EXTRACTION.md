# 📡 PHASE 7: MICROSERVICE EXTRACTION GUIDE

**Status**: Ready to implement
**Estimated Duration**: 10-14 hours of focused work
**Purpose**: Extract Auth Service from Nexus Direct, establish microservice communication pattern
**Created**: 2024-11-28

---

## 🎯 PHASE 7 OVERVIEW

### Current State (Monolithic)
```
Nexus Direct Service (Port 8081)
├── Auth (embedded)
│   ├── User management
│   ├── JWT generation
│   └── Token refresh
├── Direct features
│   ├── Doctors
│   ├── Patients
│   ├── Appointments
│   └── Messages
└── Database
    └── Single MySQL database
```

### Target State (Microservices)
```
API Gateway (Port 8080)
├── Routes to Auth Service
├── Routes to Direct Service
├── Routes to other services
│   ├── Connect Service
│   ├── Proxy Service
│   └── etc.

Auth Service (Port 8082) ← NEW/SEPARATED
├── User management
├── JWT generation
├── Token refresh
└── Auth database

Nexus Direct Service (Port 8081) ← REFACTORED
├── Direct features only
│   ├── Doctors
│   ├── Patients
│   ├── Appointments
│   └── Messages
└── Direct database

Event Bus (Kafka/RabbitMQ)
├── Listens for auth events
├── Publishes service events
└── Enables async communication
```

---

## 📋 PHASE 7 IMPLEMENTATION STAGES

### **STAGE 1: Understand Current Architecture (30 minutes)**

**Purpose**: Review existing auth implementation before extraction

**Tasks**:
1. Review current auth package structure
   ```bash
   # See what we're extracting
   find src/main/java/com/carenexus/auth -type f -name "*.java" | wc -l
   ```

2. Identify all auth dependencies
   - Which files import from auth package?
   - Which services depend on AuthService?
   - Which controllers use JwtService?

3. Document auth API endpoints
   ```
   POST   /api/auth/register
   POST   /api/auth/login
   POST   /api/auth/refresh
   ```

4. List all auth-related tables
   ```sql
   users
   refresh_tokens
   ```

**Deliverable**: Understanding of complete auth system

**What this looks like**:
```
Auth System has:
✅ AuthService.java
✅ AuthController.java
✅ JwtService.java
✅ User.java entity
✅ RefreshToken.java entity
✅ UserRepository.java
✅ RefreshTokenRepository.java
✅ Global exception handling
✅ Security configuration
```

**Timeline**: 30 min
**When done**: Proceed to Stage 2

---

### **STAGE 2: Design Microservice Architecture (30 minutes)**

**Purpose**: Plan the service-to-service communication

**Tasks**:

1. **Define Auth Service Boundaries**
   ```
   AUTH SERVICE RESPONSIBILITIES:
   ✅ User registration
   ✅ User login
   ✅ Token generation
   ✅ Token validation
   ✅ Token refresh
   ✅ User information retrieval

   Direct Service will:
   ✅ Call Auth Service for validation
   ✅ Extract user email from token
   ✅ Use user email for data filtering
   ```

2. **Design Service Communication**
   ```
   Frontend
      ↓
   API Gateway (routes requests)
      ↓ /api/auth/* → Auth Service (8082)
      ↓ /api/doctors/* → Direct Service (8081)
      ↓ /api/patients/* → Direct Service (8081)

   Direct Service (8081)
      ↓ (calls Auth Service for token validation)
   Auth Service (8082)
      ↓
   Shared MySQL
   ```

3. **Define API Contracts**

   **Auth Service APIs**:
   ```
   POST /api/auth/register
   POST /api/auth/login
   POST /api/auth/refresh
   GET  /api/auth/validate/{token}  ← New: for service-to-service
   GET  /api/auth/user/{email}       ← New: for service-to-service
   ```

4. **Plan Event Bus (Kafka)**
   ```
   Events to publish:
   - user.registered
   - user.loggedIn
   - token.refreshed
   - user.loggedOut

   Events to subscribe:
   - (Prepare structure, won't implement events yet)
   ```

**Deliverable**: Architecture diagram and API contracts

**Timeline**: 30 min
**When done**: Proceed to Stage 3

---

### **STAGE 3: Create Auth Service (Spring Boot App) (1 hour)**

**Purpose**: Setup new Spring Boot application for Auth Service

**Tasks**:

1. **Create new Spring Boot project**
   ```bash
   # Navigate to CareNexus root
   cd /Users/cosy/Documents/CareNexus

   # Create auth-service directory
   mkdir auth-service
   cd auth-service

   # Create Maven project structure
   mkdir -p src/main/java/com/carenexus/auth
   mkdir -p src/main/resources
   mkdir -p src/test
   ```

2. **Create pom.xml** (Maven dependencies)
   ```xml
   <project>
     <modelVersion>4.0.0</modelVersion>
     <groupId>com.carenexus</groupId>
     <artifactId>auth-service</artifactId>
     <version>1.0.0</version>
     <name>CareNexus Auth Service</name>

     <dependencies>
       <!-- Spring Boot -->
       <dependency>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-starter-web</artifactId>
         <version>3.2.0</version>
       </dependency>

       <!-- Spring Data JPA -->
       <dependency>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-starter-data-jpa</artifactId>
         <version>3.2.0</version>
       </dependency>

       <!-- MySQL -->
       <dependency>
         <groupId>com.mysql</groupId>
         <artifactId>mysql-connector-j</artifactId>
         <version>8.0.33</version>
       </dependency>

       <!-- JWT -->
       <dependency>
         <groupId>io.jsonwebtoken</groupId>
         <artifactId>jjwt-api</artifactId>
         <version>0.12.3</version>
       </dependency>

       <!-- Lombok -->
       <dependency>
         <groupId>org.projectlombok</groupId>
         <artifactId>lombok</artifactId>
         <version>1.18.30</version>
       </dependency>
     </dependencies>
   </project>
   ```

3. **Create application.yml**
   ```yaml
   spring:
     application:
       name: auth-service
     datasource:
       url: jdbc:mysql://mysql-db:3306/carenexus_auth
       username: appuser
       password: apppassword
     jpa:
       hibernate:
         ddl-auto: update
       show-sql: false

   server:
     port: 8082
     servlet:
       context-path: /api

   jwt:
     secret: ${JWT_SECRET:U29tZVN1cGVyU2VjdXJlSldUU2VjcmV0S2V5MTIzNCE=}
     expiration: ${JWT_EXPIRATION:86400000}
     refreshExpiration: ${JWT_REFRESH_EXPIRATION:604800000}
   ```

4. **Create AuthApplication.java** (Main Spring Boot class)
   ```java
   @SpringBootApplication
   public class AuthApplication {
       public static void main(String[] args) {
           SpringApplication.run(AuthApplication.class, args);
       }
   }
   ```

**Deliverable**: Runnable Spring Boot application (not yet functional)

**Timeline**: 1 hour
**When done**: Proceed to Stage 4

---

### **STAGE 4: Extract Auth Code (2 hours)**

**Purpose**: Move auth classes from direct to auth-service

**Tasks**:

1. **Copy auth package classes**
   ```
   Copy from: direct/src/main/java/com/carenexus/auth/*
   Copy to:   auth-service/src/main/java/com/carenexus/auth/*

   Files to copy:
   ✅ User.java
   ✅ RefreshToken.java
   ✅ Role.java
   ✅ UserRepository.java
   ✅ RefreshTokenRepository.java
   ✅ JwtService.java
   ✅ AuthService.java
   ✅ AuthController.java
   ✅ GlobalExceptionHandler.java
   ✅ PasswordConfig.java
   ✅ ApplicationConfig.java
   ```

2. **Update auth controllers for service-to-service**
   ```java
   // Add new endpoints in AuthController

   @GetMapping("/validate/{token}")
   public ResponseEntity<Boolean> validateToken(@PathVariable String token) {
       try {
           jwtService.extractEmail(token);
           return ResponseEntity.ok(true);
       } catch (Exception e) {
           return ResponseEntity.ok(false);
       }
   }

   @GetMapping("/user/{email}")
   public ResponseEntity<UserInfoResponse> getUserByEmail(@PathVariable String email) {
       User user = userRepository.findByEmail(email)
           .orElseThrow(() -> new NotFoundException("User not found"));
       return ResponseEntity.ok(UserInfoResponse.from(user));
   }
   ```

3. **Create UserInfoResponse DTO**
   ```java
   public class UserInfoResponse {
       private Long id;
       private String email;
       private String fullName;
       private String role;
   }
   ```

4. **Create database schema for auth**
   ```sql
   CREATE DATABASE carenexus_auth;
   USE carenexus_auth;

   -- Users table (copied from direct service)
   CREATE TABLE users (
       id BIGINT PRIMARY KEY AUTO_INCREMENT,
       email VARCHAR(100) UNIQUE NOT NULL,
       password_hash VARCHAR(255) NOT NULL,
       full_name VARCHAR(100),
       role VARCHAR(50),
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
   );

   -- Refresh tokens table
   CREATE TABLE refresh_tokens (
       id BIGINT PRIMARY KEY AUTO_INCREMENT,
       user_email VARCHAR(100) NOT NULL,
       token TEXT NOT NULL,
       expiry_date DATETIME NOT NULL,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       FOREIGN KEY (user_email) REFERENCES users(email)
   );
   ```

**Deliverable**: Auth service with complete auth functionality

**Timeline**: 2 hours
**When done**: Proceed to Stage 5

---

### **STAGE 5: Setup Inter-Service Communication (2 hours)**

**Purpose**: Enable Direct Service to call Auth Service

**Tasks**:

1. **Create RestTemplate Bean** in Direct Service
   ```java
   @Configuration
   public class RestClientConfig {
       @Bean
       public RestTemplate restTemplate() {
           return new RestTemplate();
       }
   }
   ```

2. **Create AuthServiceClient** in Direct Service
   ```java
   @Service
   @RequiredArgsConstructor
   public class AuthServiceClient {
       private final RestTemplate restTemplate;
       private static final String AUTH_SERVICE_URL = "http://auth-service:8082/api";

       public boolean validateToken(String token) {
           try {
               ResponseEntity<Boolean> response = restTemplate.getForEntity(
                   AUTH_SERVICE_URL + "/auth/validate/" + token,
                   Boolean.class
               );
               return response.getBody() != null && response.getBody();
           } catch (Exception e) {
               return false;
           }
       }

       public UserInfoResponse getUserInfo(String email) {
           try {
               ResponseEntity<UserInfoResponse> response = restTemplate.getForEntity(
                   AUTH_SERVICE_URL + "/auth/user/" + email,
                   UserInfoResponse.class
               );
               return response.getBody();
           } catch (Exception e) {
               return null;
           }
       }
   }
   ```

3. **Update JwtAuthenticationFilter** in Direct Service
   ```java
   // Instead of validating JWT directly,
   // call Auth Service to validate token

   String token = extractTokenFromHeader(request);
   if (authServiceClient.validateToken(token)) {
       String userEmail = jwtService.extractEmail(token);
       // Create authentication
   }
   ```

4. **Update docker-compose.yml**
   ```yaml
   services:
     mysql-db:
       # ... existing config

     auth-service:  # ← NEW
       build:
         context: ./auth-service
         dockerfile: Dockerfile
       container_name: carenexus-auth-service
       depends_on:
         mysql-db:
           condition: service_healthy
       environment:
         SPRING_DATASOURCE_URL: jdbc:mysql://mysql-db:3306/carenexus_auth
         JWT_SECRET: ${JWT_SECRET}
       ports:
         - "8082:8082"
       networks:
         - carenexus-net

     direct-service:
       # ... existing config, now depends on auth-service
       depends_on:
         - mysql-db
         - auth-service  # ← ADD THIS
   ```

**Deliverable**: Service-to-service communication working

**Timeline**: 2 hours
**When done**: Proceed to Stage 6

---

### **STAGE 6: Configure Event Bus Foundation (2 hours)**

**Purpose**: Setup Kafka/RabbitMQ for async communication

**Tasks**:

1. **Add Kafka to docker-compose.yml**
   ```yaml
   zookeeper:
     image: confluentinc/cp-zookeeper:7.5.0
     environment:
       ZOOKEEPER_CLIENT_PORT: 2181
     networks:
       - carenexus-net

   kafka:
     image: confluentinc/cp-kafka:7.5.0
     depends_on:
       - zookeeper
     environment:
       KAFKA_BROKER_ID: 1
       KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
       KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
       KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
     ports:
       - "9092:9092"
     networks:
       - carenexus-net
   ```

2. **Create EventPublisher Service** in Auth Service
   ```java
   @Service
   @RequiredArgsConstructor
   public class EventPublisher {
       private final KafkaTemplate<String, String> kafkaTemplate;

       public void publishUserRegistered(String userEmail) {
           kafkaTemplate.send("user.registered",
               "{\"email\":\"" + userEmail + "\"}");
       }

       public void publishUserLoggedIn(String userEmail) {
           kafkaTemplate.send("user.loggedIn",
               "{\"email\":\"" + userEmail + "\"}");
       }
   }
   ```

3. **Create EventSubscriber** in Direct Service
   ```java
   @Service
   public class EventListener {
       @KafkaListener(topics = "user.registered")
       public void handleUserRegistered(String message) {
           // Handle user registration event
           // e.g., create notification, log analytics
       }

       @KafkaListener(topics = "user.loggedIn")
       public void handleUserLoggedIn(String message) {
           // Handle login event
       }
   }
   ```

4. **Create Event Topics**
   ```bash
   # Topics to create:
   - user.registered
   - user.loggedIn
   - token.refreshed
   - user.loggedOut
   - appointment.scheduled
   - message.sent
   ```

**Deliverable**: Event bus foundation (not yet publishing/subscribing events)

**Timeline**: 2 hours
**When done**: Proceed to Stage 7

---

### **STAGE 7: Docker Multi-Service Setup (1 hour)**

**Purpose**: Create Dockerfile for auth-service and update docker-compose

**Tasks**:

1. **Create auth-service/Dockerfile**
   ```dockerfile
   FROM maven:3.9.4-eclipse-temurin-21 AS builder
   WORKDIR /build
   COPY pom.xml .
   RUN mvn clean package -DskipTests

   FROM eclipse-temurin:21-jre
   WORKDIR /app
   COPY --from=builder /build/target/*.jar auth-service.jar
   ENTRYPOINT ["java", "-jar", "auth-service.jar"]
   EXPOSE 8082
   ```

2. **Update docker-compose.yml** with complete setup
   ```yaml
   version: "3.8"

   services:
     mysql-db:
       image: mysql:8.0
       # ... existing config

     zookeeper:
       # ... kafka setup

     kafka:
       # ... kafka setup

     auth-service:
       build:
         context: ./auth-service
         dockerfile: Dockerfile
       # ... existing config from earlier

     direct-service:
       build:
         context: ./direct
         dockerfile: Dockerfile
       depends_on:
         - mysql-db
         - auth-service
         - kafka
       # ... existing config

     adminer:
       # ... existing config

   networks:
     carenexus-net:
       driver: bridge
   ```

3. **Start all services**
   ```bash
   cd /Users/cosy/Documents/CareNexus
   docker compose down
   docker compose build --no-cache
   docker compose up -d
   ```

4. **Verify all services running**
   ```bash
   docker compose ps

   # Expected output:
   # carenexus-mysql      - Up (healthy)
   # carenexus-auth-service - Up (healthy)
   # carenexus-direct-service - Up (healthy)
   # carenexus-zookeeper  - Up
   # carenexus-kafka      - Up
   # carenexus-adminer    - Up
   ```

**Deliverable**: Multi-service Docker setup running

**Timeline**: 1 hour
**When done**: Proceed to Stage 8

---

### **STAGE 8: Test Complete Flow (1 hour)**

**Purpose**: Verify everything works end-to-end

**Tests to Run**:

1. **Test Auth Service Directly**
   ```bash
   # Register
   curl -X POST http://localhost:8082/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{
       "fullName":"Test User",
       "email":"test@test.com",
       "password":"password123",
       "role":"DOCTOR"
     }'

   # Login
   curl -X POST http://localhost:8082/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{
       "email":"test@test.com",
       "password":"password123"
     }'
   ```

2. **Test Direct Service API (with Auth Service)**
   ```bash
   # Get doctors (should work with JWT from auth service)
   curl -X GET http://localhost:8081/api/doctors \
     -H "Authorization: Bearer <TOKEN_FROM_AUTH_SERVICE>"
   ```

3. **Test Service-to-Service**
   ```bash
   # Direct service validates token with auth service
   curl -X GET http://localhost:8082/api/auth/validate/<TOKEN>
   ```

4. **Test Event Bus**
   ```bash
   # Check Kafka topics
   docker compose exec kafka kafka-topics --list --bootstrap-server localhost:9092

   # Publish test event
   docker compose exec kafka kafka-console-producer \
     --broker-list localhost:9092 \
     --topic user.registered
   ```

5. **Monitor Logs**
   ```bash
   docker compose logs -f auth-service
   docker compose logs -f direct-service
   docker compose logs -f kafka
   ```

**Success Criteria**:
- ✅ Auth Service starts and is healthy
- ✅ Direct Service starts and is healthy
- ✅ Can register user on Auth Service
- ✅ Can login and get JWT
- ✅ JWT works with Direct Service
- ✅ Service-to-service calls succeed
- ✅ Kafka topics created and working

**Timeline**: 1 hour
**When done**: Phase 7 is COMPLETE!

---

### **STAGE 9: Document & Commit (1 hour)**

**Purpose**: Document the new pattern and push to GitHub

**Tasks**:

1. **Update SYSTEM_ARCHITECTURE.md**
   - Add Auth Service details
   - Update communication diagram
   - Show microservice pattern

2. **Create PHASE_7_COMPLETION.md**
   - Document what was done
   - Show new service structure
   - Explain how other services will follow pattern

3. **Update README files**
   - Add auth-service startup instructions
   - Document inter-service communication
   - Add troubleshooting guide

4. **Commit to GitHub**
   ```bash
   git add -A
   git commit -m "feat: Implement Phase 7 - Microservice extraction (Auth Service)

   - Extract Auth Service to separate Spring Boot application
   - Configure service-to-service REST communication
   - Setup Kafka foundation for event bus
   - Create multi-service Docker orchestration
   - Add service health checks and logging
   - Document microservice communication patterns"

   git push origin main
   ```

**Timeline**: 1 hour
**When done**: Ready for next modules!

---

## 📊 TIMELINE SUMMARY

| Stage | Task | Duration | Total |
|-------|------|----------|-------|
| 1 | Understand current architecture | 30 min | 30 min |
| 2 | Design microservice architecture | 30 min | 1 hour |
| 3 | Create Auth Service app | 1 hour | 2 hours |
| 4 | Extract auth code | 2 hours | 4 hours |
| 5 | Setup inter-service communication | 2 hours | 6 hours |
| 6 | Configure event bus foundation | 2 hours | 8 hours |
| 7 | Docker multi-service setup | 1 hour | 9 hours |
| 8 | Test complete flow | 1 hour | 10 hours |
| 9 | Document & commit | 1 hour | 11 hours |

**Total: 11 hours (can be done in 1-2 focused days)**

---

## ✅ SUCCESS CRITERIA

After Phase 7, you'll have:

✅ Auth Service running independently on port 8082
✅ Direct Service running on port 8081 (calling Auth Service)
✅ JWT validation working across services
✅ Kafka event bus foundation ready
✅ Docker multi-service orchestration working
✅ Clear pattern to follow for next 7 modules
✅ Service-to-service REST communication documented
✅ Ready to build Connect, Proxy, and other modules

---

## 🚀 AFTER PHASE 7

Once this is done, building new modules becomes easier:

**For each new module (Connect, Proxy, etc.)**:
1. Copy auth-service structure
2. Replace "auth" with module name
3. Replace entities and APIs
4. Register in docker-compose
5. Add to Event Bus subscriptions
6. Deploy independently

**Each module follows same pattern** = Faster development!

---

## 📞 REFERENCE WHILE IMPLEMENTING

Keep this document open:
- Each stage has specific tasks
- Each stage has code examples
- Each stage has expected outcomes
- Each stage shows what's next

**You're not guessing** - every step is documented!

---

**Ready to start Phase 7?** Let me know when you want to begin Stage 1! 🚀
