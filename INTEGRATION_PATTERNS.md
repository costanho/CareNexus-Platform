# 🔗 Integration Patterns Reference

Guide for integrating new modules and maintaining consistency across CareNexus.

---

## 📐 Architectural Pattern Overview

CareNexus follows a **modular microservices pattern** with clear separation of concerns:

```
┌─────────────────────────────────────────────────┐
│  Frontend Layer (Angular)                       │
│  - Auth Module (Login/Register)                 │
│  - Nexus Direct Module (Doctors/Patients)       │
│  - Nexus Connect Module (Provider Matching)     │
│  - ... Other Modules                            │
└────────────────┬────────────────────────────────┘
                 │ HTTP/REST
┌────────────────▼────────────────────────────────┐
│  API Layer (Spring Boot Controllers)             │
│  - JwtAuthenticationFilter (Auth middleware)    │
│  - ExceptionHandling (Global error handler)     │
│  - JWT Interceptor (Token validation)           │
└────────────────┬────────────────────────────────┘
                 │ Service Calls
┌────────────────▼────────────────────────────────┐
│  Business Logic Layer (Spring Services)          │
│  - DoctorService (Doctor business logic)        │
│  - PatientService (Patient business logic)      │
│  - Ownership validation (Multi-tenancy)         │
└────────────────┬────────────────────────────────┘
                 │ Repository Queries
┌────────────────▼────────────────────────────────┐
│  Data Layer (JPA Repositories)                  │
│  - DoctorRepository (JPQL queries)              │
│  - PatientRepository (Search, pagination)       │
│  - Custom query methods                         │
└────────────────┬────────────────────────────────┘
                 │ SQL
┌────────────────▼────────────────────────────────┐
│  Database Layer (MySQL)                         │
│  - users table                                  │
│  - doctors table                                │
│  - patients table                               │
│  - appointments table                           │
│  - messages table                               │
└──────────────────────────────────────────────────┘
```

---

## 🏗️ How to Add a New Backend Module

### Step 1: Create Package Structure

```bash
mkdir -p src/main/java/com/carenexus/new-module/{
  controller,
  service,
  repository,
  model,
  dto,
  mapper,
  exception
}
```

### Step 2: Create JPA Entity

Example: `NexusConnectProvider.java`

```java
package com.carenexus.connect.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "connect_providers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Provider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // IMPORTANT: Multi-tenancy key
    @Column(nullable = false)
    private String userEmail;

    private String name;
    private String specialization;
    private Double rating;
    private Double consultationFee;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

**Key Points**:
- Always include `userEmail` for multi-tenancy
- Use `@Builder` for easy creation
- Add timestamps (createdAt, updatedAt)
- Set `updatable = false` for createdAt

### Step 3: Create Repository

Example: `ProviderRepository.java`

```java
package com.carenexus.connect.repository;

