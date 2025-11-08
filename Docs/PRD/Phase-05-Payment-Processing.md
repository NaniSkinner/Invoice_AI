# Phase 5: Payment Processing (CQRS + VSA)

**Estimated Time:** 4-6 hours
**Dependencies:** Phase 4 (Invoice Management)
**Status:** Not Started

## Overview

Implement payment processing with idempotency, overpayment prevention, partial payment logic, and automatic invoice status updates using CQRS and Vertical Slice Architecture.

## Objectives

- Implement RecordPayment command with client-side idempotency
- Implement payment validation (overpayment prevention, partial payment rules)
- Implement Payment queries
- Create REST API for payment processing
- Write comprehensive tests for idempotency and validation

## Business Rules

1. **Idempotency:** Payment ID generated on client prevents duplicate submissions
2. **Overpayment Prevention:** Payment amount ≤ invoice balance remaining
3. **Partial Payment Control:** If disabled, payment must equal full balance
4. **Automatic Status Update:** Invoice transitions to PAID when balance = 0
5. **Direct Linking:** Each payment linked to specific invoice via payment link

---

## Tasks

### 5.1 Implement RecordPayment Command

**Package:** `com.invoiceme.application.payments.RecordPayment`

**RecordPaymentCommand.java:**

```java
package com.invoiceme.application.payments.RecordPayment;

import com.invoiceme.domain.payment.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class RecordPaymentCommand {
    private UUID paymentId;  // Client-generated for idempotency
    private UUID invoiceId;
    private BigDecimal paymentAmount;
    private LocalDate paymentDate;
    private PaymentMethod paymentMethod;
    private String transactionReference;
    private String notes;
}
```

**RecordPaymentHandler.java:**

```java
package com.invoiceme.application.payments.RecordPayment;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceStatus;
import com.invoiceme.domain.payment.Payment;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import com.invoiceme.infrastructure.persistence.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordPaymentHandler {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    @Transactional
    public UUID handle(RecordPaymentCommand command) {
        // Idempotency check: If payment ID already exists, return existing
        if (command.getPaymentId() != null && paymentRepository.existsById(command.getPaymentId())) {
            return command.getPaymentId();
        }

        // Find invoice
        Invoice invoice = invoiceRepository.findById(command.getInvoiceId())
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));

        // Validate invoice status
        if (invoice.getStatus() != InvoiceStatus.SENT) {
            throw new IllegalStateException(
                "Cannot record payment for invoice in " + invoice.getStatus() + " status. " +
                "Only SENT invoices can receive payments."
            );
        }

        // Create payment
        Payment payment = new Payment();
        payment.setId(command.getPaymentId() != null ? command.getPaymentId() : UUID.randomUUID());
        payment.setInvoice(invoice);
        payment.setPaymentAmount(command.getPaymentAmount());
        payment.setPaymentDate(command.getPaymentDate() != null ? command.getPaymentDate() : LocalDate.now());
        payment.setPaymentMethod(command.getPaymentMethod());
        payment.setTransactionReference(command.getTransactionReference());
        payment.setNotes(command.getNotes());

        // Validate payment
        payment.validate();

        // Update invoice amounts
        BigDecimal newAmountPaid = invoice.getAmountPaid().add(command.getPaymentAmount());
        invoice.setAmountPaid(newAmountPaid);
        invoice.calculateTotals();

        // Check if fully paid
        if (invoice.getBalanceRemaining().compareTo(BigDecimal.ZERO) == 0) {
            invoice.markAsPaid();
        }

        // Save payment and invoice
        paymentRepository.save(payment);
        invoiceRepository.save(invoice);

        return payment.getId();
    }
}
```

**Action Items:**
- [ ] Create RecordPaymentCommand
- [ ] Create RecordPaymentHandler
- [ ] Test idempotency (duplicate payment IDs return existing)
- [ ] Test automatic invoice status update to PAID

---

### 5.2 Implement Payment Validation

The validation logic is in the `Payment` entity's `validate()` method (from Phase 2), but let's add comprehensive tests:

**PaymentValidationTest.java:**

