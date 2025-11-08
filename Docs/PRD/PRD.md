# InvoiceMe - Complete Product Requirements Document (PRD) v2.0

## AI-Assisted Full-Stack ERP Assessment Project

**Document Version:** 2.0 Final  
**Last Updated:** November 8, 2025  
**Project Type:** Demo/Assessment Project with Production-Quality Architecture  
**Estimated Completion Time:** 5-7 days

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Core Functional Requirements](#2-core-functional-requirements)
3. [AI-Driven Features](#3-ai-driven-features)
4. [Architecture & Technical Stack](#4-architecture--technical-stack)
5. [Business Rules & Edge Cases](#5-business-rules--edge-cases)
6. [Testing Requirements](#6-testing-requirements)
7. [Deployment & Configuration](#7-deployment--configuration)
8. [Project Deliverables](#8-project-deliverables)
9. [Implementation Checklist](#9-implementation-checklist)

---

## 1. Project Overview

### 1.1 Project Goal

Build a small, production-quality, ERP-style invoicing system that explicitly demonstrates mastery of:

- **Domain-Driven Design (DDD)**
- **Command Query Responsibility Segregation (CQRS)**
- **Vertical Slice Architecture (VSA)**
- **AI-Assisted Development Tools**
- **Modern Spring Boot & React Best Practices**

### 1.2 Core Business Domains

The system concentrates on three primary domains:

- **Customers** - Client management and contact information
- **Invoices** - Invoice creation, lifecycle management, and line items
- **Payments** - Payment tracking and invoice balance reconciliation

### 1.3 Demo Context

**CRITICAL:** This project will be demonstrated using **mockup data**. The application must include:

- Sample customers (5-10 realistic customer profiles)
- Sample invoices in various states (draft, sent, paid, overdue)
- Sample payment history
- Pre-configured scenarios to demonstrate all features

---

## 2. Core Functional Requirements

### 2.1 Domain Model Overview

The system implements a clean separation between Commands (write operations) and Queries (read operations) per CQRS principles.

| Domain Entity | Commands (Write Operations)                                        | Queries (Read Operations)                           |
| ------------- | ------------------------------------------------------------------ | --------------------------------------------------- |
| **Customer**  | Create, Update, Delete Customer                                    | Get Customer by ID, List All Customers              |
| **Invoice**   | Create (Draft), Update, Mark as Sent, Mark as Paid, Cancel Invoice | Get Invoice by ID, List by Status, List by Customer |
| **Payment**   | Record Payment (linked to Invoice)                                 | Get Payment by ID, List Payments for Invoice        |

### 2.2 Customer Domain

#### 2.2.1 Customer Entity

**Required Fields:**

- Customer ID (UUID, auto-generated)
- Business Name (required)
- Primary Contact Name (required)
- Email Address (required, validated)
- Phone Number (optional)
- Billing Address (required)
  - Street Address
  - City
  - State/Province
  - Postal Code
  - Country

**Optional Fields:**

- Shipping Address (flag to enable if different from billing)
  - Same structure as billing address
  - Checkbox: "Use separate shipping address"

#### 2.2.2 Customer Commands

1. **Create Customer**

   - Validate all required fields
   - Ensure unique email address
   - Generate UUID
   - Timestamp: createdAt, updatedAt

2. **Update Customer**

   - Allow modification of all fields except ID
   - Update timestamp: updatedAt

3. **Delete Customer**
   - **Business Rule:** BLOCK deletion if customer has unpaid invoices
   - Pre-deletion checks:
     1. Check for unpaid invoices
     2. If unpaid invoices exist, require user to:
        - Mark invoices as paid, OR
        - Cancel invoices first
     3. Only allow deletion when all invoices are paid or cancelled
   - Implement as soft delete (mark as inactive) rather than hard delete

#### 2.2.3 Customer Queries

- Get Customer by ID
- List All Customers (active only)
- Search Customers by name or email

---

### 2.3 Invoice Domain

#### 2.3.1 Invoice Entity

**Core Fields:**

- Invoice ID (UUID, auto-generated)
- Invoice Number (auto-incremented, format: INV-YYYY-0001)
- Customer ID (foreign key, required)
- Issue Date (date, defaults to current date)
- Due Date (date, required)
- Status (enum: DRAFT, SENT, PAID, CANCELLED)
- Subtotal (calculated from line items)
- Tax Amount (optional, percentage or fixed)
- Total Amount (subtotal + tax)
- Amount Paid (running total of payments)
- Balance Remaining (total - amount paid)
- Notes (optional, internal notes)
- Terms & Conditions (optional, displayed on invoice)
- Payment Link (unique URL generated when status = SENT)
- Allows Partial Payment (boolean, default: false)
- Cancellation Reason (populated when cancelled)
- Timestamps: createdAt, updatedAt, sentAt, paidAt, cancelledAt

#### 2.3.2 Line Items (Embedded in Invoice)

Each invoice contains 1-n line items:

- Line Item ID (UUID)
- Description (required, max 500 chars)
- Quantity (required, decimal, min: 0.01)
- Unit Price (required, decimal, min: 0.00)
- Line Total (calculated: quantity × unit price)

**Business Rules:**

- Minimum 1 line item per invoice
- Line items can be added/edited only in DRAFT status
- Once invoice is SENT, line items are locked

#### 2.3.3 Invoice Lifecycle State Machine

```
┌─────────┐
│  DRAFT  │ ← Initial state when created
└────┬────┘
     │
     │ (Send Invoice Command)
     ↓
┌─────────┐
│  SENT   │ ← Invoice sent to customer, payment link active
└────┬────┘
     │
     │ (Record Payment Command)
     ↓
┌─────────┐
│  PAID   │ ← Final state, balance = 0
└─────────┘

     ↓ (Cancel Invoice Command - from any state)
┌───────────┐
│ CANCELLED │ ← Terminal state
└───────────┘
```

**State Transition Rules:**

1. **DRAFT → SENT:**

   - Requires: At least 1 line item, valid customer, due date set
   - Action: Generate payment link, set sentAt timestamp
   - Trigger: Email draft generation (user must approve)

2. **SENT → PAID:**

   - Requires: Balance remaining = 0
   - Action: Set paidAt timestamp
   - Trigger: Move to "Paid Invoices" section

3. **ANY STATE → CANCELLED:**

   - Requires: User-provided cancellation reason
   - Action: Set cancelledAt timestamp, record cancellation reason
   - If status was SENT: Generate cancellation email to customer (user must approve)
   - Payment link becomes inactive

4. **PAID → SENT (Undo Mistake):**
   - Allowed ONLY if payment was recorded in error
   - Requires: User confirmation
   - Action: Reverse payment transaction, return to SENT status

#### 2.3.4 Invoice Commands

**1. Create Invoice (Draft)**

- Initialize with DRAFT status
- Generate Invoice Number
- Set Issue Date to current date
- Require Customer ID
- Allow Due Date to be set (default: 30 days from issue)
- Initialize empty line items array

**2. Update Invoice (DRAFT only)**

- Allow modification of:
  - Customer (if no payments received)
  - Due Date
  - Line Items (add, edit, delete)
  - Tax settings
  - Notes and Terms
- Recalculate totals automatically

**3. Send Invoice (DRAFT → SENT)**

- Validate: Customer exists, at least 1 line item, due date set
- Generate unique payment link (UUID-based)
- Set sentAt timestamp
- **Trigger AI Feature:** Generate email draft with invoice details
- Update status to SENT
- Lock line items from editing

**4. Edit Sent Invoice**

- Create popup: "Editing this invoice will require re-sending to the customer"
- Allow modifications to line items
- On save: Trigger "Save and Resend to Customer" popup
- Generate new AI email draft for re-sending
- Update updatedAt timestamp

**5. Record Payment**

- Link payment to specific invoice via payment link
- Validate: Payment amount > 0
- Check: If partial payments disabled, amount must equal balance remaining
- Check: Payment amount ≤ balance remaining (prevent overpayment)
- Update: amountPaid and balanceRemaining
- If balance = 0: Transition to PAID status
- Idempotency: Use client-generated paymentId (UUID) to prevent duplicates

**6. Cancel Invoice**

- Available from any status
- Require cancellation reason selection:
  - Dropdown options:
    - "Wrong Amount"
    - "Wrong Customer"
    - "Cancelled Order"
    - "Needs More Information"
    - "Other" (requires text input for detailed reason)
- Set cancelledAt timestamp
- Record cancellation reason
- If status was SENT: Generate cancellation email to customer
- Deactivate payment link
- Update status to CANCELLED

**7. Mark as Paid (Manual Override)**

- Available only from SENT status
- Requires: User confirmation
- Action: Set amountPaid = total, balance = 0, status = PAID
- Use case: Payment received outside the system (check, wire transfer, etc.)

#### 2.3.5 Invoice Queries

**Primary Queries:**

- Get Invoice by ID (includes line items and payment history)
- List Invoices by Status (DRAFT, SENT, PAID, CANCELLED)
- List Invoices by Customer ID
- List Overdue Invoices (status = SENT, due date < current date)
- Get Invoice Statistics:
  - Total invoices by status
  - Total amount outstanding (sum of SENT invoices' balances)
  - Total overdue amount
  - Average days to payment

**Dashboard Queries:**

- Recent Invoices (last 10)
- Overdue Invoices Count
- Total Revenue (this month/year)
- Payment Activity (last 30 days)

---

### 2.4 Payment Domain

#### 2.4.1 Payment Entity

**Fields:**

- Payment ID (UUID, generated on client side for idempotency)
- Invoice ID (foreign key, required)
- Payment Amount (decimal, required)
- Payment Date (date, defaults to current)
- Payment Method (enum: CREDIT_CARD, BANK_TRANSFER, CHECK, CASH, OTHER)
- Transaction Reference (optional, for external payment IDs)
- Notes (optional)
- Timestamps: createdAt

#### 2.4.2 Payment Commands

**1. Record Payment**

- Validate: Invoice exists and is in SENT status
- Validate: Payment amount > 0
- Validate: Payment amount ≤ invoice balance remaining
- Check idempotency: If paymentId already exists, return existing payment
- Update invoice: amountPaid += payment amount, recalculate balance
- If balance = 0: Update invoice status to PAID
- Create payment record with timestamp

#### 2.4.3 Payment Queries

- Get Payment by ID
- List Payments for Invoice ID
- List All Payments (paginated)
- Get Payment History for Customer (across all invoices)

---

## 3. AI-Driven Features

### 3.1 AI-Assisted Overdue Invoice Reminder

#### 3.1.1 Feature Overview

Automatically generate personalized, professional email reminders for overdue invoices using OpenAI API.

#### 3.1.2 Trigger Mechanism

- **Scheduled Job:** Daily at midnight (user's local time)
- **Logic:** Query all invoices where:
  - Status = SENT
  - Due Date < Current Date
  - Last Reminder Date is NULL OR ≥ 1 day ago

#### 3.1.3 User Workflow

1. **Detection:** System identifies overdue invoice(s)
2. **Notification:** User sees indicator (badge, notification) in UI
3. **Bulk View:** User opens "Overdue Invoices" section
4. **Bulk Actions:**
   - "Generate Reminders for All" button
   - Or individual "Generate Reminder" per invoice
5. **AI Generation:** System calls OpenAI API to generate email draft
6. **Review Modal:** User reviews AI-generated email in popup
7. **Edit:** User can edit the email text directly
8. **Options:**
   - "Send Now" - User approves sending (mocked email via OAuth)
   - "Remind Me Later" dropdown:
     - Tomorrow
     - In 3 days
     - In 1 week
     - Don't remind me again
   - "Cancel"
9. **Tracking:** System records lastReminderSentDate

#### 3.1.4 AI Prompt Structure

**System Prompt:**

```
You are a professional accounts receivable assistant for a small business.
Your role is to generate polite, professional, yet friendly email reminders
for overdue invoices. The tone should encourage payment without being
aggressive or damaging the customer relationship.
```

**User Prompt Template:**

```
Generate an overdue invoice reminder email with the following details:

Customer Name: {customerName}
Invoice Number: {invoiceNumber}
Invoice Date: {issueDate}
Due Date: {dueDate}
Days Overdue: {daysOverdue}
Total Amount: ${totalAmount}
Amount Paid: ${amountPaid}
Balance Remaining: ${balanceRemaining}

Line Items Summary:
{lineItemsList}

Instructions:
- Use a friendly but professional tone
- Acknowledge the business relationship
- Clearly state the amount owed and how long it's been overdue
- Include a brief summary of what the invoice covers
- Provide the payment link: {paymentLink}
- Offer assistance if there are any questions or issues
- Close with a polite call to action
- Keep the email concise (under 200 words)

Generate ONLY the email body, no subject line.
```

#### 3.1.5 Email Mock-up Implementation

- **For Demo:** Display "Email sent via [user@company.com]" confirmation message
- **Technical Note:** Document OAuth integration approach (Gmail API, Microsoft Graph) for production
- **UI Mock:** Show email client UI with "From" field populated
- System logs "email sent" event but doesn't actually send

#### 3.1.6 Reminder Frequency Management

- Track `lastReminderSentDate` on invoice
- Respect user's "Remind Me Later" selections
- If user selects "Don't remind me again," set flag on invoice: `remindersSuppressed = true`
- Daily job skips invoices with active suppression or future reminder dates

---

### 3.2 AI Chat Assistant - "Virtual Accountant"

#### 3.2.1 Feature Overview

A persistent chat interface that allows users to query their invoicing data using natural language, powered by OpenAI's function calling.

#### 3.2.2 User Interface

- **Location:** Floating chat bubble icon (bottom-right corner)
- **Expandable:** Clicks opens chat window (300px × 500px)
- **Persistent:** Chat remains accessible across all pages
- **Visual Design:** Modern chat UI with:
  - User messages (right-aligned, blue)
  - AI responses (left-aligned, gray)
  - Typing indicator while processing
  - Input field with "Send" button

#### 3.2.3 Conversation Management

- **Session History:** Maintain conversation context within the session
- **Context Window:** Include last 10 messages for follow-up questions
- **Clear Chat:** Button to reset conversation
- **No Persistence:** For demo, chat history clears on page refresh (optional: localStorage for session persistence)

#### 3.2.4 Supported Query Types (Minimum Viable for Demo)

**Predefined Functions for OpenAI Function Calling:**

1. **getOverdueInvoices**

   - Parameters: `month?: string` (optional, format: "YYYY-MM")
   - Returns: Count of overdue invoices, list of invoice numbers, total overdue amount
   - Example: "How many invoices are overdue this month?"

2. **getTotalAmountOwed**

   - Parameters: None
   - Returns: Total balance across all SENT invoices
   - Example: "What's the total amount we're still owed?"

3. **getInvoicesByCustomer**

   - Parameters: `customerName: string`
   - Returns: List of invoices for specified customer with statuses and amounts
   - Example: "List all invoices for Acme Corp"

4. **getInvoicesByStatus**

   - Parameters: `status: 'draft' | 'sent' | 'paid' | 'cancelled'`
   - Returns: Count and total amount for specified status
   - Example: "How many invoices are in draft?"

5. **getPaymentHistory**

   - Parameters: `invoiceId?: string` (optional)
   - Returns: Recent payments with dates and amounts
   - Example: "Show me payments received this week"

6. **getCustomerSummary**

   - Parameters: `customerId: string`
   - Returns: Customer name, total invoices, total paid, total outstanding
   - Example: "Give me a summary for customer ID 12345"

7. **getInvoiceStatistics**
   - Parameters: `period?: 'month' | 'quarter' | 'year'` (optional)
   - Returns: Aggregated statistics (total revenue, average invoice amount, average days to payment)
   - Example: "What are my invoice stats for this quarter?"

#### 3.2.5 Implementation Flow

```
User Input: "How many invoices are overdue?"
     ↓
Backend: Send to OpenAI with function definitions
     ↓
OpenAI Response:
{
  "function_call": {
    "name": "getOverdueInvoices",
    "arguments": {}
  }
}
     ↓
Backend: Execute getOverdueInvoices() against database
     ↓
Backend: Return results to OpenAI with function result
     ↓
OpenAI Response: "You currently have 5 overdue invoices totaling $12,450.00.
These invoices are from customers ABC Corp ($5,000), XYZ Ltd ($3,200)..."
     ↓
Frontend: Display formatted response to user
```

#### 3.2.6 OpenAI Function Definitions (JSON Schema)

```json
{
  "name": "getOverdueInvoices",
  "description": "Get the count and details of overdue invoices, optionally filtered by month",
  "parameters": {
    "type": "object",
    "properties": {
      "month": {
        "type": "string",
        "description": "Optional month filter in YYYY-MM format, e.g., '2025-11'"
      }
    }
  }
}
```

(Similar definitions for all other functions)

#### 3.2.7 Security & Limitations

- **No Write Operations:** AI can ONLY query data, never modify
- **Prompt Injection Protection:**
  - Validate all function names against whitelist
  - Sanitize all user inputs
  - Never execute raw SQL from AI responses
- **Rate Limiting:** (Not required for demo, but document the approach)
- **Error Handling:** If AI is down, display: "The AI assistant is temporarily unavailable. Please try again later."
- **Graceful Degradation:** If function call fails, AI responds: "I'm having trouble accessing that data right now. Please try rephrasing your question."

#### 3.2.8 Example Conversation

```
User: "Hi, what's my current outstanding balance?"
AI: "Your current outstanding balance across all sent invoices is $45,230.00."

User: "Which customers owe the most?"
AI: "Here are your top customers by outstanding balance:
1. Acme Corp - $15,000.00 (2 invoices)
2. TechStart Inc - $12,450.00 (1 invoice)
3. Global Widgets - $8,900.00 (3 invoices)"

User: "What about last month?"
AI: "In October 2025, you had $38,500.00 outstanding at month-end, with 8 sent invoices."
```

---

## 4. Architecture & Technical Stack

### 4.1 Architectural Principles

#### 4.1.1 Domain-Driven Design (DDD)

- **Bounded Contexts:** Separate Customer, Invoice, and Payment domains
- **Entities:** Rich domain objects with business logic encapsulated
- **Value Objects:** Use for complex attributes (Address, Money, InvoiceNumber)
- **Aggregates:** Invoice as aggregate root containing LineItems
- **Domain Events:** (Optional) Emit events for:
  - InvoiceCreated
  - InvoiceSent
  - PaymentReceived
  - InvoicePaid
  - InvoiceCancelled

#### 4.1.2 Command Query Responsibility Segregation (CQRS)

**Implementation Strategy for Demo:**

- **Single Database:** PostgreSQL (logical separation, not physical)
- **Commands:** Handle write operations, enforce business rules
  - Located in: `com.invoiceme.application.commands`
  - Examples: `CreateInvoiceCommand`, `RecordPaymentCommand`
- **Queries:** Optimized for read operations, return DTOs
  - Located in: `com.invoiceme.application.queries`
  - Examples: `GetInvoiceByIdQuery`, `ListOverdueInvoicesQuery`
- **No Event Sourcing:** Not required for this scope
- **Consistency:** Immediate consistency (same DB, same transaction)

#### 4.1.3 Vertical Slice Architecture (VSA)

Organize code by feature/use case rather than technical layers.

**Project Structure:**

```
src/main/java/com/invoiceme/
├── domain/                    # Core domain models
│   ├── customer/
│   │   ├── Customer.java
│   │   ├── Address.java
│   ├── invoice/
│   │   ├── Invoice.java
│   │   ├── InvoiceNumber.java
│   │   ├── LineItem.java
│   │   ├── InvoiceStatus.java
│   ├── payment/
│   │   ├── Payment.java
│   │   ├── Money.java
│
├── application/               # Application layer (use cases)
│   ├── customers/
│   │   ├── CreateCustomer/
│   │   │   ├── CreateCustomerCommand.java
│   │   │   ├── CreateCustomerHandler.java
│   │   │   ├── CreateCustomerValidator.java
│   │   ├── GetCustomer/
│   │   │   ├── GetCustomerQuery.java
│   │   │   ├── GetCustomerHandler.java
│   │   ├── ListCustomers/
│   │   │   ├── ListCustomersQuery.java
│   │   │   ├── ListCustomersHandler.java
│   ├── invoices/
│   │   ├── CreateInvoice/
│   │   ├── SendInvoice/
│   │   ├── CancelInvoice/
│   │   ├── GetInvoice/
│   │   ├── ListOverdueInvoices/
│   ├── payments/
│   │   ├── RecordPayment/
│   │   ├── GetPaymentHistory/
│   ├── ai/
│   │   ├── GenerateEmailReminder/
│   │   ├── ProcessChatQuery/
│
├── infrastructure/            # Infrastructure layer
│   ├── persistence/
│   │   ├── CustomerRepository.java
│   │   ├── InvoiceRepository.java
│   │   ├── PaymentRepository.java
│   ├── ai/
│   │   ├── OpenAIService.java
│   ├── scheduling/
│   │   ├── OverdueInvoiceScheduler.java
│
├── api/                       # REST API controllers
│   ├── CustomerController.java
│   ├── InvoiceController.java
│   ├── PaymentController.java
│   ├── AIController.java
│
├── config/                    # Configuration
│   ├── SecurityConfig.java
│   ├── OpenAIConfig.java
```

#### 4.1.4 Clean Architecture / Layered Architecture

- **Domain Layer:** Pure business logic, no dependencies on other layers
- **Application Layer:** Orchestrates domain objects, implements use cases
- **Infrastructure Layer:** Technical details (DB, external APIs, scheduling)
- **API Layer:** HTTP endpoints, DTOs, request/response handling

**Dependency Rule:** Dependencies point inward (API → Application → Domain)

---

### 4.2 Technical Stack

#### 4.2.1 Backend (API)

- **Language:** Java 17+
- **Framework:** Spring Boot 3.x
- **Dependencies:**
  - `spring-boot-starter-web` - REST API
  - `spring-boot-starter-data-jpa` - Database access
  - `spring-boot-starter-validation` - Input validation
  - `spring-boot-starter-security` - Authentication
  - `postgresql` - Database driver
  - `flyway-core` - Database migrations
  - `lombok` - Reduce boilerplate
  - `mapstruct` - DTO mapping
  - OpenAI Java Client (e.g., `com.theokanning.openai-gpt3-java`)

#### 4.2.2 Frontend (UI)

- **Language:** TypeScript 5.x
- **Framework:** React 18+ with Next.js 14+ (App Router)
- **State Management:** React Context API or Zustand (lightweight)
- **UI Library:**
  - Tailwind CSS for styling
  - shadcn/ui for components (optional but recommended)
- **HTTP Client:** Axios or native Fetch API
- **Form Handling:** React Hook Form with Zod validation
- **Date Handling:** date-fns or Day.js
- **Architecture:** MVVM (Model-View-ViewModel)
  - Models: Domain DTOs
  - Views: React components (presentational)
  - ViewModels: Custom hooks for business logic

#### 4.2.3 Database

- **Primary:** PostgreSQL 15+
- **Schema Management:** Flyway for migrations
- **Development:** Can use H2 in-memory database for rapid testing
- **Production:** PostgreSQL with connection pooling

#### 4.2.4 AI Integration

- **Provider:** OpenAI API
- **Models:**
  - Chat: `gpt-4o` or `gpt-4o-mini` (function calling support)
  - Embedding: Not required for this scope
- **API Key Management:** Environment variables

#### 4.2.5 Cloud Platforms (Deployment Readiness)

- **AWS:** EC2 + RDS PostgreSQL, or ECS/Fargate containers
- **Azure:** App Service + Azure Database for PostgreSQL
- **Documentation:** Include deployment architecture diagrams

---

### 4.3 Performance Requirements

#### 4.3.1 API Performance

- **Standard CRUD Operations:** < 200ms response time (local testing environment)
- **Invoice with Line Items:** < 250ms for creation/retrieval
- **AI Email Generation:** < 5 seconds (acceptable for user workflow)
- **AI Chat Response:** < 3 seconds for simple queries
- **Database Queries:** < 100ms for single-record lookups

#### 4.3.2 UI Performance

- **Page Load:** < 2 seconds for initial render
- **Navigation:** < 500ms for client-side routing
- **Form Submission:** Optimistic UI updates, immediate feedback
- **No Lag:** Smooth interactions, no noticeable delays

---

### 4.4 Code Quality Requirements

#### 4.4.1 Code Organization

- **Modularity:** Each feature/slice is self-contained
- **Readability:** Clear naming conventions, meaningful variable names
- **Documentation:**
  - JavaDoc for all public methods
  - README files in each major package
  - Inline comments for complex business logic only

#### 4.4.2 Data Transfer Objects (DTOs)

- **Boundary Crossing:** Never expose domain entities directly via API
- **Request DTOs:** For incoming data (e.g., `CreateInvoiceRequest`)
- **Response DTOs:** For outgoing data (e.g., `InvoiceResponse`)
- **Mapping:** Use MapStruct for automated mapping between entities and DTOs

Example:

```java
// Request DTO
public record CreateInvoiceRequest(
    UUID customerId,
    LocalDate dueDate,
    List<LineItemDto> lineItems,
    BigDecimal taxAmount
) {}

// Response DTO
public record InvoiceResponse(
    UUID id,
    String invoiceNumber,
    CustomerDto customer,
    LocalDate issueDate,
    LocalDate dueDate,
    InvoiceStatus status,
    BigDecimal totalAmount,
    BigDecimal amountPaid,
    BigDecimal balanceRemaining,
    List<LineItemDto> lineItems
) {}
```

#### 4.4.3 Domain Events (Optional Advanced Feature)

If implemented, use Spring's `ApplicationEventPublisher`:

```java
// Domain Event
public record InvoicePaidEvent(UUID invoiceId, LocalDateTime paidAt) {}

// In command handler
applicationEventPublisher.publishEvent(new InvoicePaidEvent(invoice.getId(), LocalDateTime.now()));

// Event listener
@EventListener
public void onInvoicePaid(InvoicePaidEvent event) {
    // Send notification, update analytics, etc.
}
```

#### 4.4.4 Naming Conventions

- **Java Classes:** PascalCase (e.g., `CreateInvoiceCommand`)
- **Methods:** camelCase (e.g., `recordPayment()`)
- **Constants:** UPPER_SNAKE_CASE (e.g., `MAX_LINE_ITEMS`)
- **Database Tables:** snake_case (e.g., `customers`, `invoice_line_items`)
- **REST Endpoints:** kebab-case (e.g., `/api/invoices/{id}/send`)

---

## 5. Business Rules & Edge Cases

### 5.1 Payment Rules

#### 5.1.1 Partial Payments

- **Configuration:** Per-invoice setting `allowsPartialPayment` (boolean)
- **Default:** `false` (full payment required)
- **When Enabled:**
  - Customers can pay any amount ≤ balance remaining
  - Invoice remains in SENT status until balance = 0
  - Track payment history for audit trail
- **When Disabled:**
  - Payment amount MUST equal balance remaining exactly
  - Validation error if amount ≠ balance

#### 5.1.2 Overpayment Prevention

- **Rule:** Payment amount CANNOT exceed balance remaining
- **Validation:** Backend validation on `RecordPaymentCommand`
- **Error Message:** "Payment amount ($X) exceeds invoice balance ($Y). Please enter a valid amount."
- **No Credit Balances:** System does not support customer credit balances

#### 5.1.3 Payment Allocation

- **Direct Linking:** Each payment is linked to a specific invoice via payment link
- **No Ambiguity:** Customer clicks invoice payment link → lands on payment page for THAT invoice only
- **No Manual Allocation:** System does not support applying one payment across multiple invoices

#### 5.1.4 Idempotency

- **Problem:** User double-clicks "Submit Payment"
- **Solution:** Generate `paymentId` (UUID) on client side when payment form loads
- **Backend Check:**
  ```java
  if (paymentRepository.existsById(paymentId)) {
      return existingPayment; // Already processed, return success
  }
  // Proceed with payment
  ```
- **Result:** Duplicate submissions safely ignored

---

### 5.2 Invoice Editing Rules

#### 5.2.1 Draft State Editing

- **Allowed:** Full editing of all fields (customer, line items, dates, notes)
- **No Restrictions:** Can add/delete line items freely
- **Validation:** On save, ensure at least 1 line item exists

#### 5.2.2 Sent State Editing

- **Popup Warning:** "Editing this invoice will require re-sending it to the customer. Continue?"
- **If User Confirms:**
  1. Allow edits to line items, dates, amounts
  2. On save, display: "Save and Resend to Customer"
  3. Generate new AI email draft
  4. User must approve before resending
- **Line Item Lock:** Line items are locked in the UI but can be unlocked via "Edit" button
- **Payment History:** If payments exist, warn user: "This invoice has received payments. Editing may cause discrepancies."

#### 5.2.3 Paid State Restrictions

- **No Editing:** Paid invoices are READ-ONLY
- **Exception:** "Mark as Unpaid" button available if payment was a mistake
- **Undo Payment:**
  - Confirm with user: "This will reverse the payment and return the invoice to SENT status. Continue?"
  - Delete payment record
  - Recalculate balance
  - Status → SENT

#### 5.2.4 Cancelled State

- **Immutable:** Cancelled invoices cannot be edited or reactivated
- **View Only:** Display all details with "CANCELLED" stamp
- **Archive:** Move to separate "Cancelled Invoices" section

---

### 5.3 Customer Management Rules

#### 5.3.1 Customer Deletion

**Pre-Deletion Checks:**

1. Query all invoices for customer: `SELECT * FROM invoices WHERE customer_id = ? AND status IN ('DRAFT', 'SENT')`
2. If any found:
   - Display error: "Cannot delete customer with active invoices. Please mark all invoices as paid or cancelled first."
   - List affected invoices with links to each
3. If no active invoices:
   - Perform soft delete: Set `customer.active = false`
   - Customer no longer appears in customer list
   - Historical invoices remain intact (customer data preserved for audit)

#### 5.3.2 Customer Information Updates

- **Invoice Linkage:** Invoices store a snapshot of customer information at the time of creation
- **Future Invoices:** New invoices use updated customer information
- **Past Invoices:** Do not update retroactively (preserve historical accuracy)

---

### 5.4 Time Zone & Date Handling

#### 5.4.1 Due Date Calculation

- **Server Time Zone:** All dates stored in UTC in database
- **User Time Zone:** For demo, assume user is in single time zone (configurable)
- **Midnight Boundary:** Overdue check runs at midnight in user's time zone
- **Formula:** Invoice is overdue if: `current_date > due_date AND status = 'SENT'`

#### 5.4.2 Payment Date Recording

- **Default:** Current date at time of payment submission
- **User Override:** Allow user to manually enter payment date (for recording past payments)

---

### 5.5 Concurrent Modification Prevention

#### 5.5.1 Invoice Status Changes

- **Scenario:** Two users try to mark the same invoice as PAID simultaneously
- **Solution:** Optimistic locking using `@Version` annotation (JPA)

```java
@Entity
public class Invoice {
    @Version
    private Long version;
    // ...
}
```

- **Behavior:** Second user gets error: "This invoice has been modified by another user. Please refresh and try again."

#### 5.5.2 Payment Race Condition

- **Scenario:** Customer submits payment via link while admin marks invoice as paid manually
- **Solution:**
  - Database unique constraint on `(invoice_id, status)` for status transitions
  - Idempotent payment processing (same paymentId = ignore)
  - Transaction isolation level: `READ_COMMITTED`

---

## 6. Testing Requirements

### 6.1 Unit Tests

**Coverage Target:** 80% for business logic

**Focus Areas:**

- Domain models: Invoice calculations, state transitions
- Command handlers: Validation logic, business rules
- Value objects: Money calculations, InvoiceNumber formatting

**Example Test Cases:**

```java
@Test
void shouldCalculateInvoiceTotalCorrectly() {
    // Given: Invoice with 2 line items
    // When: Calculate total
    // Then: Total = sum of line items + tax
}

@Test
void shouldPreventOverpayment() {
    // Given: Invoice with $100 balance
    // When: Attempt to record $150 payment
    // Then: Throw ValidationException
}

@Test
void shouldTransitionToMaidAfterFullPayment() {
    // Given: Invoice in SENT status with $100 balance
    // When: Record $100 payment
    // Then: Invoice status = PAID, balance = 0
}
```

---

### 6.2 Integration Tests

**Mandatory Coverage:**

1. **Customer-Invoice-Payment Flow (End-to-End)**

   ```java
   @Test
   @Transactional
   void completeInvoicePaymentFlow() {
       // 1. Create customer
       CustomerResponse customer = createCustomer("Acme Corp");

       // 2. Create invoice in draft
       InvoiceResponse invoice = createInvoice(customer.id());

       // 3. Add line items
       addLineItem(invoice.id(), "Consulting", 10, 100.00);

       // 4. Send invoice
       sendInvoice(invoice.id());

       // 5. Record payment
       recordPayment(invoice.id(), 1000.00);

       // 6. Verify invoice is paid
       InvoiceResponse paid = getInvoice(invoice.id());
       assertEquals(InvoiceStatus.PAID, paid.status());
       assertEquals(0, paid.balanceRemaining());
   }
   ```

2. **AI Email Generation Integration**

   - Mock OpenAI API responses
   - Test prompt construction with real invoice data
   - Verify email draft contains correct details

3. **AI Chat Function Calling**

   - Mock OpenAI function call responses
   - Test all 7 predefined functions execute correctly
   - Verify database queries return accurate data

4. **Overdue Invoice Scheduler**
   - Create test invoices with past due dates
   - Trigger scheduled job manually
   - Verify correct invoices identified

---

### 6.3 API Tests (REST Endpoints)

Use Spring's `@SpringBootTest` and `MockMvc` or `RestAssured`

**Test Coverage:**

- All CRUD endpoints for Customer, Invoice, Payment
- Error responses (400, 404, 500)
- Authentication (401 unauthorized)
- Validation errors (422 unprocessable entity)

**Example:**

```java
@Test
void shouldReturn404WhenInvoiceNotFound() {
    given()
        .when()
        .get("/api/invoices/{id}", UUID.randomUUID())
        .then()
        .statusCode(404)
        .body("message", equalTo("Invoice not found"));
}
```

---

### 6.4 AI Feature Testing Strategy

#### 6.4.1 OpenAI Mock Responses

For consistent testing, mock OpenAI API:

```java
@Bean
@Profile("test")
public OpenAIService mockOpenAIService() {
    OpenAIService mock = mock(OpenAIService.class);
    when(mock.generateEmailReminder(any()))
        .thenReturn("Dear customer, your invoice INV-2025-0001 for $1,000 is now 5 days overdue...");
    return mock;
}
```

#### 6.4.2 Function Calling Tests

- Test each function independently with various inputs
- Test function result formatting back to OpenAI
- Test error handling when function execution fails

---

## 7. Deployment & Configuration

### 7.1 Database Migrations (Flyway)

#### 7.1.1 Setup

Add dependency:

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

#### 7.1.2 Migration Files

Location: `src/main/resources/db/migration/`

**Naming Convention:** `V{version}__{description}.sql`

**Example Files:**

- `V1__initial_schema.sql` - Create customers, invoices, payments tables
- `V2__add_payment_link_to_invoice.sql` - Add payment_link column
- `V3__add_cancellation_reason.sql` - Add cancellation_reason column

#### 7.1.3 Sample Initial Schema

```sql
-- V1__initial_schema.sql

CREATE TABLE customers (
    id UUID PRIMARY KEY,
    business_name VARCHAR(255) NOT NULL,
    contact_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50),
    billing_street VARCHAR(255) NOT NULL,
    billing_city VARCHAR(100) NOT NULL,
    billing_state VARCHAR(100) NOT NULL,
    billing_postal_code VARCHAR(20) NOT NULL,
    billing_country VARCHAR(100) NOT NULL,
    shipping_street VARCHAR(255),
    shipping_city VARCHAR(100),
    shipping_state VARCHAR(100),
    shipping_postal_code VARCHAR(20),
    shipping_country VARCHAR(100),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE invoices (
    id UUID PRIMARY KEY,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id UUID NOT NULL REFERENCES customers(id),
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    subtotal DECIMAL(15,2) NOT NULL,
    tax_amount DECIMAL(15,2) DEFAULT 0,
    total_amount DECIMAL(15,2) NOT NULL,
    amount_paid DECIMAL(15,2) DEFAULT 0,
    balance_remaining DECIMAL(15,2) NOT NULL,
    allows_partial_payment BOOLEAN DEFAULT FALSE,
    payment_link VARCHAR(255),
    notes TEXT,
    terms TEXT,
    cancellation_reason TEXT,
    reminders_suppressed BOOLEAN DEFAULT FALSE,
    last_reminder_sent_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP,
    paid_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE TABLE invoice_line_items (
    id UUID PRIMARY KEY,
    invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    description VARCHAR(500) NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    line_total DECIMAL(15,2) NOT NULL,
    line_order INT NOT NULL
);

CREATE TABLE payments (
    id UUID PRIMARY KEY,
    invoice_id UUID NOT NULL REFERENCES invoices(id),
    payment_amount DECIMAL(15,2) NOT NULL,
    payment_date DATE NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    transaction_reference VARCHAR(255),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_invoices_customer_id ON invoices(customer_id);
CREATE INDEX idx_invoices_status ON invoices(status);
CREATE INDEX idx_invoices_due_date ON invoices(due_date);
CREATE INDEX idx_payments_invoice_id ON payments(invoice_id);
```

---

### 7.2 Environment Configuration

#### 7.2.1 application.properties (or application.yml)

```properties
# Database
spring.datasource.url=${DATABASE_URL:jdbc:postgresql://localhost:5432/invoiceme}
spring.datasource.username=${DATABASE_USERNAME:postgres}
spring.datasource.password=${DATABASE_PASSWORD:postgres}

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

# OpenAI
openai.api.key=${OPENAI_API_KEY}
openai.api.model=${OPENAI_MODEL:gpt-4o-mini}

# Scheduling
scheduling.overdue-check.cron=0 0 0 * * *

# Security
jwt.secret=${JWT_SECRET:your-secret-key-change-in-production}
jwt.expiration=86400000

# Server
server.port=8080
```

#### 7.2.2 Environment Variables

**Local Development (.env file):**

```
DATABASE_URL=jdbc:postgresql://localhost:5432/invoiceme
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres
OPENAI_API_KEY=sk-your-key-here
OPENAI_MODEL=gpt-4o-mini
JWT_SECRET=local-dev-secret-key-change-me
```

**Production:**

- **AWS:** Store in AWS Secrets Manager or Parameter Store
- **Azure:** Store in Azure Key Vault
- **Docker:** Use environment variables in docker-compose.yml

**CRITICAL:** Never commit `.env` file to Git. Add to `.gitignore`.

---

### 7.3 Scheduled Jobs (Spring @Scheduled)

#### 7.3.1 Configuration Class

```java
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
```

#### 7.3.2 Overdue Invoice Checker

```java
@Component
public class OverdueInvoiceScheduler {

    @Scheduled(cron = "${scheduling.overdue-check.cron}")
    public void checkOverdueInvoices() {
        LocalDate today = LocalDate.now();
        List<Invoice> overdueInvoices = invoiceRepository
            .findByStatusAndDueDateBefore(InvoiceStatus.SENT, today);

        for (Invoice invoice : overdueInvoices) {
            if (shouldSendReminder(invoice)) {
                // Trigger AI reminder generation
                // (Store in queue or send notification to user)
            }
        }
    }

    private boolean shouldSendReminder(Invoice invoice) {
        if (invoice.isRemindersSuppressed()) return false;

        LocalDate lastReminder = invoice.getLastReminderSentAt();
        if (lastReminder == null) return true;

        // Check if enough time has passed based on user's "remind me later" preference
        return lastReminder.plusDays(invoice.getReminderFrequencyDays()).isBefore(LocalDate.now());
    }
}
```

---

### 7.4 Security Configuration

#### 7.4.1 Basic Authentication (Demo)

For demo purposes, implement simple username/password authentication:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable() // For demo; enable in production
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/payments/link/**").permitAll() // Public payment pages
                .anyRequest().authenticated()
            )
            .httpBasic();
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
            .username("demo")
            .password("{noop}password") // No encoding for demo
            .roles("USER")
            .build();
        return new InMemoryUserDetailsManager(user);
    }
}
```

**Login Credentials for Demo:**

- Username: `demo`
- Password: `password`

---

### 7.5 Deployment Architecture (Documentation Only)

#### 7.5.1 AWS Deployment

```
┌─────────────────────────────────────────┐
│  Route 53 (DNS)                         │
└───────────────┬─────────────────────────┘
                │
┌───────────────▼─────────────────────────┐
│  CloudFront (CDN) - Frontend Assets     │
└───────────────┬─────────────────────────┘
                │
┌───────────────▼─────────────────────────┐
│  S3 Bucket - Next.js Static Export      │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│  API Gateway (Optional)                 │
└───────────────┬─────────────────────────┘
                │
┌───────────────▼─────────────────────────┐
│  ECS Fargate / EC2 - Spring Boot API    │
└───────────────┬─────────────────────────┘
                │
┌───────────────▼─────────────────────────┐
│  RDS PostgreSQL (Multi-AZ)              │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│  EventBridge / CloudWatch - Scheduler   │
└─────────────────────────────────────────┘
```

#### 7.5.2 Azure Deployment

```
┌─────────────────────────────────────────┐
│  Azure Front Door (CDN + Routing)       │
└───────────────┬─────────────────────────┘
                │
┌───────────────▼─────────────────────────┐
│  Static Web Apps - Next.js Frontend     │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│  App Service - Spring Boot API          │
└───────────────┬─────────────────────────┘
                │
┌───────────────▼─────────────────────────┐
│  Azure Database for PostgreSQL          │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│  Azure Functions - Scheduled Jobs       │
└─────────────────────────────────────────┘
```

---

## 8. Project Deliverables

### 8.1 Code Repository

**Platform:** GitHub (preferred)

**Repository Structure:**

```
invoiceme/
├── backend/                  # Spring Boot API
│   ├── src/
│   ├── pom.xml
│   ├── README.md
│   └── .env.example
├── frontend/                 # Next.js React app
│   ├── src/
│   ├── public/
│   ├── package.json
│   ├── README.md
│   └── .env.example
├── docs/                     # Documentation
│   ├── architecture.md
│   ├── ai-tools-usage.md
│   ├── deployment-guide.md
│   └── mockup-data.md
├── database/                 # SQL scripts
│   └── mockup-data.sql
├── README.md                 # Main project README
└── .gitignore
```

**README.md Must Include:**

- Project overview and goals
- Technology stack
- Setup instructions (backend + frontend)
- Environment variables guide
- How to run tests
- Demo credentials
- Screenshots or GIFs of key features

---

### 8.2 Demo Video or Live Presentation

**Duration:** 10-15 minutes

**Demo Flow:**

1. **Introduction (1 min)**
   - Project goals and architecture overview
2. **Customer Management (2 min)**
   - Create a new customer
   - Show customer list
3. **Invoice Creation (3 min)**
   - Create draft invoice
   - Add multiple line items
   - Show real-time total calculation
   - Send invoice (trigger AI email generation)
   - Review and edit AI-generated email
4. **AI Features Showcase (4 min)**
   - **Overdue Reminder:**
     - Navigate to overdue invoices
     - Trigger bulk reminder generation
     - Show AI-generated email drafts
     - Demonstrate "Remind Me Later" options
   - **AI Chat Assistant:**
     - Ask: "How many invoices are overdue?"
     - Ask: "What's the total amount we're owed?"
     - Ask: "Show me invoices for [Customer Name]"
     - Demonstrate follow-up questions
5. **Payment Flow (2 min)**
   - Navigate to payment link
   - Record payment (partial if enabled)
   - Show invoice status update to PAID
6. **Edge Cases (2 min)**
   - Try to delete customer with unpaid invoices (blocked)
   - Cancel an invoice and show cancellation email
   - Attempt overpayment (prevented)
7. **Architecture Highlight (1 min)**
   - Quick walkthrough of code structure (VSA)
   - Show CQRS separation
   - Show test coverage

**Recording Options:**

- Loom (screen recording)
- OBS Studio (professional recording)
- Live Zoom presentation

---

### 8.3 Technical Write-Up (1-2 Pages)

**File:** `docs/architecture.md`

**Required Sections:**

#### 1. Architecture Overview

- High-level system diagram
- Explanation of DDD bounded contexts
- CQRS implementation approach
- VSA organization rationale

#### 2. Domain Model

- Core entities and relationships
- Entity-Relationship Diagram (ERD)
- Key business rules and invariants

#### 3. Database Schema

- Tables and relationships
- Key indexes and constraints
- Migration strategy (Flyway)

#### 4. AI Integration

- OpenAI API usage
- Function calling implementation
- Prompt engineering strategies
- Error handling and fallbacks

#### 5. Design Decisions

- Why PostgreSQL over NoSQL
- Why Next.js over plain React
- Why logical CQRS over event sourcing
- Trade-offs and compromises made for demo scope

#### 6. Testing Strategy

- Unit test coverage areas
- Integration test scenarios
- AI feature testing approach

---

### 8.4 AI Tools Documentation

**File:** `docs/ai-tools-usage.md`

**Required Content:**

#### 1. Tools Used

List all AI tools used in development:

- Claude / ChatGPT / GitHub Copilot
- Specific models and configurations

#### 2. Example Prompts

Document at least 5 prompts used during development:

**Example:**

```
Prompt: "Generate a Spring Boot service class for recording payments
using CQRS pattern. Include validation for overpayment and idempotency
check using payment ID. Use Java 17 records for DTOs."

Result: [Code snippet generated]

How it accelerated development: Saved 45 minutes of boilerplate writing
and ensured consistent error handling patterns.
```

#### 3. Code Review Process

- What percentage of AI-generated code was used as-is vs. modified?
- How did you validate architectural compliance?
- Examples of AI suggestions that were rejected and why

#### 4. Architectural Quality Maintenance

- How did you ensure AI-generated code adhered to DDD principles?
- How did you prevent architectural erosion?
- How did you maintain consistent naming and structure?

#### 5. Productivity Metrics (Estimated)

- Total development time
- Estimated time saved using AI tools
- Areas where AI was most helpful
- Areas where AI struggled

---

### 8.5 Test Cases and Validation Results

**File:** `docs/test-results.md`

**Must Include:**

1. **Test Coverage Report**

   - Screenshot or export from JaCoCo or similar tool
   - Minimum 80% coverage for business logic

2. **Integration Test Results**

   - List of all integration tests executed
   - Pass/fail status
   - Execution time

3. **Manual Test Scenarios**

   - Checklist of manually tested user flows
   - Screenshots of successful flows
   - Edge case validation results

4. **Performance Benchmarks**
   - API response times for key endpoints
   - Database query performance metrics

**Example Format:**

```markdown
## Integration Tests

✅ Customer-Invoice-Payment Complete Flow - PASSED (2.3s)
✅ AI Email Generation with Real Invoice Data - PASSED (4.1s)
✅ Overdue Invoice Detection Scheduler - PASSED (0.8s)
✅ Payment Idempotency Check - PASSED (0.5s)
✅ Customer Deletion Blocked with Active Invoices - PASSED (0.4s)

## Manual Tests

✅ Create customer with shipping address
✅ Generate bulk overdue reminders (10 invoices)
✅ AI chat: Complex follow-up question
✅ Cancel sent invoice and verify email generation
✅ Partial payment workflow
```

---

### 8.6 Mockup Data Requirements

**File:** `database/mockup-data.sql`

**Required Mockup Data:**

1. **Customers (10 Sample Profiles)**

   - Mix of business types (tech, retail, services)
   - Realistic company names and addresses
   - Valid email formats

2. **Invoices (20-30 Samples)**

   - **5 Draft invoices** - Various line item counts
   - **8 Sent invoices** - Some near due date, some not
   - **5 Overdue invoices** - 5 days, 10 days, 30 days overdue
   - **7 Paid invoices** - With payment history
   - **2 Cancelled invoices** - With cancellation reasons

3. **Line Items**

   - Realistic service descriptions
   - Varied quantities and prices
   - Examples: "Website Development (10 hours @ $150/hr)", "Logo Design (1 @ $500)"

4. **Payments**
   - Full payments and partial payments
   - Various payment methods
   - Realistic dates

**Implementation:**

```sql
-- Example mockup data structure
INSERT INTO customers (id, business_name, contact_name, email, ...) VALUES
('uuid-1', 'Acme Corporation', 'John Smith', 'john@acmecorp.com', ...),
('uuid-2', 'TechStart Inc', 'Jane Doe', 'jane@techstart.io', ...);

INSERT INTO invoices (id, invoice_number, customer_id, status, due_date, ...) VALUES
('uuid-101', 'INV-2025-0001', 'uuid-1', 'SENT', '2025-10-15', ...),
('uuid-102', 'INV-2025-0002', 'uuid-2', 'OVERDUE', '2025-10-01', ...);
```

**Loading Script:**

- Create SQL file that can be executed via Flyway or manual import
- Include in README: "Run `psql -d invoiceme -f database/mockup-data.sql` to load demo data"

---

## 9. Implementation Checklist

### Phase 1: Project Setup (Day 1)

- [ ] Initialize Git repository
- [ ] Set up Spring Boot backend project structure
- [ ] Set up Next.js frontend project
- [ ] Configure PostgreSQL database
- [ ] Set up Flyway for migrations
- [ ] Create initial schema (V1 migration)
- [ ] Configure environment variables
- [ ] Set up basic security (demo auth)

### Phase 2: Core Domain Implementation (Day 2-3)

- [ ] Implement Customer domain
  - [ ] Customer entity
  - [ ] Create/Update/Delete commands
  - [ ] Customer queries
  - [ ] REST endpoints
  - [ ] Unit tests
- [ ] Implement Invoice domain
  - [ ] Invoice entity with line items
  - [ ] Invoice state machine
  - [ ] Create/Update/Send/Cancel commands
  - [ ] Invoice queries
  - [ ] REST endpoints
  - [ ] Unit tests
- [ ] Implement Payment domain
  - [ ] Payment entity
  - [ ] Record payment command
  - [ ] Payment idempotency
  - [ ] Payment queries
  - [ ] REST endpoints
  - [ ] Unit tests

### Phase 3: Frontend Implementation (Day 3-4)

- [ ] Set up Tailwind CSS and UI components
- [ ] Create Customer management UI
  - [ ] List customers page
  - [ ] Create/edit customer form
  - [ ] Delete customer with validation
- [ ] Create Invoice management UI
  - [ ] Dashboard with invoice stats
  - [ ] List invoices by status
  - [ ] Create invoice form with line items
  - [ ] Invoice detail view
  - [ ] Send invoice button
  - [ ] Cancel invoice modal
- [ ] Create Payment UI
  - [ ] Public payment link page
  - [ ] Payment form
  - [ ] Payment confirmation

### Phase 4: AI Features (Day 4-5)

- [ ] Set up OpenAI API integration
- [ ] Implement AI Email Reminder
  - [ ] Scheduled job for overdue detection
  - [ ] AI prompt construction
  - [ ] Email generation service
  - [ ] Review/edit modal UI
  - [ ] "Remind Me Later" options
- [ ] Implement AI Chat Assistant
  - [ ] Chat UI component
  - [ ] OpenAI function calling setup
  - [ ] Backend functions for data queries
  - [ ] Function result formatting
  - [ ] Conversation context management
  - [ ] Error handling UI

### Phase 5: Integration & Testing (Day 5-6)

- [ ] Write integration tests
  - [ ] Complete customer-invoice-payment flow
  - [ ] AI email generation test
  - [ ] AI chat function calling test
  - [ ] Overdue scheduler test
- [ ] Create mockup data SQL script
- [ ] Load mockup data and test all features
- [ ] Manual testing of all user flows
- [ ] Edge case validation
- [ ] Performance testing (API response times)

### Phase 6: Documentation & Demo (Day 6-7)

- [ ] Write main README.md
- [ ] Write technical architecture document
- [ ] Write AI tools usage documentation
- [ ] Document test results
- [ ] Create deployment architecture diagrams
- [ ] Record demo video
- [ ] Prepare live presentation (if applicable)
- [ ] Final code review and cleanup

---

## 10. Success Criteria

This project will be considered successful if it demonstrates:

### Technical Excellence

✅ Clean separation of Commands and Queries (CQRS)  
✅ Domain models with rich behavior (DDD)  
✅ Vertical slice organization (VSA)  
✅ Proper use of DTOs and mappers  
✅ API response times < 200ms  
✅ 80%+ test coverage for business logic  
✅ Database migrations via Flyway  
✅ Secure environment variable management

### Feature Completeness

✅ Full customer CRUD with deletion validation  
✅ Complete invoice lifecycle (Draft → Sent → Paid)  
✅ Payment recording with idempotency  
✅ AI-generated overdue reminders with human review  
✅ AI chat assistant with 7 functional queries  
✅ Mockup data pre-loaded for demo

### Code Quality

✅ Consistent naming conventions  
✅ Well-structured, modular code  
✅ Meaningful documentation (JavaDoc, README)  
✅ Passing integration tests  
✅ No critical security vulnerabilities

### AI Tool Integration

✅ Documented use of AI development tools  
✅ Example prompts with justification  
✅ Evidence of time savings while maintaining quality

### Deliverables

✅ GitHub repository with complete code  
✅ 10-15 minute demo video  
✅ 1-2 page technical write-up  
✅ AI tools documentation  
✅ Test results report

---

## Appendix A: API Endpoint Summary

### Customer Endpoints

```
POST   /api/customers                  - Create customer
GET    /api/customers                  - List all customers
GET    /api/customers/{id}             - Get customer by ID
PUT    /api/customers/{id}             - Update customer
DELETE /api/customers/{id}             - Delete customer (with validation)
```

### Invoice Endpoints

```
POST   /api/invoices                   - Create invoice (draft)
GET    /api/invoices                   - List all invoices
GET    /api/invoices/{id}              - Get invoice by ID
PUT    /api/invoices/{id}              - Update invoice
POST   /api/invoices/{id}/send         - Send invoice
POST   /api/invoices/{id}/cancel       - Cancel invoice
GET    /api/invoices/status/{status}   - List by status
GET    /api/invoices/customer/{customerId} - List by customer
GET    /api/invoices/overdue           - List overdue invoices
```

### Payment Endpoints

```
POST   /api/payments                   - Record payment
GET    /api/payments/{id}              - Get payment by ID
GET    /api/payments/invoice/{invoiceId} - List payments for invoice
```

### AI Endpoints

```
POST   /api/ai/generate-reminder       - Generate email reminder
POST   /api/ai/chat                    - Send chat message
GET    /api/ai/chat/history            - Get chat history (optional)
```

### Authentication Endpoints

```
POST   /api/auth/login                 - Login (returns JWT)
POST   /api/auth/logout                - Logout
```

---

## Appendix B: Database Schema ERD

```
┌─────────────────────┐
│     customers       │
├─────────────────────┤
│ id (PK)             │
│ business_name       │
│ contact_name        │
│ email (UNIQUE)      │
│ phone               │
│ billing_address     │
│ shipping_address    │
│ active              │
│ created_at          │
│ updated_at          │
└──────────┬──────────┘
           │
           │ 1:N
           │
┌──────────▼──────────┐
│      invoices       │
├─────────────────────┤
│ id (PK)             │
│ invoice_number (UK) │
│ customer_id (FK)    │
│ issue_date          │
│ due_date            │
│ status              │
│ subtotal            │
│ tax_amount          │
│ total_amount        │
│ amount_paid         │
│ balance_remaining   │
│ allows_partial      │
│ payment_link        │
│ notes               │
│ cancellation_reason │
│ created_at          │
│ sent_at             │
│ paid_at             │
│ cancelled_at        │
│ version             │
└──────────┬──────────┘
           │
           │ 1:N
           │
┌──────────▼──────────┐
│ invoice_line_items  │
├─────────────────────┤
│ id (PK)             │
│ invoice_id (FK)     │
│ description         │
│ quantity            │
│ unit_price          │
│ line_total          │
│ line_order          │
└─────────────────────┘

           │
           │ 1:N
           │
┌──────────▼──────────┐
│      payments       │
├─────────────────────┤
│ id (PK)             │
│ invoice_id (FK)     │
│ payment_amount      │
│ payment_date        │
│ payment_method      │
│ transaction_ref     │
│ notes               │
│ created_at          │
└─────────────────────┘
```

---

## Appendix C: OpenAI Function Definitions (Full)

```json
[
  {
    "name": "getOverdueInvoices",
    "description": "Get count and details of overdue invoices, optionally filtered by month",
    "parameters": {
      "type": "object",
      "properties": {
        "month": {
          "type": "string",
          "description": "Optional month in YYYY-MM format"
        }
      }
    }
  },
  {
    "name": "getTotalAmountOwed",
    "description": "Get the total balance across all sent invoices",
    "parameters": {
      "type": "object",
      "properties": {}
    }
  },
  {
    "name": "getInvoicesByCustomer",
    "description": "List invoices for a specific customer",
    "parameters": {
      "type": "object",
      "properties": {
        "customerName": {
          "type": "string",
          "description": "Customer business name or contact name"
        }
      },
      "required": ["customerName"]
    }
  },
  {
    "name": "getInvoicesByStatus",
    "description": "Get count and total for invoices by status",
    "parameters": {
      "type": "object",
      "properties": {
        "status": {
          "type": "string",
          "enum": ["draft", "sent", "paid", "cancelled"],
          "description": "Invoice status"
        }
      },
      "required": ["status"]
    }
  },
  {
    "name": "getPaymentHistory",
    "description": "List recent payments, optionally for a specific invoice",
    "parameters": {
      "type": "object",
      "properties": {
        "invoiceId": {
          "type": "string",
          "description": "Optional invoice UUID"
        },
        "days": {
          "type": "integer",
          "description": "Number of days to look back (default 30)"
        }
      }
    }
  },
  {
    "name": "getCustomerSummary",
    "description": "Get summary of invoices and payments for a customer",
    "parameters": {
      "type": "object",
      "properties": {
        "customerId": {
          "type": "string",
          "description": "Customer UUID"
        }
      },
      "required": ["customerId"]
    }
  },
  {
    "name": "getInvoiceStatistics",
    "description": "Get aggregated invoice statistics for a time period",
    "parameters": {
      "type": "object",
      "properties": {
        "period": {
          "type": "string",
          "enum": ["month", "quarter", "year"],
          "description": "Time period for statistics"
        }
      }
    }
  }
]
```

---

**END OF DOCUMENT**

---

**Version History:**

- v1.0 - Initial PRD (Original document)
- v2.0 - Comprehensive update with all clarifications, technical decisions, and implementation details
