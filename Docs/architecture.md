# InvoiceMe - Complete Architecture Diagrams

## 1. High-Level System Architecture

```mermaid
graph TB
    subgraph "Client Layer"
        Browser[Web Browser]
        Mobile[Mobile Browser]
    end

    subgraph "Frontend - Next.js/React"
        UI[React Components]
        VM[ViewModels/Hooks]
        API_Client[API Client - Axios]
        Chat[AI Chat Component]
    end

    subgraph "Backend - Spring Boot API"
        Gateway[API Gateway Layer]
        Auth[Security/Auth Filter]

        subgraph "Application Layer - VSA"
            CustSlice[Customer Slices]
            InvSlice[Invoice Slices]
            PaySlice[Payment Slices]
            AISlice[AI Slices]
        end

        subgraph "Domain Layer"
            CustDomain[Customer Domain]
            InvDomain[Invoice Domain]
            PayDomain[Payment Domain]
        end

        subgraph "Infrastructure Layer"
            Repos[Repositories - JPA]
            OpenAI[OpenAI Service]
            Scheduler[Spring Scheduler]
            Email[Email Service - Mock]
        end
    end

    subgraph "Data Layer"
        DB[(PostgreSQL Database)]
        Flyway[Flyway Migrations]
    end

    subgraph "External Services"
        OpenAI_API[OpenAI API]
        OAuth[OAuth Provider - Mock]
    end

    Browser --> UI
    Mobile --> UI
    UI --> VM
    VM --> API_Client
    Chat --> API_Client

    API_Client --> Gateway
    Gateway --> Auth
    Auth --> CustSlice
    Auth --> InvSlice
    Auth --> PaySlice
    Auth --> AISlice

    CustSlice --> CustDomain
    InvSlice --> InvDomain
    PaySlice --> PayDomain
    AISlice --> InvDomain

    CustDomain --> Repos
    InvDomain --> Repos
    PayDomain --> Repos

    AISlice --> OpenAI
    OpenAI --> OpenAI_API

    Repos --> DB
    Flyway --> DB

    Scheduler --> InvSlice
    Email --> OAuth

    style UI fill:#61dafb
    style Gateway fill:#6db33f
    style DB fill:#336791
    style OpenAI_API fill:#10a37f
```

## 2. Backend Architecture - Vertical Slice Architecture with CQRS

