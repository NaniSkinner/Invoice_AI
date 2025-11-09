package com.invoiceme.domain.invoice;

import com.invoiceme.domain.customer.Customer;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    private UUID id;

    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("lineOrder")
    private List<LineItem> lineItems = new ArrayList<>();

    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount")
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount")
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "amount_paid")
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "balance_remaining")
    private BigDecimal balanceRemaining = BigDecimal.ZERO;

    @Column(name = "allows_partial_payment")
    private boolean allowsPartialPayment = false;

    @Column(name = "payment_link", unique = true)
    private String paymentLink;

    private String notes;
    private String terms;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "reminders_suppressed")
    private boolean remindersSuppressed = false;

    @Column(name = "last_reminder_sent_at")
    private LocalDateTime lastReminderSentAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Version
    private Long version;

    // Constructors
    public Invoice() {
    }

    public Invoice(UUID id, String invoiceNumber, Customer customer, LocalDate issueDate,
                   LocalDate dueDate, InvoiceStatus status, List<LineItem> lineItems,
                   BigDecimal subtotal, BigDecimal taxAmount, BigDecimal totalAmount,
                   BigDecimal amountPaid, BigDecimal balanceRemaining, boolean allowsPartialPayment,
                   String paymentLink, String notes, String terms, String cancellationReason,
                   boolean remindersSuppressed, LocalDateTime lastReminderSentAt,
                   LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime sentAt,
                   LocalDateTime paidAt, LocalDateTime cancelledAt, Long version) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.customer = customer;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.status = status;
        this.lineItems = lineItems;
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
        this.version = version;
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

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
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

    public List<LineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<LineItem> lineItems) {
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    // Business Logic Methods

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void calculateTotals() {
        this.subtotal = lineItems.stream()
            .map(LineItem::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalAmount = subtotal.add(taxAmount != null ? taxAmount : BigDecimal.ZERO);
        this.balanceRemaining = totalAmount.subtract(amountPaid != null ? amountPaid : BigDecimal.ZERO);
    }

    public void send() {
        if (status != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Can only send invoices in DRAFT status");
        }
        if (lineItems.isEmpty()) {
            throw new IllegalStateException("Cannot send invoice without line items");
        }

        this.status = InvoiceStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.paymentLink = UUID.randomUUID().toString();
    }

    public void markAsPaid() {
        if (status != InvoiceStatus.SENT) {
            throw new IllegalStateException("Can only mark SENT invoices as paid");
        }

        this.status = InvoiceStatus.PAID;
        this.paidAt = LocalDateTime.now();
        this.balanceRemaining = BigDecimal.ZERO;
        this.amountPaid = totalAmount;
    }

    public void cancel(String reason) {
        if (status == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("Invoice is already cancelled");
        }

        this.status = InvoiceStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }

    public boolean isOverdue() {
        return status == InvoiceStatus.SENT && dueDate.isBefore(LocalDate.now());
    }

    public void addLineItem(LineItem item) {
        item.setInvoice(this);
        item.setLineOrder(lineItems.size());
        lineItems.add(item);
        calculateTotals();
    }

    // equals, hashCode, and toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invoice invoice = (Invoice) o;
        return allowsPartialPayment == invoice.allowsPartialPayment &&
               remindersSuppressed == invoice.remindersSuppressed &&
               Objects.equals(id, invoice.id) &&
               Objects.equals(invoiceNumber, invoice.invoiceNumber) &&
               Objects.equals(customer, invoice.customer) &&
               Objects.equals(issueDate, invoice.issueDate) &&
               Objects.equals(dueDate, invoice.dueDate) &&
               status == invoice.status &&
               Objects.equals(lineItems, invoice.lineItems) &&
               Objects.equals(subtotal, invoice.subtotal) &&
               Objects.equals(taxAmount, invoice.taxAmount) &&
               Objects.equals(totalAmount, invoice.totalAmount) &&
               Objects.equals(amountPaid, invoice.amountPaid) &&
               Objects.equals(balanceRemaining, invoice.balanceRemaining) &&
               Objects.equals(paymentLink, invoice.paymentLink) &&
               Objects.equals(notes, invoice.notes) &&
               Objects.equals(terms, invoice.terms) &&
               Objects.equals(cancellationReason, invoice.cancellationReason) &&
               Objects.equals(lastReminderSentAt, invoice.lastReminderSentAt) &&
               Objects.equals(createdAt, invoice.createdAt) &&
               Objects.equals(updatedAt, invoice.updatedAt) &&
               Objects.equals(sentAt, invoice.sentAt) &&
               Objects.equals(paidAt, invoice.paidAt) &&
               Objects.equals(cancelledAt, invoice.cancelledAt) &&
               Objects.equals(version, invoice.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, invoiceNumber, customer, issueDate, dueDate, status, lineItems,
                            subtotal, taxAmount, totalAmount, amountPaid, balanceRemaining,
                            allowsPartialPayment, paymentLink, notes, terms, cancellationReason,
                            remindersSuppressed, lastReminderSentAt, createdAt, updatedAt, sentAt,
                            paidAt, cancelledAt, version);
    }

    @Override
    public String toString() {
        return "Invoice{" +
               "id=" + id +
               ", invoiceNumber='" + invoiceNumber + '\'' +
               ", customer=" + customer +
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
               ", version=" + version +
               '}';
    }
}
