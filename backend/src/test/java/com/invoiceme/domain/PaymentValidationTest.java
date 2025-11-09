package com.invoiceme.domain;

import com.invoiceme.TestDataFactory;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.payment.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for Payment domain entity validation logic.
 * 
 * Tests business rules:
 * 1. Payment amount must be greater than zero
 * 2. Payment cannot exceed invoice balance
 * 3. Partial payments require explicit permission
 * 4. Payment validation enforces business constraints
 */
@DisplayName("Payment Validation Tests")
class PaymentValidationTest {

    private Customer testCustomer;
    private Invoice testInvoice;

    @BeforeEach
    void setUp() {
        testCustomer = TestDataFactory.aCustomer().build();
        testInvoice = TestDataFactory.anInvoice()
            .withCustomer(testCustomer)
            .withLineItem("Test Service", 1, new BigDecimal("1000.00"))
            .build();
        testInvoice.send();
    }

    @Test
    @DisplayName("Should validate payment with valid amount")
    void shouldValidatePaymentWithValidAmount() {
        // Given: Payment with valid amount
        Payment payment = TestDataFactory.aPayment()
            .withInvoice(testInvoice)
            .withAmount(new BigDecimal("1000.00"))
            .build();

        // When/Then: Validation should pass
        payment.validate(); // Should not throw exception
        assertThat(payment.getPaymentAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    @DisplayName("Should reject payment with zero amount")
    void shouldRejectPaymentWithZeroAmount() {
        // Given: Payment with zero amount
        Payment payment = TestDataFactory.aPayment()
            .withInvoice(testInvoice)
            .withAmount(BigDecimal.ZERO)
            .build();

        // When/Then: Validation should fail
        assertThatThrownBy(() -> payment.validate())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Payment amount must be greater than zero");
    }

    @Test
    @DisplayName("Should reject payment with negative amount")
    void shouldRejectPaymentWithNegativeAmount() {
        // Given: Payment with negative amount
        Payment payment = TestDataFactory.aPayment()
            .withInvoice(testInvoice)
            .withAmount(new BigDecimal("-100.00"))
            .build();

        // When/Then: Validation should fail
        assertThatThrownBy(() -> payment.validate())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Payment amount must be greater than zero");
    }

    @Test
    @DisplayName("Should reject payment exceeding invoice balance")
    void shouldRejectPaymentExceedingBalance() {
        // Given: Payment exceeding invoice balance (overpayment)
        Payment payment = TestDataFactory.aPayment()
            .withInvoice(testInvoice)
            .withAmount(new BigDecimal("1500.00")) // Invoice total is 1000
            .build();

        // When/Then: Validation should fail
        assertThatThrownBy(() -> payment.validate())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("exceeds invoice balance");
    }

    @Test
    @DisplayName("Should accept full payment equal to balance")
    void shouldAcceptFullPayment() {
        // Given: Payment equal to invoice total
        Payment payment = TestDataFactory.aPayment()
            .withInvoice(testInvoice)
            .withAmount(new BigDecimal("1000.00"))
            .build();

        // When/Then: Validation should pass
        payment.validate(); // Should not throw
        assertThat(payment.getPaymentAmount()).isEqualByComparingTo(testInvoice.getBalanceRemaining());
    }

    @Test
    @DisplayName("Should reject partial payment when not allowed")
    void shouldRejectPartialPaymentWhenNotAllowed() {
        // Given: Invoice that does NOT allow partial payments
        testInvoice.setAllowsPartialPayment(false);
        
        Payment payment = TestDataFactory.aPayment()
            .withInvoice(testInvoice)
            .withAmount(new BigDecimal("500.00")) // Partial payment
            .build();

        // When/Then: Validation should fail
        assertThatThrownBy(() -> payment.validate())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Partial payments are not allowed");
    }

    @Test
    @DisplayName("Should accept partial payment when explicitly allowed")
    void shouldAcceptPartialPaymentWhenAllowed() {
        // Given: Invoice that ALLOWS partial payments
        testInvoice.setAllowsPartialPayment(true);
        
        Payment payment = TestDataFactory.aPayment()
            .withInvoice(testInvoice)
            .withAmount(new BigDecimal("500.00"))
            .build();

        // When/Then: Validation should pass
        payment.validate(); // Should not throw
        assertThat(payment.getPaymentAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    @DisplayName("Should validate payment after invoice has existing payments")
    void shouldValidatePaymentWithExistingBalance() {
        // Given: Invoice with partial payment already made
        testInvoice.setAllowsPartialPayment(true);
        testInvoice.setAmountPaid(new BigDecimal("600.00"));
        testInvoice.calculateTotals(); // Balance now 400

        Payment secondPayment = TestDataFactory.aPayment()
            .withInvoice(testInvoice)
            .withAmount(new BigDecimal("400.00"))
            .build();

        // When/Then: Validation should pass
        secondPayment.validate(); // Should not throw
        assertThat(secondPayment.getPaymentAmount())
            .isEqualByComparingTo(testInvoice.getBalanceRemaining());
    }

    @Test
    @DisplayName("Should reject payment exceeding remaining balance")
    void shouldRejectPaymentExceedingRemainingBalance() {
        // Given: Invoice with partial payment already made
        testInvoice.setAllowsPartialPayment(true);
        testInvoice.setAmountPaid(new BigDecimal("600.00"));
        testInvoice.calculateTotals(); // Balance now 400

        Payment secondPayment = TestDataFactory.aPayment()
            .withInvoice(testInvoice)
            .withAmount(new BigDecimal("500.00")) // Exceeds remaining 400
            .build();

        // When/Then: Validation should fail
        assertThatThrownBy(() -> secondPayment.validate())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("exceeds invoice balance");
    }

    @Test
    @DisplayName("Should validate payment with exact remaining balance")
    void shouldValidatePaymentWithExactRemainingBalance() {
        // Given: Invoice with partial payment already made
        testInvoice.setAllowsPartialPayment(true);
        testInvoice.setAmountPaid(new BigDecimal("700.00"));
        testInvoice.calculateTotals(); // Balance now 300

        Payment finalPayment = TestDataFactory.aPayment()
            .withInvoice(testInvoice)
            .withAmount(new BigDecimal("300.00"))
            .build();

        // When/Then: Validation should pass (final payment)
        finalPayment.validate(); // Should not throw
        assertThat(finalPayment.getPaymentAmount())
            .isEqualByComparingTo(testInvoice.getBalanceRemaining());
    }

    @Test
    @DisplayName("Should handle very small payment amounts")
    void shouldHandleVerySmallAmounts() {
        // Given: Payment with very small amount (1 cent)
        testInvoice.setAllowsPartialPayment(true);
        
        Payment payment = TestDataFactory.aPayment()
            .withInvoice(testInvoice)
            .withAmount(new BigDecimal("0.01"))
            .build();

        // When/Then: Validation should pass
        payment.validate(); // Should not throw
        assertThat(payment.getPaymentAmount()).isEqualByComparingTo(new BigDecimal("0.01"));
    }

    @Test
    @DisplayName("Should handle large payment amounts")
    void shouldHandleLargeAmounts() {
        // Given: Invoice with large amount
        Invoice largeInvoice = TestDataFactory.anInvoice()
            .withCustomer(testCustomer)
            .withLineItem("Large Project", 1, new BigDecimal("999999.99"))
            .build();
        largeInvoice.send();

        Payment largePayment = TestDataFactory.aPayment()
            .withInvoice(largeInvoice)
            .withAmount(new BigDecimal("999999.99"))
            .build();

        // When/Then: Validation should pass
        largePayment.validate(); // Should not throw
        assertThat(largePayment.getPaymentAmount()).isEqualByComparingTo(new BigDecimal("999999.99"));
    }

    @Test
    @DisplayName("Should maintain precision with decimal amounts")
    void shouldMaintainPrecisionWithDecimals() {
        // Given: Invoice and payment with precise decimal amounts
        Invoice preciseInvoice = TestDataFactory.anInvoice()
            .withCustomer(testCustomer)
            .withLineItem("Service", 1, new BigDecimal("123.45"))
            .withTaxAmount(new BigDecimal("6.17"))
            .build();
        preciseInvoice.send();
        // Total: 123.45 + 6.17 = 129.62

        Payment precisePayment = TestDataFactory.aPayment()
            .withInvoice(preciseInvoice)
            .withAmount(new BigDecimal("129.62"))
            .build();

        // When/Then: Validation should pass with exact precision
        precisePayment.validate(); // Should not throw
        assertThat(precisePayment.getPaymentAmount())
            .isEqualByComparingTo(new BigDecimal("129.62"));
        assertThat(precisePayment.getPaymentAmount())
            .isEqualByComparingTo(preciseInvoice.getTotalAmount());
    }
}

