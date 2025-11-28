# 🖥️ Commands Reference (VS Code Terminal)

Quick reference for common commands you'll use while developing in VS Code.

---

## 📁 Navigation Commands

```bash
# Navigate to backend directory
cd /Users/cosy/Documents/CareNexus/direct

# Navigate to frontend directory (once created)
cd /Users/cosy/Documents/CareNexus/frontend

# Show current directory
pwd

# List files
ls -la

# Clear terminal
clear (Mac/Linux) or cls (Windows)
```

---

## 🐳 Docker Commands (Backend)

### Start Services

```bash
# Navigate to backend directory first
cd /Users/cosy/Documents/CareNexus/direct

# Build and start all services (MySQL + Direct Service)
docker compose up -d --build

# Start services without rebuilding
docker compose up -d

# Start services in foreground (see logs live)
docker compose up
```

**What `-d` means**: Detached mode (runs in background)

### Monitor Services

```bash
# View running containers
docker compose ps

# View logs for all services
docker compose logs -f

# View logs for specific service
docker compose logs -f direct-service
docker compose logs -f mysql-db

# View last 100 lines of logs
docker compose logs -f --tail=100

# View logs with timestamps
docker compose logs -f --timestamps
```

### Stop Services

```bash
# Stop all services (keeps data)
docker compose stop

# Stop specific service
docker compose stop direct-service

# Stop and remove containers (data persists in volumes)
docker compose down

# Stop and remove everything including volumes (DATA LOSS!)
docker compose down -v  # ⚠️ Be careful!
```

### Rebuild & Debug

```bash
# Rebuild everything from scratch
docker compose build --no-cache
docker compose up -d

# Restart a service
docker compose restart direct-service

# Rebuild just one service
docker compose build direct-service --no-cache

# View service details
docker compose ps --services
```

### Database Access

```bash
# Connect to MySQL from command line
docker compose exec mysql-db mysql -u appuser -p carenexus_direct
# Password: apppassword

# Or access via Adminer web UI
# Visit: http://localhost:8082
# Server: mysql-db
# User: appuser
# Password: apppassword
# Database: carenexus_direct
```

### Health Checks

```bash
# Check if services are healthy
docker compose ps

# If direct-service is not healthy, restart it
docker compose restart direct-service

# Wait for MySQL to be ready before running tests
docker compose logs mysql-db | grep "ready for connections"
```

---

## 🔨 Maven Commands (Backend Build)

### Build Project

```bash
# Build project (compile + package)
mvn clean package

# Build without running tests
mvn clean package -DskipTests

# Compile only
mvn compile

# Show dependency tree
mvn dependency:tree
```

### Run Tests

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=DoctorServiceTest

# Run with coverage
mvn clean test jacoco:report
```

### Clean & Reset

```bash
# Clean all compiled files
mvn clean

# Clean and rebuild
mvn clean install
```

### Package

```bash
# Create JAR file
mvn package

# Create JAR and skip tests
mvn package -DskipTests
```

---

## 📦 NPM Commands (Frontend)

### Project Setup

```bash
# Navigate to frontend directory
cd /Users/cosy/Documents/CareNexus/frontend

# Install all dependencies
npm install

# Install specific package
npm install @angular/material

# Install development dependency
npm install --save-dev typescript

# Update dependencies
npm update

# Check for outdated packages
npm outdated
```

### Development Server

```bash
# Start development server
ng serve

# Start with auto-open in browser
ng serve --open

# Start on different port (default 4200)
ng serve --port 4300

# Disable browser auto-open
ng serve --open=false

# Build with optimization
ng serve --configuration development
```

### Generation Commands

```bash
# Generate component
ng generate component modules/nexus-direct/pages/dashboard

# Generate service
ng generate service core/services/auth

# Generate module
ng generate module modules/auth/auth --routing

# Generate guard
ng generate guard core/guards/auth

# Generate interceptor
ng generate interceptor core/interceptors/jwt
```

### Build for Production

```bash
# Build for production
ng build --configuration production

# Build with specific environment
ng build --configuration development

