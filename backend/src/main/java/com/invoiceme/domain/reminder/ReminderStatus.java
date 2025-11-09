package com.invoiceme.domain.reminder;

/**
 * Represents the status of a reminder email.
 */
public enum ReminderStatus {
    /**
     * Reminder has been scheduled but not yet sent.
     */
    PENDING,

    /**
     * Reminder has been successfully sent.
     */
    SENT,

    /**
     * Reminder failed to send (e.g., due to email service error).
     */
    FAILED
}
