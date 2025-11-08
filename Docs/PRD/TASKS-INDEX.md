# InvoiceMe - Tasks Index

**Detailed Execution Guides for All 15 Phases**

---

## Status: In Progress

✅ **Phase 1 Tasks** - Complete (4-6 hours)
✅ **Phase 2 Tasks** - Complete (4-6 hours)
🔄 **Phases 3-15** - In Progress

---

## How to Use These Task Guides

Each `Phase-XX-Tasks.md` file provides:

1. **Step-by-step terminal commands** - Copy-paste ready
2. **Expected outputs** - Verify each step succeeded
3. **Verification checklists** - Confirm completion
4. **Troubleshooting sections** - Common issues and fixes
5. **Git commits** - Track progress

### Execution Format

Each task follows this structure:
```
Task X.Y: Task Name
  Step X.Y.1: Subtask
    ```bash
    # Exact command to run
    command here
    ```
    **Expected Output:**
    ```
    what you should see
    ```
  Step X.Y.2: Next subtask
    ...
```

---

## Complete Task Guide List

### **Foundation** (Days 1-2, 8-12 hours)

#### ✅ [Phase-01-Tasks.md](Phase-01-Tasks.md) - Project Setup & Infrastructure
**Time:** 4-6 hours | **Status:** Complete

**What You'll Build:**
- Spring Boot backend project structure
- Next.js frontend with TypeScript
- PostgreSQL database setup
- Environment variables configuration
- Basic security with demo credentials
- Git repository initialization

**Key Commands:**
- `./mvnw clean compile` - Build backend
- `npm run dev` - Start frontend
- `createdb invoiceme` - Create database
- `git commit` - Save progress

---

#### ✅ [Phase-02-Tasks.md](Phase-02-Tasks.md) - Domain Model & Database Schema
**Time:** 4-6 hours | **Status:** Complete

**What You'll Build:**
- Flyway migration V1__initial_schema.sql
- Customer entity with Address value object
- Invoice entity with LineItem (state machine)
- Payment entity with validation
- All repository interfaces
- Domain unit tests

**Key Commands:**
- `./mvnw spring-boot:run` - Run migrations
- `psql -d invoiceme -c "\dt"` - Verify tables
- `./mvnw test` - Run tests

---

### **Backend Core** (Days 2-3, 20-26 hours)

#### 🔄 [Phase-03-Tasks.md](Phase-03-Tasks.md) - Customer Management (CQRS + VSA)
**Time:** 6-8 hours | **Status:** Not Started

**What You'll Build:**
- CreateCustomer command + handler + validator
- UpdateCustomer command + handler
- DeleteCustomer command with business rules
- Customer queries (GetById, ListAll)
- REST controller with all endpoints
- Unit and integration tests

**Key Deliverables:**
- `/api/customers` endpoints working
- Customer deletion blocked with active invoices
- Soft delete implemented

---

#### 🔄 [Phase-04-Tasks.md](Phase-04-Tasks.md) - Invoice Management (CQRS + VSA)
**Time:** 10-12 hours | **Status:** Not Started

**What You'll Build:**
- CreateInvoice, UpdateInvoice, SendInvoice commands
- CancelInvoice, MarkAsPaid commands
- Invoice queries (all types)
- Invoice state machine validation
- REST controller
- State transition tests

**Key Deliverables:**
- Complete invoice lifecycle (Draft → Sent → Paid)
- Payment link generation
- Cancellation with reasons

---

#### 🔄 [Phase-05-Tasks.md](Phase-05-Tasks.md) - Payment Processing (CQRS + VSA)
**Time:** 4-6 hours | **Status:** Not Started

**What You'll Build:**
- RecordPayment command with idempotency
- Payment validation (overpayment, partial payments)
- Payment queries
- Public payment link endpoint
- Idempotency tests

**Key Deliverables:**
- `/api/payments` endpoints
- Client-side payment ID for idempotency
- Automatic invoice status updates

---

### **AI Features** (Days 3-4, 14-18 hours)

