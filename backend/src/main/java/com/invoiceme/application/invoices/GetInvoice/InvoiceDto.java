package com.invoiceme.application.invoices.GetInvoice;

import com.invoiceme.domain.invoice.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Data Transfer Object for Invoice responses.
 * Used in query operations to return invoice data.
 */
public class InvoiceDto {

    private UUID id;
    private String invoiceNumber;
    private UUID customerId;
    private String customerName;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private InvoiceStatus status;
    private List<LineItemDto> lineItems;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal balanceRemaining;
    private boolean allowsPartialPayment;
    private String paymentLink;
    private String notes;
    private String terms;
    private String cancellationReason;
    private boolean remindersSuppressed;
    private LocalDateTime lastReminderSentAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime sentAt;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;
    private boolean overdue;

    // Constructors
    public InvoiceDto() {
        this.lineItems = new ArrayList<>();
    }

    public InvoiceDto(UUID id, String invoiceNumber, UUID customerId, String customerName,
                      LocalDate issueDate, LocalDate dueDate, InvoiceStatus status,
                      List<LineItemDto> lineItems, BigDecimal subtotal, BigDecimal taxAmount,
                      BigDecimal totalAmount, BigDecimal amountPaid, BigDecimal balanceRemaining,
                      boolean allowsPartialPayment, String paymentLink, String notes, String terms,
                      String cancellationReason, boolean remindersSuppressed,
                      LocalDateTime lastReminderSentAt, LocalDateTime createdAt,
                      LocalDateTime updatedAt, LocalDateTime sentAt, LocalDateTime paidAt,
                      LocalDateTime cancelledAt, boolean overdue) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.status = status;
        this.lineItems = lineItems != null ? lineItems : new ArrayList<>();
        this.subtotal = subtotal;
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;
        this.amountPaid = amountPaid;
        this.balanceRemaining = balanceRemaining;
        this.allowsPartialPayment = allowsPartialPayment;
        this.paymentLink = paymentLink;
        this.notes = notes;
        this.terms = terms;
        this.cancellationReason = cancellationReason;
        this.remindersSuppressed = remindersSuppressed;
        this.lastReminderSentAt = lastReminderSentAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.sentAt = sentAt;
        this.paidAt = paidAt;
        this.cancelledAt = cancelledAt;
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

