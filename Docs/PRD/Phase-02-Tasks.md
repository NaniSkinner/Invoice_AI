# Phase 2 Tasks - Domain Model & Database Schema

**Execution Guide with Step-by-Step Commands**

**Estimated Time:** 4-6 hours
**Prerequisites:** Phase 1 complete, PostgreSQL running

---

## Task 2.1: Create Database Schema Migration

### Step 2.1.1: Navigate to migration directory
```bash
cd ~/dev/Gauntlet/Invoice_AI/backend
cd src/main/resources/db/migration
pwd
```

**Expected Output:**
```
.../backend/src/main/resources/db/migration
```

### Step 2.1.2: Create V1__initial_schema.sql
```bash
cat > V1__initial_schema.sql << 'EOF'
-- InvoiceMe Initial Schema
-- Version: 1.0
-- Description: Creates customers, invoices, line_items, and payments tables

-- ============================================
-- CUSTOMERS TABLE
-- ============================================
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

-- ============================================
-- INVOICES TABLE
-- ============================================
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

-- ============================================
-- INVOICE LINE ITEMS TABLE
-- ============================================
CREATE TABLE invoice_line_items (
    id UUID PRIMARY KEY,
    invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    description VARCHAR(500) NOT NULL,
    quantity DECIMAL(10,2) NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(15,2) NOT NULL CHECK (unit_price >= 0),
    line_total DECIMAL(15,2) NOT NULL,
    line_order INT NOT NULL
);

-- ============================================
-- PAYMENTS TABLE
-- ============================================
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

-- ============================================
-- INDEXES FOR PERFORMANCE
-- ============================================

-- Customer Indexes
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_customers_active ON customers(active);

-- Invoice Indexes
CREATE INDEX idx_invoices_customer_id ON invoices(customer_id);
CREATE INDEX idx_invoices_status ON invoices(status);
CREATE INDEX idx_invoices_due_date ON invoices(due_date);
CREATE INDEX idx_invoices_payment_link ON invoices(payment_link);

-- Line Items Index
CREATE INDEX idx_line_items_invoice_id ON invoice_line_items(invoice_id);

-- Payment Indexes
CREATE INDEX idx_payments_invoice_id ON payments(invoice_id);
CREATE INDEX idx_payments_created_at ON payments(created_at);
EOF

# Verify file created
ls -la V1__initial_schema.sql
wc -l V1__initial_schema.sql
```

**Expected Output:**
```
-rw-r--r--  1 nanis  staff  4567 Nov  8 15:00 V1__initial_schema.sql
     145 V1__initial_schema.sql
```

### Step 2.1.3: Run Flyway migration
```bash
cd ~/dev/Gauntlet/Invoice_AI/backend

# This will start the app and run migrations
./mvnw spring-boot:run
```

**Expected Output in logs:**
```
INFO  FlywayMigrationStrategy : Migrating schema "public" to version "1 - initial schema"
INFO  FlywayMigrationStrategy : Successfully applied 1 migration
INFO  InvoiceMeApplication    : Started InvoiceMeApplication
```

Press Ctrl+C to stop after verifying migration succeeded

### Step 2.1.4: Verify tables created
```bash
psql -d invoiceme -c "\dt"
```

**Expected Output:**
```
                    List of relations
 Schema |         Name          | Type  | Owner
--------+-----------------------+-------+-------
 public | customers             | table | nanis
 public | flyway_schema_history | table | nanis
 public | invoice_line_items    | table | nanis
 public | invoices              | table | nanis
 public | payments              | table | nanis
```

### Step 2.1.5: Verify indexes created
```bash
psql -d invoiceme -c "\di"
```

**Expected Output:** Should show all indexes created

**Verification:**
- [ ] Migration file created
- [ ] Flyway migration executed successfully
- [ ] All 4 main tables created
- [ ] All indexes created
- [ ] Foreign keys established

---

## Task 2.2: Implement Customer Domain Entity

### Step 2.2.1: Create domain package structure
```bash
cd ~/dev/Gauntlet/Invoice_AI/backend
mkdir -p src/main/java/com/invoiceme/domain/customer
```

### Step 2.2.2: Create Address value object
```bash
cat > src/main/java/com/invoiceme/domain/customer/Address.java << 'EOF'
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
EOF

cat src/main/java/com/invoiceme/domain/customer/Address.java
```

