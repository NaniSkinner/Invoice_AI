# Phase 5: Payment Processing (CQRS + VSA)

**Time Estimate:** 4-6 hours
**Status:** Not Started
**Prerequisites:** Phase 4 (Invoice Management) completed

---

## What You'll Build

- **RecordPayment** command with idempotency
- Payment validation (overpayment, partial payments)
- Payment queries (GetById, GetByInvoice, ListAll)
- Public payment link endpoint (no authentication)
- Automatic invoice status update on full payment
- Client-side payment ID for idempotency
- Payment history view
- REST controller with public and authenticated endpoints
- Unit tests with idempotency scenarios
- Integration test for payment flow

---

## Task 5.1: Create Payment Application Structure

### Step 5.1.1: Create Vertical Slice Directories

```bash
cd ~/dev/Gauntlet/Invoice_AI/backend/src/main/java/com/invoiceme/application

# Create payment feature slices
mkdir -p payments/RecordPayment
mkdir -p payments/GetPayment
mkdir -p payments/ListPayments
mkdir -p payments/GetPaymentsByInvoice
```

**Expected Output:**
```
Created 4 directories for payment vertical slices
```

---

## Task 5.2: Implement RecordPayment Command

### Step 5.2.1: Create RecordPaymentCommand

```bash
cat > src/main/java/com/invoiceme/application/payments/RecordPayment/RecordPaymentCommand.java << 'EOF'
package com.invoiceme.application.payments.RecordPayment;

import java.math.BigDecimal;
import java.util.UUID;

public record RecordPaymentCommand(
    UUID clientPaymentId,  // Client-generated for idempotency
    UUID invoiceId,
    BigDecimal amount,
    String paymentMethod,
    String notes
) {}
EOF
```

### Step 5.2.2: Create RecordPaymentValidator

```bash
cat > src/main/java/com/invoiceme/application/payments/RecordPayment/RecordPaymentValidator.java << 'EOF'
package com.invoiceme.application.payments.RecordPayment;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceStatus;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import com.invoiceme.infrastructure.persistence.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class RecordPaymentValidator {
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    public void validate(RecordPaymentCommand command) {
        // Client payment ID required for idempotency
        if (command.clientPaymentId() == null) {
            throw new IllegalArgumentException(
                "Client payment ID is required for idempotency"
            );
        }

        // Check if payment already recorded (idempotency)
        if (paymentRepository.existsByClientPaymentId(
            command.clientPaymentId())) {
            throw new IllegalStateException(
                "Payment already recorded with this client payment ID: " +
                command.clientPaymentId()
            );
        }

        // Invoice must exist
        Invoice invoice = invoiceRepository.findById(command.invoiceId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Invoice not found: " + command.invoiceId()
            ));

        // Cannot pay cancelled invoice
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException(
                "Cannot record payment for cancelled invoice"
            );
        }

        // Amount must be positive
        if (command.amount() == null ||
            command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "Payment amount must be positive"
            );
        }

        // Check for overpayment
        BigDecimal totalPaid = paymentRepository
            .sumAmountByInvoiceId(invoice.getId());
        if (totalPaid == null) {
            totalPaid = BigDecimal.ZERO;
        }

        BigDecimal newTotal = totalPaid.add(command.amount());
        if (newTotal.compareTo(invoice.getTotalAmount()) > 0) {
            throw new IllegalArgumentException(
                String.format(
                    "Payment would exceed invoice total. " +
                    "Invoice: %s, Paid: %s, This payment: %s, Would be: %s",
                    invoice.getTotalAmount(),
                    totalPaid,
                    command.amount(),
                    newTotal
                )
            );
        }

        // Payment method required
        if (command.paymentMethod() == null ||
            command.paymentMethod().isBlank()) {
            throw new IllegalArgumentException("Payment method is required");
        }
    }
}
EOF
```

### Step 5.2.3: Create RecordPaymentHandler