import com.carenexus.connect.model.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProviderRepository extends JpaRepository<Provider, Long> {
    // Find by ID and userEmail (ownership validation)
    Optional<Provider> findByIdAndUserEmail(Long id, String userEmail);

    // Find all by userEmail
    List<Provider> findByUserEmail(String userEmail);

    // Paginated search
    Page<Provider> findByUserEmail(String userEmail, Pageable pageable);

    // Custom search by name
    @Query("SELECT p FROM Provider p WHERE p.userEmail = :userEmail AND " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Provider> searchByNameAndUserEmail(@Param("userEmail") String userEmail,
                                           @Param("name") String name,
                                           Pageable pageable);

    // Custom search by specialization
    @Query("SELECT p FROM Provider p WHERE p.userEmail = :userEmail AND " +
           "LOWER(p.specialization) LIKE LOWER(CONCAT('%', :specialization, '%'))")
    Page<Provider> searchBySpecializationAndUserEmail(
        @Param("userEmail") String userEmail,
        @Param("specialization") String specialization,
        Pageable pageable);
}
```

**Query Pattern**:
```
SELECT entity FROM Entity entity
WHERE entity.userEmail = :userEmail
AND [additional filters]
```

Every query MUST include `WHERE entity.userEmail = :userEmail` for multi-tenancy!

### Step 4: Create DTO

Example: `ProviderDTO.java`

```java
package com.carenexus.connect.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderDTO {
    private Long id;
    private String userEmail;
    private String name;
    private String specialization;
    private Double rating;
    private Double consultationFee;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### Step 5: Create Mapper

Example: `ProviderMapper.java`

```java
package com.carenexus.connect.mapper;

import com.carenexus.connect.dto.ProviderDTO;
import com.carenexus.connect.model.Provider;

public class ProviderMapper {
    public static ProviderDTO toDto(Provider provider) {
        if (provider == null) return null;

        return ProviderDTO.builder()
            .id(provider.getId())
            .userEmail(provider.getUserEmail())
            .name(provider.getName())
            .specialization(provider.getSpecialization())
            .rating(provider.getRating())
            .consultationFee(provider.getConsultationFee())
            .createdAt(provider.getCreatedAt())
            .updatedAt(provider.getUpdatedAt())
            .build();
    }

    public static Provider toEntity(ProviderDTO dto, String userEmail) {
        if (dto == null) return null;

        return Provider.builder()
            .id(dto.getId())
            .userEmail(userEmail)  // Override with authenticated user's email
            .name(dto.getName())
            .specialization(dto.getSpecialization())
            .rating(dto.getRating())
            .consultationFee(dto.getConsultationFee())
            .build();
    }
}
```

**Important**: Always pass `userEmail` from authenticated user to `toEntity()`

### Step 6: Create Service

Example: `ProviderService.java`

```java
package com.carenexus.connect.service;

import com.carenexus.connect.dto.ProviderDTO;
import com.carenexus.connect.exception.NotFoundException;
import com.carenexus.connect.mapper.ProviderMapper;
import com.carenexus.connect.model.Provider;
import com.carenexus.connect.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProviderService {
    private final ProviderRepository providerRepository;

    // CREATE
    public Provider save(Provider provider) {
        return providerRepository.save(provider);
    }

    // READ - Single (with ownership validation)
    public Provider getById(Long id, String userEmail) {
        return providerRepository.findByIdAndUserEmail(id, userEmail)
            .orElseThrow(() -> new NotFoundException(
                "Provider not found or you don't have access"));
    }

    // READ - All (filtered by userEmail)
    public List<Provider> getAll(String userEmail) {
        return providerRepository.findByUserEmail(userEmail);
    }

    // READ - Paginated
    public Page<Provider> getAllWithPagination(String userEmail, Pageable pageable) {
        return providerRepository.findByUserEmail(userEmail, pageable);
    }

    // UPDATE (with ownership validation)
    public Provider update(Long id, Provider updated, String userEmail) {
        Provider existing = getById(id, userEmail);

        existing.setName(updated.getName());
        existing.setSpecialization(updated.getSpecialization());
        existing.setRating(updated.getRating());
        existing.setConsultationFee(updated.getConsultationFee());

        return providerRepository.save(existing);
    }

    // DELETE (with ownership validation)
    public void delete(Long id, String userEmail) {
        Provider provider = getById(id, userEmail);
        providerRepository.delete(provider);
    }

    // SEARCH by name
    public Page<Provider> searchByName(String userEmail, String name, Pageable pageable) {
        return providerRepository.searchByNameAndUserEmail(userEmail, name, pageable);
    }

    // SEARCH by specialization
    public Page<Provider> searchBySpecialization(String userEmail, String specialization, Pageable pageable) {
        return providerRepository.searchBySpecializationAndUserEmail(
            userEmail, specialization, pageable);
    }
}
```

**Service Pattern**:
- Every method that accesses data includes `userEmail` parameter
- All CRUD operations validate ownership at service level
- Repository methods filter by `userEmail` at database level
- Ownership validation enforced in TWO places (belt-and-suspenders)

### Step 7: Create Controller

Example: `ProviderController.java`

```java
package com.carenexus.connect.controller;

import com.carenexus.connect.dto.ProviderDTO;
import com.carenexus.connect.mapper.ProviderMapper;
import com.carenexus.connect.model.Provider;
import com.carenexus.connect.service.ProviderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/providers")  // Module-specific endpoint
@RequiredArgsConstructor
public class ProviderController {
    private final ProviderService providerService;

    @PostMapping
    public ResponseEntity<ProviderDTO> createProvider(
            @Valid @RequestBody ProviderDTO dto,
            Principal principal) {
        String userEmail = principal.getName();
        Provider provider = ProviderMapper.toEntity(dto, userEmail);
        Provider saved = providerService.save(provider);
        return ResponseEntity.ok(ProviderMapper.toDto(saved));
    }

    @GetMapping
    public ResponseEntity<List<ProviderDTO>> getAllProviders(Principal principal) {
        String userEmail = principal.getName();
        List<ProviderDTO> providers = providerService.getAll(userEmail)
            .stream()
            .map(ProviderMapper::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(providers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProviderDTO> getProviderById(
            @PathVariable Long id,
            Principal principal) {
        String userEmail = principal.getName();
        Provider provider = providerService.getById(id, userEmail);
        return ResponseEntity.ok(ProviderMapper.toDto(provider));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProviderDTO> updateProvider(
            @PathVariable Long id,
            @Valid @RequestBody ProviderDTO dto,
            Principal principal) {
        String userEmail = principal.getName();
        Provider updated = providerService.update(
            id,
            ProviderMapper.toEntity(dto, userEmail),
            userEmail);
        return ResponseEntity.ok(ProviderMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProvider(
            @PathVariable Long id,
            Principal principal) {
        String userEmail = principal.getName();
        providerService.delete(id, userEmail);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search/paginated")
    public ResponseEntity<Page<ProviderDTO>> getProvidersWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            Principal principal) {
        String userEmail = principal.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<ProviderDTO> result = providerService.getAllWithPagination(userEmail, pageable)
            .map(ProviderMapper::toDto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search/by-name")
    public ResponseEntity<Page<ProviderDTO>> searchByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        String userEmail = principal.getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<ProviderDTO> result = providerService.searchByName(userEmail, name, pageable)
            .map(ProviderMapper::toDto);
        return ResponseEntity.ok(result);
    }
}
```

**Controller Pattern**:
- Endpoint path: `/api/{module-name}`
- Extract `userEmail` from `Principal.getName()`
- Pass to service for ownership validation
- Return DTOs (not entities)
- Use `@Valid` for input validation

### Step 8: Register in Spring Boot Application

Update `DirectApplication.java` (main class):

```java
@SpringBootApplication(
    scanBasePackages = {
        "com.carenexus.auth",
        "com.carenexus.direct",
        "com.carenexus.connect"  // Add new module
    }
)
public class DirectApplication {
    public static void main(String[] args) {
        SpringApplication.run(DirectApplication.class, args);
    }
}
```

---

## 🎨 How to Add a New Frontend Module

### Step 1: Generate Module Structure

```bash
cd /Users/cosy/Documents/CareNexus/frontend

# Generate module
ng generate module modules/nexus-connect/nexus-connect --routing

# Generate components for module
ng generate component modules/nexus-connect/pages/provider-list
ng generate component modules/nexus-connect/pages/provider-detail
ng generate component modules/nexus-connect/pages/provider-form
```

### Step 2: Create Models

Create `src/core/models/provider.model.ts`:

```typescript
export interface Provider {
  id: number;
  userEmail: string;
  name: string;
  specialization: string;
  rating: number;
  consultationFee: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateProviderRequest {
  name: string;
  specialization: string;
  rating: number;
  consultationFee: number;
}

export interface ProviderPage {
  content: Provider[];
  pageable: { pageNumber: number; pageSize: number };
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
```

### Step 3: Create Service

Create `src/modules/nexus-connect/services/provider.service.ts`:

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Provider, CreateProviderRequest, ProviderPage } from '../../../core/models/provider.model';

@Injectable({
  providedIn: 'root'
})
export class ProviderService {
  private apiUrl = 'http://localhost:8081/api/providers';

  constructor(private http: HttpClient) {}

  create(data: CreateProviderRequest): Observable<Provider> {
    return this.http.post<Provider>(this.apiUrl, data);
  }

  getPaginated(
    page: number = 0,
    size: number = 20
  ): Observable<ProviderPage> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ProviderPage>(`${this.apiUrl}/search/paginated`, { params });
  }

  searchByName(name: string, page: number = 0): Observable<ProviderPage> {
    const params = new HttpParams()
      .set('name', name)
      .set('page', page.toString());
    return this.http.get<ProviderPage>(`${this.apiUrl}/search/by-name`, { params });
  }

  getById(id: number): Observable<Provider> {
    return this.http.get<Provider>(`${this.apiUrl}/${id}`);
  }

  update(id: number, data: Partial<CreateProviderRequest>): Observable<Provider> {
    return this.http.put<Provider>(`${this.apiUrl}/${id}`, data);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
```

### Step 4: Create Component

Create `src/modules/nexus-connect/pages/provider-list/provider-list.component.ts`:

```typescript
import { Component, OnInit } from '@angular/core';
import { ProviderService } from '../../services/provider.service';
import { Provider, ProviderPage } from '../../../../core/models/provider.model';

@Component({
  selector: 'app-provider-list',
  templateUrl: './provider-list.component.html',
  styleUrls: ['./provider-list.component.scss']
})
export class ProviderListComponent implements OnInit {
  providers: Provider[] = [];
  currentPage = 0;
  pageSize = 20;
  totalPages = 0;
  loading = false;

  constructor(private providerService: ProviderService) {}

  ngOnInit(): void {
    this.loadProviders();
  }

  loadProviders(): void {
    this.loading = true;
    this.providerService.getPaginated(this.currentPage, this.pageSize).subscribe({
      next: (response: ProviderPage) => {
        this.providers = response.content;
        this.totalPages = response.totalPages;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading providers', err);
        this.loading = false;
      }
    });
  }

  onPageChange(newPage: number): void {
    this.currentPage = newPage;
    this.loadProviders();
  }
}
```

### Step 5: Create Module Routing

Update `src/modules/nexus-connect/nexus-connect-routing.module.ts`:

```typescript
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ProviderListComponent } from './pages/provider-list/provider-list.component';
import { ProviderDetailComponent } from './pages/provider-detail/provider-detail.component';

const routes: Routes = [
  {
    path: '',
    component: ProviderListComponent
  },
  {
    path: ':id',
    component: ProviderDetailComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class NexusConnectRoutingModule { }
```

### Step 6: Update App Routing

Update `src/app/app-routing.module.ts`:

```typescript
const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () => import('./modules/auth/auth.module')
      .then(m => m.AuthModule)
  },
  {
    path: 'direct',
    canActivate: [AuthGuard],
    loadChildren: () => import('./modules/nexus-direct/nexus-direct.module')
      .then(m => m.NexusDirectModule)
  },
  {
    path: 'connect',  // New module route
    canActivate: [AuthGuard],
    loadChildren: () => import('./modules/nexus-connect/nexus-connect.module')
      .then(m => m.NexusConnectModule)
  },
  // ... other routes
];
```

---

## 🔄 API Response Pattern

All paginated endpoints return:

```typescript
interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      unsorted: boolean;
    };
  };
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
```

Use this in all modules consistently!

---

## 🛡️ Security & Ownership Validation

**Multi-tenancy Rules**:
1. Every table has `userEmail` column
2. Every query filters by `userEmail`
3. Service validates ownership before CRUD
4. Frontend stores `userEmail` from JWT token
5. Users can ONLY access their own data

**Example violation to avoid**:
```java
// ❌ WRONG - exposes all data
List<Doctor> doctors = doctorRepository.findAll();

