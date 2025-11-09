package com.invoiceme.application.invoices.ListInvoices;

import com.invoiceme.domain.invoice.InvoiceStatus;

import java.util.Objects;

/**
 * Query to retrieve a list of invoices.
 * This is a read operation in the CQRS pattern.
 * Optionally filtered by status.
 */
public class ListInvoicesQuery {

    private InvoiceStatus status;

    // Constructors
    public ListInvoicesQuery() {
    }

    public ListInvoicesQuery(InvoiceStatus status) {
        this.status = status;
    }

    // Getters and Setters
    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListInvoicesQuery that = (ListInvoicesQuery) o;
        return status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status);
    }

    @Override
    public String toString() {
        return "ListInvoicesQuery{" +
               "status=" + status +
               '}';
    }
}