```bash
cat > src/main/java/com/invoiceme/application/payments/RecordPayment/RecordPaymentHandler.java << 'EOF'
package com.invoiceme.application.payments.RecordPayment;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceStatus;
import com.invoiceme.domain.payment.Money;
import com.invoiceme.domain.payment.Payment;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import com.invoiceme.infrastructure.persistence.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordPaymentHandler {
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final RecordPaymentValidator validator;

    @Transactional
    public UUID handle(RecordPaymentCommand command) {
        // Validate
        validator.validate(command);

        // Fetch invoice
        Invoice invoice = invoiceRepository.findById(command.invoiceId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Invoice not found: " + command.invoiceId()
            ));

        // Create payment
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setClientPaymentId(command.clientPaymentId());
        payment.setInvoice(invoice);
        payment.setAmount(new Money(command.amount(), "USD"));
        payment.setPaymentMethod(command.paymentMethod());
        payment.setNotes(command.notes());
        payment.setPaymentDate(LocalDateTime.now());

        // Save payment
        Payment saved = paymentRepository.save(payment);

        // Calculate total paid
        BigDecimal totalPaid = paymentRepository
            .sumAmountByInvoiceId(invoice.getId());

        // Update invoice status if fully paid
        if (totalPaid.compareTo(invoice.getTotalAmount()) == 0) {
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaidDate(LocalDateTime.now());
            invoiceRepository.save(invoice);
        }

        return saved.getId();
    }
}
EOF
```

**Verification:**
```bash
ls -la src/main/java/com/invoiceme/application/payments/RecordPayment/
```

**Expected Output:**
```
-rw-r--r-- RecordPaymentCommand.java
-rw-r--r-- RecordPaymentValidator.java
-rw-r--r-- RecordPaymentHandler.java
```

---

## Task 5.3: Implement Payment Queries

### Step 5.3.1: Create PaymentDto

```bash
cat > src/main/java/com/invoiceme/application/payments/GetPayment/PaymentDto.java << 'EOF'
package com.invoiceme.application.payments.GetPayment;

import com.invoiceme.domain.payment.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentDto(
    UUID id,
    UUID clientPaymentId,
    UUID invoiceId,
    String invoiceNumber,
    BigDecimal amount,
    String currency,
    String paymentMethod,
    String notes,
    LocalDateTime paymentDate
) {
    public static PaymentDto fromEntity(Payment payment) {
        return new PaymentDto(
            payment.getId(),
            payment.getClientPaymentId(),
            payment.getInvoice().getId(),
            payment.getInvoice().getInvoiceNumber().getValue(),
            payment.getAmount().getAmount(),
            payment.getAmount().getCurrency(),
            payment.getPaymentMethod(),
            payment.getNotes(),
            payment.getPaymentDate()
        );
    }
}
EOF
```

### Step 5.3.2: Create GetPaymentHandler

```bash
cat > src/main/java/com/invoiceme/application/payments/GetPayment/GetPaymentHandler.java << 'EOF'
package com.invoiceme.application.payments.GetPayment;

import com.invoiceme.domain.payment.Payment;
import com.invoiceme.infrastructure.persistence.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPaymentHandler {
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public PaymentDto handle(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Payment not found: " + paymentId
            ));
        return PaymentDto.fromEntity(payment);
    }
}
EOF
```

### Step 5.3.3: Create ListPaymentsHandler

```bash
cat > src/main/java/com/invoiceme/application/payments/ListPayments/ListPaymentsHandler.java << 'EOF'
package com.invoiceme.application.payments.ListPayments;

import com.invoiceme.application.payments.GetPayment.PaymentDto;
import com.invoiceme.infrastructure.persistence.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListPaymentsHandler {
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public List<PaymentDto> handle() {
        return paymentRepository.findAll().stream()
            .map(PaymentDto::fromEntity)
            .toList();
    }
}
EOF
```

### Step 5.3.4: Create GetPaymentsByInvoiceHandler

```bash
cat > src/main/java/com/invoiceme/application/payments/GetPaymentsByInvoice/GetPaymentsByInvoiceHandler.java << 'EOF'
package com.invoiceme.application.payments.GetPaymentsByInvoice;

import com.invoiceme.application.payments.GetPayment.PaymentDto;
import com.invoiceme.infrastructure.persistence.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPaymentsByInvoiceHandler {
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public PaymentHistoryDto handle(UUID invoiceId) {
        List<PaymentDto> payments = paymentRepository
            .findByInvoiceId(invoiceId).stream()
            .map(PaymentDto::fromEntity)
            .toList();

        BigDecimal totalPaid = paymentRepository
            .sumAmountByInvoiceId(invoiceId);
        if (totalPaid == null) {
            totalPaid = BigDecimal.ZERO;
        }

        return new PaymentHistoryDto(payments, totalPaid);
    }

    public record PaymentHistoryDto(
        List<PaymentDto> payments,
        BigDecimal totalPaid
    ) {}
}
EOF
```

