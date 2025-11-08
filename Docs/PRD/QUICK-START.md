# InvoiceMe - Quick Start Guide

**Get started building the AI-assisted ERP system in 5 minutes**

## What You Have

✅ **Complete PRD** (63KB) - Full product specification
✅ **Implementation Roadmap** - Master guide with all phases
✅ **10 Phase Documents** - Step-by-step implementation guides with code examples
✅ **Total Documentation** - 226KB of detailed technical guidance

## 5-Minute Quick Start

### Step 1: Understand the Project (2 min)

**Read This First:** [00-Implementation-Roadmap.md](00-Implementation-Roadmap.md)

**Quick Summary:**
- Build an invoicing system with AI features
- Demonstrate DDD + CQRS + VSA architecture
- Use Spring Boot (backend) + Next.js (frontend)
- Integrate OpenAI for smart features
- Complete in 5-7 days

### Step 2: Review Requirements (2 min)

**Skim:** [PRD.md](PRD.md) - Focus on:
- Section 1: Project Overview
- Section 2: Core Functional Requirements
- Section 3: AI-Driven Features

**Key Features:**
- ✅ Customer management
- ✅ Invoice lifecycle (Draft → Sent → Paid)
- ✅ Payment processing
- ✅ AI overdue reminders
- ✅ AI chat assistant

### Step 3: Start Building (1 min)

**Begin Here:** [Phase-01-Project-Setup.md](Phase-01-Project-Setup.md)

Follow the phases in order:
1. Project Setup (4-6 hours)
2. Domain Model (4-6 hours)
3. Customer Management (6-8 hours)
4. Invoice Management (10-12 hours)
5. Payment Processing (4-6 hours)
6. AI Email Reminder (6-8 hours)
7. AI Chat Assistant (8-10 hours)
8-11. Frontend UI (22-30 hours)
12-15. Testing & Delivery (20-28 hours)

## One-Page Architecture

```
┌─────────────────────────────────────────────────────┐
│                   INVOICEME SYSTEM                  │
├─────────────────────────────────────────────────────┤
│                                                      │
│  FRONTEND (Next.js 14 + TypeScript + Tailwind)     │
│  ├── Customer Management UI                         │
│  ├── Invoice Lifecycle UI                           │
│  ├── Payment UI (Public + Authenticated)            │
│  └── AI Chat Assistant UI                           │
│                                                      │
├─────────────────────────────────────────────────────┤
│                                                      │
│  BACKEND (Spring Boot 3 + Java 17 + PostgreSQL)    │
│                                                      │
│  ┌──────────────────────────────────────┐          │
│  │   Domain Layer (DDD)                 │          │
│  │   ├── Customer (Entity + Address VO) │          │
│  │   ├── Invoice (Aggregate Root)       │          │
│  │   │   └── LineItem (Entity)          │          │
│  │   └── Payment (Entity + Money VO)    │          │
│  └──────────────────────────────────────┘          │
│                                                      │
│  ┌──────────────────────────────────────┐          │
│  │   Application Layer (CQRS + VSA)     │          │
│  │                                       │          │
│  │   Customers/                          │          │
│  │   ├── CreateCustomer/                 │          │
│  │   │   ├── Command                     │          │
│  │   │   ├── Handler                     │          │
│  │   │   └── Validator                   │          │
│  │   ├── DeleteCustomer/                 │          │
│  │   └── GetCustomer/                    │          │
│  │                                       │          │
│  │   Invoices/                           │          │
│  │   ├── CreateInvoice/                  │          │
│  │   ├── SendInvoice/                    │          │
│  │   ├── CancelInvoice/                  │          │
│  │   └── GetInvoice/                     │          │
│  │                                       │          │
│  │   Payments/                           │          │
│  │   ├── RecordPayment/                  │          │
│  │   └── GetPaymentHistory/              │          │
│  │                                       │          │
│  │   AI/                                 │          │
│  │   ├── GenerateEmailReminder/          │          │
│  │   └── ProcessChatQuery/               │          │
│  └──────────────────────────────────────┘          │
│                                                      │
│  ┌──────────────────────────────────────┐          │
│  │   Infrastructure Layer                │          │
│  │   ├── Repositories (JPA)              │          │
│  │   ├── OpenAI Service                  │          │
│  │   └── Scheduler (Cron)                │          │
│  └──────────────────────────────────────┘          │
│                                                      │
│  ┌──────────────────────────────────────┐          │
│  │   API Layer (REST)                    │          │
│  │   ├── CustomerController              │          │
│  │   ├── InvoiceController               │          │
│  │   ├── PaymentController               │          │
│  │   └── AIController                    │          │
│  └──────────────────────────────────────┘          │
│                                                      │
├─────────────────────────────────────────────────────┤
│                                                      │
│  EXTERNAL SERVICES                                  │
│  ├── OpenAI API (GPT-4o-mini)                      │
│  └── PostgreSQL Database                            │
│                                                      │
└─────────────────────────────────────────────────────┘
```

## Key Patterns Explained (30 seconds each)

### Domain-Driven Design (DDD)
- **Entities:** Customer, Invoice, Payment have IDs and lifecycle
- **Value Objects:** Address, Money are immutable with no ID
- **Aggregates:** Invoice contains LineItems, Invoice is the root
- **Rich Models:** Business logic lives IN the domain objects

### CQRS (Command Query Responsibility Segregation)
- **Commands:** Write operations (CreateInvoice, RecordPayment)
- **Queries:** Read operations (GetInvoice, ListCustomers)
- **Separation:** Different code paths, optimized differently
- **DTOs:** Queries return simple data transfer objects

