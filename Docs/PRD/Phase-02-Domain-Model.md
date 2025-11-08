# Phase 2: Domain Model & Database Schema

**Estimated Time:** 4-6 hours
**Dependencies:** Phase 1 (Project Setup)
**Status:** Not Started

## Overview

Implement the core domain model following Domain-Driven Design (DDD) principles and create the initial database schema using Flyway migrations. This phase establishes the foundation for all business logic.

## Objectives

- Create Flyway migration for complete database schema
- Implement Customer domain entity with Address value object
- Implement Invoice domain entity with LineItem and state machine
- Implement Payment domain entity with Money value object
- Ensure all entities follow DDD principles (rich domain models)

## Tasks

### 2.1 Create Database Schema Migration (V1__initial_schema.sql)

**File Location:** `backend/src/main/resources/db/migration/V1__initial_schema.sql`

**Schema Definition:**

```sql
-- Customers Table
CREATE TABLE customers (
    id UUID PRIMARY KEY,
    business_name VARCHAR(255) NOT NULL,
    contact_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50),

    -- Billing Address (required)
    billing_street VARCHAR(255) NOT NULL,
    billing_city VARCHAR(100) NOT NULL,
    billing_state VARCHAR(100) NOT NULL,
    billing_postal_code VARCHAR(20) NOT NULL,
    billing_country VARCHAR(100) NOT NULL,

    -- Shipping Address (optional)
    shipping_street VARCHAR(255),
    shipping_city VARCHAR(100),
    shipping_state VARCHAR(100),
    shipping_postal_code VARCHAR(20),
    shipping_country VARCHAR(100),

    -- Metadata
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Invoices Table
CREATE TABLE invoices (
    id UUID PRIMARY KEY,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id UUID NOT NULL REFERENCES customers(id),

    -- Dates
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,

    -- Status and State Machine
    status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'SENT', 'PAID', 'CANCELLED')),

    -- Financial Fields
    subtotal DECIMAL(15,2) NOT NULL,
    tax_amount DECIMAL(15,2) DEFAULT 0,
    total_amount DECIMAL(15,2) NOT NULL,
    amount_paid DECIMAL(15,2) DEFAULT 0,
    balance_remaining DECIMAL(15,2) NOT NULL,

    -- Configuration
    allows_partial_payment BOOLEAN DEFAULT FALSE,
    payment_link VARCHAR(255) UNIQUE,

    -- Text Fields
    notes TEXT,
    terms TEXT,
    cancellation_reason TEXT,

    -- AI Reminder Management
    reminders_suppressed BOOLEAN DEFAULT FALSE,
    last_reminder_sent_at TIMESTAMP,

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP,
    paid_at TIMESTAMP,
    cancelled_at TIMESTAMP,

    -- Optimistic Locking
    version BIGINT DEFAULT 0
);

-- Invoice Line Items Table
CREATE TABLE invoice_line_items (
    id UUID PRIMARY KEY,
    invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    description VARCHAR(500) NOT NULL,
    quantity DECIMAL(10,2) NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(15,2) NOT NULL CHECK (unit_price >= 0),
    line_total DECIMAL(15,2) NOT NULL,
    line_order INT NOT NULL
);

-- Payments Table
CREATE TABLE payments (
    id UUID PRIMARY KEY,
    invoice_id UUID NOT NULL REFERENCES invoices(id),
    payment_amount DECIMAL(15,2) NOT NULL CHECK (payment_amount > 0),
    payment_date DATE NOT NULL,
    payment_method VARCHAR(50) NOT NULL CHECK (payment_method IN ('CREDIT_CARD', 'BANK_TRANSFER', 'CHECK', 'CASH', 'OTHER')),
    transaction_reference VARCHAR(255),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for Performance
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_customers_active ON customers(active);

CREATE INDEX idx_invoices_customer_id ON invoices(customer_id);
CREATE INDEX idx_invoices_status ON invoices(status);
CREATE INDEX idx_invoices_due_date ON invoices(due_date);
CREATE INDEX idx_invoices_payment_link ON invoices(payment_link);

CREATE INDEX idx_line_items_invoice_id ON invoice_line_items(invoice_id);

CREATE INDEX idx_payments_invoice_id ON payments(invoice_id);
CREATE INDEX idx_payments_created_at ON payments(created_at);
```

