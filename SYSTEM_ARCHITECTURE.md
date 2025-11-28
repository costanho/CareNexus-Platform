# 🏗️ CareNexus System Architecture Reference

## Overview
CareNexus is a comprehensive healthcare platform serving Zimbabwe's underserved regions. It's built as a **microservices ecosystem** with 8 interconnected modules. Currently, we're building the backend starting with **Nexus Direct**.

---

## 📊 8 Nexus Modules (Complete Ecosystem)

### 1. **Nexus Direct** ✅ (Currently Building)
**Purpose**: Direct in-house doctors management - patients book appointments with company doctors
- **Status**: Phases 1-5 complete, Phase 8 (Docker) complete, Phase 7 pending
- **Current Implementation**:
  - JWT authentication (embedded in Direct Service)
  - Doctor CRUD with ownership validation
  - Patient CRUD with ownership validation
  - Appointment scheduling
  - Message/consultation messaging
  - Search, pagination, sorting on all domains
- **Tech Stack**: Spring Boot, Spring Data JPA, MySQL, JWT
- **Port**: 8081 (direct-service)
- **Database**: carenexus_direct (MySQL 8.0)

### 2. **Nexus Connect** 🔄 (Future)
**Purpose**: External provider matching - connect patients with vetted external healthcare providers
- **Planned Features**:
  - Provider search and filtering
  - Specialization matching algorithm
  - Rating and review system
  - Booking external consultations
  - Insurance verification
  - Quality metrics tracking
- **Will Use**: Rating/matching algorithms, third-party integrations
- **Port**: 8082 (reserved, not yet built)
- **Database**: carenexus_connect (separate from Direct)

### 3. **Nexus Proxy** 📱 (Future)
**Purpose**: Remote caregiving - doctors provide remote care to patients (video calls, remote diagnostics)
- **Planned Features**:
  - Video call integration (Twilio/Agora)
  - Real-time messaging
  - Remote prescription management
  - Session recording
  - Prescription delivery to pharmacies
- **Will Use**: WebSockets, video streaming, prescription APIs
- **Port**: 8083 (reserved, not yet built)

### 4. **Nexus FacilityConnect** 🏥 (Future)
**Purpose**: Hospital/clinic integration - manage facilities, bookings, and patient records across multiple facilities
- **Planned Features**:
  - Facility management (beds, equipment, departments)
  - Integrated appointment scheduling across facilities
  - Patient records aggregation
  - Bed availability tracking
  - Inter-facility patient transfers
- **Will Use**: Legacy EHR system APIs, HL7 standards
- **Port**: 8084 (reserved, not yet built)

### 5. **Nexus Urgent** 🚨 (Future)
**Purpose**: Emergency dispatch - emergency response routing, ambulance dispatch, GPS tracking
- **Planned Features**:
  - Real-time GPS tracking
  - Ambulance/emergency vehicle routing
  - Emergency triage system
  - First responder coordination
  - Geolocation-based nearest facility dispatch
- **Will Use**: Google Maps API, real-time GPS, WebSockets
- **Port**: 8085 (reserved, not yet built)

### 6. **Nexus Learn** 📚 (Future)
**Purpose**: Educational platform - medical education, doctor training, patient health literacy
- **Planned Features**:
  - Course management
  - Video lectures
  - Quizzes and assessments
  - Certificates
  - Progress tracking
  - Health literacy content for patients
- **Will Use**: Video streaming, LMS features, CDN
- **Port**: 8086 (reserved, not yet built)

### 7. **Nexus Companion** 🤖 (Future)
**Purpose**: AI-powered health assistant - chatbot for preliminary diagnosis, health tips, medication reminders
- **Planned Features**:
  - Natural language processing
  - Symptom checker
  - Medication reminders
  - Health tips and education
  - AI-driven preliminary triage
- **Will Use**: OpenAI/Claude API, NLP, Kafka for real-time updates
- **Port**: 8087 (reserved, not yet built)

### 8. **Nexus Core Infrastructure** ⚙️ (Foundation)
**Purpose**: Core backend engine - authentication, database, shared services, event bus
- **Current Components**:
  - JWT Authentication Service (currently embedded in Direct)
  - MySQL Database
  - Docker orchestration
  - Configuration management (.env)
