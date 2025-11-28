# ⚡ Quick Start Guide (VS Code)

Everything you need to start developing CareNexus from VS Code console in 10 minutes.

---

## 🎯 What is CareNexus?

A **microservices healthcare platform** for Zimbabwe with 8 modules:
1. **Nexus Direct** ✅ (In-house doctors) - Currently building
2. **Nexus Connect** (External providers) - Coming next
3. **Nexus Proxy** (Remote caregiving)
4. **Nexus FacilityConnect** (Hospital integration)
5. **Nexus Urgent** (Emergency dispatch)
6. **Nexus Learn** (Education platform)
7. **Nexus Companion** (AI chatbot)
8. **Core Infrastructure** (Foundation)

**Status**: Backend Phase 1-5 complete, frontend not started, Phase 7 (microservices extraction) pending.

---

## 📂 Your Directory Structure

```
/Users/cosy/Documents/CareNexus/
├── direct/                           # Backend (Spring Boot)
│   ├── src/
│   ├── docker-compose.yml
│   ├── pom.xml
│   ├── Dockerfile
│   ├── .env
│   ├── SYSTEM_ARCHITECTURE.md        ⭐ READ THIS FIRST
│   ├── BACKEND_STRUCTURE.md
│   ├── COMMANDS_REFERENCE.md
│   ├── INTEGRATION_PATTERNS.md
│   └── FRONTEND_SETUP.md
│
└── frontend/                          # Frontend (Angular) - Will create
    ├── src/
    ├── angular.json
    ├── package.json
    └── ...
```

---

## 🚀 Step 1: Understand the System (5 min)

**Open in VS Code and read** (in this order):
1. `SYSTEM_ARCHITECTURE.md` - Understand all 8 modules and how they connect
2. `BACKEND_STRUCTURE.md` - See what API endpoints exist and how to use them
3. This file (`QUICK_START.md`) - Get started immediately

**Key Concepts**:
- **Multi-tenancy**: Users only see their own data (enforced by `userEmail` column)
- **JWT Tokens**: Every API request needs authorization header
- **Microservices**: Will eventually run as separate services (currently embedded)

---

## 🔥 Step 2: Start Backend (3 min)

### Check Backend is Ready

```bash
# Open Terminal 1 in VS Code (Ctrl + `)
cd /Users/cosy/Documents/CareNexus/direct

# See what's running
docker compose ps

# If nothing running, start it
docker compose up -d --build

# Watch the logs
docker compose logs -f --tail=50
```

**You should see**:
```
direct-service    | Started DirectApplication in X seconds
mysql-db          | ready for connections
```

**Access Points**:
- Backend API: `http://localhost:8081`
- MySQL Admin: `http://localhost:8082` (Adminer)
- MySQL Direct: `localhost:3307`

---

## 🎨 Step 3: Create Frontend Project (2 min)

### Generate Angular Project

```bash
# Open Terminal 2 (Ctrl + Shift + 5 to split)
cd /Users/cosy/Documents/CareNexus

# Create Angular project
ng new frontend --routing --style=scss --skip-git

# Navigate to frontend
cd frontend

# Install dependencies
npm install

# Start development server
ng serve --open
```

**Frontend will open**: `http://localhost:4200`

---

## 🧪 Step 4: Test Connection (Quick API Test)

### Verify Backend is Working

**In Terminal 3 (or use Thunder Client in VS Code)**:

```bash
# 1. Register a test user
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName":"Test Doctor",
    "email":"test@test.com",
    "password":"password123",
    "role":"DOCTOR"
  }'

# Response will include accessToken (copy this!)
# Example: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# 2. Login (get fresh token)
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email":"test@test.com",
    "password":"password123"
  }'

# 3. Use token to access protected endpoint
# Replace TOKEN with actual token from above
curl -X GET http://localhost:8081/api/doctors \
  -H "Authorization: Bearer TOKEN"

# Should return empty array: []
```

**✅ If this works**, backend is working perfectly!

---

## 📋 Running Checklist

After Step 4, you should have:

- [ ] Backend running on `http://localhost:8081`
- [ ] MySQL running on `localhost:3307`
- [ ] Frontend generating on `http://localhost:4200`
- [ ] Can register/login users
- [ ] Can call API endpoints with JWT token

---

## 🔨 Development Workflow

**Recommended Terminal Layout in VS Code**:

```
┌─────────────────────────────────────────┐
│ VS Code                                 │
├─────────────────────┬───────────────────┤
│ Terminal 1: Logs    │ Terminal 2: Dev   │
│                     │                   │
│ cd direct           │ cd frontend       │
│ docker logs -f      │ ng serve --open   │
├─────────────────────┴───────────────────┤
│ Terminal 3: Commands                    │
│ cd direct (or frontend) for git, etc    │
└─────────────────────────────────────────┘
```

