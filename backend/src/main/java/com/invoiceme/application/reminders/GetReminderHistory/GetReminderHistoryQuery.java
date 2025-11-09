package com.invoiceme.application.reminders.GetReminderHistory;

import java.util.Objects;
import java.util.UUID;

/**
 * Query to retrieve the reminder history for a specific invoice.
 */
public class GetReminderHistoryQuery {

    private UUID invoiceId;

    // Constructors
    public GetReminderHistoryQuery() {
    }

    public GetReminderHistoryQuery(UUID invoiceId) {
        this.invoiceId = invoiceId;
    }

    // Getters and Setters
    public UUID getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(UUID invoiceId) {
        this.invoiceId = invoiceId;
    }

    // equals, hashCode, and toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetReminderHistoryQuery that = (GetReminderHistoryQuery) o;
        return Objects.equals(invoiceId, that.invoiceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invoiceId);
    }

    @Override
    public String toString() {
        return "GetReminderHistoryQuery{" +
               "invoiceId=" + invoiceId +
               '}';
    }
}