```mermaid
graph TB
    subgraph "API Controllers"
        CustomerCtrl[CustomerController]
        InvoiceCtrl[InvoiceController]
        PaymentCtrl[PaymentController]
        AICtrl[AIController]
    end

    subgraph "Customer Vertical Slice"
        subgraph "Commands"
            CreateCust[CreateCustomerCommand]
            UpdateCust[UpdateCustomerCommand]
            DeleteCust[DeleteCustomerCommand]
        end

        subgraph "Queries"
            GetCust[GetCustomerQuery]
            ListCust[ListCustomersQuery]
        end

        subgraph "Handlers"
            CreateCustH[CreateCustomerHandler]
            GetCustH[GetCustomerHandler]
        end
    end

    subgraph "Invoice Vertical Slice"
        subgraph "Commands "
            CreateInv[CreateInvoiceCommand]
            SendInv[SendInvoiceCommand]
            CancelInv[CancelInvoiceCommand]
            UpdateInv[UpdateInvoiceCommand]
        end

        subgraph "Queries "
            GetInv[GetInvoiceQuery]
            ListOverdue[ListOverdueQuery]
            GetByStatus[GetByStatusQuery]
        end

        subgraph "Handlers "
            CreateInvH[CreateInvoiceHandler]
            SendInvH[SendInvoiceHandler]
            GetInvH[GetInvoiceHandler]
        end
    end

    subgraph "Payment Vertical Slice"
        subgraph "Commands  "
            RecordPay[RecordPaymentCommand]
        end

        subgraph "Queries  "
            GetPay[GetPaymentQuery]
            ListPayHist[ListPaymentHistoryQuery]
        end

        subgraph "Handlers  "
            RecordPayH[RecordPaymentHandler]
            GetPayH[GetPaymentHandler]
        end
    end

    subgraph "AI Vertical Slice"
        subgraph "Commands   "
            GenReminder[GenerateReminderCommand]
            ProcessChat[ProcessChatCommand]
        end

        subgraph "Handlers   "
            GenReminderH[GenerateReminderHandler]
            ProcessChatH[ProcessChatHandler]
        end
    end

    subgraph "Domain Models"
        Customer[Customer Entity]
        Invoice[Invoice Aggregate]
        LineItem[LineItem Value Object]
        Payment[Payment Entity]
    end

    subgraph "Infrastructure"
        CustRepo[(Customer Repository)]
        InvRepo[(Invoice Repository)]
        PayRepo[(Payment Repository)]
        OpenAISvc[OpenAI Service]
    end

    CustomerCtrl --> CreateCust
    CustomerCtrl --> GetCust
    CreateCust --> CreateCustH
    GetCust --> GetCustH

    InvoiceCtrl --> CreateInv
    InvoiceCtrl --> SendInv
    InvoiceCtrl --> GetInv
    CreateInv --> CreateInvH
    SendInv --> SendInvH
    GetInv --> GetInvH

    PaymentCtrl --> RecordPay
    PaymentCtrl --> GetPay
    RecordPay --> RecordPayH
    GetPay --> GetPayH

    AICtrl --> GenReminder
    AICtrl --> ProcessChat
    GenReminder --> GenReminderH
    ProcessChat --> ProcessChatH

    CreateCustH --> Customer
    CreateInvH --> Invoice
    RecordPayH --> Payment
    RecordPayH --> Invoice

    Customer --> CustRepo
    Invoice --> InvRepo
    Payment --> PayRepo
    Invoice --> LineItem

    GenReminderH --> OpenAISvc
    ProcessChatH --> OpenAISvc
    ProcessChatH --> InvRepo

    style CreateCust fill:#ffd700
    style GetCust fill:#87ceeb
    style CreateInv fill:#ffd700
    style GetInv fill:#87ceeb
    style RecordPay fill:#ffd700
    style GetPay fill:#87ceeb
```

## 3. Domain Model - Entity Relationships

```mermaid
erDiagram
    CUSTOMER ||--o{ INVOICE : "has"
    INVOICE ||--|{ LINE_ITEM : "contains"
    INVOICE ||--o{ PAYMENT : "receives"

    CUSTOMER {
        uuid id PK
        string business_name
        string contact_name
        string email UK
        string phone
        json billing_address
        json shipping_address
        boolean active
        timestamp created_at
        timestamp updated_at
    }

    INVOICE {
        uuid id PK
        string invoice_number UK
        uuid customer_id FK
        date issue_date
        date due_date
        enum status
        decimal subtotal
        decimal tax_amount
        decimal total_amount
        decimal amount_paid
        decimal balance_remaining
        boolean allows_partial_payment
        string payment_link
        text notes
        text cancellation_reason
        boolean reminders_suppressed
        timestamp last_reminder_sent_at
        timestamp created_at
        timestamp sent_at
        timestamp paid_at
        timestamp cancelled_at
        bigint version
    }

    LINE_ITEM {
        uuid id PK
        uuid invoice_id FK
        string description
        decimal quantity
        decimal unit_price
        decimal line_total
        int line_order
    }

    PAYMENT {
        uuid id PK
        uuid invoice_id FK
        decimal payment_amount
        date payment_date
        enum payment_method
        string transaction_reference
        text notes
        timestamp created_at
    }
```

## 4. Invoice Lifecycle State Machine

```mermaid
stateDiagram-v2
    [*] --> DRAFT: Create Invoice

    DRAFT --> SENT: Send Invoice
    DRAFT --> CANCELLED: Cancel Invoice

    SENT --> PAID: Payment Received\n(Balance = 0)
    SENT --> SENT: Partial Payment\n(Balance > 0)
    SENT --> CANCELLED: Cancel Invoice
    SENT --> DRAFT: Edit Invoice\n(Requires Resend)

    PAID --> SENT: Mark as Unpaid\n(Error Correction)
    PAID --> [*]: Archive

    CANCELLED --> [*]: Archive

    note right of DRAFT
        - Editable
        - Can add/remove line items
        - No payment link
    end note

    note right of SENT
        - Payment link active
        - Line items locked
        - Can receive payments
        - Overdue check active
    end note

    note right of PAID
        - Read-only
        - Balance = 0
        - All payments recorded
    end note

    note right of CANCELLED
        - Immutable
        - Payment link deactivated
        - Cancellation reason recorded
    end note
```