### Vertical Slice Architecture (VSA)
- **By Feature:** Group all code for one feature together
- **Self-Contained:** Each slice has command/query/handler/tests
- **Not Layers:** Don't organize by Controller/Service/Repository
- **Find Quickly:** All CreateCustomer code in CreateCustomer/ folder

## Technology Cheat Sheet

### Backend Commands
```bash
# Build
./mvnw clean install

# Run
./mvnw spring-boot:run

# Test
./mvnw test

# Test with coverage
./mvnw test jacoco:report
```

### Frontend Commands
```bash
# Install
npm install

# Dev server
npm run dev

# Build
npm run build

# Production
npm start
```

### Database Commands
```bash
# Create database
createdb invoiceme

# Run migrations (automatic on startup)
# Flyway executes db/migration/*.sql

# Load mockup data
psql -d invoiceme -f database/mockup-data.sql
```

## Critical Files Reference

### Backend Structure
```
backend/src/main/java/com/invoiceme/
├── domain/                    # Entities, Value Objects
│   ├── customer/Customer.java
│   ├── invoice/Invoice.java
│   └── payment/Payment.java
│
├── application/               # Commands, Queries, Handlers
│   ├── customers/
│   │   ├── CreateCustomer/
│   │   │   ├── CreateCustomerCommand.java
│   │   │   ├── CreateCustomerHandler.java
│   │   │   └── CreateCustomerValidator.java
│   │   └── GetCustomer/
│   │       └── GetCustomerHandler.java
│   └── invoices/...
│
├── infrastructure/            # Repositories, External Services
│   ├── persistence/
│   └── ai/OpenAIService.java
│
└── api/                       # REST Controllers
    └── CustomerController.java

backend/src/main/resources/
├── application.properties
└── db/migration/
    └── V1__initial_schema.sql
```

### Frontend Structure
```
frontend/src/
├── app/                       # Next.js App Router
│   ├── customers/
│   │   ├── page.tsx          # List customers
│   │   └── [id]/page.tsx     # Customer detail
│   ├── invoices/
│   └── pay/[link]/page.tsx   # Public payment page
│
├── components/                # Reusable components
│   ├── ui/                    # shadcn/ui components
│   ├── customers/
│   └── invoices/
│
├── lib/
│   └── api.ts                 # API client
│
└── types/
    ├── customer.ts
    └── invoice.ts
```

## Daily Progress Checklist

### Day 1 (8-12 hours)
- [ ] Complete Phase 1: Project Setup
- [ ] Complete Phase 2: Domain Model
- [ ] Start Phase 3: Customer Management

### Day 2 (8-12 hours)
- [ ] Complete Phase 3: Customer Management
- [ ] Complete Phase 4: Invoice Management
- [ ] Complete Phase 5: Payment Processing

### Day 3 (8-12 hours)
- [ ] Complete Phase 6: AI Email Reminder
- [ ] Complete Phase 7: AI Chat Assistant
- [ ] Start Phase 8: Customer UI

### Day 4 (8-12 hours)
- [ ] Complete Phase 8: Customer UI
- [ ] Complete Phase 9: Invoice UI
- [ ] Start Phase 10: Payment UI

### Day 5 (8-12 hours)
- [ ] Complete Phase 10: Payment UI
- [ ] Complete Phase 11: AI Chat UI
- [ ] Start Phase 12: Integration Testing

### Day 6 (8-12 hours)
- [ ] Complete Phase 12: Integration Testing
- [ ] Complete Phase 13: Mockup Data
- [ ] Start Phase 14: Documentation

### Day 7 (8-12 hours)
- [ ] Complete Phase 14: Documentation
- [ ] Complete Phase 15: Demo & Delivery
- [ ] Final review and submission

## Common Pitfalls to Avoid

❌ **Don't skip Phase 1** - Proper setup saves hours later
❌ **Don't mix layers** - Keep domain pure, no infrastructure dependencies
❌ **Don't expose entities** - Always use DTOs for API responses
❌ **Don't skip tests** - Write tests as you go, not at the end
❌ **Don't hardcode values** - Use environment variables
❌ **Don't ignore the PRD** - It has critical business rules

## Success Signals

✅ **You're doing well if:**
- Code organized by feature, not layer
- Commands and queries clearly separated
- Each phase checklist completed before moving on
- Tests passing as you build
- Can explain DDD/CQRS/VSA to someone else

## Emergency Reference

**Stuck? Check these in order:**

1. **Current Phase Document** - Re-read the specific phase you're on
2. **Main PRD** - Look up the business rules in Section 2
3. **Implementation Roadmap** - Review dependencies and prerequisites
4. **Code Examples** - Each phase has working code templates
5. **Previous Phases** - Did you complete all verification checklist items?

## Environment Variables Quick Reference

### Backend (.env)
```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/invoiceme
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres
OPENAI_API_KEY=sk-your-key-here
OPENAI_MODEL=gpt-4o-mini
JWT_SECRET=change-me-in-production
```

### Frontend (.env.local)
```bash
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

## Demo Credentials

```
Username: demo
Password: password
```

## Next Steps

1. **Read:** [00-Implementation-Roadmap.md](00-Implementation-Roadmap.md) (5 min)
2. **Skim:** [PRD.md](PRD.md) sections 1-3 (10 min)
3. **Start:** [Phase-01-Project-Setup.md](Phase-01-Project-Setup.md) (4-6 hours)
4. **Build:** Follow phases 2-15 in order
5. **Ship:** Demo video + complete repository

---

**You're ready to build! Start with Phase 1 now.**

Good luck! 🚀