- **Planned Components**:
  - Kafka/RabbitMQ (event bus for inter-service communication)
  - Redis (caching and session management)
  - API Gateway (route requests to correct service)
  - Logging and monitoring (ELK stack)
  - Billing/Claims+ system
  - Analytics service

---

## 🔗 How Modules Communicate (Phase 7+)

```
┌─────────────────────────────────────────────────────────────┐
│                      API GATEWAY                             │
│              (Routes requests to services)                   │
└────┬────────────┬────────────┬────────────┬────────────────┘
     │            │            │            │
     ↓            ↓            ↓            ↓
┌─────────┐  ┌──────────┐  ┌────────┐  ┌──────────┐
│ Nexus   │  │ Nexus    │  │ Nexus  │  │ Nexus    │
│ Direct  │  │ Connect  │  │ Proxy  │  │ Urgent   │
└────┬────┘  └──────────┘  └────────┘  └──────────┘
     │            │            │            │
     └────────────┴────────────┴────────────┘
            │
            ↓
    ┌──────────────────┐
    │   EVENT BUS      │
    │  (Kafka/RabbitMQ)│
    └──────────────────┘
            │
            ↓
    ┌──────────────────┐
    │ Core Services    │
    │ - Auth Service   │
    │ - MySQL Database │
    │ - Redis Cache    │
    │ - Billing/Claims │
    │ - Analytics      │
    └──────────────────┘
```

### Communication Patterns:

**1. Synchronous (REST APIs)**
- Nexus Connect → Nexus Direct (check if doctor available for referral)
- Nexus Proxy → Nexus Direct (verify doctor credentials)
- Nexus FacilityConnect → Nexus Direct (schedule appointment at facility)

**2. Asynchronous (Event Bus via Kafka)**
- Appointment Created → triggers notification to Companion
- Patient Registered → triggers welcome email flow
- Consultation Completed → triggers analytics event
- Emergency Reported → triggers Urgent service dispatch

**3. Shared Resources**
- **Database**: All services share same MySQL instance initially (multi-schema approach)
- **Auth**: All services use same JWT service (will be extracted in Phase 7)
- **Caching**: Redis for cross-service caching (user sessions, provider lists)

---

## 🔐 Authentication Flow (Current Implementation)

```
Client (Angular) → POST /api/auth/register
                      ↓
                 [Spring Security creates User]
                      ↓
                 [BCrypt password hashing (10 rounds)]
                      ↓
                 [JWT token generated via JwtService]
                      ↓
                 Response: { accessToken, refreshToken, user }
                      ↓
Client stores JWT → All subsequent requests include:
                   Authorization: Bearer <JWT_TOKEN>
                      ↓
            [JwtAuthenticationFilter validates token]
                      ↓
            [Principal.getName() returns userEmail]
                      ↓
        [All queries filtered by userEmail for multi-tenancy]
```

**Key Points**:
- JWT Secret: Base64 encoded (minimum 32 bytes for HS256)
- Access Token Expiration: 24 hours (86400000 ms)
- Refresh Token Expiration: 7 days (604800000 ms)
- Password Hashing: BCrypt with 10 rounds
- Multi-tenancy: Enforced at database level via `WHERE userEmail = :userEmail`

---

## 🗄️ Database Schema (Current State)

### Users Table
```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY,
  email VARCHAR(100) UNIQUE,
  password_hash VARCHAR(255),
  full_name VARCHAR(100),
  role ENUM('ADMIN', 'DOCTOR', 'PATIENT'),
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);
```

### Doctors Table
```sql
CREATE TABLE doctors (
  id BIGINT PRIMARY KEY,
  user_email VARCHAR(100),  -- Multi-tenancy key
  name VARCHAR(100),
  specialization VARCHAR(100),
  license_number VARCHAR(100),
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  FOREIGN KEY (user_email) REFERENCES users(email)
);
```