## 5. Create and Send Invoice Flow

```mermaid
sequenceDiagram
    actor User
    participant UI as Frontend UI
    participant API as Invoice Controller
    participant Handler as SendInvoiceHandler
    participant Domain as Invoice Domain
    participant AI as OpenAI Service
    participant Repo as Invoice Repository
    participant DB as PostgreSQL

    User->>UI: Click "Send Invoice"
    UI->>API: POST /api/invoices/{id}/send

    API->>Handler: Execute SendInvoiceCommand
    Handler->>Repo: findById(invoiceId)
    Repo->>DB: SELECT * FROM invoices
    DB-->>Repo: Invoice data
    Repo-->>Handler: Invoice entity

    Handler->>Domain: invoice.validateForSending()
    Domain-->>Handler: Validation passed

    Handler->>Domain: invoice.send()
    Domain->>Domain: Generate payment link
    Domain->>Domain: Set status = SENT
    Domain->>Domain: Set sentAt timestamp
    Domain-->>Handler: Invoice updated

    Handler->>Repo: save(invoice)
    Repo->>DB: UPDATE invoices
    DB-->>Repo: Success

    Handler->>AI: generateEmailReminder(invoice)
    AI->>AI: Build prompt with invoice details
    AI->>AI: Call OpenAI API
    AI-->>Handler: Email draft text

    Handler-->>API: InvoiceResponse + Email draft
    API-->>UI: 200 OK + Email draft

    UI->>User: Show email review modal
    User->>UI: Review and approve
    UI->>API: POST /api/ai/send-email
    API-->>UI: Email sent (mocked)
    UI->>User: Confirmation message
```

## 6. Record Payment Flow with Idempotency

```mermaid
sequenceDiagram
    actor Customer
    participant UI as Payment Page
    participant API as Payment Controller
    participant Handler as RecordPaymentHandler
    participant PayRepo as Payment Repository
    participant InvRepo as Invoice Repository
    participant Domain as Invoice Domain
    participant DB as PostgreSQL

    Customer->>UI: Load payment page\n(via payment link)
    UI->>UI: Generate paymentId (UUID)
    Customer->>UI: Enter payment details
    Customer->>UI: Click "Submit Payment"

    UI->>API: POST /api/payments\n{paymentId, invoiceId, amount}

    API->>Handler: Execute RecordPaymentCommand

    rect rgb(255, 240, 200)
        Note over Handler,PayRepo: Idempotency Check
        Handler->>PayRepo: existsById(paymentId)
        PayRepo->>DB: SELECT COUNT(*) WHERE id = ?
        DB-->>PayRepo: count = 0
        PayRepo-->>Handler: Payment doesn't exist
    end

    Handler->>InvRepo: findById(invoiceId)
    InvRepo->>DB: SELECT * FROM invoices
    DB-->>InvRepo: Invoice data
    InvRepo-->>Handler: Invoice entity

    Handler->>Domain: invoice.validatePayment(amount)
    Domain->>Domain: Check: amount <= balance
    Domain->>Domain: Check: status = SENT
    Domain-->>Handler: Validation passed

    Handler->>Domain: invoice.recordPayment(payment)
    Domain->>Domain: amountPaid += payment
    Domain->>Domain: Calculate balance

    alt Balance = 0
        Domain->>Domain: Set status = PAID
        Domain->>Domain: Set paidAt timestamp
    end

    Domain-->>Handler: Invoice updated

    Handler->>PayRepo: save(payment)
    PayRepo->>DB: INSERT INTO payments
    DB-->>PayRepo: Success

    Handler->>InvRepo: save(invoice)
    InvRepo->>DB: UPDATE invoices
    DB-->>InvRepo: Success

    Handler-->>API: PaymentResponse
    API-->>UI: 200 OK + Confirmation
    UI->>Customer: Payment successful!
```

