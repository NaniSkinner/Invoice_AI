package com.invoiceme.domain.reminder;

/**
 * Represents the type of reminder email to send.
 * Each type corresponds to a specific timing relative to the invoice due date.
 */
public enum ReminderType {
    /**
     * Reminder sent before the due date (e.g., 3 days before).
     */
    BEFORE_DUE,

    /**
     * Reminder sent on the due date itself.
     */
    ON_DUE_DATE,

    /**
     * Reminder sent 7 days after the due date has passed.
     */
    OVERDUE_7_DAYS,

    /**
     * Reminder sent 14 days after the due date has passed.
     */
    OVERDUE_14_DAYS,

    /**
     * Reminder sent 30 days after the due date has passed.
     */
    OVERDUE_30_DAYS
}
