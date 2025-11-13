package com.invoiceme.application.payments.ListPayments;

import jakarta.validation.constraints.NotNull;

import java.util.Objects;
import java.util.UUID;

/**
 * Query to retrieve a list of payments.
 * Optionally filtered by invoice ID.
 */
public class ListPaymentsQuery {

    
    private UUID invoiceId;

    // Constructors
    public ListPaymentsQuery() {
    }

    public ListPaymentsQuery(@NotNull UUID invoiceId) {
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
        ListPaymentsQuery that = (ListPaymentsQuery) o;
        return Objects.equals(invoiceId, that.invoiceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invoiceId);
    }

    @Override
    public String toString() {
        return "ListPaymentsQuery{" +
               "invoiceId=" + invoiceId +
               '}';
    }
}
