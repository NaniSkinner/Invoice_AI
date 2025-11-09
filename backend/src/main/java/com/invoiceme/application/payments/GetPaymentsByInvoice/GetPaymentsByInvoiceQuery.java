package com.invoiceme.application.payments.GetPaymentsByInvoice;

import java.util.Objects;
import java.util.UUID;

/**
 * Query to retrieve all payments for a specific invoice.
 */
public class GetPaymentsByInvoiceQuery {

    private UUID invoiceId;

    // Constructors
    public GetPaymentsByInvoiceQuery() {
    }

    public GetPaymentsByInvoiceQuery(UUID invoiceId) {
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
        GetPaymentsByInvoiceQuery that = (GetPaymentsByInvoiceQuery) o;
        return Objects.equals(invoiceId, that.invoiceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invoiceId);
    }

    @Override
    public String toString() {
        return "GetPaymentsByInvoiceQuery{" +
               "invoiceId=" + invoiceId +
               '}';
    }
}
