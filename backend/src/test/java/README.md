# InvoiceMe Test Suite

## Overview

This test suite provides comprehensive coverage of the InvoiceMe application, demonstrating DDD, CQRS, and VSA principles through testing.

## Test Structure

```
src/test/java/com/invoiceme/
├── TestDataFactory.java                              [Test Data Builder]
├── integration/
│   ├── CustomerInvoicePaymentFlowTest.java          [Integration Tests]
│   └── PaymentIdempotencyTest.java                  [Integration Tests]
└── domain/
    ├── InvoiceStateMachineTest.java                 [Unit Tests]
    ├── PaymentValidationTest.java                   [Unit Tests]
    └── CustomerDomainTest.java                      [Unit Tests]
```

## Test Coverage

### 1. Integration Tests (End-to-End)

#### CustomerInvoicePaymentFlowTest.java

**Purpose:** Verifies complete business flow from customer creation to payment

**Test Scenarios:**

- ✅ Complete customer-invoice-payment lifecycle
- ✅ Partial payment handling (multiple payments)
- ✅ Customer deletion prevention with active invoices
- ✅ Customer deletion after invoices completed
- ✅ Data integrity across transaction boundaries

**Key Assertions:**

- Customer creation and persistence
- Invoice state transitions (DRAFT → SENT → PAID)
- Payment recording and balance calculation
- Relationship integrity between entities
- Business rule enforcement

#### PaymentIdempotencyTest.java

**Purpose:** Ensures payment operations are idempotent (critical for preventing double-charging)

**Test Scenarios:**

- ✅ Duplicate payment submission with same ID
- ✅ Separate payments with different IDs
- ✅ Duplicate prevention with identical amounts
- ✅ Invoice balance consistency with idempotent payments
- ✅ Idempotency with partial payments
- ✅ Consistent data for duplicate lookups
- ✅ Client-generated UUID handling
- ✅ Payment tracking for invoices

**Key Assertions:**

- Only one payment created for duplicate IDs
- Payment retrieval by ID is consistent
- Invoice balance updated correctly
- No double-payment scenarios

---

### 2. Domain Unit Tests

#### InvoiceStateMachineTest.java

**Purpose:** Tests all invoice state transitions and business logic

**Test Scenarios:**

- ✅ DRAFT → SENT transition validation
- ✅ SENT → PAID transition validation
- ✅ ANY → CANCELLED transition validation
- ✅ Invalid transition rejection (with exceptions)
- ✅ Business logic: calculate totals, update balance
- ✅ Edge cases: no line items, no tax, multiple line items

**State Transitions Tested:**

```
DRAFT ──send()──> SENT ──markAsPaid()──> PAID
  │                 │
  │                 └──cancel()──> CANCELLED
  └──cancel()──> CANCELLED
```

**Key Assertions:**

- State transitions follow business rules
- Invalid transitions throw IllegalStateException
- Payment link generated on send
- Balance calculations correct
- Timestamps updated appropriately

#### PaymentValidationTest.java

**Purpose:** Tests payment validation business rules

**Test Scenarios:**

- ✅ Valid payment amounts
- ✅ Zero/negative amount rejection
- ✅ Overpayment prevention
- ✅ Full payment acceptance
- ✅ Partial payment handling (when allowed/disallowed)
- ✅ Validation with existing balance
- ✅ Decimal precision handling
- ✅ Very small and very large amounts

**Business Rules Validated:**

- Payment amount > 0
- Payment ≤ invoice balance
- Partial payments require explicit permission
- No overpayment allowed

#### CustomerDomainTest.java

**Purpose:** Tests Customer and Address domain logic

**Test Scenarios:**

- ✅ Complete address validation
- ✅ Address formatting
- ✅ Incomplete address detection
- ✅ Shipping address handling
- ✅ Customer activation/deactivation
- ✅ Customer identity maintenance
- ✅ International address support
- ✅ Address equality comparison

**Key Assertions:**

- Address completeness validation
- Billing vs. shipping address distinction
- Customer active status management
- Metadata (timestamps, IDs) properly set

---

### 3. Test Data Factory

#### TestDataFactory.java

**Purpose:** Builder pattern for creating test data with sensible defaults

**Builders Provided:**

- `CustomerBuilder` - Create test customers
- `InvoiceBuilder` - Create test invoices with line items
- `PaymentBuilder` - Create test payments
- `Address` helpers - Create test addresses

**Example Usage:**

```java
Customer customer = TestDataFactory.aCustomer()
    .withBusinessName("Acme Corp")
    .withEmail("test@acme.com")
    .build();

Invoice invoice = TestDataFactory.anInvoice()
    .withCustomer(customer)
    .withLineItem("Service", 10, new BigDecimal("100.00"))
    .withTaxAmount(new BigDecimal("80.00"))
    .build();

Payment payment = TestDataFactory.aPayment()
    .withInvoice(invoice)
    .withAmount(new BigDecimal("1080.00"))
    .build();
```