```java
package com.invoiceme.domain.payment;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PaymentValidationTest {

    @Test
    void shouldPreventOverpayment() {
        // Given: Invoice with $100 balance
        Invoice invoice = new Invoice();
        invoice.setStatus(InvoiceStatus.SENT);
        invoice.setTotalAmount(new BigDecimal("100.00"));
        invoice.setAmountPaid(BigDecimal.ZERO);
        invoice.setBalanceRemaining(new BigDecimal("100.00"));

        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setPaymentAmount(new BigDecimal("150.00")); // More than balance
        payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            payment::validate
        );
        assertTrue(exception.getMessage().contains("exceeds invoice balance"));
    }

    @Test
    void shouldRequireFullPaymentWhenPartialPaymentDisabled() {
        // Given: Invoice with partial payments disabled
        Invoice invoice = new Invoice();
        invoice.setStatus(InvoiceStatus.SENT);
        invoice.setTotalAmount(new BigDecimal("100.00"));
        invoice.setAmountPaid(BigDecimal.ZERO);
        invoice.setBalanceRemaining(new BigDecimal("100.00"));
        invoice.setAllowsPartialPayment(false);

        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setPaymentAmount(new BigDecimal("50.00")); // Partial payment
        payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            payment::validate
        );
        assertTrue(exception.getMessage().contains("requires full payment"));
    }

    @Test
    void shouldAllowPartialPaymentWhenEnabled() {
        // Given: Invoice with partial payments enabled
        Invoice invoice = new Invoice();
        invoice.setStatus(InvoiceStatus.SENT);
        invoice.setTotalAmount(new BigDecimal("100.00"));
        invoice.setAmountPaid(BigDecimal.ZERO);
        invoice.setBalanceRemaining(new BigDecimal("100.00"));
        invoice.setAllowsPartialPayment(true);

        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setPaymentAmount(new BigDecimal("50.00"));
        payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        // When & Then
        assertDoesNotThrow(payment::validate);
    }

    @Test
    void shouldRejectZeroOrNegativePaymentAmount() {
        Invoice invoice = new Invoice();
        invoice.setStatus(InvoiceStatus.SENT);
        invoice.setBalanceRemaining(new BigDecimal("100.00"));

        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setPaymentAmount(BigDecimal.ZERO);
        payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        assertThrows(IllegalArgumentException.class, payment::validate);
    }
}
```

**Action Items:**
- [ ] Write tests for overpayment prevention
- [ ] Write tests for partial payment validation
- [ ] Write tests for zero/negative amounts
- [ ] Verify all validation rules enforce correctly

---

### 5.3 Implement Payment Queries

**Package:** `com.invoiceme.application.payments.GetPayment`

**PaymentDto.java:**

```java
package com.invoiceme.application.payments;

import com.invoiceme.domain.payment.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class PaymentDto {
    private UUID id;
    private UUID invoiceId;
    private String invoiceNumber;
    private BigDecimal paymentAmount;
    private LocalDate paymentDate;
    private PaymentMethod paymentMethod;
    private String transactionReference;
    private String notes;
    private LocalDateTime createdAt;
}
```

**GetPaymentHandler.java:**

```java
package com.invoiceme.application.payments.GetPayment;

import com.invoiceme.application.payments.PaymentDto;
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
            .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        return mapToDto(payment);
    }

    private PaymentDto mapToDto(Payment payment) {
        return new PaymentDto(
            payment.getId(),
            payment.getInvoice().getId(),
            payment.getInvoice().getInvoiceNumber(),
            payment.getPaymentAmount(),
            payment.getPaymentDate(),
            payment.getPaymentMethod(),
            payment.getTransactionReference(),
            payment.getNotes(),
            payment.getCreatedAt()
        );
    }
}
```

**ListPaymentsForInvoiceHandler.java:**

```java
package com.invoiceme.application.payments.ListPaymentsForInvoice;

import com.invoiceme.application.payments.PaymentDto;
import com.invoiceme.infrastructure.persistence.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListPaymentsForInvoiceHandler {

    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public List<PaymentDto> handle(UUID invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId)
            .stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    private PaymentDto mapToDto(com.invoiceme.domain.payment.Payment payment) {
        return new PaymentDto(
            payment.getId(),
            payment.getInvoice().getId(),
            payment.getInvoice().getInvoiceNumber(),
            payment.getPaymentAmount(),
            payment.getPaymentDate(),
            payment.getPaymentMethod(),
            payment.getTransactionReference(),
            payment.getNotes(),
            payment.getCreatedAt()
        );
    }
}
```

