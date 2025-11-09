package com.invoiceme.application.reminders.PreviewReminder;

import java.util.Objects;

/**
 * Data Transfer Object for previewing a reminder email.
 * Contains the subject, body, and recipient email that would be sent.
 */
public class PreviewReminderDto {

    private String subject;
    private String emailBody;
    private String recipientEmail;

    // Constructors
    public PreviewReminderDto() {
    }

    public PreviewReminderDto(String subject, String emailBody, String recipientEmail) {
        this.subject = subject;
        this.emailBody = emailBody;
        this.recipientEmail = recipientEmail;
    }

    // Getters and Setters
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
        PreviewReminderDto that = (PreviewReminderDto) o;
        return Objects.equals(subject, that.subject) &&
               Objects.equals(emailBody, that.emailBody) &&
               Objects.equals(recipientEmail, that.recipientEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, emailBody, recipientEmail);
    }

    @Override
    public String toString() {
        return "PreviewReminderDto{" +
               "subject='" + subject + '\'' +
               ", emailBody='" + emailBody + '\'' +
               ", recipientEmail='" + recipientEmail + '\'' +
               '}';
    }
}
