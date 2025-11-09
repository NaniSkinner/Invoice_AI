package com.invoiceme.domain.reminder;

import com.invoiceme.domain.invoice.Invoice;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a reminder email sent for an invoice.
 * Tracks email content, delivery status, and scheduling information.
 */
@Entity
@Table(name = "reminder_emails")
public class ReminderEmail {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Column(nullable = false)
    private String subject;

    @Column(name = "email_body", nullable = false, columnDefinition = "TEXT")
    private String emailBody;

    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_type", nullable = false)
    private ReminderType reminderType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReminderStatus status = ReminderStatus.PENDING;

    @Column(name = "scheduled_for", nullable = false)
    private LocalDateTime scheduledFor;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public ReminderEmail() {
    }

    public ReminderEmail(UUID id, Invoice invoice, String recipientEmail, String subject,
                        String emailBody, ReminderType reminderType, ReminderStatus status,
                        LocalDateTime scheduledFor, LocalDateTime sentAt, String errorMessage,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.invoice = invoice;
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

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
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

    // Business Logic Methods

    /**
     * Lifecycle callback that sets ID and timestamps before persisting.
     */
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Lifecycle callback that updates the updatedAt timestamp before updating.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Marks this reminder email as successfully sent.
     *
     * @param sentAt the timestamp when the email was sent
     */
    public void markAsSent(LocalDateTime sentAt) {
        this.status = ReminderStatus.SENT;
        this.sentAt = sentAt;
        this.errorMessage = null;
    }

    /**
     * Marks this reminder email as failed with an error message.
     *
     * @param errorMessage the error message describing why the send failed
     */
    public void markAsFailed(String errorMessage) {
        this.status = ReminderStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    // equals, hashCode, and toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReminderEmail that = (ReminderEmail) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(invoice, that.invoice) &&
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
        return Objects.hash(id, invoice, recipientEmail, subject, emailBody,
                           reminderType, status, scheduledFor, sentAt, errorMessage,
                           createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "ReminderEmail{" +
               "id=" + id +
               ", invoice=" + invoice +
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