**Action Items:**
- [ ] Create PaymentDto
- [ ] Create GetPaymentHandler
- [ ] Create ListPaymentsForInvoiceHandler
- [ ] Write unit tests for queries

---

### 5.4 Create Payment REST Controller

**Package:** `com.invoiceme.api`

**PaymentController.java:**

```java
package com.invoiceme.api;

import com.invoiceme.application.payments.GetPayment.GetPaymentHandler;
import com.invoiceme.application.payments.ListPaymentsForInvoice.ListPaymentsForInvoiceHandler;
import com.invoiceme.application.payments.PaymentDto;
import com.invoiceme.application.payments.RecordPayment.RecordPaymentCommand;
import com.invoiceme.application.payments.RecordPayment.RecordPaymentHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final RecordPaymentHandler recordPaymentHandler;
    private final GetPaymentHandler getPaymentHandler;
    private final ListPaymentsForInvoiceHandler listPaymentsForInvoiceHandler;

    @PostMapping
    public ResponseEntity<UUID> recordPayment(@RequestBody RecordPaymentCommand command) {
        try {
            UUID paymentId = recordPaymentHandler.handle(command);
            return ResponseEntity.status(HttpStatus.CREATED).body(paymentId);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDto> getPayment(@PathVariable UUID id) {
        try {
            PaymentDto payment = getPaymentHandler.handle(id);
            return ResponseEntity.ok(payment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<List<PaymentDto>> getPaymentsForInvoice(@PathVariable UUID invoiceId) {
        List<PaymentDto> payments = listPaymentsForInvoiceHandler.handle(invoiceId);
        return ResponseEntity.ok(payments);
    }
}
```

**Public Payment Link Controller:**

```java
package com.invoiceme.api;

import com.invoiceme.application.invoices.GetInvoice.GetInvoiceHandler;
import com.invoiceme.application.invoices.InvoiceDto;
import com.invoiceme.application.payments.RecordPayment.RecordPaymentCommand;
import com.invoiceme.application.payments.RecordPayment.RecordPaymentHandler;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments/link")
@RequiredArgsConstructor
public class PublicPaymentController {

    private final InvoiceRepository invoiceRepository;
    private final GetInvoiceHandler getInvoiceHandler;
    private final RecordPaymentHandler recordPaymentHandler;

    /**
     * Get invoice by payment link (no authentication required)
     */
    @GetMapping("/{paymentLink}")
    public ResponseEntity<InvoiceDto> getInvoiceByPaymentLink(@PathVariable String paymentLink) {
        return invoiceRepository.findByPaymentLink(paymentLink)
            .map(invoice -> {
                InvoiceDto dto = getInvoiceHandler.handle(invoice.getId());
                return ResponseEntity.ok(dto);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Submit payment via payment link (no authentication required)
     */
    @PostMapping("/{paymentLink}/pay")
    public ResponseEntity<UUID> submitPayment(
        @PathVariable String paymentLink,
        @RequestBody RecordPaymentCommand command
    ) {
        return invoiceRepository.findByPaymentLink(paymentLink)
            .map(invoice -> {
                command.setInvoiceId(invoice.getId());
                UUID paymentId = recordPaymentHandler.handle(command);
                return ResponseEntity.ok(paymentId);
            })
            .orElse(ResponseEntity.notFound().build());
    }
}
```

**Action Items:**
- [ ] Create PaymentController
- [ ] Create PublicPaymentController (no auth)
- [ ] Test all endpoints
- [ ] Write integration tests

---

### 5.5 Write Unit Tests for Idempotency

**RecordPaymentHandlerTest.java:**

