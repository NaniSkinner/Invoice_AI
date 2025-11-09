package com.invoiceme.application.invoices.GetInvoice;

import java.util.Objects;
import java.util.UUID;

/**
 * Query to retrieve a single invoice by ID.
 * This is a read operation in the CQRS pattern.
 */
public class GetInvoiceQuery {

    private UUID invoiceId;

    // Constructors
    public GetInvoiceQuery() {
    }

    public GetInvoiceQuery(UUID invoiceId) {
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
        GetInvoiceQuery that = (GetInvoiceQuery) o;
        return Objects.equals(invoiceId, that.invoiceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invoiceId);
    }

    @Override
    public String toString() {
        return "GetInvoiceQuery{" +
               "invoiceId=" + invoiceId +
               '}';
    }
}
