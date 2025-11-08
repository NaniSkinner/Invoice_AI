# Phases 4-15 Tasks Summary

**Quick Reference for Remaining Implementation Phases**

**Note:** For complete code examples, refer to the architecture documents:
- [Phase-04-Invoice-Management.md](Phase-04-Invoice-Management.md)
- [Phase-05-Payment-Processing.md](Phase-05-Payment-Processing.md)
- [Phase-06-AI-Email-Reminder.md](Phase-06-AI-Email-Reminder.md)
- [Phase-07-AI-Chat-Assistant.md](Phase-07-AI-Chat-Assistant.md)
- [Phase-08-15-Frontend-Testing-Delivery.md](Phase-08-15-Frontend-Testing-Delivery.md)

---

## Phase 4: Invoice Management (10-12 hours)

### Task 4.1: Create Invoice Command Structure
```bash
mkdir -p src/main/java/com/invoiceme/application/invoices/{CreateInvoice,UpdateInvoice,SendInvoice,CancelInvoice,MarkAsPaid}
```

### Task 4.2: Implement CreateInvoice
**Files to create:**
- `CreateInvoiceCommand.java` - with LineItemDto list
- `CreateInvoiceHandler.java` - generates invoice number, adds line items
- Test: Invoice starts in DRAFT status

### Task 4.3: Implement SendInvoice (DRAFT → SENT)
**Files to create:**
- `SendInvoiceCommand.java` - just invoiceId
- `SendInvoiceHandler.java` - calls `invoice.send()`, generates payment link
- Test: Can only send DRAFT invoices with line items

### Task 4.4: Implement CancelInvoice
**Files to create:**
- `CancelInvoiceCommand.java` - invoiceId + cancellationReason
- `CancelInvoiceHandler.java` - calls `invoice.cancel(reason)`
- Test: Can cancel from any state

### Task 4.5: Implement MarkAsPaid
**Files to create:**
- `MarkAsPaidCommand.java`
- `MarkAsPaidHandler.java` - manual override for SENT → PAID
- Test: Can only mark SENT invoices as paid

### Task 4.6: Implement Invoice Queries
**Files to create:**
- `InvoiceDto.java` - complete with lineItems list
- `GetInvoiceHandler.java` - returns invoice with all details
- `ListOverdueInvoicesHandler.java` - finds overdue invoices
- `ListInvoicesByStatusHandler.java`
- `ListInvoicesByCustomerHandler.java`

### Task 4.7: Create InvoiceController
**Endpoints:**
```java
POST   /api/invoices              - Create
GET    /api/invoices/{id}         - Get by ID
PUT    /api/invoices/{id}         - Update
POST   /api/invoices/{id}/send    - Send
POST   /api/invoices/{id}/cancel  - Cancel
POST   /api/invoices/{id}/mark-paid - Mark as paid
GET    /api/invoices/overdue      - List overdue
```

### Task 4.8: Test with curl
```bash
# Create invoice
curl -X POST http://localhost:8080/api/invoices \
  -u demo:password \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUSTOMER_UUID",
    "dueDate": "2025-12-31",
    "lineItems": [{
      "description": "Consulting",
      "quantity": 10,
      "unitPrice": 150.00
    }]
  }'

# Send invoice
curl -X POST http://localhost:8080/api/invoices/{id}/send -u demo:password
```

**Verification:**
- [ ] Invoice lifecycle works (Draft → Sent → Paid)
- [ ] Line items calculate totals correctly
- [ ] State machine enforces rules
- [ ] Payment link generated on send
- [ ] All tests pass

---

## Phase 5: Payment Processing (4-6 hours)

### Task 5.1: Create Payment Structure
```bash
mkdir -p src/main/java/com/invoiceme/application/payments/{RecordPayment,GetPayment,ListPayments}
```

### Task 5.2: Implement RecordPayment with Idempotency
**Key code in RecordPaymentHandler:**
```java
// Idempotency check
if (command.getPaymentId() != null && paymentRepository.existsById(command.getPaymentId())) {
    return command.getPaymentId();
}

// Validate payment
payment.validate(); // Checks overpayment, partial payment rules

// Update invoice
invoice.setAmountPaid(invoice.getAmountPaid().add(payment.getPaymentAmount()));
invoice.calculateTotals();

// Auto-transition to PAID if balance = 0
if (invoice.getBalanceRemaining().compareTo(BigDecimal.ZERO) == 0) {
    invoice.markAsPaid();
}
```