### Step 2.2.3: Create Customer entity
```bash
cat > src/main/java/com/invoiceme/domain/customer/Customer.java << 'EOF'
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
EOF

cat src/main/java/com/invoiceme/domain/customer/Customer.java
```

### Step 2.2.4: Create CustomerRepository
```bash
mkdir -p src/main/java/com/invoiceme/infrastructure/persistence

cat > src/main/java/com/invoiceme/infrastructure/persistence/CustomerRepository.java << 'EOF'
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
EOF

cat src/main/java/com/invoiceme/infrastructure/persistence/CustomerRepository.java
```

### Step 2.2.5: Test compilation
```bash
cd ~/dev/Gauntlet/Invoice_AI/backend
./mvnw clean compile
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
```

**Verification:**
- [ ] Address value object created
- [ ] Customer entity created with JPA annotations
- [ ] CustomerRepository created
- [ ] Code compiles successfully

---

## Task 2.3: Implement Invoice Domain Entity

### Step 2.3.1: Create invoice package
```bash
mkdir -p src/main/java/com/invoiceme/domain/invoice
```

### Step 2.3.2: Create InvoiceStatus enum
```bash
cat > src/main/java/com/invoiceme/domain/invoice/InvoiceStatus.java << 'EOF'
package com.invoiceme.domain.invoice;

public enum InvoiceStatus {
    DRAFT,
    SENT,
    PAID,
    CANCELLED
}
EOF
```

### Step 2.3.3: Create InvoiceNumber value object
```bash
cat > src/main/java/com/invoiceme/domain/invoice/InvoiceNumber.java << 'EOF'
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
EOF
```

### Step 2.3.4: Create LineItem entity
```bash
cat > src/main/java/com/invoiceme/domain/invoice/LineItem.java << 'EOF'
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

    @ManyToOne(fetch = FetchType.LAZY)
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
EOF
```

### Step 2.3.5: Create Invoice entity (part 1 - fields)
```bash
cat > src/main/java/com/invoiceme/domain/invoice/Invoice.java << 'EOF'
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

    @ManyToOne(fetch = FetchType.LAZY)
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
EOF
```

### Step 2.3.6: Create InvoiceRepository
```bash
cat > src/main/java/com/invoiceme/infrastructure/persistence/InvoiceRepository.java << 'EOF'
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
EOF
```

### Step 2.3.7: Test compilation
```bash
./mvnw clean compile
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
```

**Verification:**
- [ ] InvoiceStatus enum created
- [ ] InvoiceNumber value object created
- [ ] LineItem entity created
- [ ] Invoice entity created with state machine methods
- [ ] InvoiceRepository created
- [ ] Code compiles successfully

---

## Task 2.4: Implement Payment Domain Entity

### Step 2.4.1: Create payment package
```bash
mkdir -p src/main/java/com/invoiceme/domain/payment
```

### Step 2.4.2: Create PaymentMethod enum
```bash
cat > src/main/java/com/invoiceme/domain/payment/PaymentMethod.java << 'EOF'
package com.invoiceme.domain.payment;

public enum PaymentMethod {
    CREDIT_CARD,
    BANK_TRANSFER,
    CHECK,
    CASH,
    OTHER
}
EOF
```

### Step 2.4.3: Create Payment entity
```bash
cat > src/main/java/com/invoiceme/domain/payment/Payment.java << 'EOF'
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

    @ManyToOne(fetch = FetchType.LAZY)
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

    // Business Logic - Validation

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
EOF
```

### Step 2.4.4: Create PaymentRepository
```bash
cat > src/main/java/com/invoiceme/infrastructure/persistence/PaymentRepository.java << 'EOF'
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
EOF
```

### Step 2.4.5: Final compilation test
```bash
./mvnw clean compile
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
```

**Verification:**
- [ ] PaymentMethod enum created
- [ ] Payment entity created with validation
- [ ] PaymentRepository created
- [ ] All code compiles successfully

---

## Task 2.5: Run Application and Verify

### Step 2.5.1: Start Spring Boot application
```bash
./mvnw spring-boot:run
```

