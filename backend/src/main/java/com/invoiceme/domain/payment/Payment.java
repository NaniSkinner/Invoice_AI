package com.invoiceme.domain.payment;

import com.invoiceme.domain.invoice.Invoice;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "payments")
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

    // Constructors
    public Payment() {
    }

    public Payment(UUID id, Invoice invoice, BigDecimal paymentAmount, LocalDate paymentDate,
                   PaymentMethod paymentMethod, String transactionReference, String notes,
                   LocalDateTime createdAt) {
        this.id = id;
        this.invoice = invoice;
        this.paymentAmount = paymentAmount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.transactionReference = transactionReference;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Business Logic Methods
    @PrePersist
    protected void onCreate() {
        // ID is set from client for idempotency, but generate if not provided
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
    }

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

    // equals, hashCode, and toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Objects.equals(id, payment.id) &&
               Objects.equals(invoice, payment.invoice) &&
               Objects.equals(paymentAmount, payment.paymentAmount) &&
               Objects.equals(paymentDate, payment.paymentDate) &&
               paymentMethod == payment.paymentMethod &&
               Objects.equals(transactionReference, payment.transactionReference) &&
               Objects.equals(notes, payment.notes) &&
               Objects.equals(createdAt, payment.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, invoice, paymentAmount, paymentDate, paymentMethod,
                            transactionReference, notes, createdAt);
    }

    @Override
    public String toString() {
        return "Payment{" +
               "id=" + id +
               ", invoice=" + invoice +
               ", paymentAmount=" + paymentAmount +
               ", paymentDate=" + paymentDate +
               ", paymentMethod=" + paymentMethod +
               ", transactionReference='" + transactionReference + '\'' +
               ", notes='" + notes + '\'' +
               ", createdAt=" + createdAt +
               '}';
    }
}
