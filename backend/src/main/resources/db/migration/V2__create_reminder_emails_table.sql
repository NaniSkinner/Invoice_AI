-- V2__create_reminder_emails_table.sql
-- Create reminder_emails table for tracking email reminders sent to customers

CREATE TABLE reminder_emails (
    id UUID PRIMARY KEY,
    invoice_id UUID NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    subject VARCHAR(500) NOT NULL,
    email_body TEXT NOT NULL,
    reminder_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    scheduled_for TIMESTAMP,
    sent_at TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_reminder_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE,
    CONSTRAINT chk_reminder_type CHECK (reminder_type IN ('BEFORE_DUE', 'ON_DUE_DATE', 'OVERDUE_7_DAYS', 'OVERDUE_14_DAYS', 'OVERDUE_30_DAYS')),
    CONSTRAINT chk_reminder_status CHECK (status IN ('PENDING', 'SENT', 'FAILED'))
);

-- Create indexes for performance
CREATE INDEX idx_reminder_invoice_id ON reminder_emails(invoice_id);
CREATE INDEX idx_reminder_status ON reminder_emails(status);
CREATE INDEX idx_reminder_scheduled_for ON reminder_emails(scheduled_for);
CREATE INDEX idx_reminder_sent_at ON reminder_emails(sent_at);

-- Composite index for finding pending reminders ready to send
CREATE INDEX idx_reminder_status_scheduled ON reminder_emails(status, scheduled_for);

-- Composite index for checking duplicate reminders
CREATE INDEX idx_reminder_invoice_type_status ON reminder_emails(invoice_id, reminder_type, status);
