# 🔧 Backend Structure Reference (Nexus Direct)

This guide explains the current backend implementation so you can build frontend components that integrate correctly.

---

## 📁 Project Structure

```
src/main/java/com/carenexus/
├── auth/                          # Authentication module (will be extracted in Phase 7)
│   ├── controller/
│   │   └── AuthController.java   # Auth endpoints
│   ├── dto/
│   │   ├── AuthResponse.java     # Response with JWT token
│   │   ├── LoginRequest.java     # Login credentials
│   │   └── RefreshTokenRequest.java
│   ├── exception/
│   │   └── GlobalExceptionHandler.java
│   ├── model/
│   │   ├── User.java
│   │   ├── Role.java
│   │   └── RefreshToken.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   └── RefreshTokenRepository.java
│   ├── security/
│   │   ├── ApplicationConfig.java   # Spring Security beans
│   │   ├── PasswordConfig.java      # BCrypt password encoder
│   │   └── JwtService.java          # JWT token generation/validation
│   └── service/
│       └── AuthService.java         # Auth business logic
│
└── direct/                        # Direct Service (Patient/Doctor management)
    ├── controller/
    │   ├── AppointmentController.java
    │   ├── DoctorController.java
    │   ├── MessageController.java
    │   ├── PatientController.java
    │   └── TestController.java
    ├── dto/                      # Data Transfer Objects (request/response)
    │   ├── AppointmentDTO.java
    │   ├── DoctorDTO.java
    │   ├── MessageDTO.java
    │   └── PatientDTO.java
    ├── exception/
    │   ├── GlobalExceptionHandler.java
    │   ├── ForbiddenException.java
    │   └── NotFoundException.java
    ├── mapper/                   # Convert Entity ↔ DTO
    │   ├── AppointmentMapper.java
    │   ├── DoctorMapper.java
    │   ├── MessageMapper.java
    │   └── PatientMapper.java
    ├── model/                    # JPA Entities (database models)
    │   ├── Appointment.java
    │   ├── Doctor.java
    │   ├── Message.java
    │   └── Patient.java
    ├── repository/               # Database access (JPA)
    │   ├── AppointmentRepository.java
    │   ├── DoctorRepository.java
    │   ├── MessageRepository.java
    │   └── PatientRepository.java
    ├── security/
    │   ├── AuthenticatedUser.java
    │   ├── JwtAuthenticationFilter.java
    │   └── SecurityConfig.java
    ├── service/                  # Business logic
    │   ├── AppointmentService.java
    │   ├── DoctorService.java
    │   ├── MessageService.java
    │   ├── PatientService.java
    │   └── RemoteUserService.java
    └── DirectApplication.java     # Main Spring Boot entry point
```

---

## 🔌 API Endpoints (For Frontend Integration)

### **Authentication Endpoints** (`/api/auth/*`)

#### Register User
```
POST /api/auth/register
Content-Type: application/json

Request Body:
{
  "fullName": "Dr. John Doe",
  "email": "john@hospital.com",
  "password": "securePassword123",
  "role": "DOCTOR"  // or "PATIENT"
}

Response (200 OK):
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "john@hospital.com",
    "fullName": "Dr. John Doe",
    "role": "DOCTOR"
  }
}
```

#### Login
```
POST /api/auth/login
Content-Type: application/json

Request Body:
{
  "email": "john@hospital.com",
  "password": "securePassword123"
}

Response (200 OK):
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "john@hospital.com",
    "fullName": "Dr. John Doe",
    "role": "DOCTOR"
  }
}
```

#### Refresh Token
```
POST /api/auth/refresh
Content-Type: application/json

Request Body:
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

Response (200 OK):
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": { ... }
}
```

---

### **Doctor Endpoints** (`/api/doctors/*`)

**Authorization**: Requires valid JWT token in `Authorization: Bearer <token>` header

#### Create Doctor Profile
```
POST /api/doctors
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

Request Body:
{
  "name": "Dr. Robert Johnson",
  "specialization": "Cardiology",
  "licenseNumber": "MED-2023-001",
  "bio": "20+ years of experience",
  "consultationFee": 5000
}

Response (200 OK):
{
  "id": 101,
  "userEmail": "john@hospital.com",  // From JWT token
  "name": "Dr. Robert Johnson",
  "specialization": "Cardiology",
  "licenseNumber": "MED-2023-001",
  "bio": "20+ years of experience",
  "consultationFee": 5000,
  "createdAt": "2024-11-19T10:30:00Z",
  "updatedAt": "2024-11-19T10:30:00Z"
}
```

#### Get All Doctors (Paginated)
```
GET /api/doctors/search/paginated?page=0&size=20&sortBy=name&direction=ASC
Authorization: Bearer <JWT_TOKEN>

Response (200 OK):
{
  "content": [
    {
      "id": 101,
      "userEmail": "john@hospital.com",
      "name": "Dr. Robert Johnson",
      "specialization": "Cardiology",
      ...
    },
    { ... }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": { "unsorted": false }
  },
  "totalElements": 45,
  "totalPages": 3,
  "first": true,
  "last": false,
  "empty": false
}
```