## 7. AI Chat Assistant Flow - Function Calling

```mermaid
sequenceDiagram
    actor User
    participant Chat as Chat UI
    participant API as AI Controller
    participant Handler as ProcessChatHandler
    participant OpenAI as OpenAI Service
    participant Functions as Query Functions
    participant Repo as Repositories
    participant DB as PostgreSQL

    User->>Chat: Types: "How many invoices\nare overdue?"
    Chat->>API: POST /api/ai/chat\n{message, conversationHistory}

    API->>Handler: Execute ProcessChatCommand
    Handler->>OpenAI: sendChatRequest(message, functions)

    OpenAI->>OpenAI: Call OpenAI API with\nfunction definitions
    OpenAI-->>Handler: Function call response:\ngetOverdueInvoices()

    Handler->>Functions: Execute getOverdueInvoices()
    Functions->>Repo: findOverdueInvoices()
    Repo->>DB: SELECT * FROM invoices\nWHERE status='SENT'\nAND due_date < NOW()
    DB-->>Repo: Overdue invoice data
    Repo-->>Functions: List of invoices

    Functions->>Functions: Calculate count and total
    Functions-->>Handler: Result:\n{count: 5, total: $12,450}

    Handler->>OpenAI: sendFunctionResult(result)
    OpenAI->>OpenAI: Format natural language\nresponse
    OpenAI-->>Handler: "You have 5 overdue invoices\ntotaling $12,450.00..."

    Handler-->>API: ChatResponse
    API-->>Chat: 200 OK + AI response
    Chat->>User: Display formatted message
```

## 8. Scheduled Overdue Invoice Check

```mermaid
sequenceDiagram
    participant Scheduler as Spring Scheduler
    participant Job as OverdueInvoiceJob
    participant Repo as Invoice Repository
    participant AI as OpenAI Service
    participant Queue as Notification Queue
    participant DB as PostgreSQL

    Note over Scheduler: Daily at Midnight

    Scheduler->>Job: @Scheduled trigger
    Job->>Repo: findOverdueInvoices()
    Repo->>DB: SELECT * FROM invoices\nWHERE status='SENT'\nAND due_date < NOW()\nAND reminders_suppressed=false
    DB-->>Repo: Overdue invoices
    Repo-->>Job: List<Invoice>

    loop For each overdue invoice
        Job->>Job: Check shouldSendReminder()

        alt Reminder needed
            Job->>AI: generateEmailReminder(invoice)
            AI->>AI: Build prompt
            AI->>AI: Call OpenAI API
            AI-->>Job: Email draft

            Job->>Queue: Add to notification queue
            Queue-->>Job: Queued

            Job->>Repo: updateLastReminderDate(invoice)
            Repo->>DB: UPDATE invoices\nSET last_reminder_sent_at=NOW()
            DB-->>Repo: Success
        end
    end

    Job-->>Scheduler: Completed

    Note over Queue: User sees notifications\nin UI dashboard
```

## 9. Complete Technology Stack Diagram

```mermaid
graph TB
    subgraph "Frontend Stack"
        TS[TypeScript 5.x]
        React[React 18+]
        Next[Next.js 14 App Router]
        Tailwind[Tailwind CSS]
        Shadcn[shadcn/ui Components]
        RHF[React Hook Form]
        Zod[Zod Validation]
        Axios[Axios HTTP Client]
    end

    subgraph "Backend Stack"
        Java[Java 17+]
        Spring[Spring Boot 3.x]
        JPA[Spring Data JPA]
        Security[Spring Security]
        Valid[Spring Validation]
        Sched[Spring Scheduler]
        Lombok[Lombok]
        MapStruct[MapStruct]
    end

    subgraph "Database & Migrations"
        Postgres[PostgreSQL 15+]
        Flyway[Flyway Migrations]
        H2[H2 In-Memory - Dev/Test]
    end

    subgraph "AI & External Services"
        OpenAI_SDK[OpenAI Java Client]
        OpenAI_API[OpenAI API]
        OAuth_Mock[OAuth Mock Service]
    end

    subgraph "Testing"
        JUnit[JUnit 5]
        Mockito[Mockito]
        SpringTest[Spring Boot Test]
        RestAssured[REST Assured]
    end

    subgraph "DevOps & Tools"
        Maven[Maven/Gradle]
        Docker[Docker]
        Git[Git/GitHub]
        Postman[Postman/Insomnia]
    end

    React --> Next
    Next --> Tailwind
    Next --> Shadcn
    Next --> RHF
    RHF --> Zod
    Next --> Axios

    Spring --> JPA
    Spring --> Security
    Spring --> Valid
    Spring --> Sched
    Java --> Lombok
    Java --> MapStruct

    JPA --> Postgres
    Postgres --> Flyway

    Spring --> OpenAI_SDK
    OpenAI_SDK --> OpenAI_API

    Spring --> JUnit
    Spring --> SpringTest
    JUnit --> Mockito
    SpringTest --> RestAssured

    style React fill:#61dafb
    style Spring fill:#6db33f
    style Postgres fill:#336791
    style OpenAI_API fill:#10a37f
```