    public List<LineItemDto> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<LineItemDto> lineItems) {
        this.lineItems = lineItems;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public BigDecimal getBalanceRemaining() {
        return balanceRemaining;
    }

    public void setBalanceRemaining(BigDecimal balanceRemaining) {
        this.balanceRemaining = balanceRemaining;
    }

    public boolean isAllowsPartialPayment() {
        return allowsPartialPayment;
    }

    public void setAllowsPartialPayment(boolean allowsPartialPayment) {
        this.allowsPartialPayment = allowsPartialPayment;
    }

    public String getPaymentLink() {
        return paymentLink;
    }

    public void setPaymentLink(String paymentLink) {
        this.paymentLink = paymentLink;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getTerms() {
        return terms;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public boolean isRemindersSuppressed() {
        return remindersSuppressed;
    }

    public void setRemindersSuppressed(boolean remindersSuppressed) {
        this.remindersSuppressed = remindersSuppressed;
    }

    public LocalDateTime getLastReminderSentAt() {
        return lastReminderSentAt;
    }

    public void setLastReminderSentAt(LocalDateTime lastReminderSentAt) {
        this.lastReminderSentAt = lastReminderSentAt;
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

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
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
        InvoiceDto that = (InvoiceDto) o;
        return allowsPartialPayment == that.allowsPartialPayment &&
               remindersSuppressed == that.remindersSuppressed &&
               overdue == that.overdue &&
               Objects.equals(id, that.id) &&
               Objects.equals(invoiceNumber, that.invoiceNumber) &&
               Objects.equals(customerId, that.customerId) &&
               Objects.equals(customerName, that.customerName) &&
               Objects.equals(issueDate, that.issueDate) &&
               Objects.equals(dueDate, that.dueDate) &&
               status == that.status &&
               Objects.equals(lineItems, that.lineItems) &&
               Objects.equals(subtotal, that.subtotal) &&
               Objects.equals(taxAmount, that.taxAmount) &&
               Objects.equals(totalAmount, that.totalAmount) &&
               Objects.equals(amountPaid, that.amountPaid) &&
               Objects.equals(balanceRemaining, that.balanceRemaining) &&
               Objects.equals(paymentLink, that.paymentLink) &&
               Objects.equals(notes, that.notes) &&
               Objects.equals(terms, that.terms) &&
               Objects.equals(cancellationReason, that.cancellationReason) &&
               Objects.equals(lastReminderSentAt, that.lastReminderSentAt) &&
               Objects.equals(createdAt, that.createdAt) &&
               Objects.equals(updatedAt, that.updatedAt) &&
               Objects.equals(sentAt, that.sentAt) &&
               Objects.equals(paidAt, that.paidAt) &&
               Objects.equals(cancelledAt, that.cancelledAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, invoiceNumber, customerId, customerName, issueDate, dueDate,
                            status, lineItems, subtotal, taxAmount, totalAmount, amountPaid,
                            balanceRemaining, allowsPartialPayment, paymentLink, notes, terms,
                            cancellationReason, remindersSuppressed, lastReminderSentAt,
                            createdAt, updatedAt, sentAt, paidAt, cancelledAt, overdue);
    }

    @Override
    public String toString() {
        return "InvoiceDto{" +
               "id=" + id +
               ", invoiceNumber='" + invoiceNumber + '\'' +
               ", customerId=" + customerId +
               ", customerName='" + customerName + '\'' +
               ", issueDate=" + issueDate +
               ", dueDate=" + dueDate +
               ", status=" + status +
               ", lineItems=" + lineItems +
               ", subtotal=" + subtotal +
               ", taxAmount=" + taxAmount +
               ", totalAmount=" + totalAmount +
               ", amountPaid=" + amountPaid +
               ", balanceRemaining=" + balanceRemaining +
               ", allowsPartialPayment=" + allowsPartialPayment +
               ", paymentLink='" + paymentLink + '\'' +
               ", notes='" + notes + '\'' +
               ", terms='" + terms + '\'' +
               ", cancellationReason='" + cancellationReason + '\'' +
               ", remindersSuppressed=" + remindersSuppressed +
               ", lastReminderSentAt=" + lastReminderSentAt +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               ", sentAt=" + sentAt +
               ", paidAt=" + paidAt +
               ", cancelledAt=" + cancelledAt +
               ", overdue=" + overdue +
               '}';
    }

    /**
     * Nested DTO for line item information.
     */
    public static class LineItemDto {
        private UUID id;
        private String description;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;
        private int lineOrder;

        // Constructors
        public LineItemDto() {
        }

        public LineItemDto(UUID id, String description, BigDecimal quantity,
                           BigDecimal unitPrice, BigDecimal lineTotal, int lineOrder) {
            this.id = id;
            this.description = description;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.lineTotal = lineTotal;
            this.lineOrder = lineOrder;
        }

        // Getters and Setters
        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public void setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public BigDecimal getLineTotal() {
            return lineTotal;
        }

        public void setLineTotal(BigDecimal lineTotal) {
            this.lineTotal = lineTotal;
        }

        public int getLineOrder() {
            return lineOrder;
        }

        public void setLineOrder(int lineOrder) {
            this.lineOrder = lineOrder;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LineItemDto that = (LineItemDto) o;
            return lineOrder == that.lineOrder &&
                   Objects.equals(id, that.id) &&
                   Objects.equals(description, that.description) &&
                   Objects.equals(quantity, that.quantity) &&
                   Objects.equals(unitPrice, that.unitPrice) &&
                   Objects.equals(lineTotal, that.lineTotal);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, description, quantity, unitPrice, lineTotal, lineOrder);
        }

        @Override
        public String toString() {
            return "LineItemDto{" +
                   "id=" + id +
                   ", description='" + description + '\'' +
                   ", quantity=" + quantity +
                   ", unitPrice=" + unitPrice +
                   ", lineTotal=" + lineTotal +
                   ", lineOrder=" + lineOrder +
                   '}';
        }
    }
}
