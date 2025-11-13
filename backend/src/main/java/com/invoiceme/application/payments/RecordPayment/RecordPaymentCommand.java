package com.invoiceme.application.payments.RecordPayment;

import jakarta.validation.constraints.NotNull;

import com.invoiceme.domain.payment.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Command to record a payment against an invoice.
 * The payment ID is client-provided for idempotency.
 */
public class RecordPaymentCommand {

    
    private UUID id;
    
    private UUID invoiceId;
    private BigDecimal paymentAmount;
    private LocalDate paymentDate;
    private PaymentMethod paymentMethod;
    private String transactionReference;
    private String notes;

    // Constructors
    public RecordPaymentCommand() {
    }

    public RecordPaymentCommand(UUID id, UUID invoiceId, BigDecimal paymentAmount,
                                LocalDate paymentDate, PaymentMethod paymentMethod,
                                String transactionReference, String notes) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.paymentAmount = paymentAmount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.transactionReference = transactionReference;
        this.notes = notes;
    }

    // Getters and Setters
    @NotNull
    public UUID getId() {
        return id;
    }

    public void setId(@NotNull UUID id) {
        this.id = id;
    }

    @NotNull
    public UUID getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(@NotNull UUID invoiceId) {
        this.invoiceId = invoiceId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecordPaymentCommand that = (RecordPaymentCommand) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(invoiceId, that.invoiceId) &&
               Objects.equals(paymentAmount, that.paymentAmount) &&
               Objects.equals(paymentDate, that.paymentDate) &&
               paymentMethod == that.paymentMethod &&
               Objects.equals(transactionReference, that.transactionReference) &&
               Objects.equals(notes, that.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, invoiceId, paymentAmount, paymentDate, paymentMethod,
                            transactionReference, notes);
    }

    @Override
    public String toString() {
        return "RecordPaymentCommand{" +
               "id=" + id +
               ", invoiceId=" + invoiceId +
               ", paymentAmount=" + paymentAmount +
               ", paymentDate=" + paymentDate +
               ", paymentMethod=" + paymentMethod +
               ", transactionReference='" + transactionReference + '\'' +
               ", notes='" + notes + '\'' +
               '}';
    }
}