### Task 5.3: Create PaymentController
**Endpoints:**
```java
POST   /api/payments                      - Record payment
GET    /api/payments/{id}                 - Get by ID
GET    /api/payments/invoice/{id}         - List for invoice

// Public endpoints (no auth)
GET    /api/payments/link/{link}          - Get invoice by payment link
POST   /api/payments/link/{link}/pay      - Submit payment
```

### Task 5.4: Test Idempotency
```bash
# Generate UUID for payment
PAYMENT_ID=$(uuidgen)

# Submit same payment twice
curl -X POST http://localhost:8080/api/payments \
  -u demo:password \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "'$PAYMENT_ID'",
    "invoiceId": "INVOICE_UUID",
    "paymentAmount": 100.00,
    "paymentMethod": "CREDIT_CARD"
  }'

# Submit again with same paymentId - should return same result
curl -X POST http://localhost:8080/api/payments \
  -u demo:password \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "'$PAYMENT_ID'",
    "invoiceId": "INVOICE_UUID",
    "paymentAmount": 100.00,
    "paymentMethod": "CREDIT_CARD"
  }'
```

**Verification:**
- [ ] Payment idempotency works
- [ ] Overpayment prevented
- [ ] Partial payment validation works
- [ ] Invoice auto-transitions to PAID
- [ ] Public payment link works (no auth)

---

## Phase 6: AI Email Reminder (6-8 hours)

### Task 6.1: Add OpenAI Configuration
```bash
# Create config
cat > src/main/java/com/invoiceme/config/OpenAIConfig.java << 'EOF'
@Configuration
public class OpenAIConfig {
    @Bean
    public OpenAiService openAiService(@Value("${openai.api.key}") String apiKey) {
        return new OpenAiService(apiKey, Duration.ofSeconds(30));
    }
}
EOF
```

### Task 6.2: Create OpenAI Service Wrapper
```bash
mkdir -p src/main/java/com/invoiceme/infrastructure/ai

# Create OpenAIServiceWrapper.java
# Implements: generateEmailReminder(prompt) and generateEmailSubject()
```

### Task 6.3: Implement GenerateEmailReminder
**Key prompt structure:**
```java
String prompt = String.format(
    "Generate an overdue invoice reminder email with the following details:\n\n" +
    "Customer Name: %s\n" +
    "Invoice Number: %s\n" +
    "Days Overdue: %d\n" +
    "Balance Remaining: $%.2f\n\n" +
    "Instructions:\n" +
    "- Use a friendly but professional tone\n" +
    "- Keep under 200 words\n" +
    "- Include payment link placeholder\n" +
    "Generate ONLY the email body, no subject line.",
    customerName, invoiceNumber, daysOverdue, balance
);
```

### Task 6.4: Create Overdue Scheduler
```bash
# Create OverdueInvoiceScheduler.java
@Scheduled(cron = "${scheduling.overdue-check.cron}")
public void checkOverdueInvoices() {
    List<Invoice> overdueInvoices = invoiceRepository
        .findByStatusAndDueDateBefore(InvoiceStatus.SENT, LocalDate.now());

    // Filter by reminder settings
    // Log results
}
```

### Task 6.5: Create AI REST Endpoints
```java
POST   /api/ai/generate-reminder    - Generate email
POST   /api/ai/send-reminder        - Mock send (update timestamp)
POST   /api/ai/reminder-settings    - Update frequency
```

### Task 6.6: Test AI Generation
```bash
# Get your OpenAI API key
export OPENAI_API_KEY="sk-your-key"

# Add to .env
echo "OPENAI_API_KEY=$OPENAI_API_KEY" >> backend/.env

# Test generation
curl -X POST http://localhost:8080/api/ai/generate-reminder \
  -u demo:password \
  -H "Content-Type: application/json" \
  -d '{"invoiceId": "OVERDUE_INVOICE_UUID"}'
```

**Verification:**
- [ ] OpenAI API configured
- [ ] Email generation works
- [ ] Scheduler identifies overdue invoices
- [ ] Reminder frequency management works
- [ ] Mock email sending updates timestamp

