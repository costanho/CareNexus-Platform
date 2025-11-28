# 🎨 Frontend Setup Guide (Angular)

This guide walks you through setting up the CareNexus Angular frontend for VS Code development.

---

## 📋 Prerequisites

Before starting, ensure you have:
- Node.js (v18+) installed
- npm (v9+) installed
- Angular CLI installed: `npm install -g @angular/cli`
- VS Code installed
- Basic understanding of TypeScript and Angular

---

## 🚀 Step 1: Generate Angular Project

Create a new Angular project in the same CareNexus directory as the backend:

```bash
# Navigate to CareNexus root
cd /Users/cosy/Documents/CareNexus

# Generate Angular project
ng new frontend --routing --style=scss --skip-git

# Navigate to frontend
cd frontend
```

**What this does**:
- Creates an Angular 17+ project with routing module
- Uses SCSS for styling (better than plain CSS)
- `--skip-git` prevents creating nested git repo

---

## 📁 Step 2: Create Modular Folder Structure

Inside `src/app/`, create this structure for the 8 modules:

```bash
mkdir -p src/app/{
  shared,
  core,
  modules/auth,
  modules/nexus-direct,
  modules/nexus-connect,
  modules/nexus-proxy,
  modules/nexus-facility-connect,
  modules/nexus-urgent,
  modules/nexus-learn,
  modules/nexus-companion
}
```

**Folder breakdown**:

```
src/app/
├── shared/                     # Shared components, pipes, directives, utils
│   ├── components/
│   │   ├── header/
│   │   ├── sidebar/
│   │   ├── footer/
│   │   └── pagination/
│   ├── pipes/
│   │   └── date-format.pipe.ts
│   ├── directives/
│   │   └── has-role.directive.ts
│   ├── utils/
│   │   └── validators.ts
│   └── shared.module.ts
│
├── core/                       # Core services, interceptors, guards
│   ├── services/
│   │   ├── auth.service.ts
│   │   ├── http.service.ts
│   │   ├── storage.service.ts
│   │   └── notification.service.ts
│   ├── guards/
│   │   ├── auth.guard.ts
│   │   └── role.guard.ts
│   ├── interceptors/
│   │   ├── jwt.interceptor.ts
│   │   └── error.interceptor.ts
│   ├── models/
│   │   ├── auth.model.ts
│   │   ├── doctor.model.ts
│   │   ├── patient.model.ts
│   │   ├── appointment.model.ts
│   │   └── message.model.ts
│   └── core.module.ts
│
├── modules/
│   │
│   ├── auth/                   # Authentication module
│   │   ├── auth-routing.module.ts
│   │   ├── auth.module.ts
│   │   ├── pages/
│   │   │   ├── login/
│   │   │   │   ├── login.component.ts
│   │   │   │   ├── login.component.html
│   │   │   │   └── login.component.scss
│   │   │   ├── register/
│   │   │   │   ├── register.component.ts
│   │   │   │   ├── register.component.html
│   │   │   │   └── register.component.scss
│   │   │   └── forgot-password/
│   │   │       ├── forgot-password.component.ts
│   │   │       ├── forgot-password.component.html
│   │   │       └── forgot-password.component.scss
│   │   └── services/
│   │       └── auth-api.service.ts
│   │
│   ├── nexus-direct/           # Patient/Doctor management
│   │   ├── nexus-direct-routing.module.ts
│   │   ├── nexus-direct.module.ts
│   │   ├── pages/
│   │   │   ├── dashboard/      # Doctor dashboard / Patient profile
│   │   │   ├── doctors/        # List doctors, create doctor
│   │   │   ├── patients/       # List patients
│   │   │   ├── appointments/   # Schedule, view appointments
│   │   │   └── messages/       # Messaging between doctor/patient
│   │   ├── components/
│   │   │   ├── doctor-card/
│   │   │   ├── patient-card/
│   │   │   └── appointment-form/
│   │   └── services/
│   │       ├── doctor.service.ts
│   │       ├── patient.service.ts
│   │       ├── appointment.service.ts
│   │       └── message.service.ts
│   │
│   ├── nexus-connect/          # External provider matching
│   │   ├── nexus-connect-routing.module.ts
│   │   ├── nexus-connect.module.ts
│   │   └── ... (similar structure)
│   │
│   ├── nexus-proxy/            # Remote caregiving
│   ├── nexus-facility-connect/ # Hospital integration
│   ├── nexus-urgent/           # Emergency dispatch
│   ├── nexus-learn/            # Education platform
│   └── nexus-companion/        # AI chatbot
│
└── app.component.ts
```

---

## ⚙️ Step 3: Install Dependencies

```bash
npm install
```

Install additional packages you'll need:

```bash
# HTTP & state management
npm install @ngrx/store @ngrx/effects

# UI component library (optional but recommended)
npm install @angular/material @angular/cdk

# JWT token handling
npm install @auth0/angular-jwt

# Form validation & utilities
npm install rxjs lodash-es

# Date handling
npm install date-fns
```

---

## 🔐 Step 4: Create Core Auth Service

