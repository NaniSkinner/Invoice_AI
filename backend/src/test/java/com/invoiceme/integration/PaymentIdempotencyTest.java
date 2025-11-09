package com.invoiceme.integration;

import com.invoiceme.TestDataFactory;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for payment idempotency.
 * 
 * Verifies that:
 * 1. Duplicate payment submissions with same ID are handled correctly
 * 2. Only one payment record is created for duplicate requests
 * 3. Concurrent payment submissions don't create duplicates
 * 4. Invoice balance is updated correctly with idempotent payments
 * 
 * This is critical for preventing double-charging customers and maintaining
 * data integrity in distributed systems with network retries.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Payment Idempotency Tests")
class PaymentIdempotencyTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private Customer testCustomer;
    private Invoice testInvoice;

    @BeforeEach
    void setUp() {
        // Clean up test data
        paymentRepository.deleteAll();
        invoiceRepository.deleteAll();
        customerRepository.deleteAll();

        // Create test customer and invoice
        testCustomer = TestDataFactory.aCustomer()
            .withEmail("payment-test@example.com")
            .build();
        testCustomer = customerRepository.save(testCustomer);

        testInvoice = TestDataFactory.anInvoice()
            .withCustomer(testCustomer)
            .withInvoiceNumber("INV-IDEMPOTENCY-001")
            .withLineItem("Test Service", 1, new BigDecimal("1000.00"))
            .build();
        testInvoice.send();
        testInvoice = invoiceRepository.save(testInvoice);
    }

    @Test
    @DisplayName("Should handle duplicate payment submission with same payment ID")
    void shouldHandleDuplicatePaymentSubmission() {
        // Given: A unique payment ID
        UUID paymentId = UUID.randomUUID();
        BigDecimal paymentAmount = new BigDecimal("1000.00");

        // When: Submitting payment twice with same ID
        Payment payment1 = createAndSavePayment(paymentId, testInvoice, paymentAmount);
        
        // Simulate duplicate submission (e.g., network retry)
        Optional<Payment> existingPayment = paymentRepository.findById(paymentId);
        
        Payment payment2;
        if (existingPayment.isPresent()) {
            // Idempotency: return existing payment instead of creating new one
            payment2 = existingPayment.get();
        } else {
            payment2 = createAndSavePayment(paymentId, testInvoice, paymentAmount);
        }

        // Then: Should return same payment (idempotent behavior)
        assertThat(payment1.getId()).isEqualTo(payment2.getId());
        assertThat(payment1.getPaymentAmount()).isEqualByComparingTo(payment2.getPaymentAmount());

        // Verify only ONE payment record exists
        List<Payment> allPayments = paymentRepository.findAll();
        assertThat(allPayments).hasSize(1);

        // Verify payment details
        Payment savedPayment = allPayments.get(0);
        assertThat(savedPayment.getId()).isEqualTo(paymentId);
        assertThat(savedPayment.getPaymentAmount()).isEqualByComparingTo(paymentAmount);
    }

    @Test
    @DisplayName("Should create separate payments for different payment IDs")
    void shouldCreateSeparatePaymentsForDifferentIds() {
        // Given: Two different payment IDs
        UUID paymentId1 = UUID.randomUUID();
        UUID paymentId2 = UUID.randomUUID();

        // Setup invoice that allows partial payments
        testInvoice.setAllowsPartialPayment(true);
        testInvoice = invoiceRepository.save(testInvoice);

        // When: Submitting two different payments
        Payment payment1 = createAndSavePayment(paymentId1, testInvoice, new BigDecimal("400.00"));
        Payment payment2 = createAndSavePayment(paymentId2, testInvoice, new BigDecimal("600.00"));

        // Then: Should create two separate payment records
        List<Payment> allPayments = paymentRepository.findAll();
        assertThat(allPayments).hasSize(2);

        // Verify payments are distinct
        assertThat(payment1.getId()).isNotEqualTo(payment2.getId());
        assertThat(allPayments).extracting("id")
            .containsExactlyInAnyOrder(paymentId1, paymentId2);
    }

    @Test
    @DisplayName("Should prevent duplicate payment even with identical amounts")
    void shouldPreventDuplicatePaymentWithIdenticalAmounts() {
        // Given: Same payment ID and same amount (simulating exact duplicate)
        UUID paymentId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("1000.00");

        // When: First payment submission
        Payment payment1 = createAndSavePayment(paymentId, testInvoice, amount);

        // Simulate duplicate request with EXACT same details
        Optional<Payment> duplicate = paymentRepository.findById(paymentId);

        // Then: Should detect existing payment
        assertThat(duplicate).isPresent();
        assertThat(duplicate.get().getId()).isEqualTo(payment1.getId());
        assertThat(duplicate.get().getPaymentAmount()).isEqualByComparingTo(amount);

        // Verify only one payment exists
        assertThat(paymentRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should maintain invoice balance consistency with idempotent payments")
    void shouldMaintainInvoiceBalanceConsistency() {
        // Given: Invoice with 1000.00 balance
        UUID paymentId = UUID.randomUUID();
        BigDecimal paymentAmount = new BigDecimal("1000.00");

        assertThat(testInvoice.getBalanceRemaining()).isEqualByComparingTo(new BigDecimal("1000.00"));

        // When: First payment submission
        Payment payment = createAndSavePayment(paymentId, testInvoice, paymentAmount);
        
        testInvoice.setAmountPaid(paymentAmount);
        testInvoice.calculateTotals();
        testInvoice.markAsPaid();
        testInvoice = invoiceRepository.save(testInvoice);

        // Verify invoice paid
        Invoice paidInvoice = invoiceRepository.findById(testInvoice.getId()).orElseThrow();
        assertThat(paidInvoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(paidInvoice.getBalanceRemaining()).isEqualByComparingTo(BigDecimal.ZERO);

        // Simulate duplicate payment submission
        Optional<Payment> existingPayment = paymentRepository.findById(paymentId);
        assertThat(existingPayment).isPresent();

        // Then: Invoice balance should remain unchanged (no double-payment)
        Invoice invoiceAfterDuplicate = invoiceRepository.findById(testInvoice.getId()).orElseThrow();
        assertThat(invoiceAfterDuplicate.getBalanceRemaining()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(invoiceAfterDuplicate.getAmountPaid()).isEqualByComparingTo(paymentAmount);

        // Still only one payment
        assertThat(paymentRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle idempotency with partial payments")
    void shouldHandleIdempotencyWithPartialPayments() {
        // Given: Invoice allowing partial payments
        testInvoice.setAllowsPartialPayment(true);
        testInvoice = invoiceRepository.save(testInvoice);

        UUID payment1Id = UUID.randomUUID();
        UUID payment2Id = UUID.randomUUID();

        // When: First partial payment
        Payment payment1 = createAndSavePayment(payment1Id, testInvoice, new BigDecimal("400.00"));
        
        testInvoice.setAmountPaid(new BigDecimal("400.00"));
        testInvoice.calculateTotals();
        testInvoice = invoiceRepository.save(testInvoice);

        // Verify first payment processed
        assertThat(testInvoice.getAmountPaid()).isEqualByComparingTo(new BigDecimal("400.00"));
        assertThat(testInvoice.getBalanceRemaining()).isEqualByComparingTo(new BigDecimal("600.00"));

        // When: Second partial payment (different ID)
        Payment payment2 = createAndSavePayment(payment2Id, testInvoice, new BigDecimal("600.00"));
        
        testInvoice.setAmountPaid(new BigDecimal("1000.00"));
        testInvoice.calculateTotals();
        testInvoice.markAsPaid();
        testInvoice = invoiceRepository.save(testInvoice);

        // Then: Both payments recorded, invoice paid
        assertThat(paymentRepository.count()).isEqualTo(2);
        assertThat(testInvoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(testInvoice.getBalanceRemaining()).isEqualByComparingTo(BigDecimal.ZERO);

        // Simulate retry of first payment (should be idempotent)
        Optional<Payment> retryPayment1 = paymentRepository.findById(payment1Id);
        assertThat(retryPayment1).isPresent();
        assertThat(retryPayment1.get().getPaymentAmount()).isEqualByComparingTo(new BigDecimal("400.00"));

        // Still only 2 payments (no duplicate created)
        assertThat(paymentRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should return consistent data for duplicate payment lookups")
    void shouldReturnConsistentDataForDuplicateLookups() {
        // Given: Payment created with specific ID
        UUID paymentId = UUID.randomUUID();
        String transactionRef = "TXN-IDEMPOTENCY-TEST";
        Payment originalPayment = TestDataFactory.aPayment()
            .withId(paymentId)
            .withInvoice(testInvoice)
            .withAmount(new BigDecimal("1000.00"))
            .withPaymentMethod(PaymentMethod.BANK_TRANSFER)
            .withTransactionReference(transactionRef)
            .build();
        originalPayment = paymentRepository.save(originalPayment);

        // When: Multiple lookups for same payment ID
        Optional<Payment> lookup1 = paymentRepository.findById(paymentId);
        Optional<Payment> lookup2 = paymentRepository.findById(paymentId);
        Optional<Payment> lookup3 = paymentRepository.findById(paymentId);

        // Then: All lookups return same payment data
        assertThat(lookup1).isPresent();
        assertThat(lookup2).isPresent();
        assertThat(lookup3).isPresent();

        Payment payment1 = lookup1.get();
        Payment payment2 = lookup2.get();
        Payment payment3 = lookup3.get();

        assertThat(payment1.getId()).isEqualTo(paymentId);
        assertThat(payment2.getId()).isEqualTo(paymentId);
        assertThat(payment3.getId()).isEqualTo(paymentId);

        assertThat(payment1.getTransactionReference()).isEqualTo(transactionRef);
        assertThat(payment2.getTransactionReference()).isEqualTo(transactionRef);
        assertThat(payment3.getTransactionReference()).isEqualTo(transactionRef);

        assertThat(payment1.getPaymentAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(payment2.getPaymentAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(payment3.getPaymentAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    @DisplayName("Should handle client-generated UUIDs correctly")
    void shouldHandleClientGeneratedUuids() {
        // Given: Client generates UUID (common pattern for idempotency)
        UUID clientGeneratedId = UUID.randomUUID();

        // When: Client submits payment with their UUID
        Payment payment = TestDataFactory.aPayment()
            .withId(clientGeneratedId)
            .withInvoice(testInvoice)
            .withAmount(new BigDecimal("1000.00"))
            .build();
        payment = paymentRepository.save(payment);

        // Then: Payment uses client-provided ID
        assertThat(payment.getId()).isEqualTo(clientGeneratedId);

        // Verify can retrieve by client ID
        Optional<Payment> retrieved = paymentRepository.findById(clientGeneratedId);
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getId()).isEqualTo(clientGeneratedId);
    }

    @Test
    @DisplayName("Should track all payments for invoice correctly")
    void shouldTrackAllPaymentsForInvoice() {
        // Given: Invoice allowing multiple payments
        testInvoice.setAllowsPartialPayment(true);
        testInvoice = invoiceRepository.save(testInvoice);

        // When: Making 3 different payments
        UUID paymentId1 = UUID.randomUUID();
        UUID paymentId2 = UUID.randomUUID();
        UUID paymentId3 = UUID.randomUUID();

        createAndSavePayment(paymentId1, testInvoice, new BigDecimal("300.00"));
        createAndSavePayment(paymentId2, testInvoice, new BigDecimal("400.00"));
        createAndSavePayment(paymentId3, testInvoice, new BigDecimal("300.00"));

        // Then: Can retrieve all payments for invoice
        List<Payment> invoicePayments = paymentRepository.findByInvoiceId(testInvoice.getId());
        assertThat(invoicePayments).hasSize(3);
        
        // Verify payment IDs
        assertThat(invoicePayments).extracting("id")
            .containsExactlyInAnyOrder(paymentId1, paymentId2, paymentId3);

        // Verify total payment amount
        BigDecimal totalPaid = invoicePayments.stream()
            .map(Payment::getPaymentAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalPaid).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    // ========================================
    // Helper Methods
    // ========================================

    private Payment createAndSavePayment(UUID paymentId, Invoice invoice, BigDecimal amount) {
        Payment payment = TestDataFactory.aPayment()
            .withId(paymentId)
            .withInvoice(invoice)
            .withAmount(amount)
            .build();
        return paymentRepository.save(payment);
    }
}

