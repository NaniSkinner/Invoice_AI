# Phase 4: Invoice Management (CQRS + VSA)

**Estimated Time:** 10-12 hours
**Dependencies:** Phase 3 (Customer Management)
**Status:** Not Started

## Overview

Implement complete Invoice management with full lifecycle state machine (DRAFT → SENT → PAID → CANCELLED), line item management, and business rule enforcement using CQRS and Vertical Slice Architecture.

## Objectives

- Implement CreateInvoice command (Draft state)
- Implement UpdateInvoice command with line item management
- Implement SendInvoice command (Draft → Sent transition)
- Implement CancelInvoice command with reason tracking
- Implement MarkAsPaid command (manual override)
- Implement Invoice queries (GetById, ListByStatus, ListByCustomer, ListOverdue)
- Create REST API with proper state transition validation
- Write comprehensive tests for state machine

## Invoice State Machine

```
┌─────────┐
│  DRAFT  │ ← Initial state when created
└────┬────┘
     │ (Send Invoice Command)
     ↓
┌─────────┐
│  SENT   │ ← Payment link active
└────┬────┘
     │ (Record Payment Command)
     ↓
┌─────────┐
│  PAID   │ ← Final state
└─────────┘

     ↓ (Cancel Invoice Command - from any state)
┌───────────┐
│ CANCELLED │ ← Terminal state
└───────────┘
```

---

## Tasks

### 4.1 Implement CreateInvoice Command

**Package:** `com.invoiceme.application.invoices.CreateInvoice`

**CreateInvoiceCommand.java:**

```java
package com.invoiceme.application.invoices.CreateInvoice;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateInvoiceCommand {
    private UUID customerId;
    private LocalDate dueDate;
    private BigDecimal taxAmount;
    private List<LineItemDto> lineItems;
    private String notes;
    private String terms;
    private boolean allowsPartialPayment;

    @Data
    public static class LineItemDto {
        private String description;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
    }
}
```

**CreateInvoiceHandler.java:**

```java
package com.invoiceme.application.invoices.CreateInvoice;

import com.invoiceme.domain.customer.Customer;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceNumber;
import com.invoiceme.domain.invoice.LineItem;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateInvoiceHandler {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public UUID handle(CreateInvoiceCommand command) {
        // Validate customer exists
        Customer customer = customerRepository.findById(command.getCustomerId())
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // Validate line items
        if (command.getLineItems() == null || command.getLineItems().isEmpty()) {
            throw new IllegalArgumentException("Invoice must have at least one line item");
        }

        // Generate invoice number
        long count = invoiceRepository.count();
        String invoiceNumber = InvoiceNumber.generate((int) count + 1);

        // Create invoice
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setCustomer(customer);
        invoice.setIssueDate(LocalDate.now());
        invoice.setDueDate(command.getDueDate() != null ? command.getDueDate() : LocalDate.now().plusDays(30));
        invoice.setTaxAmount(command.getTaxAmount() != null ? command.getTaxAmount() : BigDecimal.ZERO);
        invoice.setNotes(command.getNotes());
        invoice.setTerms(command.getTerms());
        invoice.setAllowsPartialPayment(command.isAllowsPartialPayment());

        // Add line items
        int order = 0;
        for (CreateInvoiceCommand.LineItemDto itemDto : command.getLineItems()) {
            LineItem lineItem = new LineItem();
            lineItem.setDescription(itemDto.getDescription());
            lineItem.setQuantity(itemDto.getQuantity());
            lineItem.setUnitPrice(itemDto.getUnitPrice());
            lineItem.setLineOrder(order++);
            invoice.addLineItem(lineItem);
        }

        Invoice saved = invoiceRepository.save(invoice);
        return saved.getId();
    }
}
```

**Action Items:**
- [ ] Create CreateInvoiceCommand
- [ ] Create CreateInvoiceHandler
- [ ] Write unit tests
- [ ] Test invoice number generation

---

### 4.2 Implement UpdateInvoice Command

**Package:** `com.invoiceme.application.invoices.UpdateInvoice`

**UpdateInvoiceCommand.java:**

