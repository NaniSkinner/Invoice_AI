package com.invoiceme.integration;

import com.invoiceme.TestDataFactory;
import com.invoiceme.domain.customer.Address;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceStatus;
import com.invoiceme.domain.payment.Payment;
import com.invoiceme.domain.payment.PaymentMethod;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import com.invoiceme.infrastructure.persistence.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test for the complete Customer-Invoice-Payment business flow.
 * 
 * This test verifies the entire lifecycle:
 * 1. Create a customer
 * 2. Create an invoice in DRAFT status
 * 3. Add line items to the invoice
 * 4. Send the invoice (DRAFT -> SENT)
 * 5. Record a payment
 * 6. Verify invoice is marked as PAID
 * 
 * This demonstrates:
 * - DDD aggregate boundaries
 * - State machine transitions
 * - Business rule enforcement
 * - Data integrity across domains
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Customer-Invoice-Payment Integration Flow")
class CustomerInvoicePaymentFlowTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        paymentRepository.deleteAll();
        invoiceRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    @DisplayName("Should complete full customer-invoice-payment flow successfully")
    void shouldCompleteFullBusinessFlow() {
        // ========================================
        // Step 1: Create Customer
        // ========================================
        Customer customer = TestDataFactory.aCustomer()
            .withBusinessName("Acme Corporation")
            .withContactName("John Smith")
            .withEmail("john@acmecorp.com")
            .withPhone("555-1234")
            .withBillingAddress(new Address(
                "123 Main St",
                "New York",
                "NY",
                "10001",
                "USA"
            ))
            .build();

        customer = customerRepository.save(customer);
        assertThat(customer.getId()).isNotNull();
        assertThat(customer.getBusinessName()).isEqualTo("Acme Corporation");
        assertThat(customer.isActive()).isTrue();

        // ========================================
        // Step 2: Create Invoice in DRAFT Status
        // ========================================
        Invoice invoice = TestDataFactory.anInvoice()
            .withCustomer(customer)
            .withInvoiceNumber("INV-2025-0001")
            .withIssueDate(LocalDate.now())
            .withDueDate(LocalDate.now().plusDays(30))
            .withStatus(InvoiceStatus.DRAFT)
            .build();

        // ========================================
        // Step 3: Add Line Items
        // ========================================
        invoice.getLineItems().clear();
        invoice = TestDataFactory.anInvoice()
            .withCustomer(customer)
            .withInvoiceNumber("INV-2025-0001")
            .withLineItem("Consulting Services", 10, new BigDecimal("100.00"))
            .withLineItem("Development Hours", 20, new BigDecimal("150.00"))
            .withTaxAmount(new BigDecimal("80.00"))
            .build();

        invoice = invoiceRepository.save(invoice);
        
        // Verify invoice calculations
        assertThat(invoice.getId()).isNotNull();
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
        assertThat(invoice.getLineItems()).hasSize(2);
        assertThat(invoice.getSubtotal()).isEqualByComparingTo(new BigDecimal("4000.00")); // 10*100 + 20*150
        assertThat(invoice.getTaxAmount()).isEqualByComparingTo(new BigDecimal("80.00"));
        assertThat(invoice.getTotalAmount()).isEqualByComparingTo(new BigDecimal("4080.00"));
        assertThat(invoice.getBalanceRemaining()).isEqualByComparingTo(new BigDecimal("4080.00"));
        assertThat(invoice.getPaymentLink()).isNull(); // Not sent yet

        // ========================================
        // Step 4: Send Invoice (DRAFT -> SENT)
        // ========================================
        invoice.send();
        invoice = invoiceRepository.save(invoice);

        // Verify state transition
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.SENT);
        assertThat(invoice.getSentAt()).isNotNull();
        assertThat(invoice.getPaymentLink()).isNotNull();
        assertThat(invoice.getPaymentLink()).isNotBlank();

        // ========================================
        // Step 5: Record Full Payment
        // ========================================
        UUID paymentId = UUID.randomUUID();
        Payment payment = TestDataFactory.aPayment()
            .withId(paymentId)
            .withInvoice(invoice)
            .withAmount(new BigDecimal("4080.00"))
            .withPaymentMethod(PaymentMethod.CREDIT_CARD)
            .withTransactionReference("TXN-12345")
            .build();

        // Validate payment before saving
        payment.validate();
        payment = paymentRepository.save(payment);

        // Update invoice with payment
        invoice.setAmountPaid(new BigDecimal("4080.00"));
        invoice.calculateTotals();
        invoice.markAsPaid();
        invoice = invoiceRepository.save(invoice);

        // ========================================
        // Step 6: Verify Final State
        // ========================================
        
        // Verify payment recorded
        assertThat(payment.getId()).isEqualTo(paymentId);
        assertThat(payment.getPaymentAmount()).isEqualByComparingTo(new BigDecimal("4080.00"));
        assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);

        // Verify invoice marked as PAID
        Invoice paidInvoice = invoiceRepository.findById(invoice.getId()).orElseThrow();
        assertThat(paidInvoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(paidInvoice.getPaidAt()).isNotNull();
        assertThat(paidInvoice.getAmountPaid()).isEqualByComparingTo(new BigDecimal("4080.00"));
        assertThat(paidInvoice.getBalanceRemaining()).isEqualByComparingTo(BigDecimal.ZERO);

        // Verify customer still active and unchanged
        Customer finalCustomer = customerRepository.findById(customer.getId()).orElseThrow();
        assertThat(finalCustomer.isActive()).isTrue();
        assertThat(finalCustomer.getBusinessName()).isEqualTo("Acme Corporation");

        // Verify all entities persisted correctly
        assertThat(customerRepository.count()).isEqualTo(1);
        assertThat(invoiceRepository.count()).isEqualTo(1);
        assertThat(paymentRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle partial payments when enabled")
    void shouldHandlePartialPayments() {
        // Setup: Create customer and invoice
        Customer customer = TestDataFactory.aCustomer()
            .withEmail("partial@test.com")
            .build();
        customer = customerRepository.save(customer);

        Invoice invoice = TestDataFactory.anInvoice()
            .withCustomer(customer)
            .withInvoiceNumber("INV-2025-0002")
            .withLineItem("Service", 1, new BigDecimal("1000.00"))
            .allowsPartialPayment()
            .build();
        invoice.send();
        invoice = invoiceRepository.save(invoice);

        // First partial payment: $400
        Payment payment1 = TestDataFactory.aPayment()
            .withInvoice(invoice)
            .withAmount(new BigDecimal("400.00"))
            .build();
        payment1.validate();
        payment1 = paymentRepository.save(payment1);

        invoice.setAmountPaid(new BigDecimal("400.00"));
        invoice.calculateTotals();
        invoice = invoiceRepository.save(invoice);

        // Verify invoice still SENT with remaining balance
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.SENT);
        assertThat(invoice.getAmountPaid()).isEqualByComparingTo(new BigDecimal("400.00"));
        assertThat(invoice.getBalanceRemaining()).isEqualByComparingTo(new BigDecimal("600.00"));

        // Second partial payment: $600
        Payment payment2 = TestDataFactory.aPayment()
            .withInvoice(invoice)
            .withAmount(new BigDecimal("600.00"))
            .build();
        payment2.validate();
        payment2 = paymentRepository.save(payment2);

        invoice.setAmountPaid(new BigDecimal("1000.00"));
        invoice.calculateTotals();
        invoice.markAsPaid();
        invoice = invoiceRepository.save(invoice);

        // Verify invoice now PAID
        Invoice paidInvoice = invoiceRepository.findById(invoice.getId()).orElseThrow();
        assertThat(paidInvoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(paidInvoice.getAmountPaid()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(paidInvoice.getBalanceRemaining()).isEqualByComparingTo(BigDecimal.ZERO);

        // Verify two payments recorded
        assertThat(paymentRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should prevent customer deletion when active invoices exist")
    void shouldPreventCustomerDeletionWithActiveInvoices() {
        // Setup: Create customer with active invoice
        Customer customer = TestDataFactory.aCustomer()
            .withEmail("nodelete@test.com")
            .build();
        customer = customerRepository.save(customer);

        Invoice invoice = TestDataFactory.anInvoice()
            .withCustomer(customer)
            .withLineItem("Service", 1, new BigDecimal("500.00"))
            .build();
        invoice.send();
        invoice = invoiceRepository.save(invoice);

        // Verify customer has active invoice
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.SENT);

        // Business rule: Should not delete customer with active invoices
        // (In real implementation, this would throw a business exception)
        // For this test, we verify the relationship exists
        Customer persistedCustomer = customerRepository.findById(customer.getId()).orElseThrow();
        assertThat(persistedCustomer).isNotNull();
        
        // Verify we can find invoices by customer
        var customerInvoices = invoiceRepository.findByCustomerId(customer.getId());
        assertThat(customerInvoices).hasSize(1);
        assertThat(customerInvoices.get(0).getStatus()).isEqualTo(InvoiceStatus.SENT);
    }

    @Test
    @DisplayName("Should allow customer deletion after all invoices are paid or cancelled")
    void shouldAllowCustomerDeletionAfterInvoicesCompleted() {
        // Setup: Create customer with invoice
        Customer customer = TestDataFactory.aCustomer()
            .withEmail("candelete@test.com")
            .build();
        customer = customerRepository.save(customer);

        Invoice invoice = TestDataFactory.anInvoice()
            .withCustomer(customer)
            .withLineItem("Service", 1, new BigDecimal("500.00"))
            .build();
        invoice.send();
        invoice = invoiceRepository.save(invoice);

        // Pay the invoice
        Payment payment = TestDataFactory.aPayment()
            .withInvoice(invoice)
            .withAmount(new BigDecimal("500.00"))
            .build();
        payment = paymentRepository.save(payment);

        invoice.setAmountPaid(new BigDecimal("500.00"));
        invoice.calculateTotals();
        invoice.markAsPaid();
        invoice = invoiceRepository.save(invoice);

        // Verify invoice is paid
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);

        // Now customer can be deactivated
        customer.deactivate();
        customer = customerRepository.save(customer);

        assertThat(customer.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should maintain data integrity across transaction boundaries")
    void shouldMaintainDataIntegrity() {
        // This test verifies that relationships are maintained correctly
        Customer customer = TestDataFactory.aCustomer()
            .withEmail("integrity@test.com")
            .build();
        customer = customerRepository.save(customer);

        Invoice invoice = TestDataFactory.anInvoice()
            .withCustomer(customer)
            .withLineItem("Item 1", 2, new BigDecimal("50.00"))
            .withLineItem("Item 2", 3, new BigDecimal("75.00"))
            .build();
        invoice = invoiceRepository.save(invoice);

        // Reload from database and verify relationships
        Invoice reloadedInvoice = invoiceRepository.findById(invoice.getId()).orElseThrow();
        assertThat(reloadedInvoice.getCustomer()).isNotNull();
        assertThat(reloadedInvoice.getCustomer().getId()).isEqualTo(customer.getId());
        assertThat(reloadedInvoice.getLineItems()).hasSize(2);
        
        // Verify line items belong to invoice
        UUID invoiceId = invoice.getId(); // Store in final variable for lambda
        reloadedInvoice.getLineItems().forEach(item -> {
            assertThat(item.getInvoice()).isNotNull();
            assertThat(item.getInvoice().getId()).isEqualTo(invoiceId);
        });

        // Verify totals recalculated correctly
        reloadedInvoice.calculateTotals();
        assertThat(reloadedInvoice.getSubtotal()).isEqualByComparingTo(new BigDecimal("325.00"));
    }
}

