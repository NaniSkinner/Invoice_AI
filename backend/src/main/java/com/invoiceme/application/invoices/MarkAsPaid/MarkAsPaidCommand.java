package com.invoiceme.application.invoices.MarkAsPaid;

import jakarta.validation.constraints.NotNull;

import java.util.Objects;
import java.util.UUID;

/**
 * Command to mark an invoice as paid.
 * This is a write operation in the CQRS pattern.
 * Transitions invoice from SENT to PAID status.
 */
public class MarkAsPaidCommand {

    
    private UUID invoiceId;

    // Constructors
    public MarkAsPaidCommand() {
    }

    public MarkAsPaidCommand(@NotNull UUID invoiceId) {
        this.invoiceId = invoiceId;
    }

    // Getters and Setters
    @NotNull
    public UUID getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(@NotNull UUID invoiceId) {
        this.invoiceId = invoiceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarkAsPaidCommand that = (MarkAsPaidCommand) o;
        return Objects.equals(invoiceId, that.invoiceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invoiceId);
    }

    @Override
    public String toString() {
        return "MarkAsPaidCommand{" +
               "invoiceId=" + invoiceId +
               '}';
    }
}