---

## Phase 7: AI Chat Assistant (8-10 hours)

### Task 7.1: Define OpenAI Functions
```bash
# Create FunctionDefinitions.java with 7 functions:
# - getOverdueInvoices
# - getTotalAmountOwed
# - getInvoicesByCustomer
# - getInvoicesByStatus
# - getPaymentHistory
# - getCustomerSummary
# - getInvoiceStatistics
```

### Task 7.2: Implement ChatQueryService
```java
public String executeFunction(String functionName, JsonNode arguments) {
    switch (functionName) {
        case "getOverdueInvoices":
            return getOverdueInvoices(arguments);
        // ... implement all 7 functions
    }
}
```

### Task 7.3: Create ProcessChatQueryHandler
**Key flow:**
```java
// 1. Send user message + functions to OpenAI
// 2. OpenAI returns function call
// 3. Execute function locally
// 4. Send function result back to OpenAI
// 5. OpenAI formats natural language response
// 6. Return to user
```

### Task 7.4: Create Chat Endpoint
```java
POST   /api/ai/chat   - Process chat message with history
```

### Task 7.5: Test Chat Assistant
```bash
curl -X POST http://localhost:8080/api/ai/chat \
  -u demo:password \
  -H "Content-Type: application/json" \
  -d '{
    "message": "How many invoices are overdue?",
    "history": []
  }'
```

**Verification:**
- [ ] All 7 functions implemented
- [ ] Function calling works
- [ ] Natural language responses generated
- [ ] Conversation context maintained
- [ ] Read-only (no data modification)

---

## Phase 8: Customer Management UI (4-6 hours)

### Task 8.1: Create Customer Pages
```bash
cd ~/dev/Gauntlet/Invoice_AI/frontend

mkdir -p src/app/customers
mkdir -p src/components/customers
```

### Task 8.2: Create Customer List Page
```typescript
// src/app/customers/page.tsx
export default function CustomersPage() {
  const [customers, setCustomers] = useState<CustomerDto[]>([]);

  useEffect(() => {
    apiClient.get('/customers').then(setCustomers);
  }, []);

  return (
    <div className="container mx-auto p-6">
      <h1 className="text-3xl font-bold mb-6">Customers</h1>
      <input placeholder="Search..." />
      <table>
        {/* Customer rows */}
      </table>
    </div>
  );
}
```

### Task 8.3: Create Customer Form
```typescript
// src/components/customers/CustomerForm.tsx
// Use react-hook-form + zod for validation
const schema = z.object({
  businessName: z.string().min(1),
  email: z.string().email(),
  billingAddress: z.object({
    street: z.string(),
    city: z.string(),
    // ...
  })
});
```

### Task 8.4: Test Frontend
```bash
npm run dev
# Visit http://localhost:3000/customers
```

**Verification:**
- [ ] Customer list displays
- [ ] Search works
- [ ] Can create customer
- [ ] Can edit customer
- [ ] Delete shows confirmation
- [ ] Form validation works

---

## Phase 9: Invoice Management UI (10-12 hours)

### Task 9.1: Create Invoice Dashboard
```typescript
// src/app/invoices/page.tsx
// Show: Total invoices, overdue count, revenue stats
// Recent invoices table
```

### Task 9.2: Create Invoice List with Tabs
```typescript
// Tabs: All | Draft | Sent | Paid | Overdue | Cancelled
const [activeTab, setActiveTab] = useState('all');
```

### Task 9.3: Create Invoice Form
```typescript
// Dynamic line items:
const [lineItems, setLineItems] = useState([{
  description: '', quantity: 1, unitPrice: 0
}]);

const addLineItem = () => {
  setLineItems([...lineItems, { description: '', quantity: 1, unitPrice: 0 }]);
};

// Real-time total calculation
const total = lineItems.reduce((sum, item) =>
  sum + (item.quantity * item.unitPrice), 0
);
```

### Task 9.4: Create Send Invoice Modal
```typescript
// Call AI generate reminder
const handleSend = async () => {
  const email = await apiClient.post('/ai/generate-reminder', { invoiceId });
  setEmailPreview(email);
  // Show modal with editable email
};
```

