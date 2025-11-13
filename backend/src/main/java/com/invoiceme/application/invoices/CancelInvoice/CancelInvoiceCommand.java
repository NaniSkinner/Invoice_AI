package com.invoiceme.application.invoices.CancelInvoice;

import jakarta.validation.constraints.NotNull;

import java.util.Objects;
import java.util.UUID;

/**
 * Command to cancel an invoice.
 * This is a write operation in the CQRS pattern.
 * Transitions invoice to CANCELLED status.
 */
public class CancelInvoiceCommand {
    @NotNull
    private UUID invoiceId;
    private String cancellationReason;

    // Constructors
    public CancelInvoiceCommand() {
    }

    public CancelInvoiceCommand(UUID invoiceId, String cancellationReason) {
        this.invoiceId = invoiceId;
        this.cancellationReason = cancellationReason;
    }

    // Getters and Setters
    @NotNull
    public UUID getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(@NotNull UUID invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CancelInvoiceCommand that = (CancelInvoiceCommand) o;
        return Objects.equals(invoiceId, that.invoiceId) &&
               Objects.equals(cancellationReason, that.cancellationReason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invoiceId, cancellationReason);
    }

    @Override
    public String toString() {
        return "CancelInvoiceCommand{" +
               "invoiceId=" + invoiceId +
               ", cancellationReason='" + cancellationReason + '\'' +
               '}';
    }
}