**How to Open Multiple Terminals**:
1. Open Terminal: `Ctrl + `` (backtick)
2. Split Terminal: `Ctrl + Shift + 5`
3. Click terminal to activate it, type commands

---

## 📝 Your Next Tasks (In Order)

### Phase A: Frontend Setup (Today)
1. **Read** `FRONTEND_SETUP.md` for detailed instructions
2. **Create** Auth module (login/register pages)
3. **Build** Nexus Direct module (doctors/patients UI)
4. **Test** frontend can call backend APIs

### Phase B: Complete Frontend (This Week)
5. Add shared components (header, sidebar, pagination)
6. Add styling (SCSS + Angular Material)
7. Test complete flow: Register → Login → View Doctors → Schedule Appointment

### Phase C: Backend Phase 7 (Next Week)
8. Extract Auth Service as separate microservice
9. Setup inter-service REST communication
10. Configure Kafka for event-driven communication

### Phase D: New Backend Module (Ongoing)
11. Build Nexus Connect (provider matching)
12. Build Nexus Proxy (video calls)
13. Build other modules iteratively

---

## 💾 How to Stay Synced While Working in VS Code

**Keep These Files Open**:
1. `SYSTEM_ARCHITECTURE.md` - Reference while building
2. `BACKEND_STRUCTURE.md` - API endpoint reference
3. `COMMANDS_REFERENCE.md` - Copy-paste commands
4. `INTEGRATION_PATTERNS.md` - Pattern reference for new modules

**Workflow**:
```
┌─ Need to call API endpoint?
│  └─ Check BACKEND_STRUCTURE.md for exact endpoint
│
├─ Creating new module?
│  └─ Follow INTEGRATION_PATTERNS.md step-by-step
│
├─ Forgot a command?
│  └─ Check COMMANDS_REFERENCE.md quick copy-paste
│
└─ Understanding architecture?
   └─ Refer to SYSTEM_ARCHITECTURE.md
```

**Using in VS Code**:
- Open files in split view: `Ctrl + \`
- Keep reference docs on right, code on left
- Alt+Tab between editor and terminal

---

## 🐛 Quick Troubleshooting

### Backend won't start?
```bash
docker compose logs mysql-db
docker compose logs direct-service
# Check for errors, restart if needed
docker compose restart direct-service
```

### Frontend won't connect to backend?
```bash
# Check backend is running
curl http://localhost:8081/api/auth/login

# If not, restart backend
cd /Users/cosy/Documents/CareNexus/direct
docker compose up -d
```

### Port already in use?
```bash
# Find what's using port 8081
lsof -i :8081

# Kill the process
kill -9 <PID>

# Or change port in .env file
DIRECT_SERVICE_PORT=8082
```

### JWT token not working?
```bash
# Register fresh user to get new token
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"New User","email":"new@test.com","password":"password123","role":"DOCTOR"}'

# Use that token (tokens expire after 24 hours)
```

---

## 🎯 Key Things to Remember

1. **Backend runs on 8081**, Frontend on 4200 - they're separate
2. **Always include JWT token** in API requests (except /api/auth/*)
3. **All data is filtered by userEmail** - multi-tenancy enforcement
4. **Every user only sees their own data** - strict isolation
5. **Database persists between restarts** - data is safe
6. **Use COMMANDS_REFERENCE.md** - it has everything you need
7. **Read INTEGRATION_PATTERNS.md** when building new modules
8. **Keep terminals open** - monitor logs while developing

---

## 📞 Documentation Map

| Need | File |
|------|------|
| Understand system | SYSTEM_ARCHITECTURE.md |
| Know API endpoints | BACKEND_STRUCTURE.md |
| Copy command | COMMANDS_REFERENCE.md |
| Build new module | INTEGRATION_PATTERNS.md |
| Setup Angular | FRONTEND_SETUP.md |
| Get started quick | QUICK_START.md (this file) |

---

## 🚀 Starting Right Now

**Copy-paste this to start everything**:

```bash
# Terminal 1: Start backend and watch logs
cd /Users/cosy/Documents/CareNexus/direct
docker compose up -d --build
docker compose logs -f --tail=50

# Terminal 2: Start frontend (new terminal)
cd /Users/cosy/Documents/CareNexus/frontend
ng serve --open

# Terminal 3: Test API (new terminal)
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName":"Test User",
    "email":"test@test.com",
    "password":"password123",
    "role":"DOCTOR"
  }'
```

**What you'll see**:
- Terminal 1: Backend logs (real-time)
- Terminal 2: Angular compilation + browser opens at 4200
- Terminal 3: User registered response with JWT token

---

## ✅ Success Criteria

You're ready when:
- ✅ Backend running and responding to requests
- ✅ Frontend Angular server running at 4200
- ✅ Can register a user via API
- ✅ Can login and get JWT token
- ✅ Can call protected endpoint with token
- ✅ Multiple terminals open watching both frontend and backend
- ✅ All reference docs are open in VS Code

---

## 🎓 Learning Resources

While developing:
1. **Angular Docs**: https://angular.io/docs
2. **Spring Boot Docs**: https://spring.io/projects/spring-boot
3. **REST API Best Practices**: RESTful API design patterns used here
4. **JWT Guide**: https://jwt.io/
5. **Docker Docs**: https://docs.docker.com/

---

## 💡 Pro Tip: Use VS Code Aliases

Add to terminal startup to make commands faster:

```bash
# Add to ~/.zshrc (Mac) or ~/.bashrc (Linux)
alias be='cd /Users/cosy/Documents/CareNexus/direct'
alias fe='cd /Users/cosy/Documents/CareNexus/frontend'
alias dcup='docker compose up -d && docker compose logs -f'
alias serve='ng serve --open'
```

Then just type:
```bash
be && dcup        # Start backend
# In new terminal
fe && serve       # Start frontend
```

Much faster! ⚡

---

## 📞 You're Not Alone!

This guide has everything you need. When stuck:
1. **Check the relevant reference file** (COMMANDS_REFERENCE.md, BACKEND_STRUCTURE.md, etc.)
2. **Check troubleshooting section** above
3. **Look at existing code patterns** in `src/` directories
4. **Test with curl** to isolate problems

**Remember**: You've already built a complete working backend (Phases 1-5). You know this system. Now you're just extending it! 💪

---

## 🏁 Next Step: Start Building Frontend

Open `FRONTEND_SETUP.md` and follow the detailed guide. You'll be building the Angular Auth module in the next 30 minutes.

**Let's build something great! 🚀**