#### 🔄 [Phase-06-Tasks.md](Phase-06-Tasks.md) - AI Email Reminder System
**Time:** 6-8 hours | **Status:** Not Started

**What You'll Build:**
- OpenAI API client configuration
- GenerateEmailReminder handler
- Overdue invoice scheduler (cron)
- Reminder frequency management
- Mock email sending

**Key Deliverables:**
- AI-generated overdue reminders
- Scheduled daily checks
- "Remind Me Later" options

---

#### 🔄 [Phase-07-Tasks.md](Phase-07-Tasks.md) - AI Chat Assistant
**Time:** 8-10 hours | **Status:** Not Started

**What You'll Build:**
- 7 OpenAI function definitions
- Backend query functions
- ProcessChatQuery handler
- Function calling orchestration
- Chat REST endpoint

**Key Deliverables:**
- Conversational AI assistant
- Natural language queries
- Context-aware follow-ups

---

### **Frontend** (Days 4-5, 22-30 hours)

#### 🔄 [Phase-08-Tasks.md](Phase-08-Tasks.md) - Customer Management UI
**Time:** 4-6 hours | **Status:** Not Started

**What You'll Build:**
- Customer list page with search
- Create/edit customer form
- Delete confirmation modal
- API integration

---

#### 🔄 [Phase-09-Tasks.md](Phase-09-Tasks.md) - Invoice Management UI
**Time:** 10-12 hours | **Status:** Not Started

**What You'll Build:**
- Invoice dashboard with statistics
- Invoice list with status filtering
- Invoice creation form with dynamic line items
- Invoice detail view with actions
- Send invoice with AI email preview
- Cancel invoice modal
- Overdue invoices section

---

#### 🔄 [Phase-10-Tasks.md](Phase-10-Tasks.md) - Payment UI
**Time:** 4-6 hours | **Status:** Not Started

**What You'll Build:**
- Public payment link page (no auth)
- Payment form with validation
- Payment confirmation page
- Payment history view

---

#### 🔄 [Phase-11-Tasks.md](Phase-11-Tasks.md) - AI Chat UI
**Time:** 4-6 hours | **Status:** Not Started

**What You'll Build:**
- Floating chat bubble
- Expandable chat window
- Message sending/response handling
- Typing indicator

---

### **Quality & Delivery** (Days 5-7, 20-28 hours)

#### 🔄 [Phase-12-Tasks.md](Phase-12-Tasks.md) - Integration Testing
**Time:** 6-8 hours | **Status:** Not Started

**What You'll Build:**
- End-to-end Customer-Invoice-Payment test
- AI email generation integration test
- Scheduler integration test
- Payment idempotency test
- Coverage report (target 80%+)

---

#### 🔄 [Phase-13-Tasks.md](Phase-13-Tasks.md) - Mockup Data & Demo
**Time:** 4-6 hours | **Status:** Not Started

**What You'll Build:**
- mockup-data.sql (10 customers, 25 invoices)
- Data loading script
- Manual testing scenarios
- Edge case validation

---

#### 🔄 [Phase-14-Tasks.md](Phase-14-Tasks.md) - Documentation
**Time:** 6-8 hours | **Status:** Not Started

**What You'll Build:**
- Main README with setup instructions
- Architecture documentation (DDD/CQRS/VSA)
- AI tools usage documentation
- Test results documentation
- Deployment diagrams

---

#### 🔄 [Phase-15-Tasks.md](Phase-15-Tasks.md) - Demo & Delivery
**Time:** 4-6 hours | **Status:** Not Started

**What You'll Build:**
- 10-15 minute demo video
- Final code review and cleanup
- Deliverables verification
- Repository submission ready

---

## Quick Reference

### Daily Progress Tracker

**Day 1:** Phases 1-2 (Foundation)
- [ ] Phase 1: Project Setup (4-6 hrs)
- [ ] Phase 2: Domain Model (4-6 hrs)

**Day 2:** Phases 3-5 (Backend Core)
- [ ] Phase 3: Customer Management (6-8 hrs)
- [ ] Phase 4: Invoice Management (10-12 hrs)