This is your foundation. Create `src/core/services/auth.service.ts`:

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { StorageService } from './storage.service';

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: {
    id: number;
    email: string;
    fullName: string;
    role: 'DOCTOR' | 'PATIENT' | 'ADMIN';
  };
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
  role: 'DOCTOR' | 'PATIENT';
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8081/api/auth';
  private currentUser$ = new BehaviorSubject<AuthResponse['user'] | null>(null);
  private isAuthenticated$ = new BehaviorSubject<boolean>(false);

  constructor(
    private http: HttpClient,
    private storage: StorageService
  ) {
    this.loadStoredUser();
  }

  register(data: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, data)
      .pipe(
        tap(response => this.handleAuthResponse(response))
      );
  }

  login(data: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, data)
      .pipe(
        tap(response => this.handleAuthResponse(response))
      );
  }

  logout(): void {
    this.storage.clear();
    this.currentUser$.next(null);
    this.isAuthenticated$.next(false);
  }

  refreshToken(refreshToken: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(
      `${this.apiUrl}/refresh`,
      { refreshToken }
    ).pipe(
      tap(response => this.handleAuthResponse(response))
    );
  }

  getCurrentUser(): Observable<AuthResponse['user'] | null> {
    return this.currentUser$.asObservable();
  }

  isAuthenticated(): Observable<boolean> {
    return this.isAuthenticated$.asObservable();
  }

  getAccessToken(): string | null {
    return this.storage.getItem('accessToken');
  }

  private handleAuthResponse(response: AuthResponse): void {
    this.storage.setItem('accessToken', response.accessToken);
    this.storage.setItem('refreshToken', response.refreshToken);
    this.storage.setItem('user', JSON.stringify(response.user));
    this.currentUser$.next(response.user);
    this.isAuthenticated$.next(true);
  }

  private loadStoredUser(): void {
    const stored = this.storage.getItem('user');
    if (stored) {
      this.currentUser$.next(JSON.parse(stored));
      this.isAuthenticated$.next(true);
    }
  }
}
```

---

## 🛡️ Step 5: Create JWT Interceptor

Create `src/core/interceptors/jwt.interceptor.ts`:

```typescript
import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    // Skip JWT addition for auth endpoints
    if (request.url.includes('/api/auth/')) {
      return next.handle(request);
    }

    const token = this.authService.getAccessToken();
    if (token) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(request);
  }
}
```

---

## 🚪 Step 6: Create Auth Guards

Create `src/core/guards/auth.guard.ts`:

```typescript
import { Injectable } from '@angular/core';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> {
    return this.authService.isAuthenticated()
      .pipe(
        map(isAuth => {
          if (isAuth) {
            return true;
          }
          this.router.navigate(['/auth/login']);
          return false;
        })
      );
  }
}
```

---

## 📦 Step 7: Create Models

Create `src/core/models/doctor.model.ts`:

```typescript
export interface Doctor {
  id: number;
  userEmail: string;
  name: string;
  specialization: string;
  licenseNumber: string;
  bio?: string;
  consultationFee: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateDoctorRequest {
  name: string;
  specialization: string;
  licenseNumber: string;
  bio?: string;
  consultationFee: number;
}

export interface DoctorPage {
  content: Doctor[];
  pageable: {
    pageNumber: number;
    pageSize: number;
  };
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
```

Create similar models for Patient, Appointment, Message...

---

## 🔌 Step 8: Create Service Layer

Create `src/modules/nexus-direct/services/doctor.service.ts`:

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Doctor, CreateDoctorRequest, DoctorPage } from '../../../core/models/doctor.model';

@Injectable({
  providedIn: 'root'
})
export class DoctorService {
  private apiUrl = 'http://localhost:8081/api/doctors';

  constructor(private http: HttpClient) {}

  // Create doctor
  create(data: CreateDoctorRequest): Observable<Doctor> {
    return this.http.post<Doctor>(this.apiUrl, data);
  }

  // Get all doctors (basic list)
  getAll(): Observable<Doctor[]> {
    return this.http.get<Doctor[]>(this.apiUrl);
  }

  // Get doctors with pagination
  getPaginated(
    page: number = 0,
    size: number = 20,
    sortBy: string = 'id',
    direction: 'ASC' | 'DESC' = 'DESC'
  ): Observable<DoctorPage> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('direction', direction);

    return this.http.get<DoctorPage>(
      `${this.apiUrl}/search/paginated`,
      { params }
    );
  }

  // Search by name
  searchByName(
    name: string,
    page: number = 0,
    size: number = 20
  ): Observable<DoctorPage> {
    const params = new HttpParams()
      .set('name', name)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<DoctorPage>(
      `${this.apiUrl}/search/by-name`,
      { params }
    );
  }

  // Search by specialization
  searchBySpecialization(
    specialization: string,
    page: number = 0,
    size: number = 20
  ): Observable<DoctorPage> {
    const params = new HttpParams()
      .set('specialization', specialization)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<DoctorPage>(
      `${this.apiUrl}/search/by-specialization`,
      { params }
    );
  }

  // Get single doctor
  getById(id: number): Observable<Doctor> {
    return this.http.get<Doctor>(`${this.apiUrl}/${id}`);
  }

  // Update doctor
  update(id: number, data: Partial<CreateDoctorRequest>): Observable<Doctor> {
    return this.http.put<Doctor>(`${this.apiUrl}/${id}`, data);
  }

  // Delete doctor
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
```

---

## 🎯 Step 9: Create Login Component

Create `src/modules/auth/pages/login/login.component.ts`:

```typescript
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  form: FormGroup;
  loading = false;
  error = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    this.loading = true;
    this.error = '';

    this.authService.login(this.form.value).subscribe({
      next: () => {
        this.router.navigate(['/direct/dashboard']);
      },
      error: (err) => {
        this.error = err.error?.message || 'Login failed';
        this.loading = false;
      }
    });
  }
}
```

Create `src/modules/auth/pages/login/login.component.html`:

```html
<div class="login-container">
  <h1>CareNexus Login</h1>

  <form [formGroup]="form" (ngSubmit)="onSubmit()">
    <div class="form-group">
      <label for="email">Email</label>
      <input
        id="email"
        type="email"
        formControlName="email"
        placeholder="Enter your email"
      />
    </div>

    <div class="form-group">
      <label for="password">Password</label>
      <input
        id="password"
        type="password"
        formControlName="password"
        placeholder="Enter your password"
      />
    </div>

    <div *ngIf="error" class="error">{{ error }}</div>

    <button
      type="submit"
      [disabled]="form.invalid || loading"
    >
      {{ loading ? 'Logging in...' : 'Login' }}
    </button>
  </form>

  <p>
    Don't have an account?
    <a routerLink="/auth/register">Register here</a>
  </p>
</div>
```

---

## 🔧 Step 10: Setup App Routing

Update `src/app/app-routing.module.ts`:

```typescript
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';

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
    path: 'connect',
    canActivate: [AuthGuard],
    loadChildren: () => import('./modules/nexus-connect/nexus-connect.module')
      .then(m => m.NexusConnectModule)
  },
  // ... other modules
  {
    path: '',
    redirectTo: '/direct/dashboard',
    pathMatch: 'full'
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
```

---

## 🎨 Step 11: Setup Core Module

Create `src/core/core.module.ts`:

```typescript
import { NgModule } from '@angular/core';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { JwtInterceptor } from './interceptors/jwt.interceptor';
import { AuthService } from './services/auth.service';
import { StorageService } from './services/storage.service';

@NgModule({
  imports: [HttpClientModule],
  providers: [
    AuthService,
    StorageService,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: JwtInterceptor,
      multi: true
    }
  ]
})
export class CoreModule { }
```

---

## 📝 Step 12: Update App Module

Update `src/app/app.module.ts`:

```typescript
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { CoreModule } from './core/core.module';
import { SharedModule } from './shared/shared.module';

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    AppRoutingModule,
    CoreModule,
    SharedModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
```

---

## 🏃 Step 13: Run Development Server

```bash
# Make sure backend is running (Docker)
# In backend directory:
docker compose up -d

# In frontend directory
ng serve --open
```

Frontend will run on: `http://localhost:4200`
Backend runs on: `http://localhost:8081`

---

## ✅ Checklist for Frontend Setup

- [ ] Angular project generated with `ng new`
- [ ] Modular folder structure created for all 8 modules
- [ ] Dependencies installed (`npm install`)
- [ ] AuthService created and working
- [ ] JwtInterceptor configured
- [ ] AuthGuard protecting routes
- [ ] Models created for Doctor, Patient, Appointment, Message
- [ ] DoctorService (and similar) created for API calls
- [ ] Login component built
- [ ] App routing configured with lazy loading
- [ ] CoreModule and SharedModule setup
- [ ] Development server running
- [ ] Backend and frontend connected and communicating

---

## 🔄 Development Workflow

From here, you'll:

1. **Build Auth Module** (Login/Register pages)
2. **Build Nexus Direct Module** (Doctors, Patients, Appointments, Messages)
3. **Create Shared Components** (Header, Sidebar, Pagination)
4. **Add state management** (NgRx when complexity grows)
5. **Style with SCSS/Angular Material**
6. **Build remaining modules** (Connect, Proxy, Facility, Urgent, Learn, Companion)

Each module follows the same pattern:
- Create routing module
- Create feature module
- Create pages (routed components)
- Create services for API calls
- Create shared components

---

## 💡 Tips for VS Code Development

**VS Code Extensions to Install**:
```
- Angular Language Service
- Angular Snippets
- Thunder Client (for API testing)
- Prettier (code formatter)
- ESLint
```

**Useful VS Code Commands**:
```
Ctrl+Shift+P → "Angular: Generate Component" (auto-generate components)
Ctrl+` → Toggle terminal
Ctrl+Shift+~ → New terminal
```

**Keep Terminal Ready**:
- Terminal 1: `ng serve` (Angular dev server)
- Terminal 2: `docker compose logs -f` (Watch backend logs)
- Terminal 3: Git/npm commands as needed

This setup ensures you can watch both frontend and backend in real-time!
