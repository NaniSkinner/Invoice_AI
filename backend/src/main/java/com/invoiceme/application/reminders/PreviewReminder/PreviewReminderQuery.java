package com.invoiceme.application.reminders.PreviewReminder;

import jakarta.validation.constraints.NotNull;

import com.invoiceme.domain.reminder.ReminderType;

import java.util.Objects;
import java.util.UUID;

/**
 * Query to preview a reminder email without sending it.
 * Allows users to see what the email will look like before sending.
 */
public class PreviewReminderQuery {

    
    private UUID invoiceId;
    private ReminderType reminderType;

    // Constructors
    public PreviewReminderQuery() {
    }

    public PreviewReminderQuery(UUID invoiceId, ReminderType reminderType) {
        this.invoiceId = invoiceId;
        this.reminderType = reminderType;
    }

    // Getters and Setters
    @NotNull
    public UUID getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(@NotNull UUID invoiceId) {
        this.invoiceId = invoiceId;
    }

    public ReminderType getReminderType() {
        return reminderType;
    }

    public void setReminderType(ReminderType reminderType) {
        this.reminderType = reminderType;
    }

    // equals, hashCode, and toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PreviewReminderQuery that = (PreviewReminderQuery) o;
        return Objects.equals(invoiceId, that.invoiceId) &&
               reminderType == that.reminderType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(invoiceId, reminderType);
    }

    @Override
    public String toString() {
        return "PreviewReminderQuery{" +
               "invoiceId=" + invoiceId +
               ", reminderType=" + reminderType +
               '}';
    }
}