---

## Task 5.4: Add Custom Repository Methods

### Step 5.4.1: Update PaymentRepository

```bash
cat > src/main/java/com/invoiceme/infrastructure/persistence/PaymentRepository.java << 'EOF'
package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    boolean existsByClientPaymentId(UUID clientPaymentId);

    List<Payment> findByInvoiceId(UUID invoiceId);

    @Query("SELECT SUM(p.amount.amount) FROM Payment p WHERE p.invoice.id = :invoiceId")
    BigDecimal sumAmountByInvoiceId(@Param("invoiceId") UUID invoiceId);
}
EOF
```

---

## Task 5.5: Create PaymentController

### Step 5.5.1: Create REST Controller with Public Endpoint

```bash
cat > src/main/java/com/invoiceme/api/PaymentController.java << 'EOF'
package com.invoiceme.api;

import com.invoiceme.application.payments.GetPayment.GetPaymentHandler;
import com.invoiceme.application.payments.GetPayment.PaymentDto;
import com.invoiceme.application.payments.GetPaymentsByInvoice.GetPaymentsByInvoiceHandler;
import com.invoiceme.application.payments.GetPaymentsByInvoice.GetPaymentsByInvoiceHandler.PaymentHistoryDto;
import com.invoiceme.application.payments.ListPayments.ListPaymentsHandler;
import com.invoiceme.application.payments.RecordPayment.RecordPaymentCommand;
import com.invoiceme.application.payments.RecordPayment.RecordPaymentHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final RecordPaymentHandler recordPaymentHandler;
    private final GetPaymentHandler getPaymentHandler;
    private final ListPaymentsHandler listPaymentsHandler;
    private final GetPaymentsByInvoiceHandler getPaymentsByInvoiceHandler;

    // PUBLIC ENDPOINT - No authentication required
    @PostMapping("/api/public/payments")
    public ResponseEntity<Map<String, UUID>> recordPaymentPublic(
        @RequestBody RecordPaymentCommand command
    ) {
        UUID paymentId = recordPaymentHandler.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of("paymentId", paymentId));
    }

    // AUTHENTICATED ENDPOINTS
    @PostMapping("/api/payments")
    public ResponseEntity<Map<String, UUID>> recordPayment(
        @RequestBody RecordPaymentCommand command
    ) {
        UUID paymentId = recordPaymentHandler.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of("paymentId", paymentId));
    }

    @GetMapping("/api/payments/{id}")
    public ResponseEntity<PaymentDto> getPayment(@PathVariable UUID id) {
        PaymentDto payment = getPaymentHandler.handle(id);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/api/payments")
    public ResponseEntity<List<PaymentDto>> listPayments() {
        List<PaymentDto> payments = listPaymentsHandler.handle();
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/api/invoices/{invoiceId}/payments")
    public ResponseEntity<PaymentHistoryDto> getPaymentsByInvoice(
        @PathVariable UUID invoiceId
    ) {
        PaymentHistoryDto history = getPaymentsByInvoiceHandler
            .handle(invoiceId);
        return ResponseEntity.ok(history);
    }
}
EOF
```

---

## Task 5.6: Update Security Configuration for Public Endpoint

### Step 5.6.1: Update SecurityConfig

```bash
cat > src/main/java/com/invoiceme/config/SecurityConfig.java << 'EOF'
package com.invoiceme.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
        throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()  // Public endpoints
                .anyRequest().authenticated()
            )
            .httpBasic(withDefaults());
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        var user = User.builder()
            .username("demo")
            .password(passwordEncoder().encode("password"))
            .roles("USER")
            .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
EOF
```

---

## Task 5.7: Build and Run

### Step 5.7.1: Clean Build

```bash
cd ~/dev/Gauntlet/Invoice_AI/backend
./mvnw clean install
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 18.456 s
```

### Step 5.7.2: Run Application

```bash
./mvnw spring-boot:run
```

**Expected Output:**
```
Started InvoiceMeApplication in 3.678 seconds
```

---

## Task 5.8: Test with curl

### Step 5.8.1: Create Invoice to Pay