# Preview production build locally
npm install -g http-server
http-server dist/frontend -p 8000
```

### Testing

```bash
# Run unit tests
ng test

# Run tests with coverage
ng test --code-coverage

# Run e2e tests
ng e2e

# Run specific test file
ng test --include='**/auth.service.spec.ts'
```

### Linting & Formatting

```bash
# Lint code
ng lint

# Fix linting issues
ng lint --fix

# Format code with Prettier
npx prettier --write .
```

### Performance & Analysis

```bash
# Build stats
ng build --stats-json
npx webpack-bundle-analyzer dist/frontend/stats.json

# Check bundle size
ng build --prod --stats-json

# Angular update check
ng update @angular/cli @angular/core
```

---

## 🔗 Git Commands (Version Control)

### Basic Operations

```bash
# Check status
git status

# Add all changes
git add .

# Add specific file
git add src/app/app.component.ts

# Commit changes
git commit -m "feat: add doctor search feature"

# View recent commits
git log --oneline -10

# Push to remote
git push origin main

# Pull from remote
git pull origin main
```

### Branching

```bash
# Create new branch
git checkout -b feature/doctor-search

# Switch to branch
git checkout main

# List branches
git branch -a

# Delete local branch
git branch -d feature/doctor-search

# Delete remote branch
git push origin --delete feature/doctor-search

# Merge branch to main
git checkout main
git merge feature/doctor-search
```

### Viewing Changes

```bash
# View unstaged changes
git diff

# View staged changes
git diff --staged

# View changes in specific file
git diff src/app/app.component.ts

# View changes between branches
git diff main feature/doctor-search
```

### Undoing Changes

```bash
# Undo unstaged changes
git checkout -- src/app/app.component.ts

# Undo all unstaged changes
git checkout -- .

# Remove staged changes
git reset HEAD src/app/app.component.ts

# Reset to previous commit (keeps files)
git reset --soft HEAD~1

# Reset to previous commit (loses changes)
git reset --hard HEAD~1
```

---

## 🧪 Testing Commands

### Backend Tests

```bash
# Run all backend tests
cd /Users/cosy/Documents/CareNexus/direct
mvn test

# Run specific test class
mvn test -Dtest=DoctorServiceTest

# Run with verbose output
mvn test -X

# Run and generate report
mvn test surefire-report:report
```

### Frontend Tests

```bash
# Run Angular unit tests
ng test

# Run with specific browser
ng test --browsers=Chrome

# Run once and exit (CI mode)
ng test --watch=false

# Run with coverage
ng test --code-coverage --watch=false
```

### Integration Testing

```bash
# Test backend API with curl
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Test User","email":"test@test.com","password":"password123","role":"DOCTOR"}'

# Test with Thunder Client (VS Code extension)
# Or use Postman, Insomnia, etc.
```

---

## 🌐 API Testing (curl)

### Authentication

```bash
# Register user
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName":"Dr. John",
    "email":"john@test.com",
    "password":"pass123",
    "role":"DOCTOR"
  }'

# Login
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email":"john@test.com",
    "password":"pass123"
  }'
```

### Doctors API (with JWT)

```bash
# Replace TOKEN with actual JWT token from login response

# Create doctor
curl -X POST http://localhost:8081/api/doctors \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name":"Dr. Jane Smith",
    "specialization":"Cardiology",
    "licenseNumber":"MED-123",
    "consultationFee":5000
  }'

# Get all doctors (paginated)
curl -X GET "http://localhost:8081/api/doctors/search/paginated?page=0&size=20" \
  -H "Authorization: Bearer TOKEN"

# Search doctors by name
curl -X GET "http://localhost:8081/api/doctors/search/by-name?name=Jane" \
  -H "Authorization: Bearer TOKEN"

# Get doctor by ID
curl -X GET http://localhost:8081/api/doctors/101 \
  -H "Authorization: Bearer TOKEN"

# Update doctor
curl -X PUT http://localhost:8081/api/doctors/101 \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name":"Dr. Jane Smith Updated",
    "specialization":"Cardiology",
    "consultationFee":5500
  }'

# Delete doctor
curl -X DELETE http://localhost:8081/api/doctors/101 \
  -H "Authorization: Bearer TOKEN"