// ✅ CORRECT - filters by userEmail
List<Doctor> doctors = doctorRepository.findByUserEmail(userEmail);
```

---

## 📝 Naming Conventions

| Component | Naming | Example |
|-----------|--------|---------|
| Entity | `PascalCase` | `Provider.java` |
| Repository | `Entity` + `Repository` | `ProviderRepository.java` |
| Service | `Entity` + `Service` | `ProviderService.java` |
| Controller | `Entity` + `Controller` | `ProviderController.java` |
| DTO | `Entity` + `DTO` | `ProviderDTO.java` |
| Mapper | `Entity` + `Mapper` | `ProviderMapper.java` |
| Endpoint | `/api/{module}/{resource}` | `/api/providers` |
| Component | `kebab-case` directory | `provider-list/` |
| Service | `kebab-case.service.ts` | `provider.service.ts` |
| Model | `PascalCase.model.ts` | `Provider.model.ts` |
| Module | `kebab-case-routing.module.ts` | `nexus-connect-routing.module.ts` |

---

## ✅ Checklist for New Module

### Backend
- [ ] Package structure created
- [ ] Entity class with `userEmail` field
- [ ] Repository with custom search queries
- [ ] DTO and Mapper
- [ ] Service with ownership validation
- [ ] Controller with all CRUD endpoints
- [ ] Module registered in Spring Boot
- [ ] Tests written
- [ ] Endpoints documented

### Frontend
- [ ] Module structure generated
- [ ] Models created matching backend DTOs
- [ ] Service created matching backend API
- [ ] Pages/components created
- [ ] Routing configured
- [ ] App routing updated
- [ ] Components styled
- [ ] Error handling implemented
- [ ] Pagination implemented if needed

---

## 🚀 Quick Copy-Paste Templates

**Backend Entity Template**:
```java
@Entity
@Table(name = "module_entities")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userEmail;

    // Your fields here

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }
    @PreUpdate void onUpdate() { updatedAt = LocalDateTime.now(); }
}
```

**Frontend Service Template**:
```typescript
@Injectable({ providedIn: 'root' })
export class EntityService {
  private apiUrl = 'http://localhost:8081/api/entities';

