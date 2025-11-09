package com.invoiceme.application.invoices.ListInvoices;

import com.invoiceme.domain.invoice.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Summary Data Transfer Object for Invoice list responses.
 * Contains essential invoice information for list views.
 */
public class InvoiceSummaryDto {

    private UUID id;
    private String invoiceNumber;
    private UUID customerId;
    private String customerName;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private InvoiceStatus status;
    private BigDecimal totalAmount;
    private BigDecimal balanceRemaining;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private boolean overdue;

    // Constructors
    public InvoiceSummaryDto() {
    }

    public InvoiceSummaryDto(UUID id, String invoiceNumber, UUID customerId, String customerName,
                             LocalDate issueDate, LocalDate dueDate, InvoiceStatus status,
                             BigDecimal totalAmount, BigDecimal balanceRemaining,
                             LocalDateTime createdAt, LocalDateTime sentAt, boolean overdue) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.balanceRemaining = balanceRemaining;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
        this.overdue = overdue;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getBalanceRemaining() {
        return balanceRemaining;
    }

    public void setBalanceRemaining(BigDecimal balanceRemaining) {
        this.balanceRemaining = balanceRemaining;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public boolean isOverdue() {
        return overdue;
    }

    public void setOverdue(boolean overdue) {
        this.overdue = overdue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvoiceSummaryDto that = (InvoiceSummaryDto) o;
        return overdue == that.overdue &&
               Objects.equals(id, that.id) &&
               Objects.equals(invoiceNumber, that.invoiceNumber) &&
               Objects.equals(customerId, that.customerId) &&
               Objects.equals(customerName, that.customerName) &&
               Objects.equals(issueDate, that.issueDate) &&
               Objects.equals(dueDate, that.dueDate) &&
               status == that.status &&
               Objects.equals(totalAmount, that.totalAmount) &&
               Objects.equals(balanceRemaining, that.balanceRemaining) &&
               Objects.equals(createdAt, that.createdAt) &&
               Objects.equals(sentAt, that.sentAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, invoiceNumber, customerId, customerName, issueDate, dueDate,
                            status, totalAmount, balanceRemaining, createdAt, sentAt, overdue);
    }

    @Override
    public String toString() {
        return "InvoiceSummaryDto{" +
               "id=" + id +
               ", invoiceNumber='" + invoiceNumber + '\'' +
               ", customerId=" + customerId +
               ", customerName='" + customerName + '\'' +
               ", issueDate=" + issueDate +
               ", dueDate=" + dueDate +
               ", status=" + status +
               ", totalAmount=" + totalAmount +
               ", balanceRemaining=" + balanceRemaining +
               ", createdAt=" + createdAt +
               ", sentAt=" + sentAt +
               ", overdue=" + overdue +
               '}';
    }
}