```bash
# First create a customer (save the customerId)
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -u demo:password \
  -d '{
    "businessName": "Payment Test Corp",
    "contactName": "Jane Smith",
    "email": "jane@paytest.com",
    "phone": "555-9999",
    "address": {
      "street": "456 Oak Ave",
      "city": "Portland",
      "state": "OR",
      "zipCode": "97201",
      "country": "USA"
    }
  }'

# Create invoice (save the invoiceId)
curl -X POST http://localhost:8080/api/invoices \
  -H "Content-Type: application/json" \
  -u demo:password \
  -d '{
    "customerId": "CUSTOMER_UUID",
    "issueDate": "2025-01-08",
    "dueDate": "2025-02-08",
    "lineItems": [
      {
        "description": "Consulting Services",
        "quantity": 10,
        "unitPrice": 150.00
      }
    ]
  }'
```

**Save the invoiceId for next steps. Invoice total is $1500.00**

### Step 5.8.2: Send Invoice

```bash
curl -X POST http://localhost:8080/api/invoices/INVOICE_UUID/send \
  -u demo:password
```

**Expected Output:**
```json
{"paymentLink": "/pay/some-unique-token"}
```

### Step 5.8.3: Record Partial Payment (Public Endpoint - No Auth)

```bash
# Generate a client payment ID (use uuidgen or online UUID generator)
# Example: b8e3f4a1-1234-5678-9abc-def012345678

curl -X POST http://localhost:8080/api/public/payments \
  -H "Content-Type: application/json" \
  -d '{
    "clientPaymentId": "b8e3f4a1-1234-5678-9abc-def012345678",
    "invoiceId": "INVOICE_UUID",
    "amount": 500.00,
    "paymentMethod": "Credit Card",
    "notes": "Partial payment 1 of 3"
  }'
```

**Expected Output:**
```json
{"paymentId": "some-payment-uuid"}
```

**Note:** Invoice status should still be SENT (not fully paid yet)

### Step 5.8.4: Verify Invoice Status (Still SENT)

```bash
curl -X GET http://localhost:8080/api/invoices/INVOICE_UUID \
  -u demo:password
```

**Expected Output:**
```json
{
  "status": "SENT",
  "totalAmount": 1500.00,
  ...
}
```

### Step 5.8.5: Check Payment History

```bash
curl -X GET http://localhost:8080/api/invoices/INVOICE_UUID/payments \
  -u demo:password
```

**Expected Output:**
```json
{
  "payments": [
    {
      "id": "...",
      "clientPaymentId": "b8e3f4a1-1234-5678-9abc-def012345678",
      "amount": 500.00,
      "paymentMethod": "Credit Card",
      "notes": "Partial payment 1 of 3",
      "paymentDate": "2025-01-08T..."
    }
  ],
  "totalPaid": 500.00
}
```

### Step 5.8.6: Record Second Partial Payment

```bash
# Generate NEW client payment ID
curl -X POST http://localhost:8080/api/public/payments \
  -H "Content-Type: application/json" \
  -d '{
    "clientPaymentId": "c9f4a5b2-2345-6789-0bcd-ef0123456789",
    "invoiceId": "INVOICE_UUID",
    "amount": 500.00,
    "paymentMethod": "Bank Transfer",
    "notes": "Partial payment 2 of 3"
  }'
```

### Step 5.8.7: Record Final Payment (Should Mark Invoice as PAID)

```bash
# Generate NEW client payment ID
curl -X POST http://localhost:8080/api/public/payments \
  -H "Content-Type: application/json" \
  -d '{
    "clientPaymentId": "d0a5b6c3-3456-7890-1cde-f01234567890",
    "invoiceId": "INVOICE_UUID",
    "amount": 500.00,
    "paymentMethod": "Credit Card",
    "notes": "Final payment"
  }'
```

### Step 5.8.8: Verify Invoice Status Changed to PAID

```bash
curl -X GET http://localhost:8080/api/invoices/INVOICE_UUID \
  -u demo:password
```

**Expected Output:**
```json
{
  "status": "PAID",
  "totalAmount": 1500.00,
  "paidDate": "2025-01-08T...",
  ...
}
```

### Step 5.8.9: Verify All Payments Recorded

```bash
curl -X GET http://localhost:8080/api/invoices/INVOICE_UUID/payments \
  -u demo:password
```