## 10. Deployment Architecture - AWS

```mermaid
graph TB
    subgraph "AWS Cloud"
        subgraph "Frontend Hosting"
            CF[CloudFront CDN]
            S3[S3 Bucket\nNext.js Static Files]
        end

        subgraph "API Hosting"
            ALB[Application Load Balancer]
            ECS[ECS Fargate\nSpring Boot Container]
            ECR[ECR\nDocker Registry]
        end

        subgraph "Database"
            RDS[RDS PostgreSQL\nMulti-AZ]
        end

        subgraph "Scheduling"
            EventBridge[EventBridge\nCron Rules]
            Lambda[Lambda Function\nTrigger API]
        end

        subgraph "Security & Config"
            Secrets[Secrets Manager\nOpenAI Key, DB Creds]
            IAM[IAM Roles & Policies]
        end

        subgraph "Monitoring"
            CloudWatch[CloudWatch Logs\nMetrics & Alarms]
        end
    end

    subgraph "External"
        Users[End Users]
        OpenAI_Ext[OpenAI API]
    end

    Users --> CF
    CF --> S3
    Users --> ALB
    ALB --> ECS

    ECS --> RDS
    ECS --> Secrets
    ECS --> OpenAI_Ext

    EventBridge --> Lambda
    Lambda --> ALB

    ECS --> CloudWatch
    RDS --> CloudWatch

    ECR -.->|Deploy| ECS

    style CF fill:#FF9900
    style S3 fill:#FF9900
    style ECS fill:#FF9900
    style RDS fill:#527FFF
    style Secrets fill:#DD344C
```

## 11. Data Flow - Complete Invoice-to-Payment Journey

```mermaid
flowchart TD
    Start([User Logs In]) --> Dashboard[View Dashboard]
    Dashboard --> CreateCust{Need to Create\nCustomer?}

    CreateCust -->|Yes| AddCust[Create New Customer]
    CreateCust -->|No| CreateInv[Create Draft Invoice]
    AddCust --> CreateInv

    CreateInv --> AddLineItems[Add Line Items]
    AddLineItems --> MoreItems{Add More\nLine Items?}
    MoreItems -->|Yes| AddLineItems
    MoreItems -->|No| Review[Review Invoice]

    Review --> ReviewOK{Invoice\nCorrect?}
    ReviewOK -->|No| EditInv[Edit Invoice]
    EditInv --> Review

    ReviewOK -->|Yes| SendInv[Click Send Invoice]
    SendInv --> GenEmail[AI Generates Email Draft]
    GenEmail --> ReviewEmail[User Reviews Email]

    ReviewEmail --> EmailOK{Email\nApproved?}
    EmailOK -->|No| EditEmail[Edit Email Text]
    EditEmail --> ReviewEmail

    EmailOK -->|Yes| MockSend[Mock Send Email]
    MockSend --> InvSent[Invoice Status: SENT]

    InvSent --> PaymentLink[Payment Link Active]
    PaymentLink --> WaitPay[Wait for Payment]

    WaitPay --> PastDue{Past Due\nDate?}
    PastDue -->|Yes| Scheduler[Daily Scheduler Runs]
    PastDue -->|No| WaitPay

    Scheduler --> GenReminder[AI Generates Reminder]
    GenReminder --> NotifyUser[Notify User]
    NotifyUser --> UserAction{User\nAction?}

    UserAction -->|Send Reminder| SendReminder[Send Reminder Email]
    UserAction -->|Remind Later| SetReminder[Set Reminder Date]
    UserAction -->|Suppress| SuppressReminder[Suppress Future Reminders]

    SendReminder --> WaitPay
    SetReminder --> WaitPay
    SuppressReminder --> WaitPay

    WaitPay --> CustPay[Customer Clicks\nPayment Link]
    CustPay --> PayForm[Fill Payment Form]
    PayForm --> Submit[Submit Payment]

    Submit --> ValidatePay{Payment\nValid?}
    ValidatePay -->|No| PayError[Show Error]
    PayError --> PayForm

    ValidatePay -->|Yes| RecordPay[Record Payment]
    RecordPay --> UpdateInv[Update Invoice]

    UpdateInv --> CheckBalance{Balance\n= 0?}
    CheckBalance -->|No| PartialPaid[Status: SENT\nPartial Payment]
    CheckBalance -->|Yes| FullPaid[Status: PAID]

    PartialPaid --> WaitPay
    FullPaid --> End([Complete])

    style Start fill:#90EE90
    style End fill:#90EE90
    style GenEmail fill:#10a37f
    style GenReminder fill:#10a37f
    style RecordPay fill:#FFD700
    style FullPaid fill:#32CD32
```

