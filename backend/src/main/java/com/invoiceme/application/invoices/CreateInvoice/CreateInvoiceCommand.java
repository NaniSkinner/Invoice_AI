package com.invoiceme.application.invoices.CreateInvoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Command to create a new invoice.
 * This is a write operation in the CQRS pattern.
 */
public class CreateInvoiceCommand {

    private UUID customerId;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private BigDecimal taxAmount;
    private String notes;
    private List<LineItemDto> lineItems;

    // Constructors
    public CreateInvoiceCommand() {
        this.lineItems = new ArrayList<>();
    }

    public CreateInvoiceCommand(UUID customerId, LocalDate issueDate, LocalDate dueDate,
                                BigDecimal taxAmount, String notes, List<LineItemDto> lineItems) {
        this.customerId = customerId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.taxAmount = taxAmount;
        this.notes = notes;
        this.lineItems = lineItems != null ? lineItems : new ArrayList<>();
    }

    // Getters and Setters
    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
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

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<LineItemDto> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<LineItemDto> lineItems) {
        this.lineItems = lineItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateInvoiceCommand that = (CreateInvoiceCommand) o;
        return Objects.equals(customerId, that.customerId) &&
               Objects.equals(issueDate, that.issueDate) &&
               Objects.equals(dueDate, that.dueDate) &&
               Objects.equals(taxAmount, that.taxAmount) &&
               Objects.equals(notes, that.notes) &&
               Objects.equals(lineItems, that.lineItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, issueDate, dueDate, taxAmount, notes, lineItems);
    }

    @Override
    public String toString() {
        return "CreateInvoiceCommand{" +
               "customerId=" + customerId +
               ", issueDate=" + issueDate +
               ", dueDate=" + dueDate +
               ", taxAmount=" + taxAmount +
               ", notes='" + notes + '\'' +
               ", lineItems=" + lineItems +
               '}';
    }

    /**
     * Nested DTO for line item information.
     */
    public static class LineItemDto {
        private String description;
        private BigDecimal quantity;
        private BigDecimal unitPrice;

        // Constructors
        public LineItemDto() {
        }

        public LineItemDto(String description, BigDecimal quantity, BigDecimal unitPrice) {
            this.description = description;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        // Getters and Setters
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LineItemDto that = (LineItemDto) o;
            return Objects.equals(description, that.description) &&
                   Objects.equals(quantity, that.quantity) &&
                   Objects.equals(unitPrice, that.unitPrice);
        }

        @Override
        public int hashCode() {
            return Objects.hash(description, quantity, unitPrice);
        }

        @Override
        public String toString() {
            return "LineItemDto{" +
                   "description='" + description + '\'' +
                   ", quantity=" + quantity +
                   ", unitPrice=" + unitPrice +
                   '}';
        }
    }
}