**Expected Output:**
```json
{
  "payments": [
    {
      "amount": 500.00,
      "paymentMethod": "Credit Card",
      "notes": "Partial payment 1 of 3",
      ...
    },
    {
      "amount": 500.00,
      "paymentMethod": "Bank Transfer",
      "notes": "Partial payment 2 of 3",
      ...
    },
    {
      "amount": 500.00,
      "paymentMethod": "Credit Card",
      "notes": "Final payment",
      ...
    }
  ],
  "totalPaid": 1500.00
}
```

### Step 5.8.10: Test Idempotency (Try Duplicate Payment)

```bash
# Try to record the same payment again with same clientPaymentId
curl -X POST http://localhost:8080/api/public/payments \
  -H "Content-Type: application/json" \
  -d '{
    "clientPaymentId": "b8e3f4a1-1234-5678-9abc-def012345678",
    "invoiceId": "INVOICE_UUID",
    "amount": 500.00,
    "paymentMethod": "Credit Card",
    "notes": "Duplicate attempt"
  }'
```

**Expected Output:**
```json
{
  "error": "Payment already recorded with this client payment ID: b8e3f4a1-1234-5678-9abc-def012345678"
}
```

### Step 5.8.11: Test Overpayment Prevention

```bash
# Create new invoice
curl -X POST http://localhost:8080/api/invoices \
  -H "Content-Type: application/json" \
  -u demo:password \
  -d '{
    "customerId": "CUSTOMER_UUID",
    "issueDate": "2025-01-08",
    "dueDate": "2025-02-08",
    "lineItems": [
      {
        "description": "Small Service",
        "quantity": 1,
        "unitPrice": 100.00
      }
    ]
  }'

# Send it
curl -X POST http://localhost:8080/api/invoices/NEW_INVOICE_UUID/send \
  -u demo:password

# Try to pay more than invoice total
curl -X POST http://localhost:8080/api/public/payments \
  -H "Content-Type: application/json" \
  -d '{
    "clientPaymentId": "e1b6c7d4-4567-8901-2def-012345678901",
    "invoiceId": "NEW_INVOICE_UUID",
    "amount": 150.00,
    "paymentMethod": "Credit Card",
    "notes": "Overpayment attempt"
  }'
```

**Expected Output:**
```json
{
  "error": "Payment would exceed invoice total. Invoice: 100.00, Paid: 0.00, This payment: 150.00, Would be: 150.00"
}
```

---

## Task 5.9: Write Unit Tests

### Step 5.9.1: Create RecordPaymentHandlerTest

```bash
cat > src/test/java/com/invoiceme/application/payments/RecordPayment/RecordPaymentHandlerTest.java << 'EOF'
package com.invoiceme.application.payments.RecordPayment;

import com.invoiceme.domain.customer.Customer;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceNumber;
import com.invoiceme.domain.invoice.InvoiceStatus;
import com.invoiceme.domain.payment.Payment;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import com.invoiceme.infrastructure.persistence.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordPaymentHandlerTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private RecordPaymentValidator validator;

    @InjectMocks
    private RecordPaymentHandler handler;

    private Invoice invoice;
    private UUID clientPaymentId;

    @BeforeEach
    void setUp() {
        clientPaymentId = UUID.randomUUID();

        invoice = new Invoice();
        invoice.setId(UUID.randomUUID());
        invoice.setInvoiceNumber(new InvoiceNumber("INV-000001"));
        invoice.setCustomer(new Customer());
        invoice.setIssueDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(30));
        invoice.setStatus(InvoiceStatus.SENT);
        invoice.setTotalAmount(BigDecimal.valueOf(1000));
    }

    @Test
    void handle_ShouldRecordPayment_AndMarkInvoiceAsPaid() {
        // Arrange
        RecordPaymentCommand command = new RecordPaymentCommand(
            clientPaymentId,
            invoice.getId(),
            BigDecimal.valueOf(1000),
            "Credit Card",
            "Full payment"
        );

        when(invoiceRepository.findById(invoice.getId()))
            .thenReturn(Optional.of(invoice));
        when(paymentRepository.save(any(Payment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.sumAmountByInvoiceId(invoice.getId()))
            .thenReturn(BigDecimal.valueOf(1000));

        // Act
        UUID paymentId = handler.handle(command);

        // Assert
        assertNotNull(paymentId);

        ArgumentCaptor<Payment> paymentCaptor =
            ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());

        Payment savedPayment = paymentCaptor.getValue();
        assertEquals(clientPaymentId, savedPayment.getClientPaymentId());
        assertEquals(BigDecimal.valueOf(1000),
            savedPayment.getAmount().getAmount());

        // Verify invoice marked as PAID
        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
        assertNotNull(invoice.getPaidDate());
        verify(invoiceRepository).save(invoice);
    }

    @Test
    void handle_ShouldRecordPartialPayment_WithoutMarkingAsPaid() {
        // Arrange
        RecordPaymentCommand command = new RecordPaymentCommand(
            clientPaymentId,
            invoice.getId(),
            BigDecimal.valueOf(500),
            "Credit Card",
            "Partial payment"
        );

        when(invoiceRepository.findById(invoice.getId()))
            .thenReturn(Optional.of(invoice));
        when(paymentRepository.save(any(Payment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.sumAmountByInvoiceId(invoice.getId()))
            .thenReturn(BigDecimal.valueOf(500));

        // Act
        UUID paymentId = handler.handle(command);

        // Assert
        assertNotNull(paymentId);

        // Verify invoice NOT marked as PAID
        assertEquals(InvoiceStatus.SENT, invoice.getStatus());
        assertNull(invoice.getPaidDate());
        verify(invoiceRepository, never()).save(invoice);
    }
}
EOF
```