### Patients Table
```sql
CREATE TABLE patients (
  id BIGINT PRIMARY KEY,
  user_email VARCHAR(100),  -- Multi-tenancy key
  name VARCHAR(100),
  email VARCHAR(100),
  phone VARCHAR(20),
  dob DATE,
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  FOREIGN KEY (user_email) REFERENCES users(email)
);
```

### Appointments Table
```sql
CREATE TABLE appointments (
  id BIGINT PRIMARY KEY,
  user_email VARCHAR(100),  -- Multi-tenancy key
  doctor_id BIGINT,
  patient_id BIGINT,
  appointment_date_time DATETIME,
  reason VARCHAR(255),
  status VARCHAR(50),
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  FOREIGN KEY (user_email) REFERENCES users(email)
);
```

### Messages Table
```sql
CREATE TABLE messages (
  id BIGINT PRIMARY KEY,
  user_email VARCHAR(100),  -- Multi-tenancy key
  sender_id BIGINT,
  receiver_id BIGINT,
  content TEXT,
  sent_at DATETIME,
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  FOREIGN KEY (user_email) REFERENCES users(email)
);
```

**Multi-tenancy Strategy**: Every table has `user_email` column. All queries include `WHERE user_email = :userEmail` filter at repository level. This ensures complete data isolation between users.

---

## 📦 Current Implementation Status

| Component | Status | Notes |
|-----------|--------|-------|
| Nexus Direct Backend | ✅ 80% | Phases 1-5 + 8 complete, Phase 7 pending |
| Auth Service (embedded) | ✅ 100% | JWT, BCrypt, refresh tokens working |
| Doctor CRUD | ✅ 100% | Full CRUD + search + pagination + ownership validation |
| Patient CRUD | ✅ 100% | Full CRUD + search + pagination + ownership validation |
| Appointment CRUD | ✅ 100% | Full CRUD + search + pagination + ownership validation |
| Message CRUD | ✅ 100% | Full CRUD + search + pagination + ownership validation |
| Docker Orchestration | ✅ 100% | docker-compose.yml ready, .env management done |
| Frontend (Angular) | ⏳ Planned | Not yet started |
| Phase 7 (Microservice Extraction) | ⏳ Pending | Auth Service extraction planned |
| Other 7 Modules | ⏳ Not Started | Will be built incrementally |

---

## 🚀 Next Steps (In Order)

1. **Run Nexus Direct locally** (Docker)
   - Verify MySQL connection
   - Test registration → login → API access flow
   - Confirm all CRUD operations work

2. **Begin Frontend Development** (Angular)
   - Generate Angular project
   - Create modular structure for all 8 modules
   - Build Auth module (login/register)
   - Build Nexus Direct UI

3. **Extract Auth Service** (Phase 7)
   - Move JwtService to separate microservice
   - Setup inter-service REST communication
   - Update Direct Service to call external Auth Service

4. **Setup Event Bus** (Phase 7+)
   - Configure Kafka/RabbitMQ
   - Publish events for cross-service communication
   - Subscribe to events

5. **Begin Nexus Connect** (New Module)
   - Provider search algorithms
   - Rating system
   - Integration with Direct Service

6. **Continue Other Modules** (Iterative)
   - Proxy (video calls)
   - FacilityConnect (hospital integration)
   - Urgent (emergency dispatch)
   - Learn (education platform)
   - Companion (AI chatbot)

---

## 💡 Key Architectural Principles

1. **Single Responsibility**: Each module handles one core domain
2. **Loose Coupling**: Modules communicate via APIs and events, not shared code
3. **Multi-tenancy**: All data isolation enforced at database level (userEmail filter)
4. **API-First Design**: All communication goes through REST APIs (and future events)
5. **Stateless Services**: Services can be scaled horizontally
6. **Database Per Service** (future): Each module will have its own database
7. **Event-Driven**: Async communication via event bus for non-blocking operations

---

## 📞 Contact Points for This Guide

Keep this file open in VS Code while developing. When you:
- Start a new module → Check "8 Nexus Modules" section
- Need to understand existing implementation → Check "Current Implementation Status"
- Want to know communication patterns → Check "How Modules Communicate"
- Need database context → Check "Database Schema"
- Unsure about next steps → Check "Next Steps"

**This is your system map. Refer back often.**