---

## Running Tests

### Run All Tests

```bash
cd backend
./mvnw test
```

### Run Specific Test Class

```bash
./mvnw test -Dtest=CustomerInvoicePaymentFlowTest
./mvnw test -Dtest=InvoiceStateMachineTest
```

### Generate Coverage Report

```bash
./mvnw test jacoco:report
```

Coverage report will be generated at:

```
backend/target/site/jacoco/index.html
```

### Run Tests with Coverage Verification

```bash
./mvnw verify
```

This will run tests and check if coverage meets minimum threshold (70%).

---

## Test Configuration

### application-test.properties

- Uses H2 in-memory database (PostgreSQL compatibility mode)
- Disables Flyway migrations (uses JPA DDL auto)
- Mock OpenAI configuration
- Debug logging enabled for troubleshooting

### Dependencies

- **Spring Boot Test Starter** - Testing framework
- **JUnit 5** - Test runner
- **AssertJ** - Fluent assertions
- **H2 Database** - In-memory test database
- **REST Assured** - API testing (available but not yet used)
- **JaCoCo** - Code coverage

---

## Coverage Goals

**Target Coverage:** 80%+ for business logic

**Priority Areas:**

1. **Domain Layer** - 90%+ (business logic)
2. **Application Layer** - 80%+ (command/query handlers)
3. **Infrastructure Layer** - 70%+ (repositories, services)
4. **API Layer** - 60%+ (controllers)

---

## Test Principles Demonstrated

### 1. DDD Testing

- **Domain entities** tested independently
- **Business rules** validated in domain tests
- **Value objects** (Address) tested for correctness
- **Aggregates** (Invoice with LineItems) tested as units

### 2. CQRS Testing

- **Commands** tested for state changes
- **Queries** tested for data retrieval
- Clear separation in test structure

### 3. Integration Testing

- **End-to-end flows** verify complete scenarios
- **Transaction boundaries** respected
- **Database persistence** verified
- **Business workflows** tested holistically

### 4. Test Data Management

- **Builder pattern** for flexible test data creation
- **Sensible defaults** reduce test boilerplate
- **Reusable builders** across all tests

---

## Test Statistics

**Total Test Classes:** 5
**Total Test Methods:** ~60+

**Breakdown:**

- Integration Tests: 2 classes, 15+ tests
- Domain Unit Tests: 3 classes, 45+ tests
- Test Data Factory: 1 utility class

**Coverage Areas:**

- ✅ Customer lifecycle
- ✅ Invoice state machine
- ✅ Payment validation
- ✅ Payment idempotency
- ✅ Domain business rules
- ✅ Data integrity
- ✅ Edge cases
- ✅ Error scenarios

---

## Known Limitations

1. **AI Service Testing:** OpenAI service not yet tested with mocks
2. **API Controller Testing:** REST endpoints not yet tested with MockMvc
3. **Security Testing:** Authentication/authorization not yet tested
4. **Scheduler Testing:** Cron job scheduler not yet tested
5. **Email Service Testing:** Email sending service not yet tested

These can be added in Phase 12 continuation.

---

## Best Practices

### Writing New Tests

1. **Use TestDataFactory** - Don't create entities manually
2. **Follow AAA Pattern** - Arrange, Act, Assert
3. **Test One Thing** - Single assertion per test when possible
4. **Descriptive Names** - Use @DisplayName with clear descriptions
5. **Test Edge Cases** - Don't just test happy path
6. **Verify Exceptions** - Use assertThatThrownBy for error cases

### Example Test Structure

```java
@Test
@DisplayName("Should reject payment with negative amount")
void shouldRejectPaymentWithNegativeAmount() {
    // Given: Setup test data
    Payment payment = TestDataFactory.aPayment()
        .withAmount(new BigDecimal("-100.00"))
        .build();

    // When/Then: Execute and verify
    assertThatThrownBy(() -> payment.validate())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must be greater than zero");
}
```

---

## Maintenance

### Adding New Tests

1. Create test class in appropriate package
2. Use @SpringBootTest for integration tests
3. Use simple JUnit for domain unit tests
4. Add @DisplayName annotations
5. Use TestDataFactory for test data

### Updating Test Data

1. Modify TestDataFactory builders
2. Keep defaults sensible
3. Allow overrides for specific scenarios

### Reviewing Coverage

1. Run `./mvnw test jacoco:report`
2. Open `target/site/jacoco/index.html`
3. Focus on domain and application layers
4. Aim for 80%+ in critical paths

---

## Next Steps

For complete test coverage:

- [ ] Add REST API tests (MockMvc)
- [ ] Add AI service tests (with mocks)
- [ ] Add scheduler tests
- [ ] Add security tests
- [ ] Add performance tests

---

**Maintained By:** InvoiceMe Development Team  
**Last Updated:** November 9, 2025  
**Test Framework:** JUnit 5 + Spring Boot Test + AssertJ