```java
package com.invoiceme.application.invoices.UpdateInvoice;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class UpdateInvoiceCommand {
    private UUID invoiceId;
    private LocalDate dueDate;
    private BigDecimal taxAmount;
    private List<LineItemDto> lineItems;
    private String notes;
    private String terms;

    @Data
    public static class LineItemDto {
        private String description;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
    }
}
```

**UpdateInvoiceHandler.java:**

```java
package com.invoiceme.application.invoices.UpdateInvoice;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceStatus;
import com.invoiceme.domain.invoice.LineItem;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateInvoiceHandler {

    private final InvoiceRepository invoiceRepository;

    @Transactional
    public void handle(UpdateInvoiceCommand command) {
        Invoice invoice = invoiceRepository.findById(command.getInvoiceId())
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));

        // Only allow updates in DRAFT status
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new IllegalStateException(
                "Cannot update invoice in " + invoice.getStatus() + " status. " +
                "Only DRAFT invoices can be freely edited."
            );
        }

        // Update fields
        if (command.getDueDate() != null) {
            invoice.setDueDate(command.getDueDate());
        }
        if (command.getTaxAmount() != null) {
            invoice.setTaxAmount(command.getTaxAmount());
        }
        if (command.getNotes() != null) {
            invoice.setNotes(command.getNotes());
        }
        if (command.getTerms() != null) {
            invoice.setTerms(command.getTerms());
        }

        // Update line items
        if (command.getLineItems() != null && !command.getLineItems().isEmpty()) {
            // Clear existing line items
            invoice.getLineItems().clear();

            // Add new line items
            int order = 0;
            for (UpdateInvoiceCommand.LineItemDto itemDto : command.getLineItems()) {
                LineItem lineItem = new LineItem();
                lineItem.setDescription(itemDto.getDescription());
                lineItem.setQuantity(itemDto.getQuantity());
                lineItem.setUnitPrice(itemDto.getUnitPrice());
                lineItem.setLineOrder(order++);
                invoice.addLineItem(lineItem);
            }
        }

        invoiceRepository.save(invoice);
    }
}
```

**Action Items:**
- [ ] Create UpdateInvoiceCommand
- [ ] Create UpdateInvoiceHandler
- [ ] Write tests for status validation
- [ ] Write tests for line item updates

---

### 4.3 Implement SendInvoice Command

**Package:** `com.invoiceme.application.invoices.SendInvoice`

**SendInvoiceCommand.java:**

```java
package com.invoiceme.application.invoices.SendInvoice;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.UUID;

@Data
@AllArgsConstructor
public class SendInvoiceCommand {
    private UUID invoiceId;
}
```

**SendInvoiceHandler.java:**

```java
package com.invoiceme.application.invoices.SendInvoice;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SendInvoiceHandler {

    private final InvoiceRepository invoiceRepository;

    @Transactional
    public String handle(SendInvoiceCommand command) {
        Invoice invoice = invoiceRepository.findById(command.getInvoiceId())
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));

        // Trigger state transition (includes validation)
        invoice.send();

        invoiceRepository.save(invoice);

        // Return payment link for client
        return invoice.getPaymentLink();
    }
}
```

**Action Items:**
- [ ] Create SendInvoiceCommand
- [ ] Create SendInvoiceHandler
- [ ] Write tests for state transition validation
- [ ] Test payment link generation

---

### 4.4 Implement CancelInvoice Command

**Package:** `com.invoiceme.application.invoices.CancelInvoice`

**CancelInvoiceCommand.java:**

```java
package com.invoiceme.application.invoices.CancelInvoice;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CancelInvoiceCommand {
    private UUID invoiceId;
    private String cancellationReason;
}
```

**CancelInvoiceHandler.java:**

```java
package com.invoiceme.application.invoices.CancelInvoice;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancelInvoiceHandler {

    private final InvoiceRepository invoiceRepository;

    @Transactional
    public void handle(CancelInvoiceCommand command) {
        Invoice invoice = invoiceRepository.findById(command.getInvoiceId())
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));

        if (command.getCancellationReason() == null || command.getCancellationReason().isBlank()) {
            throw new IllegalArgumentException("Cancellation reason is required");
        }

        // Trigger cancel (includes validation)
        invoice.cancel(command.getCancellationReason());

        invoiceRepository.save(invoice);
    }
}
```