### Step 5.9.2: Run Tests

```bash
./mvnw test
```

**Expected Output:**
```
Tests run: 20, Failures: 0, Errors: 0, Skipped: 0

[INFO] BUILD SUCCESS
```

---

## Task 5.10: Write Integration Test

### Step 5.10.1: Create PaymentIntegrationTest

```bash
cat > src/test/java/com/invoiceme/api/PaymentIntegrationTest.java << 'EOF'
package com.invoiceme.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.invoiceme.application.invoices.CreateInvoice.CreateInvoiceCommand;
import com.invoiceme.application.payments.RecordPayment.RecordPaymentCommand;
import com.invoiceme.domain.customer.Address;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import com.invoiceme.infrastructure.persistence.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private UUID customerId;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        invoiceRepository.deleteAll();
        customerRepository.deleteAll();

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setBusinessName("Test Corp");
        customer.setContactName("John Doe");
        customer.setEmail("john@test.com");
        customer.setPhone("555-1234");
        customer.setAddress(new Address(
            "123 Main St", "SF", "CA", "94105", "USA"
        ));
        customer = customerRepository.save(customer);
        customerId = customer.getId();
    }

    @Test
    void fullPaymentFlow_WithPartialPayments_ShouldSucceed() throws Exception {
        // Create invoice
        CreateInvoiceCommand createCommand = new CreateInvoiceCommand(
            customerId,
            LocalDate.of(2025, 1, 8),
            LocalDate.of(2025, 2, 8),
            List.of(
                new CreateInvoiceCommand.LineItemDto(
                    "Service",
                    10,
                    BigDecimal.valueOf(100)
                )
            )
        );

        String createResponse = mockMvc.perform(
                post("/api/invoices")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createCommand))
            )
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String invoiceId = objectMapper.readTree(createResponse)
            .get("invoiceId").asText();

        // Send invoice
        mockMvc.perform(post("/api/invoices/" + invoiceId + "/send"))
            .andExpect(status().isOk());

        // First partial payment
        RecordPaymentCommand payment1 = new RecordPaymentCommand(
            UUID.randomUUID(),
            UUID.fromString(invoiceId),
            BigDecimal.valueOf(400),
            "Credit Card",
            "Partial 1"
        );

        mockMvc.perform(post("/api/public/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payment1)))
            .andExpect(status().isCreated());

        // Verify still SENT
        mockMvc.perform(get("/api/invoices/" + invoiceId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SENT"));

        // Second payment (completes total)
        RecordPaymentCommand payment2 = new RecordPaymentCommand(
            UUID.randomUUID(),
            UUID.fromString(invoiceId),
            BigDecimal.valueOf(600),
            "Bank Transfer",
            "Final"
        );

        mockMvc.perform(post("/api/public/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payment2)))
            .andExpect(status().isCreated());

        // Verify now PAID
        mockMvc.perform(get("/api/invoices/" + invoiceId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PAID"))
            .andExpect(jsonPath("$.paidDate").exists());

        // Verify payment history
        mockMvc.perform(get("/api/invoices/" + invoiceId + "/payments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.payments.length()").value(2))
            .andExpect(jsonPath("$.totalPaid").value(1000.00));
    }

    @Test
    void idempotency_ShouldPreventDuplicatePayments() throws Exception {
        // Create and send invoice
        CreateInvoiceCommand createCommand = new CreateInvoiceCommand(
            customerId,
            LocalDate.of(2025, 1, 8),
            LocalDate.of(2025, 2, 8),
            List.of(
                new CreateInvoiceCommand.LineItemDto(
                    "Service",
                    1,
                    BigDecimal.valueOf(100)
                )
            )
        );

        String createResponse = mockMvc.perform(
                post("/api/invoices")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createCommand))
            )
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String invoiceId = objectMapper.readTree(createResponse)
            .get("invoiceId").asText();

        mockMvc.perform(post("/api/invoices/" + invoiceId + "/send"))
            .andExpect(status().isOk());

        // Record payment
        UUID clientPaymentId = UUID.randomUUID();
        RecordPaymentCommand payment = new RecordPaymentCommand(
            clientPaymentId,
            UUID.fromString(invoiceId),
            BigDecimal.valueOf(100),
            "Credit Card",
            "Payment"
        );

        mockMvc.perform(post("/api/public/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payment)))
            .andExpect(status().isCreated());

        // Try duplicate with same clientPaymentId
        mockMvc.perform(post("/api/public/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payment)))
            .andExpect(status().is4xxClientError());
    }
}
EOF
```