**Action Items:**
- [ ] Create V1__initial_schema.sql file
- [ ] Run Spring Boot application to execute migration
- [ ] Verify all tables created successfully
- [ ] Verify indexes created

---

### 2.2 Implement Customer Domain Entity

**Package:** `com.invoiceme.domain.customer`

**Address Value Object:**

```java
package com.invoiceme.domain.customer;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    public boolean isComplete() {
        return street != null && !street.isBlank()
            && city != null && !city.isBlank()
            && state != null && !state.isBlank()
            && postalCode != null && !postalCode.isBlank()
            && country != null && !country.isBlank();
    }

    public String toFormattedString() {
        return String.format("%s, %s, %s %s, %s",
            street, city, state, postalCode, country);
    }
}
```

**Customer Entity:**

```java
package com.invoiceme.domain.customer;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    private UUID id;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "contact_name", nullable = false)
    private String contactName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street", column = @Column(name = "billing_street")),
        @AttributeOverride(name = "city", column = @Column(name = "billing_city")),
        @AttributeOverride(name = "state", column = @Column(name = "billing_state")),
        @AttributeOverride(name = "postalCode", column = @Column(name = "billing_postal_code")),
        @AttributeOverride(name = "country", column = @Column(name = "billing_country"))
    })
    private Address billingAddress;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street", column = @Column(name = "shipping_street")),
        @AttributeOverride(name = "city", column = @Column(name = "shipping_city")),
        @AttributeOverride(name = "state", column = @Column(name = "shipping_state")),
        @AttributeOverride(name = "postalCode", column = @Column(name = "shipping_postal_code")),
        @AttributeOverride(name = "country", column = @Column(name = "shipping_country"))
    })
    private Address shippingAddress;

    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Business Logic Methods

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean hasShippingAddress() {
        return shippingAddress != null && shippingAddress.isComplete();
    }
}
```

**Action Items:**
- [ ] Create Address value object
- [ ] Create Customer entity
- [ ] Create CustomerRepository interface
- [ ] Write unit tests for Customer business logic

---

### 2.3 Implement Invoice Domain Entity

**Package:** `com.invoiceme.domain.invoice`

**InvoiceStatus Enum:**

```java
package com.invoiceme.domain.invoice;

public enum InvoiceStatus {
    DRAFT,
    SENT,
    PAID,
    CANCELLED
}
```

**InvoiceNumber Value Object:**

```java
package com.invoiceme.domain.invoice;

import java.time.Year;

public class InvoiceNumber {

    public static String generate(int sequenceNumber) {
        int year = Year.now().getValue();
        return String.format("INV-%d-%04d", year, sequenceNumber);
    }

    public static boolean isValid(String invoiceNumber) {
        return invoiceNumber != null
            && invoiceNumber.matches("INV-\\d{4}-\\d{4}");
    }
}
```

**LineItem Entity:**

```java
package com.invoiceme.domain.invoice;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "invoice_line_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineItem {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "line_total", nullable = false)
    private BigDecimal lineTotal;

    @Column(name = "line_order", nullable = false)
    private int lineOrder;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        calculateLineTotal();
    }

    @PreUpdate
    protected void onUpdate() {
        calculateLineTotal();
    }

    public void calculateLineTotal() {
        this.lineTotal = quantity.multiply(unitPrice);
    }
}
```

**Invoice Entity:**