**Action Items:**
- [ ] Create CancelInvoiceCommand
- [ ] Create CancelInvoiceHandler
- [ ] Write tests for cancellation from all states
- [ ] Test cancellation reason requirement

---

### 4.5 Implement MarkAsPaid Command

**Package:** `com.invoiceme.application.invoices.MarkAsPaid`

**MarkAsPaidCommand.java:**

```java
package com.invoiceme.application.invoices.MarkAsPaid;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.UUID;

@Data
@AllArgsConstructor
public class MarkAsPaidCommand {
    private UUID invoiceId;
}
```

**MarkAsPaidHandler.java:**

```java
package com.invoiceme.application.invoices.MarkAsPaid;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarkAsPaidHandler {

    private final InvoiceRepository invoiceRepository;

    @Transactional
    public void handle(MarkAsPaidCommand command) {
        Invoice invoice = invoiceRepository.findById(command.getInvoiceId())
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));

        // Trigger mark as paid (includes validation)
        invoice.markAsPaid();

        invoiceRepository.save(invoice);
    }
}
```

**Action Items:**
- [ ] Create MarkAsPaidCommand
- [ ] Create MarkAsPaidHandler
- [ ] Write tests

---

### 4.6 Implement Invoice Queries

**InvoiceDto.java:**

```java
package com.invoiceme.application.invoices;

import com.invoiceme.domain.invoice.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class InvoiceDto {
    private UUID id;
    private String invoiceNumber;
    private UUID customerId;
    private String customerName;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private InvoiceStatus status;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal balanceRemaining;
    private boolean allowsPartialPayment;
    private String paymentLink;
    private String notes;
    private String terms;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;
    private List<LineItemDto> lineItems;

    @Data
    @AllArgsConstructor
    public static class LineItemDto {
        private UUID id;
        private String description;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;
    }
}
```

**GetInvoiceHandler.java:**

```java
package com.invoiceme.application.invoices.GetInvoice;

import com.invoiceme.application.invoices.InvoiceDto;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetInvoiceHandler {

    private final InvoiceRepository invoiceRepository;

    @Transactional(readOnly = true)
    public InvoiceDto handle(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));

        return mapToDto(invoice);
    }

    private InvoiceDto mapToDto(Invoice invoice) {
        return new InvoiceDto(
            invoice.getId(),
            invoice.getInvoiceNumber(),
            invoice.getCustomer().getId(),
            invoice.getCustomer().getBusinessName(),
            invoice.getIssueDate(),
            invoice.getDueDate(),
            invoice.getStatus(),
            invoice.getSubtotal(),
            invoice.getTaxAmount(),
            invoice.getTotalAmount(),
            invoice.getAmountPaid(),
            invoice.getBalanceRemaining(),
            invoice.isAllowsPartialPayment(),
            invoice.getPaymentLink(),
            invoice.getNotes(),
            invoice.getTerms(),
            invoice.getCancellationReason(),
            invoice.getCreatedAt(),
            invoice.getSentAt(),
            invoice.getPaidAt(),
            invoice.getCancelledAt(),
            invoice.getLineItems().stream()
                .map(item -> new InvoiceDto.LineItemDto(
                    item.getId(),
                    item.getDescription(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getLineTotal()
                ))
                .collect(Collectors.toList())
        );
    }
}
```

**ListOverdueInvoicesHandler.java:**

```java
package com.invoiceme.application.invoices.ListOverdueInvoices;

import com.invoiceme.application.invoices.InvoiceDto;
import com.invoiceme.domain.invoice.InvoiceStatus;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListOverdueInvoicesHandler {

    private final InvoiceRepository invoiceRepository;

    @Transactional(readOnly = true)
    public List<InvoiceDto> handle() {
        return invoiceRepository
            .findByStatusAndDueDateBefore(InvoiceStatus.SENT, LocalDate.now())
            .stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    private InvoiceDto mapToDto(com.invoiceme.domain.invoice.Invoice invoice) {
        // ... same mapping as GetInvoiceHandler
    }
}
```