**Day 3:** Phase 5 + AI Start
- [ ] Phase 5: Payment Processing (4-6 hrs)
- [ ] Phase 6: AI Email Reminder (6-8 hrs)

**Day 4:** AI + Frontend Start
- [ ] Phase 7: AI Chat Assistant (8-10 hrs)
- [ ] Phase 8: Customer UI (4-6 hrs)

**Day 5:** Frontend Continued
- [ ] Phase 9: Invoice UI (10-12 hrs)
- [ ] Phase 10: Payment UI (4-6 hrs)

**Day 6:** Frontend Finish + Testing
- [ ] Phase 11: AI Chat UI (4-6 hrs)
- [ ] Phase 12: Integration Testing (6-8 hrs)

**Day 7:** Demo & Delivery
- [ ] Phase 13: Mockup Data (4-6 hrs)
- [ ] Phase 14: Documentation (6-8 hrs)
- [ ] Phase 15: Demo & Delivery (4-6 hrs)

---

## Commands Quick Reference

### Backend
```bash
cd ~/dev/Gauntlet/Invoice_AI/backend

# Build
./mvnw clean install

# Run
./mvnw spring-boot:run

# Test
./mvnw test

# Coverage
./mvnw test jacoco:report
```

### Frontend
```bash
cd ~/dev/Gauntlet/Invoice_AI/frontend

# Install
npm install

# Dev
npm run dev

# Build
npm run build

# Production
npm start
```

### Database
```bash
# Create database
createdb invoiceme

# Connect
psql -d invoiceme

# List tables
psql -d invoiceme -c "\dt"

# Run SQL file
psql -d invoiceme -f path/to/file.sql
```

### Git
```bash
# Status
git status

# Add all
git add .

# Commit
git commit -m "Phase X: Description"

# View log
git log --oneline
```

---

## Success Criteria Tracking

Track your progress through all phases:

### Technical Excellence
- [ ] Clean CQRS separation (Phases 3-7)
- [ ] Rich domain models (Phase 2)
- [ ] Vertical slice organization (Phases 3-7)
- [ ] API response times < 200ms (Phase 12)
- [ ] 80%+ test coverage (Phase 12)

### Feature Completeness
- [ ] Customer CRUD (Phase 3)
- [ ] Invoice lifecycle (Phase 4)
- [ ] Payment processing (Phase 5)
- [ ] AI email reminders (Phase 6)
- [ ] AI chat assistant (Phase 7)
- [ ] Frontend UI (Phases 8-11)

### Deliverables
- [ ] Complete documentation (Phase 14)
- [ ] Demo video (Phase 15)
- [ ] Clean repository (Phase 15)

---

## Troubleshooting Guide

### Backend Won't Start
1. Check Java version: `java -version` (need 17+)
2. Check database: `psql -d invoiceme`
3. Clean build: `./mvnw clean install -U`
4. Check logs in console output

### Frontend Won't Start
1. Delete node_modules: `rm -rf node_modules`
2. Reinstall: `npm install`
3. Check port: `lsof -i :3000`
4. Clear Next.js cache: `rm -rf .next`

### Database Issues
1. Check PostgreSQL running: `brew services list`
2. Restart: `brew services restart postgresql@15`
3. Verify database exists: `psql -l | grep invoiceme`

### Tests Failing
1. Check test database connection
2. Run single test: `./mvnw test -Dtest=ClassName`
3. Check for port conflicts
4. Verify H2 or test DB configured

---

## Next Steps

1. **If Starting Fresh:** Begin with [Phase-01-Tasks.md](Phase-01-Tasks.md)

2. **If Phase 1-2 Complete:** Continue with [Phase-03-Tasks.md](Phase-03-Tasks.md)

3. **If Resuming:** Check your last git commit and start the next phase

---

**Total Estimated Time: 84-114 hours (5-7 days full-time)**

**Status:** 2/15 phases complete
**Remaining:** Phases 3-15 execution guides to be created
