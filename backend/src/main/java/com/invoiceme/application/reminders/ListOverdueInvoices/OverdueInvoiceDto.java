package com.invoiceme.application.reminders.ListOverdueInvoices;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Data Transfer Object for overdue invoice summary.
 * Contains key information about an overdue invoice for reminder purposes.
 */
public class OverdueInvoiceDto {

    private UUID invoiceId;
    private String invoiceNumber;
    private String customerName;
    private LocalDate dueDate;
    private int daysOverdue;
    private BigDecimal balanceRemaining;
    private LocalDateTime lastReminderSent;

    // Constructors
    public OverdueInvoiceDto() {
    }

    public OverdueInvoiceDto(UUID invoiceId, String invoiceNumber, String customerName,
                            LocalDate dueDate, int daysOverdue, BigDecimal balanceRemaining,
                            LocalDateTime lastReminderSent) {
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.customerName = customerName;
        this.dueDate = dueDate;
        this.daysOverdue = daysOverdue;
        this.balanceRemaining = balanceRemaining;
        this.lastReminderSent = lastReminderSent;
    }

    // Getters and Setters
    public UUID getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(UUID invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public int getDaysOverdue() {
        return daysOverdue;
    }

    public void setDaysOverdue(int daysOverdue) {
        this.daysOverdue = daysOverdue;
    }

    public BigDecimal getBalanceRemaining() {
        return balanceRemaining;
    }

    public void setBalanceRemaining(BigDecimal balanceRemaining) {
        this.balanceRemaining = balanceRemaining;
    }

    public LocalDateTime getLastReminderSent() {
        return lastReminderSent;
    }

    public void setLastReminderSent(LocalDateTime lastReminderSent) {
        this.lastReminderSent = lastReminderSent;
    }

    // equals, hashCode, and toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OverdueInvoiceDto that = (OverdueInvoiceDto) o;
        return daysOverdue == that.daysOverdue &&
               Objects.equals(invoiceId, that.invoiceId) &&
               Objects.equals(invoiceNumber, that.invoiceNumber) &&
               Objects.equals(customerName, that.customerName) &&
               Objects.equals(dueDate, that.dueDate) &&
               Objects.equals(balanceRemaining, that.balanceRemaining) &&
               Objects.equals(lastReminderSent, that.lastReminderSent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invoiceId, invoiceNumber, customerName, dueDate,
                           daysOverdue, balanceRemaining, lastReminderSent);
    }

    @Override
    public String toString() {
        return "OverdueInvoiceDto{" +
               "invoiceId=" + invoiceId +
               ", invoiceNumber='" + invoiceNumber + '\'' +
               ", customerName='" + customerName + '\'' +
               ", dueDate=" + dueDate +
               ", daysOverdue=" + daysOverdue +
               ", balanceRemaining=" + balanceRemaining +
               ", lastReminderSent=" + lastReminderSent +
               '}';
    }
}