**Expected Output:**
```
INFO  Hikari            : HikariPool-1 - Starting...
INFO  Hikari            : HikariPool-1 - Start completed.
INFO  FlywayMigrationStrategy : Current version of schema "public": 1
INFO  InvoiceMeApplication : Started InvoiceMeApplication in X.XXX seconds
```

### Step 2.5.2: Verify entities in logs
Look for JPA mappings:
```
INFO  EntityManagerFactory : Hibernate:
    create table customers ...
    create table invoices ...
    create table invoice_line_items ...
    create table payments ...
```

### Step 2.5.3: Test database queries
In another terminal:
```bash
psql -d invoiceme << 'EOF'
-- Test customer table
SELECT count(*) FROM customers;

-- Test invoice table
SELECT count(*) FROM invoices;

-- Test line items table
SELECT count(*) FROM invoice_line_items;

-- Test payments table
SELECT count(*) FROM payments;

-- Verify foreign keys
SELECT
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
  ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
  ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY';
EOF
```

**Expected Output:**
```
 count
-------
     0
(All tables should exist but be empty)

Foreign keys should show:
- invoices -> customers
- invoice_line_items -> invoices
- payments -> invoices
```

### Step 2.5.4: Stop application
Press Ctrl+C

**Verification:**
- [ ] Application starts without errors
- [ ] All entities load correctly
- [ ] Database tables accessible
- [ ] Foreign keys established
- [ ] No compilation errors

---

## Task 2.6: Write Basic Domain Tests

### Step 2.6.1: Create test package structure
```bash
mkdir -p src/test/java/com/invoiceme/domain/customer
mkdir -p src/test/java/com/invoiceme/domain/invoice
mkdir -p src/test/java/com/invoiceme/domain/payment
```

### Step 2.6.2: Test Customer entity
```bash
cat > src/test/java/com/invoiceme/domain/customer/CustomerTest.java << 'EOF'
package com.invoiceme.domain.customer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    @Test
    void shouldCreateCustomerWithBillingAddress() {
        // Given
        Address billingAddress = new Address("123 Main St", "New York", "NY", "10001", "USA");
        Customer customer = new Customer();
        customer.setBusinessName("Test Corp");
        customer.setContactName("John Doe");
        customer.setEmail("john@test.com");
        customer.setBillingAddress(billingAddress);

        // When
        customer.onCreate();

        // Then
        assertNotNull(customer.getId());
        assertNotNull(customer.getCreatedAt());
        assertTrue(customer.isActive());
    }

    @Test
    void shouldDeactivateCustomer() {
        // Given
        Customer customer = new Customer();
        customer.setActive(true);

        // When
        customer.deactivate();

        // Then
        assertFalse(customer.isActive());
    }

    @Test
    void shouldDetectShippingAddress() {
        // Given
        Customer customer = new Customer();
        Address shippingAddress = new Address("456 Oak Ave", "Boston", "MA", "02101", "USA");
        customer.setShippingAddress(shippingAddress);

        // When/Then
        assertTrue(customer.hasShippingAddress());
    }
}
EOF
```

