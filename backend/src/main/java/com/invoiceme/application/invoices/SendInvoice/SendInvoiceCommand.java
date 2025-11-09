package com.invoiceme.application.invoices.SendInvoice;

import java.util.Objects;
import java.util.UUID;

/**
 * Command to send an invoice.
 * This is a write operation in the CQRS pattern.
 * Transitions invoice from DRAFT to SENT status.
 */
public class SendInvoiceCommand {

    private UUID invoiceId;

    // Constructors
    public SendInvoiceCommand() {
    }

    public SendInvoiceCommand(UUID invoiceId) {
        this.invoiceId = invoiceId;
    }

    // Getters and Setters
    public UUID getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(UUID invoiceId) {
        this.invoiceId = invoiceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SendInvoiceCommand that = (SendInvoiceCommand) o;
        return Objects.equals(invoiceId, that.invoiceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invoiceId);
    }

    @Override
    public String toString() {
        return "SendInvoiceCommand{" +
               "invoiceId=" + invoiceId +
               '}';
    }
}