**Verification:**
- [ ] Dashboard shows stats
- [ ] Invoice list filters by status
- [ ] Can create invoice with line items
- [ ] Totals calculate correctly
- [ ] Send invoice shows AI email
- [ ] Cancel invoice works
- [ ] Overdue section works

---

## Phase 10: Payment UI (4-6 hours)

### Task 10.1: Create Public Payment Page
```typescript
// src/app/pay/[paymentLink]/page.tsx
// NO authentication required
export default function PaymentPage({ params }) {
  const [invoice, setInvoice] = useState<InvoiceDto | null>(null);

  useEffect(() => {
    fetch(`/api/payments/link/${params.paymentLink}`)
      .then(res => res.json())
      .then(setInvoice);
  }, []);

  // Payment form with validation
}
```

### Task 10.2: Generate Client-Side Payment ID
```typescript
import { v4 as uuidv4 } from 'uuid';

const handlePayment = async () => {
  const paymentId = uuidv4(); // For idempotency
  await apiClient.post(`/payments/link/${paymentLink}/pay`, {
    paymentId,
    paymentAmount,
    paymentMethod
  });
};
```

**Verification:**
- [ ] Public payment page loads (no auth)
- [ ] Invoice details display
- [ ] Payment validation works
- [ ] Idempotency ID generated
- [ ] Payment submission works
- [ ] Confirmation page shows

---

## Phase 11: AI Chat UI (4-6 hours)

### Task 11.1: Create Chat Bubble
```typescript
// src/components/ai/ChatBubble.tsx
export default function ChatBubble() {
  const [isOpen, setIsOpen] = useState(false);

  return (
    <>
      <button className="fixed bottom-6 right-6 ...">
        <MessageIcon />
      </button>
      {isOpen && <ChatWindow onClose={() => setIsOpen(false)} />}
    </>
  );
}
```

### Task 11.2: Create Chat Window
```typescript
const [messages, setMessages] = useState<ChatMessage[]>([]);
const [isLoading, setIsLoading] = useState(false);

const sendMessage = async (text: string) => {
  setMessages([...messages, { role: 'user', content: text }]);
  setIsLoading(true);

  const response = await apiClient.post('/ai/chat', {
    message: text,
    history: messages
  });

  setMessages([...messages,
    { role: 'user', content: text },
    { role: 'assistant', content: response.message }
  ]);
  setIsLoading(false);
};
```

**Verification:**
- [ ] Chat bubble shows bottom-right
- [ ] Window expands on click
- [ ] Messages display correctly
- [ ] Typing indicator shows
- [ ] Context maintained
- [ ] Follow-up questions work

---

## Phase 12: Integration Testing (6-8 hours)

### Task 12.1: E2E Customer-Invoice-Payment Test
```java
@SpringBootTest
@Transactional
class EndToEndFlowTest {
    @Test
    void completeBusinessFlow() {
        // 1. Create customer
        UUID customerId = createCustomer();

        // 2. Create invoice
        UUID invoiceId = createInvoice(customerId);

        // 3. Send invoice
        sendInvoice(invoiceId);

        // 4. Record payment
        recordPayment(invoiceId);

        // 5. Verify PAID
        Invoice invoice = getInvoice(invoiceId);
        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
    }
}
```

### Task 12.2: Generate Coverage Report
```bash
./mvnw test jacoco:report

# View report
open target/site/jacoco/index.html
```

**Verification:**
- [ ] E2E test passes
- [ ] AI tests with mocks pass
- [ ] Scheduler test passes
- [ ] Idempotency test passes
- [ ] Coverage ≥ 80% for business logic

---

## Phase 13: Mockup Data (4-6 hours)

### Task 13.1: Create Mockup SQL
```sql
-- database/mockup-data.sql
INSERT INTO customers (id, business_name, contact_name, email, ...) VALUES
('11111111-1111-1111-1111-111111111111', 'Acme Corp', 'John Smith', 'john@acme.com', ...),
-- 9 more customers

INSERT INTO invoices (id, invoice_number, customer_id, status, due_date, ...) VALUES
-- 5 DRAFT invoices
-- 8 SENT invoices
-- 5 OVERDUE invoices (due date in past)
-- 7 PAID invoices
-- 2 CANCELLED invoices

INSERT INTO invoice_line_items (id, invoice_id, description, quantity, unit_price, ...) VALUES
-- Line items for all invoices

INSERT INTO payments (id, invoice_id, payment_amount, ...) VALUES
-- Payments for PAID invoices
```

