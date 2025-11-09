package com.invoiceme.domain;

import com.invoiceme.TestDataFactory;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Invoice state machine transitions.
 * 
 * Verifies all valid and invalid state transitions:
 * - DRAFT -> SENT (via send())
 * - SENT -> PAID (via markAsPaid())
 * - ANY -> CANCELLED (via cancel())
 * - Invalid transitions should throw exceptions
 */
@DisplayName("Invoice State Machine Tests")
class InvoiceStateMachineTest {

    private Customer testCustomer;
    private Invoice invoice;

    @BeforeEach
    void setUp() {
        testCustomer = TestDataFactory.aCustomer().build();
    }

    // ========================================
    // DRAFT -> SENT Transition Tests
    // ========================================

    @Test
    @DisplayName("Should transition from DRAFT to SENT when sending invoice")
    void shouldTransitionFromDraftToSent() {
        // Given: Invoice in DRAFT status with line items
        invoice = TestDataFactory.anInvoice()
            .withCustomer(testCustomer)
            .withStatus(InvoiceStatus.DRAFT)
            .withLineItem("Service", 1, new BigDecimal("100.00"))
            .build();

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
        assertThat(invoice.getPaymentLink()).isNull();
        assertThat(invoice.getSentAt()).isNull();

        // When: Sending the invoice
        invoice.send();

        // Then: Status changes to SENT and metadata updated
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.SENT);
        assertThat(invoice.getPaymentLink()).isNotNull();
        assertThat(invoice.getPaymentLink()).isNotBlank();
        assertThat(invoice.getSentAt()).isNotNull();
    }

    @Test
    @DisplayName("Should fail to send invoice without line items")
    void shouldFailToSendInvoiceWithoutLineItems() {
        // Given: Invoice in DRAFT status WITHOUT line items
        invoice = TestDataFactory.anInvoice()
            .withCustomer(testCustomer)
            .withStatus(InvoiceStatus.DRAFT)
            .build();
        invoice.getLineItems().clear();

        // When/Then: Sending should throw exception
        assertThatThrownBy(() -> invoice.send())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Cannot send invoice without line items");

        // Status should remain DRAFT
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
    }

    @Test
    @DisplayName("Should fail to send invoice that is not in DRAFT status")
    void shouldFailToSendNonDraftInvoice() {
        // Given: Invoice already SENT
        invoice = TestDataFactory.anInvoice()
            .withCustomer(testCustomer)
            .withStatus(InvoiceStatus.SENT)
            .withLineItem("Service", 1, new BigDecimal("100.00"))
            .build();
        invoice.setPaymentLink("existing-link");

        // When/Then: Sending again should throw exception
        assertThatThrownBy(() -> invoice.send())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Can only send invoices in DRAFT status");
    }

    // ========================================
    // SENT -> PAID Transition Tests
    // ========================================

    @Test
    @DisplayName("Should transition from SENT to PAID when marked as paid")
    void shouldTransitionFromSentToPaid() {
        // Given: Invoice in SENT status
        invoice = TestDataFactory.anInvoice()
            .withCustomer(testCustomer)
            .withStatus(InvoiceStatus.SENT)
            .withLineItem("Service", 1, new BigDecimal("500.00"))
            .build();
        invoice.setPaymentLink("test-payment-link");

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.SENT);
        assertThat(invoice.getPaidAt()).isNull();

        // When: Marking as paid
        invoice.markAsPaid();

        // Then: Status changes to PAID
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(invoice.getPaidAt()).isNotNull();
        assertThat(invoice.getBalanceRemaining()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(invoice.getAmountPaid()).isEqualByComparingTo(invoice.getTotalAmount());
    }

    @Test
    @DisplayName("Should fail to mark DRAFT invoice as paid")
    void shouldFailToMarkDraftInvoiceAsPaid() {
        // Given: Invoice in DRAFT status
        invoice = TestDataFactory.anInvoice()
            .withCustomer(testCustomer)
            .withStatus(InvoiceStatus.DRAFT)
            .withLineItem("Service", 1, new BigDecimal("500.00"))
            .build();

        // When/Then: Marking as paid should throw exception
        assertThatThrownBy(() -> invoice.markAsPaid())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Can only mark SENT invoices as paid");

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
    }

    @Test
    @DisplayName("Should fail to mark CANCELLED invoice as paid")
    void shouldFailToMarkCancelledInvoiceAsPaid() {
        // Given: Invoice in CANCELLED status
        invoice = TestDataFactory.anInvoice()
            .withCustomer(testCustomer)
            .withStatus(InvoiceStatus.CANCELLED)
            .withLineItem("Service", 1, new BigDecimal("500.00"))
            .build();
        invoice.setCancelledAt(java.time.LocalDateTime.now());
        invoice.setCancellationReason("Test cancellation");

        // When/Then: Marking as paid should throw exception
        assertThatThrownBy(() -> invoice.markAsPaid())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Can only mark SENT invoices as paid");

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.CANCELLED);
    }

    // ========================================
    // ANY -> CANCELLED Transition Tests
    // ========================================

    @Test
    @DisplayName("Should cancel DRAFT invoice")
    void shouldCancelDraftInvoice() {
        // Given: Invoice in DRAFT status
        invoice = TestDataFactory.anInvoice()
            .withCustomer(testCustomer)
            .withStatus(InvoiceStatus.DRAFT)
            .withLineItem("Service", 1, new BigDecimal("500.00"))
            .build();

        // When: Cancelling invoice
        invoice.cancel("Wrong customer");

        // Then: Status changes to CANCELLED
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.CANCELLED);
        assertThat(invoice.getCancellationReason()).isEqualTo("Wrong customer");
        assertThat(invoice.getCancelledAt()).isNotNull();
    }

    @Test
    @DisplayName("Should cancel SENT invoice")
    void shouldCancelSentInvoice() {
        // Given: Invoice in SENT status
        invoice = TestDataFactory.anInvoice()
            .withCustomer(testCustomer)
            .withStatus(InvoiceStatus.SENT)
            .withLineItem("Service", 1, new BigDecimal("500.00"))
            .build();
        invoice.setPaymentLink("test-payment-link");

        // When: Cancelling invoice
        invoice.cancel("Customer requested cancellation");

        // Then: Status changes to CANCELLED
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.CANCELLED);
        assertThat(invoice.getCancellationReason()).isEqualTo("Customer requested cancellation");
        assertThat(invoice.getCancelledAt()).isNotNull();
    }

    @Test
    @DisplayName("Should cancel PAID invoice (edge case)")
    void shouldCancelPaidInvoice() {
        // Given: Invoice in PAID status (rare but valid for refunds)
        invoice = TestDataFactory.anInvoice()
            .withCustomer(testCustomer)
            .withStatus(InvoiceStatus.PAID)
            .withLineItem("Service", 1, new BigDecimal("500.00"))
            .build();
        invoice.setPaidAt(java.time.LocalDateTime.now());

        // When: Cancelling invoice
        invoice.cancel("Refund requested");

        // Then: Status changes to CANCELLED
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.CANCELLED);
        assertThat(invoice.getCancellationReason()).isEqualTo("Refund requested");
        assertThat(invoice.getCancelledAt()).isNotNull();
    }

    // ========================================
    // Business Logic Tests
    // ========================================

    @Test
    @DisplayName("Should calculate totals correctly")
    void shouldCalculateTotalsCorrectly() {
        // Given: Invoice with multiple line items and tax
        invoice = TestDataFactory.anInvoice()
            .withCustomer(testCustomer)
            .withLineItem("Item 1", 2, new BigDecimal("50.00"))
            .withLineItem("Item 2", 3, new BigDecimal("100.00"))
            .withTaxAmount(new BigDecimal("70.00"))
            .build();

        // When: Calculating totals
        invoice.calculateTotals();

        // Then: Totals are correct
        // Subtotal: (2 * 50) + (3 * 100) = 100 + 300 = 400
        // Total: 400 + 70 = 470
        assertThat(invoice.getSubtotal()).isEqualByComparingTo(new BigDecimal("400.00"));
        assertThat(invoice.getTotalAmount()).isEqualByComparingTo(new BigDecimal("470.00"));
        assertThat(invoice.getBalanceRemaining()).isEqualByComparingTo(new BigDecimal("470.00"));
    }

    @Test
    @DisplayName("Should update balance after payment")
    void shouldUpdateBalanceAfterPayment() {
        // Given: Invoice with total of 1000
        invoice = TestDataFactory.anInvoice()
            .withCustomer(testCustomer)
            .withLineItem("Service", 1, new BigDecimal("1000.00"))
            .build();

        // When: Recording payment of 400
        invoice.setAmountPaid(new BigDecimal("400.00"));
        invoice.calculateTotals();

        // Then: Balance should be 600
        assertThat(invoice.getTotalAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(invoice.getAmountPaid()).isEqualByComparingTo(new BigDecimal("400.00"));
        assertThat(invoice.getBalanceRemaining()).isEqualByComparingTo(new BigDecimal("600.00"));
    }

    @Test
    @DisplayName("Should have zero balance when fully paid")
    void shouldHaveZeroBalanceWhenFullyPaid() {
        // Given: Invoice with total of 1000
        invoice = TestDataFactory.anInvoice()
            .withCustomer(testCustomer)
            .withLineItem("Service", 1, new BigDecimal("1000.00"))
            .build();

        // When: Recording full payment
        invoice.setAmountPaid(new BigDecimal("1000.00"));
        invoice.calculateTotals();

        // Then: Balance should be zero
        assertThat(invoice.getBalanceRemaining()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should not allow sending invoice in PAID status")
    void shouldNotAllowSendingPaidInvoice() {
        // Given: Invoice already PAID
        invoice = TestDataFactory.anInvoice()
            .withCustomer(testCustomer)
            .withStatus(InvoiceStatus.PAID)
            .withLineItem("Service", 1, new BigDecimal("100.00"))
            .build();

        // When/Then: Cannot send paid invoice
        assertThatThrownBy(() -> invoice.send())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Can only send invoices in DRAFT status");
    }

    @Test
    @DisplayName("Should not allow sending invoice in CANCELLED status")
    void shouldNotAllowSendingCancelledInvoice() {
        // Given: Invoice already CANCELLED
        invoice = TestDataFactory.anInvoice()
            .withCustomer(testCustomer)
            .withStatus(InvoiceStatus.CANCELLED)
            .withLineItem("Service", 1, new BigDecimal("100.00"))
            .build();

        // When/Then: Cannot send cancelled invoice
        assertThatThrownBy(() -> invoice.send())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Can only send invoices in DRAFT status");
    }

    // ========================================
    // Edge Cases and Validation
    // ========================================

    @Test
    @DisplayName("Should handle invoice with no tax")
    void shouldHandleInvoiceWithNoTax() {
        // Given: Invoice without tax
        invoice = TestDataFactory.anInvoice()
            .withCustomer(testCustomer)
            .withLineItem("Service", 1, new BigDecimal("100.00"))
            .withTaxAmount(BigDecimal.ZERO)
            .build();

        // When: Calculating totals
        invoice.calculateTotals();

        // Then: Total equals subtotal
        assertThat(invoice.getSubtotal()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(invoice.getTaxAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(invoice.getTotalAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Should maintain invoice number uniqueness")
    void shouldMaintainInvoiceNumberUniqueness() {
        // Given: Two invoices
        Invoice invoice1 = TestDataFactory.anInvoice()
            .withCustomer(testCustomer)
            .withInvoiceNumber("INV-2025-0001")
            .withLineItem("Service", 1, new BigDecimal("100.00"))
            .build();

        Invoice invoice2 = TestDataFactory.anInvoice()
            .withCustomer(testCustomer)
            .withInvoiceNumber("INV-2025-0002")
            .withLineItem("Service", 1, new BigDecimal("100.00"))
            .build();

        // Then: Invoice numbers should be different
        assertThat(invoice1.getInvoiceNumber()).isNotEqualTo(invoice2.getInvoiceNumber());
    }
}