### Step 2.6.3: Test Invoice state machine
```bash
cat > src/test/java/com/invoiceme/domain/invoice/InvoiceTest.java << 'EOF'
package com.invoiceme.domain.invoice;

import com.invoiceme.domain.customer.Address;
import com.invoiceme.domain.customer.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceTest {

    private Invoice invoice;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setBusinessName("Test Corp");
        customer.setEmail("test@example.com");
        customer.setBillingAddress(new Address("123 Main", "NY", "NY", "10001", "USA"));

        invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setInvoiceNumber("INV-2025-0001");
        invoice.setIssueDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(30));
    }

    @Test
    void shouldStartInDraftStatus() {
        assertEquals(InvoiceStatus.DRAFT, invoice.getStatus());
    }

    @Test
    void shouldTransitionToSentWhenSent() {
        // Given
        LineItem item = new LineItem();
        item.setDescription("Test Item");
        item.setQuantity(BigDecimal.ONE);
        item.setUnitPrice(BigDecimal.valueOf(100));
        invoice.addLineItem(item);

        // When
        invoice.send();

        // Then
        assertEquals(InvoiceStatus.SENT, invoice.getStatus());
        assertNotNull(invoice.getSentAt());
        assertNotNull(invoice.getPaymentLink());
    }

    @Test
    void shouldThrowExceptionWhenSendingWithoutLineItems() {
        assertThrows(IllegalStateException.class, () -> invoice.send());
    }

    @Test
    void shouldCalculateTotalsCorrectly() {
        // Given
        LineItem item1 = new LineItem();
        item1.setDescription("Item 1");
        item1.setQuantity(BigDecimal.valueOf(2));
        item1.setUnitPrice(BigDecimal.valueOf(50));

        LineItem item2 = new LineItem();
        item2.setDescription("Item 2");
        item2.setQuantity(BigDecimal.valueOf(3));
        item2.setUnitPrice(BigDecimal.valueOf(30));

        invoice.setTaxAmount(BigDecimal.valueOf(19));

        // When
        invoice.addLineItem(item1);
        invoice.addLineItem(item2);

        // Then
        assertEquals(BigDecimal.valueOf(190), invoice.getSubtotal()); // (2*50) + (3*30) = 190
        assertEquals(BigDecimal.valueOf(209), invoice.getTotalAmount()); // 190 + 19 = 209
    }

    @Test
    void shouldMarkAsPaid() {
        // Given
        invoice.setStatus(InvoiceStatus.SENT);
        invoice.setTotalAmount(BigDecimal.valueOf(100));

        // When
        invoice.markAsPaid();

        // Then
        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
        assertNotNull(invoice.getPaidAt());
        assertEquals(BigDecimal.ZERO, invoice.getBalanceRemaining());
        assertEquals(invoice.getTotalAmount(), invoice.getAmountPaid());
    }

    @Test
    void shouldCancelWithReason() {
        // When
        invoice.cancel("Wrong customer");

        // Then
        assertEquals(InvoiceStatus.CANCELLED, invoice.getStatus());
        assertEquals("Wrong customer", invoice.getCancellationReason());
        assertNotNull(invoice.getCancelledAt());
    }
}
EOF
```

### Step 2.6.4: Run tests
```bash
./mvnw test
```

**Expected Output:**
```
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Verification:**
- [ ] Test files created
- [ ] All tests pass
- [ ] Domain logic validated
- [ ] Build succeeds

---

## Task 2.7: Commit Phase 2

### Step 2.7.1: Check git status
```bash
cd ~/dev/Gauntlet/Invoice_AI
git status
```

### Step 2.7.2: Add all new files
```bash
git add backend/src/
```

### Step 2.7.3: Commit with descriptive message
```bash
git commit -m "Phase 2: Domain model and database schema complete

- Created Flyway migration V1__initial_schema.sql
- Implemented Customer entity with Address value object
- Implemented Invoice entity with LineItem and state machine
- Implemented Payment entity with validation logic
- Created all repository interfaces
- Added domain unit tests
- All tests passing"
```

### Step 2.7.4: Verify commit
```bash
git log --oneline -2
```

**Expected Output:**
```
def5678 Phase 2: Domain model and database schema complete
abc1234 Phase 1: Project setup complete
```

---

## Phase 2 Completion Checklist

### Database Schema ✅
- [ ] Migration file V1__initial_schema.sql created
- [ ] All tables created (customers, invoices, line_items, payments)
- [ ] Indexes created for performance
- [ ] Foreign keys established
- [ ] Migration executes successfully

### Domain Entities ✅
- [ ] Address value object created
- [ ] Customer entity with JPA annotations
- [ ] InvoiceStatus and InvoiceNumber created
- [ ] LineItem entity created
- [ ] Invoice entity with state machine methods
- [ ] PaymentMethod enum created
- [ ] Payment entity with validation

### Repositories ✅
- [ ] CustomerRepository created
- [ ] InvoiceRepository created
- [ ] PaymentRepository created
- [ ] All query methods defined

### Testing ✅
- [ ] Customer entity tests pass
- [ ] Invoice state machine tests pass
- [ ] All domain tests pass
- [ ] Build succeeds

### Git ✅
- [ ] All code committed
- [ ] Descriptive commit message

---

## Next Steps

✅ **Phase 2 Complete!**

Proceed to **Phase 3: Customer Management (CQRS + VSA)**
- File: `Phase-03-Tasks.md`
- Estimated time: 6-8 hours
- Implements full customer CRUD with CQRS pattern

---

**Phase 2 Complete! Total Time: ~4-6 hours**
