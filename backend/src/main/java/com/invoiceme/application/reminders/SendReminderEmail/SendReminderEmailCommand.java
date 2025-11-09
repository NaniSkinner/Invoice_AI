package com.invoiceme.application.reminders.SendReminderEmail;

import com.invoiceme.domain.reminder.ReminderType;

import java.util.Objects;
import java.util.UUID;

/**
 * Command to send a reminder email for an invoice.
 * Contains the invoice ID, reminder type, and optional recipient email override.
 */
public class SendReminderEmailCommand {

    private UUID invoiceId;
    private ReminderType reminderType;
    private String recipientEmail; // Optional: override invoice customer email

    // Constructors
    public SendReminderEmailCommand() {
    }

    public SendReminderEmailCommand(UUID invoiceId, ReminderType reminderType, String recipientEmail) {
        this.invoiceId = invoiceId;
        this.reminderType = reminderType;
        this.recipientEmail = recipientEmail;
    }

    // Getters and Setters
    public UUID getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(UUID invoiceId) {
        this.invoiceId = invoiceId;
    }

    public ReminderType getReminderType() {
        return reminderType;
    }

    public void setReminderType(ReminderType reminderType) {
        this.reminderType = reminderType;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    // equals, hashCode, and toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SendReminderEmailCommand that = (SendReminderEmailCommand) o;
        return Objects.equals(invoiceId, that.invoiceId) &&
               reminderType == that.reminderType &&
               Objects.equals(recipientEmail, that.recipientEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invoiceId, reminderType, recipientEmail);
    }

    @Override
    public String toString() {
        return "SendReminderEmailCommand{" +
               "invoiceId=" + invoiceId +
               ", reminderType=" + reminderType +
               ", recipientEmail='" + recipientEmail + '\'' +
               '}';
    }
}