```java
package com.invoiceme.domain.invoice;

import com.invoiceme.domain.customer.Customer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    @Id
    private UUID id;

    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("lineOrder")
    private List<LineItem> lineItems = new ArrayList<>();

    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount")
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount")
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "amount_paid")
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "balance_remaining")
    private BigDecimal balanceRemaining = BigDecimal.ZERO;

    @Column(name = "allows_partial_payment")
    private boolean allowsPartialPayment = false;

    @Column(name = "payment_link", unique = true)
    private String paymentLink;

    private String notes;
    private String terms;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "reminders_suppressed")
    private boolean remindersSuppressed = false;

    @Column(name = "last_reminder_sent_at")
    private LocalDateTime lastReminderSentAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Version
    private Long version;

    // Business Logic Methods

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void calculateTotals() {
        this.subtotal = lineItems.stream()
            .map(LineItem::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalAmount = subtotal.add(taxAmount != null ? taxAmount : BigDecimal.ZERO);
        this.balanceRemaining = totalAmount.subtract(amountPaid != null ? amountPaid : BigDecimal.ZERO);
    }

    public void send() {
        if (status != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Can only send invoices in DRAFT status");
        }
        if (lineItems.isEmpty()) {
            throw new IllegalStateException("Cannot send invoice without line items");
        }

        this.status = InvoiceStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.paymentLink = UUID.randomUUID().toString();
    }

    public void markAsPaid() {
        if (status != InvoiceStatus.SENT) {
            throw new IllegalStateException("Can only mark SENT invoices as paid");
        }

        this.status = InvoiceStatus.PAID;
        this.paidAt = LocalDateTime.now();
        this.balanceRemaining = BigDecimal.ZERO;
        this.amountPaid = totalAmount;
    }

    public void cancel(String reason) {
        if (status == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("Invoice is already cancelled");
        }

        this.status = InvoiceStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }

    public boolean isOverdue() {
        return status == InvoiceStatus.SENT && dueDate.isBefore(LocalDate.now());
    }

    public void addLineItem(LineItem item) {
        item.setInvoice(this);
        item.setLineOrder(lineItems.size());
        lineItems.add(item);
        calculateTotals();
    }
}
```

**Action Items:**
- [ ] Create InvoiceStatus enum
- [ ] Create InvoiceNumber value object
- [ ] Create LineItem entity
- [ ] Create Invoice entity with state machine logic
- [ ] Create InvoiceRepository interface
- [ ] Write unit tests for Invoice state transitions
- [ ] Write tests for total calculations

---

### 2.4 Implement Payment Domain Entity

**Package:** `com.invoiceme.domain.payment`

**PaymentMethod Enum:**

```java
package com.invoiceme.domain.payment;

public enum PaymentMethod {
    CREDIT_CARD,
    BANK_TRANSFER,
    CHECK,
    CASH,
    OTHER
}
```

**Payment Entity:**

```java
package com.invoiceme.domain.payment;

import com.invoiceme.domain.invoice.Invoice;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "payment_amount", nullable = false)
    private BigDecimal paymentAmount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "transaction_reference")
    private String transactionReference;

    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        // ID is set from client for idempotency, but generate if not provided
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
    }

    // Business Logic

    public void validate() {
        if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        BigDecimal remainingBalance = invoice.getBalanceRemaining();
        if (paymentAmount.compareTo(remainingBalance) > 0) {
            throw new IllegalArgumentException(
                String.format("Payment amount (%.2f) exceeds invoice balance (%.2f)",
                    paymentAmount, remainingBalance)
            );
        }

        if (!invoice.isAllowsPartialPayment() && paymentAmount.compareTo(remainingBalance) != 0) {
            throw new IllegalArgumentException("This invoice requires full payment. Partial payments are not allowed.");
        }
    }
}
```

**Action Items:**
- [ ] Create PaymentMethod enum
- [ ] Create Payment entity
- [ ] Create PaymentRepository interface
- [ ] Write unit tests for payment validation logic

---

## Repository Interfaces

**CustomerRepository:**

```java
package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByEmail(String email);
    List<Customer> findByActiveTrue();
    boolean existsByEmail(String email);
}
```

**InvoiceRepository:**

```java
package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Optional<Invoice> findByPaymentLink(String paymentLink);
    List<Invoice> findByCustomerId(UUID customerId);
    List<Invoice> findByStatus(InvoiceStatus status);
    List<Invoice> findByStatusAndDueDateBefore(InvoiceStatus status, LocalDate date);
}
```

**PaymentRepository:**

```java
package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByInvoiceId(UUID invoiceId);
    boolean existsById(UUID id);
}
```

---

## Verification Checklist

After completing Phase 2, verify:

- [ ] Flyway migration executes successfully
- [ ] All tables and indexes created in PostgreSQL
- [ ] Customer entity persists and retrieves correctly
- [ ] Invoice entity with line items persists correctly
- [ ] Invoice state transitions work as expected
- [ ] Payment entity validates correctly
- [ ] All unit tests pass
- [ ] No compilation errors

## Next Steps

Proceed to [Phase 3: Customer Management (CQRS + VSA)](Phase-03-Customer-Management.md)

---

## Reference Files

- Main PRD: `Docs/PRD/PRD.md` (Section 2: Core Functional Requirements)
- Database Schema: PRD Appendix B