## 12. CQRS Pattern Implementation Detail

```mermaid
graph LR
    subgraph "Write Side - Commands"
        CreateCmd[Create Command]
        UpdateCmd[Update Command]
        DeleteCmd[Delete Command]

        CreateHandler[Command Handler]

        ValidateCmd[Validation]
        DomainLogic[Domain Logic]
        Events[Domain Events]
    end

    subgraph "Read Side - Queries"
        GetQuery[Get Query]
        ListQuery[List Query]
        SearchQuery[Search Query]

        QueryHandler[Query Handler]

        ReadModel[Optimized Read Model]
        DTOs[Response DTOs]
    end

    subgraph "Shared"
        Database[(Single PostgreSQL DB)]
    end

    CreateCmd --> CreateHandler
    UpdateCmd --> CreateHandler
    DeleteCmd --> CreateHandler

    CreateHandler --> ValidateCmd
    ValidateCmd --> DomainLogic
    DomainLogic --> Events
    Events --> Database

    GetQuery --> QueryHandler
    ListQuery --> QueryHandler
    SearchQuery --> QueryHandler

    QueryHandler --> ReadModel
    ReadModel --> Database
    ReadModel --> DTOs

    style CreateCmd fill:#ffd700
    style UpdateCmd fill:#ffd700
    style DeleteCmd fill:#ffd700
    style GetQuery fill:#87ceeb
    style ListQuery fill:#87ceeb
    style SearchQuery fill:#87ceeb
    style Database fill:#336791
```

---

## Diagram Usage Guide

### Viewing These Diagrams

1. **In GitHub:** Diagrams render automatically in markdown preview
2. **In VS Code:** Install "Markdown Preview Mermaid Support" extension
3. **Online:** Copy diagram code to [Mermaid Live Editor](https://mermaid.live/)
4. **In Documentation:** Use these in your technical write-up

### Diagram Purposes

- **Diagram 1:** Overview for stakeholders and initial planning
- **Diagram 2:** Development guide for backend implementation
- **Diagram 3:** Database schema reference
- **Diagram 4:** Business logic reference for invoice states
- **Diagrams 5-8:** Implementation guides for specific features
- **Diagram 9:** Technology selection reference
- **Diagram 10:** Deployment planning
- **Diagram 11:** End-to-end business process
- **Diagram 12:** CQRS pattern implementation guide

### Exporting Diagrams

To export as images for presentations:

1. Visit [Mermaid Live Editor](https://mermaid.live/)
2. Paste diagram code
3. Click "Download" → PNG/SVG

---

**Note:** These diagrams are comprehensive representations of the InvoiceMe architecture as defined in PRD v2.0. Use them as reference during development and include them in your technical documentation deliverable.