#### Search Doctors by Name
```
GET /api/doctors/search/by-name?name=Johnson&page=0&size=20&sortBy=name&direction=ASC
Authorization: Bearer <JWT_TOKEN>

Response: Same paginated structure as above
```

#### Search Doctors by Specialization
```
GET /api/doctors/search/by-specialization?specialization=Cardiology&page=0&size=20&sortBy=specialization&direction=ASC
Authorization: Bearer <JWT_TOKEN>

Response: Same paginated structure as above
```

#### Get Single Doctor
```
GET /api/doctors/{id}
Authorization: Bearer <JWT_TOKEN>

Response (200 OK):
{
  "id": 101,
  "userEmail": "john@hospital.com",
  "name": "Dr. Robert Johnson",
  "specialization": "Cardiology",
  ...
}

Response (404 NOT FOUND if not owner):
{
  "error": "Doctor not found or you don't have access"
}

Response (403 FORBIDDEN if not owner):
{
  "error": "You don't have permission to access this resource"
}
```

#### Update Doctor
```
PUT /api/doctors/{id}
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

Request Body:
{
  "name": "Dr. Robert Johnson Updated",
  "specialization": "Cardiology",
  "licenseNumber": "MED-2023-001",
  "consultationFee": 5500
}

Response (200 OK): Updated doctor object
```

#### Delete Doctor
```
DELETE /api/doctors/{id}
Authorization: Bearer <JWT_TOKEN>

Response (204 NO CONTENT): Success
Response (403 FORBIDDEN): Not owner
Response (404 NOT FOUND): Doctor doesn't exist
```

---

### **Patient Endpoints** (`/api/patients/*`)

**Same structure as Doctor endpoints** with fields:
- `name`, `email`, `phone`, `dateOfBirth`, `medicalHistory`

Key endpoints:
```
POST /api/patients                                    # Create
GET /api/patients                                     # Get all (basic list)
GET /api/patients/{id}                                # Get by ID
PUT /api/patients/{id}                                # Update
DELETE /api/patients/{id}                             # Delete
GET /api/patients/search/paginated                    # Paginated list
GET /api/patients/search/by-name?name=...            # Search by name
GET /api/patients/search/by-email?email=...          # Search by email
```

---

### **Appointment Endpoints** (`/api/appointments/*`)

```
POST /api/appointments
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

Request Body:
{
  "doctorId": 101,
  "patientId": 201,
  "appointmentDateTime": "2024-12-20T14:30:00Z",
  "reason": "General checkup",
  "status": "SCHEDULED"  // or "COMPLETED", "CANCELLED"
}

Response (200 OK): Created appointment object

Other endpoints:
GET /api/appointments                                 # Get all
GET /api/appointments/{id}                            # Get by ID
PUT /api/appointments/{id}                            # Update
DELETE /api/appointments/{id}                         # Delete
GET /api/appointments/search/paginated               # Paginated
GET /api/appointments/search/by-reason?reason=...   # Search by reason
GET /api/appointments/search/by-date-range?start=...&end=...  # Date range
```

---

### **Message Endpoints** (`/api/messages/*`)

```
POST /api/messages
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

Request Body:
{
  "senderId": 101,
  "receiverId": 201,
  "content": "Hello, how are you feeling today?"
}

Response (200 OK): Created message object

Other endpoints:
GET /api/messages                                     # Get all
GET /api/messages/{id}                                # Get by ID
PUT /api/messages/{id}                                # Update
DELETE /api/messages/{id}                             # Delete
GET /api/messages/search/paginated                   # Paginated
GET /api/messages/search/by-content?content=...     # Search by text
GET /api/messages/search/by-date-range?start=...&end=...  # Date range
```

---

## 🔐 JWT Token Structure

