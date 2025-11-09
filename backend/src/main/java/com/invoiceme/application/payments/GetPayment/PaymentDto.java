package com.invoiceme.application.payments.GetPayment;

import com.invoiceme.domain.payment.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Data Transfer Object for Payment responses.
 * Used in query operations to return payment data.
 */
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

    // Constructors
    public PaymentDto() {
    }

    public PaymentDto(UUID id, UUID invoiceId, String invoiceNumber,
                      BigDecimal paymentAmount, LocalDate paymentDate,
                      PaymentMethod paymentMethod, String transactionReference,
                      String notes, LocalDateTime createdAt) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
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

    public UUID getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(UUID invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentDto that = (PaymentDto) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(invoiceId, that.invoiceId) &&
               Objects.equals(invoiceNumber, that.invoiceNumber) &&
               Objects.equals(paymentAmount, that.paymentAmount) &&
               Objects.equals(paymentDate, that.paymentDate) &&
               paymentMethod == that.paymentMethod &&
               Objects.equals(transactionReference, that.transactionReference) &&
               Objects.equals(notes, that.notes) &&
               Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, invoiceId, invoiceNumber, paymentAmount, paymentDate,
                            paymentMethod, transactionReference, notes, createdAt);
    }

    @Override
    public String toString() {
        return "PaymentDto{" +
               "id=" + id +
               ", invoiceId=" + invoiceId +
               ", invoiceNumber='" + invoiceNumber + '\'' +
               ", paymentAmount=" + paymentAmount +
               ", paymentDate=" + paymentDate +
               ", paymentMethod=" + paymentMethod +
               ", transactionReference='" + transactionReference + '\'' +
               ", notes='" + notes + '\'' +
               ", createdAt=" + createdAt +
               '}';
    }
}