### Task 13.2: Load Data
```bash
psql -d invoiceme -f database/mockup-data.sql
```

**Verification:**
- [ ] 10 customers loaded
- [ ] 25 invoices across all statuses
- [ ] Line items for all invoices
- [ ] Payment records for paid invoices
- [ ] Dashboard shows realistic data

---

## Phase 14: Documentation (6-8 hours)

### Task 14.1: Write Main README
```markdown
# InvoiceMe

## Quick Start
1. `createdb invoiceme`
2. `cd backend && ./mvnw spring-boot:run`
3. `cd frontend && npm run dev`
4. Demo credentials: demo/password

## Features
- Customer management
- Invoice lifecycle
- AI overdue reminders
- AI chat assistant
```

### Task 14.2: Document Architecture
```markdown
# Architecture

## DDD
- Bounded contexts: Customer, Invoice, Payment
- Rich domain models with business logic
- Value objects: Address, InvoiceNumber

## CQRS
- Commands: CreateCustomer, CreateInvoice, RecordPayment
- Queries: GetCustomer, ListInvoices

## VSA
- Features organized by vertical slice
- Self-contained: CreateCustomer/ has all related code
```

### Task 14.3: Document AI Tools Usage
```markdown
# AI Tools Usage

## Tools Used
- Claude for architecture design
- GitHub Copilot for boilerplate

## Example Prompts
1. "Generate Spring Boot service for CQRS command handler"
   - Saved 45 minutes
   - Modified validation logic

## Productivity
- Total time: 5 days
- Estimated without AI: 8-10 days
- Time saved: 38%
```

**Verification:**
- [ ] README complete
- [ ] Architecture documented
- [ ] AI usage documented
- [ ] Test results documented
- [ ] Deployment diagrams created

---

## Phase 15: Demo & Delivery (4-6 hours)

### Task 15.1: Record Demo Video
**Script (10-15 minutes):**
1. Intro: Project overview (1 min)
2. Customer management demo (2 min)
3. Invoice lifecycle demo (3 min)
4. AI features showcase (4 min)
5. Payment flow (2 min)
6. Edge cases (2 min)
7. Architecture walkthrough (1 min)

### Task 15.2: Final Code Review
```bash
# Remove debug code
grep -r "console.log" frontend/src/
grep -r "System.out.println" backend/src/

# Remove TODOs
grep -r "TODO" backend/src/
grep -r "TODO" frontend/src/

# Format code
./mvnw fmt:format
npm run format
```

### Task 15.3: Verify Deliverables
- [ ] GitHub repository ready
- [ ] Demo video uploaded
- [ ] All documentation complete
- [ ] Tests passing
- [ ] Clean code (no debug statements)

---

## Quick Command Reference

### Build & Run
```bash
# Backend
cd backend
./mvnw spring-boot:run

# Frontend
cd frontend
npm run dev

# Database
createdb invoiceme
psql -d invoiceme -f database/mockup-data.sql
```

### Testing
```bash
# Unit tests
./mvnw test

# Integration tests
./mvnw verify

# Coverage
./mvnw test jacoco:report
open target/site/jacoco/index.html
```

### Git Commits
```bash
git add .
git commit -m "Phase X: Description"
git push
```

---

## Success Tracking

### Phases 4-7 (Backend)
- [ ] Phase 4: Invoice Management
- [ ] Phase 5: Payment Processing
- [ ] Phase 6: AI Email Reminder
- [ ] Phase 7: AI Chat Assistant

### Phases 8-11 (Frontend)
- [ ] Phase 8: Customer UI
- [ ] Phase 9: Invoice UI
- [ ] Phase 10: Payment UI
- [ ] Phase 11: AI Chat UI

### Phases 12-15 (Quality)
- [ ] Phase 12: Integration Testing
- [ ] Phase 13: Mockup Data
- [ ] Phase 14: Documentation
- [ ] Phase 15: Demo & Delivery

---

**For detailed code examples, see the corresponding architecture documents.**

**Total Remaining Time: ~75-95 hours (4-6 days)**