**Action Items:**
- [ ] Create InvoiceDto
- [ ] Create GetInvoiceHandler
- [ ] Create ListInvoicesByStatusHandler
- [ ] Create ListInvoicesByCustomerHandler
- [ ] Create ListOverdueInvoicesHandler
- [ ] Write tests for all queries

---

### 4.7 Create Invoice REST Controller

**InvoiceController.java:**

```java
package com.invoiceme.api;

import com.invoiceme.application.invoices.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final CreateInvoiceHandler createInvoiceHandler;
    private final UpdateInvoiceHandler updateInvoiceHandler;
    private final SendInvoiceHandler sendInvoiceHandler;
    private final CancelInvoiceHandler cancelInvoiceHandler;
    private final MarkAsPaidHandler markAsPaidHandler;
    private final GetInvoiceHandler getInvoiceHandler;
    private final ListOverdueInvoicesHandler listOverdueInvoicesHandler;

    @PostMapping
    public ResponseEntity<UUID> createInvoice(@RequestBody CreateInvoiceCommand command) {
        UUID invoiceId = createInvoiceHandler.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDto> getInvoice(@PathVariable UUID id) {
        InvoiceDto invoice = getInvoiceHandler.handle(id);
        return ResponseEntity.ok(invoice);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateInvoice(
        @PathVariable UUID id,
        @RequestBody UpdateInvoiceCommand command
    ) {
        command.setInvoiceId(id);
        updateInvoiceHandler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<String> sendInvoice(@PathVariable UUID id) {
        String paymentLink = sendInvoiceHandler.handle(new SendInvoiceCommand(id));
        return ResponseEntity.ok(paymentLink);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelInvoice(
        @PathVariable UUID id,
        @RequestBody CancelInvoiceRequest request
    ) {
        cancelInvoiceHandler.handle(new CancelInvoiceCommand(id, request.getReason()));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/mark-paid")
    public ResponseEntity<Void> markAsPaid(@PathVariable UUID id) {
        markAsPaidHandler.handle(new MarkAsPaidCommand(id));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<InvoiceDto>> getOverdueInvoices() {
        List<InvoiceDto> invoices = listOverdueInvoicesHandler.handle();
        return ResponseEntity.ok(invoices);
    }
}
```

**Action Items:**
- [ ] Create InvoiceController
- [ ] Test all endpoints
- [ ] Write integration tests

---

### 4.8 Write Unit Tests

**Invoice State Machine Tests:**

```java
@Test
void shouldTransitionFromDraftToSent() {
    // Given: Invoice in DRAFT with line items
    // When: send() is called
    // Then: Status = SENT, sentAt is set, paymentLink is generated
}

@Test
void shouldThrowExceptionWhenSendingNonDraftInvoice() {
    // Given: Invoice in SENT status
    // When: send() is called
    // Then: IllegalStateException is thrown
}

@Test
void shouldCalculateTotalsCorrectly() {
    // Given: Invoice with multiple line items and tax
    // When: calculateTotals() is called
    // Then: Subtotal, totalAmount, balanceRemaining are correct
}
```

**Action Items:**
- [ ] Write tests for all state transitions
- [ ] Write tests for calculation logic
- [ ] Write tests for validation rules
- [ ] Ensure 80%+ coverage

---

## Verification Checklist

- [ ] Invoice can be created in DRAFT status
- [ ] Invoice number auto-generates correctly
- [ ] Line items calculate totals properly
- [ ] State transitions enforce rules
- [ ] Payment link generates on send
- [ ] Cancellation requires reason
- [ ] Overdue detection works
- [ ] All unit tests pass
- [ ] Integration tests pass

## API Endpoints Summary

```
POST   /api/invoices                   - Create invoice (draft)
GET    /api/invoices/{id}              - Get invoice by ID
PUT    /api/invoices/{id}              - Update invoice
POST   /api/invoices/{id}/send         - Send invoice
POST   /api/invoices/{id}/cancel       - Cancel invoice
POST   /api/invoices/{id}/mark-paid    - Mark as paid
GET    /api/invoices/overdue           - List overdue invoices
```

## Next Steps

Proceed to [Phase 5: Payment Processing (CQRS + VSA)](Phase-05-Payment-Processing.md)

---

## Reference Files

- Main PRD: `Docs/PRD/PRD.md` (Section 2.3: Invoice Domain)