  constructor(private http: HttpClient) {}

  getPaginated(page = 0, size = 20): Observable<PageResponse<Entity>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<Entity>>(`${this.apiUrl}/search/paginated`, { params });
  }
}
```

---

## 🔗 Communication Between Modules (Phase 7+)

When modules need to communicate (future):

**REST API Call** (synchronous):
```typescript
// Module A calling Module B
providerService.checkAvailability(providerId).subscribe(/* ... */);

// Backend: Module Connect calls Module Direct
@GetMapping("/api/providers/{id}/check")
public ResponseEntity<Boolean> checkProviderAvailable(@PathVariable Long id) {
    // Call Direct service REST API
    return restTemplate.getForObject("http://direct-service:8081/api/doctors/" + id, Boolean.class);
}
```

**Event Bus** (asynchronous):
```typescript
// Module A publishes event
eventBus.publish('appointment.scheduled', appointmentData);

// Module B subscribes
eventBus.subscribe('appointment.scheduled', (data) => {
    this.updateNotifications(data);
});

// Backend: Publish via Kafka
kafkaTemplate.send("appointment-events", appointmentJson);

// Backend: Subscribe
@KafkaListener(topics = "appointment-events")
public void handleAppointmentEvent(String message) {
    // Process event
}
```

---

## 📞 Key Integration Points

1. **Authentication**: All modules use shared JWT service
2. **Database**: All modules query from same MySQL (multi-schema)
3. **Events**: Modules publish/subscribe via event bus
4. **APIs**: Modules call each other via REST endpoints
5. **State**: Frontend modules share auth state via NgRx (future)

This ensures consistency and scalability across all 8 Nexus modules!