```

---

## 🛠️ VS Code Specific

### Terminal Operations

```bash
# Open new terminal in VS Code
Ctrl + ` (backtick)

# Split terminal
Ctrl + Shift + 5

# Close terminal
Type 'exit' or Ctrl + D

# Clear terminal
clear
```

### Quick Actions

```bash
# Format code
Shift + Alt + F

# Find files
Ctrl + P

# Find in files
Ctrl + Shift + F

# Open command palette
Ctrl + Shift + P

# Go to line
Ctrl + G

# Go to definition
Ctrl + Click or F12

# Find references
Ctrl + Shift + H
```

---

## 📋 Multi-Terminal Setup for Development

**Recommended terminal layout**:

```
Terminal 1: Backend logs
$ docker compose logs -f --tail=100

Terminal 2: Frontend dev server
$ ng serve --open

Terminal 3: Git/Build commands
$ git status
```

**How to setup in VS Code**:
1. Open VS Code terminal (Ctrl + `)
2. Split terminal (Ctrl + Shift + 5)
3. In first: `cd backend && docker compose logs -f`
4. In second: `cd frontend && ng serve --open`
5. In third: Use for git/npm/other commands

---

## ⚡ Quick Command Snippets (Copy-Paste Ready)

**Quick Backend Setup**:
```bash
cd /Users/cosy/Documents/CareNexus/direct && \
docker compose down && \
docker compose build --no-cache && \
docker compose up -d && \
docker compose logs -f --tail=100
```

**Quick Frontend Setup**:
```bash
cd /Users/cosy/Documents/CareNexus/frontend && \
npm install && \
ng serve --open
```

**Full System Start**:
```bash
# Terminal 1
cd /Users/cosy/Documents/CareNexus/direct && docker compose up -d && docker compose logs -f

# Terminal 2
cd /Users/cosy/Documents/CareNexus/frontend && ng serve --open
```

**Test Everything**:
```bash
# Backend
cd /Users/cosy/Documents/CareNexus/direct && mvn clean test

# Frontend
cd /Users/cosy/Documents/CareNexus/frontend && ng test --watch=false
```

---

## 🔑 Key Points to Remember

1. **Always navigate to correct directory** before running commands
2. **Backend must be running** before testing frontend API calls
3. **Use `-f` flag** with `docker compose logs` to follow live output
4. **Use `--no-cache`** when rebuilding Docker to get fresh builds
5. **JWT token expires** - Get new one by logging in again
6. **Port conflicts** - If ports are in use, change them in .env
7. **Database persistence** - `docker compose down -v` will DELETE all data
8. **CORS enabled** - Frontend (4200) can call backend (8081)
9. **Keep multiple terminals open** - One for logs, one for dev server, one for commands
10. **Git before major changes** - Commit frequently to avoid losing work

---

## 📞 Troubleshooting Commands

```bash
# Backend won't start?
docker compose logs mysql-db
docker compose logs direct-service

# Port already in use?
lsof -i :8081  # Find what's using port 8081
kill -9 <PID>  # Kill the process

# Frontend not connecting to backend?
curl http://localhost:8081/api/auth/login  # Check if backend is running

# Clear npm cache and reinstall
rm -rf node_modules package-lock.json
npm install

# Clear Docker everything (⚠️ deletes data!)
docker system prune -a --volumes

# Check environment variables are loaded
docker compose config | grep JWT_SECRET
```

---

## 💡 Pro Tips

**Alias shortcuts** (add to ~/.zshrc or ~/.bashrc):
```bash
alias be='cd /Users/cosy/Documents/CareNexus/direct'
alias fe='cd /Users/cosy/Documents/CareNexus/frontend'
alias dc='docker compose'
alias dcup='docker compose up -d && docker compose logs -f'
alias dcdown='docker compose down'
alias dclogs='docker compose logs -f'
alias serve='ng serve --open'
```

Then you can just type:
```bash
be              # Go to backend
dcup            # Start all services
# In new terminal
fe              # Go to frontend
serve           # Start dev server
```

This makes your workflow much faster!