### Step 5.10.2: Run Integration Test

```bash
./mvnw test -Dtest=PaymentIntegrationTest
```

**Expected Output:**
```
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
```

---

## Task 5.11: Git Commit

### Step 5.11.1: Check Status

```bash
git status
```

### Step 5.11.2: Add All Changes

```bash
git add .
```

### Step 5.11.3: Commit

```bash
git commit -m "$(cat <<'EOF'
Phase 5: Payment Processing (CQRS + VSA)

Implemented payment recording with idempotency:
- RecordPayment command with client-generated payment ID
- Payment validation (overpayment, partial payments)
- Automatic invoice status update on full payment
- Payment queries (GetById, List, GetByInvoice)
- Public payment endpoint (no authentication)
- PaymentController with public and authenticated endpoints
- Custom repository methods (existsByClientPaymentId, sumAmountByInvoiceId)
- Security configuration for public endpoints
- Unit tests with idempotency scenarios
- Integration test for full payment flow

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
EOF
)"
```

---

## Verification Checklist

After completing all tasks, verify:

- [ ] RecordPayment command handler created with idempotency
- [ ] Payment validation prevents overpayment
- [ ] Partial payments supported
- [ ] Full payment automatically marks invoice as PAID
- [ ] Public payment endpoint accessible without auth
- [ ] Authenticated payment endpoints require credentials
- [ ] Payment history shows all payments for an invoice
- [ ] Client payment ID prevents duplicate payments
- [ ] Custom repository methods working
- [ ] curl tests successful for all scenarios
- [ ] Idempotency test passes
- [ ] Overpayment prevention test passes
- [ ] Unit tests passing
- [ ] Integration tests passing
- [ ] Git commit created

---

## Troubleshooting

### Issue: "Payment already recorded" error
**Solution:** This is expected behavior for idempotency. Use a new UUID for clientPaymentId

### Issue: "Payment would exceed invoice total" error
**Solution:** Check total amount already paid. Sum of all payments cannot exceed invoice total

### Issue: Invoice not marked as PAID
**Solution:** Verify that sum of all payments exactly equals invoice total amount

### Issue: 401 Unauthorized on public endpoint
**Solution:** Ensure SecurityConfig allows /api/public/** endpoints without authentication

### Issue: Can't find sumAmountByInvoiceId method
**Solution:** Verify PaymentRepository interface has the @Query annotation

---

## What's Next?

Continue to [Phase-06-Tasks.md](Phase-06-Tasks.md) for AI Email Reminder System implementation.

---

**Phase 5 Complete!** ✅

You now have a fully functional payment processing system with idempotency, partial payment support, and automatic invoice status updates.