```java
package com.invoiceme.application.payments.RecordPayment;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceStatus;
import com.invoiceme.domain.payment.Payment;
import com.invoiceme.domain.payment.PaymentMethod;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import com.invoiceme.infrastructure.persistence.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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

    @InjectMocks
    private RecordPaymentHandler handler;

    @Test
    void shouldRecordPaymentSuccessfully() {
        // Given
        UUID invoiceId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        Invoice invoice = new Invoice();
        invoice.setId(invoiceId);
        invoice.setStatus(InvoiceStatus.SENT);
        invoice.setTotalAmount(new BigDecimal("100.00"));
        invoice.setAmountPaid(BigDecimal.ZERO);
        invoice.setBalanceRemaining(new BigDecimal("100.00"));

        RecordPaymentCommand command = new RecordPaymentCommand();
        command.setPaymentId(paymentId);
        command.setInvoiceId(invoiceId);
        command.setPaymentAmount(new BigDecimal("100.00"));
        command.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        when(paymentRepository.existsById(paymentId)).thenReturn(false);
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));

        // When
        UUID result = handler.handle(command);

        // Then
        assertEquals(paymentId, result);
        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
        verify(paymentRepository).save(any(Payment.class));
        verify(invoiceRepository).save(invoice);
    }

    @Test
    void shouldReturnExistingPaymentWhenDuplicateSubmitted() {
        // Given
        UUID existingPaymentId = UUID.randomUUID();

        RecordPaymentCommand command = new RecordPaymentCommand();
        command.setPaymentId(existingPaymentId);
        command.setInvoiceId(UUID.randomUUID());
        command.setPaymentAmount(new BigDecimal("100.00"));

        when(paymentRepository.existsById(existingPaymentId)).thenReturn(true);

        // When
        UUID result = handler.handle(command);

        // Then
        assertEquals(existingPaymentId, result);
        verify(paymentRepository, never()).save(any());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void shouldTransitionInvoiceToPaidWhenFullyPaid() {
        // Given
        Invoice invoice = new Invoice();
        invoice.setStatus(InvoiceStatus.SENT);
        invoice.setTotalAmount(new BigDecimal("100.00"));
        invoice.setAmountPaid(BigDecimal.ZERO);
        invoice.setBalanceRemaining(new BigDecimal("100.00"));

        RecordPaymentCommand command = new RecordPaymentCommand();
        command.setInvoiceId(invoice.getId());
        command.setPaymentAmount(new BigDecimal("100.00"));
        command.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        when(paymentRepository.existsById(any())).thenReturn(false);
        when(invoiceRepository.findById(any())).thenReturn(Optional.of(invoice));

        // When
        handler.handle(command);

        // Then
        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
        assertNotNull(invoice.getPaidAt());
    }
}
```

**Action Items:**
- [ ] Write idempotency tests (duplicate payment IDs)
- [ ] Write tests for automatic status transitions
- [ ] Write tests for partial payment scenarios
- [ ] Ensure all edge cases covered

---

## Verification Checklist

After completing Phase 5, verify:

- [ ] RecordPayment command implemented and tested
- [ ] Payment idempotency works (duplicate IDs return existing)
- [ ] Overpayment validation prevents invalid amounts
- [ ] Partial payment logic enforces correctly
- [ ] Invoice automatically transitions to PAID when balance = 0
- [ ] Payment queries return correct data
- [ ] REST API endpoints functional
- [ ] Public payment link endpoint works without auth
- [ ] Unit tests achieve 80%+ coverage
- [ ] Integration tests pass

## API Endpoints Summary

```
POST   /api/payments                   - Record payment
GET    /api/payments/{id}              - Get payment by ID
GET    /api/payments/invoice/{id}      - List payments for invoice

# Public endpoints (no auth)
GET    /api/payments/link/{link}       - Get invoice by payment link
POST   /api/payments/link/{link}/pay   - Submit payment via link
```

## Next Steps

Proceed to [Phase 6: AI Integration - OpenAI Setup](Phase-06-AI-Email-Reminder.md)

---

## Reference Files

- Main PRD: `Docs/PRD/PRD.md` (Section 2.4: Payment Domain, Section 5.1: Payment Rules)
- Domain Model: [Phase-02-Domain-Model.md](Phase-02-Domain-Model.md)