All requests (except /api/auth/*) require:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huQGhvc3BpdGFsLmNvbSIsImlhdCI6MTczMTk2NDAwMCwiZXhwIjoxNzMyMDUwNDAwfQ.signature
```

**Token Claims**:
```json
{
  "sub": "john@hospital.com",      // Subject = user email
  "iat": 1731964000,                // Issued at
  "exp": 1732050400                 // Expiration (24 hours from issue)
}
```

**How to extract userEmail from token**:
- In Spring Security: `Principal.getName()` returns email
- In Angular: Decode JWT using `jwt-decode` library

---

## ⚠️ Error Responses

All endpoints return standard error responses:

**400 Bad Request** (validation error)
```json
{
  "error": "Validation failed",
  "details": {
    "name": "Name is required",
    "email": "Invalid email format"
  }
}
```

**401 Unauthorized** (missing/invalid JWT)
```json
{
  "error": "Unauthorized: Invalid token"
}
```

**403 Forbidden** (authenticated but no permission)
```json
{
  "error": "You don't have permission to access this resource"
}
```

**404 Not Found**
```json
{
  "error": "Resource not found"
}
```

**500 Internal Server Error**
```json
{
  "error": "Internal server error",
  "message": "Detailed error message"
}
```

---

## 📊 Database Models (for reference)

### User Entity
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String passwordHash;
    private String fullName;

    @Enumerated(EnumType.STRING)
    private Role role;  // DOCTOR, PATIENT, ADMIN

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### Doctor Entity
```java
@Entity
@Table(name = "doctors")
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;  // Multi-tenancy key
    private String name;
    private String specialization;
    private String licenseNumber;
    private String bio;
    private Double consultationFee;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### Patient Entity
```java
@Entity
@Table(name = "patients")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;  // Multi-tenancy key
    private String name;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String medicalHistory;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### Appointment Entity
```java
@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;  // Multi-tenancy key
    private Long doctorId;
    private Long patientId;
    private LocalDateTime appointmentDateTime;
    private String reason;
    private String status;  // SCHEDULED, COMPLETED, CANCELLED

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### Message Entity
```java
@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;  // Multi-tenancy key
    private Long senderId;
    private Long receiverId;
    private String content;
    private LocalDateTime sentAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

---

## 🔄 Service Layer Patterns

### DoctorService Example
```java
@Service
@RequiredArgsConstructor
public class DoctorService {
    private final DoctorRepository doctorRepository;

    // Create
    public Doctor save(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    // Read (with ownership validation)
    public Doctor getById(Long id, String userEmail) {
        return doctorRepository.findByIdAndUserEmail(id, userEmail)
            .orElseThrow(() -> new NotFoundException("Doctor not found"));
    }

    // Read All (filtered by owner)
    public List<Doctor> getAll(String userEmail) {
        return doctorRepository.findByUserEmail(userEmail);
    }

    // Update (with ownership validation)
    public Doctor update(Long id, Doctor updated, String userEmail) {
        Doctor existing = getById(id, userEmail);
        existing.setName(updated.getName());
        existing.setSpecialization(updated.getSpecialization());
        // ... other fields
        return doctorRepository.save(existing);
    }

    // Delete (with ownership validation)
    public void delete(Long id, String userEmail) {
        Doctor doctor = getById(id, userEmail);
        doctorRepository.delete(doctor);
    }

    // Search with pagination
    public Page<Doctor> searchByName(String userEmail, String name, Pageable pageable) {
        return doctorRepository.searchByNameAndUserEmail(userEmail, name, pageable);
    }
}
```

**Pattern**: Every method that accesses data includes `userEmail` parameter for ownership validation.

---

## 🚀 Environment Variables (from .env)

```
# Database
MYSQL_DATABASE=carenexus_direct
MYSQL_USER=appuser
MYSQL_PASSWORD=apppassword
MYSQL_PORT=3307

# JWT
JWT_SECRET=U29tZVN1cGVyU2VjdXJlSldUU2VjcmV0S2V5MTIzNCE=
JWT_EXPIRATION=86400000          # 24 hours
JWT_REFRESH_EXPIRATION=604800000 # 7 days

# Service
DIRECT_SERVICE_PORT=8081
SPRING_PROFILES_ACTIVE=docker
```

**When running locally**:
- Backend runs on `http://localhost:8081`
- MySQL on `localhost:3307`
- Adminer (optional) on `http://localhost:8082`

---

## 📝 Important Notes for Frontend Development

1. **JWT Token Storage**: Store in localStorage or sessionStorage, but include in every API request header
2. **Ownership Validation**: Backend enforces that users can only access their own data
3. **Pagination**: Always use pagination endpoints for lists (better performance with large datasets)
4. **Error Handling**: Always check for 401/403 errors and redirect to login if needed
5. **User Email**: Backend uses email as tenant identifier. When user logs in, store the email in Angular state
6. **Refresh Tokens**: Implement token refresh logic to keep users logged in longer
7. **CORS**: If frontend runs on different port, CORS is enabled in backend
8. **Consistent Naming**: DTOs match these names exactly (case-sensitive for JSON serialization)

---

## 🔗 Connect Your Frontend

When building Angular services, use these base URL and endpoints:
```typescript
const API_BASE_URL = 'http://localhost:8081/api';

// Auth Service
POST   ${API_BASE_URL}/auth/register
POST   ${API_BASE_URL}/auth/login
POST   ${API_BASE_URL}/auth/refresh

// Doctor endpoints
POST   ${API_BASE_URL}/doctors
GET    ${API_BASE_URL}/doctors
GET    ${API_BASE_URL}/doctors/{id}
PUT    ${API_BASE_URL}/doctors/{id}
DELETE ${API_BASE_URL}/doctors/{id}

// ... and similar for patients, appointments, messages
```

This structure maps exactly to what you'll build in Angular services.
