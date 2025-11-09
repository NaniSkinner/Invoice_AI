package com.invoiceme.application.reminders.GetReminderHistory;

import com.invoiceme.domain.reminder.ReminderStatus;
import com.invoiceme.domain.reminder.ReminderType;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Data Transfer Object for ReminderEmail.
 * Contains all fields from the ReminderEmail entity for display purposes.
 */
public class ReminderEmailDto {

    private UUID id;
    private UUID invoiceId;
    private String recipientEmail;
    private String subject;
    private String emailBody;
    private ReminderType reminderType;
    private ReminderStatus status;
    private LocalDateTime scheduledFor;
    private LocalDateTime sentAt;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public ReminderEmailDto() {
    }

    public ReminderEmailDto(UUID id, UUID invoiceId, String recipientEmail, String subject,
                           String emailBody, ReminderType reminderType, ReminderStatus status,
                           LocalDateTime scheduledFor, LocalDateTime sentAt, String errorMessage,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.emailBody = emailBody;
        this.reminderType = reminderType;
        this.status = status;
        this.scheduledFor = scheduledFor;
        this.sentAt = sentAt;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(UUID invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getEmailBody() {
        return emailBody;
    }

    public void setEmailBody(String emailBody) {
        this.emailBody = emailBody;
    }

    public ReminderType getReminderType() {
        return reminderType;
    }

    public void setReminderType(ReminderType reminderType) {
        this.reminderType = reminderType;
    }

    public ReminderStatus getStatus() {
        return status;
    }

    public void setStatus(ReminderStatus status) {
        this.status = status;
    }

    public LocalDateTime getScheduledFor() {
        return scheduledFor;
    }

    public void setScheduledFor(LocalDateTime scheduledFor) {
        this.scheduledFor = scheduledFor;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // equals, hashCode, and toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReminderEmailDto that = (ReminderEmailDto) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(invoiceId, that.invoiceId) &&
               Objects.equals(recipientEmail, that.recipientEmail) &&
               Objects.equals(subject, that.subject) &&
               Objects.equals(emailBody, that.emailBody) &&
               reminderType == that.reminderType &&
               status == that.status &&
               Objects.equals(scheduledFor, that.scheduledFor) &&
               Objects.equals(sentAt, that.sentAt) &&
               Objects.equals(errorMessage, that.errorMessage) &&
               Objects.equals(createdAt, that.createdAt) &&
               Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, invoiceId, recipientEmail, subject, emailBody,
                           reminderType, status, scheduledFor, sentAt, errorMessage,
                           createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "ReminderEmailDto{" +
               "id=" + id +
               ", invoiceId=" + invoiceId +
               ", recipientEmail='" + recipientEmail + '\'' +
               ", subject='" + subject + '\'' +
               ", emailBody='" + emailBody + '\'' +
               ", reminderType=" + reminderType +
               ", status=" + status +
               ", scheduledFor=" + scheduledFor +
               ", sentAt=" + sentAt +
               ", errorMessage='" + errorMessage + '\'' +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }
}
