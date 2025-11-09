package com.invoiceme.domain.invoice;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "invoice_line_items")
public class LineItem {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "line_total", nullable = false)
    private BigDecimal lineTotal;

    @Column(name = "line_order", nullable = false)
    private int lineOrder;

    // Constructors
    public LineItem() {
    }

    public LineItem(UUID id, Invoice invoice, String description, BigDecimal quantity,
                    BigDecimal unitPrice, BigDecimal lineTotal, int lineOrder) {
        this.id = id;
        this.invoice = invoice;
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

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
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

    // Business Logic Methods
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        calculateLineTotal();
    }

    @PreUpdate
    protected void onUpdate() {
        calculateLineTotal();
    }

    public void calculateLineTotal() {
        this.lineTotal = quantity.multiply(unitPrice);
    }

    // equals, hashCode, and toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LineItem lineItem = (LineItem) o;
        return lineOrder == lineItem.lineOrder &&
               Objects.equals(id, lineItem.id) &&
               Objects.equals(invoice, lineItem.invoice) &&
               Objects.equals(description, lineItem.description) &&
               Objects.equals(quantity, lineItem.quantity) &&
               Objects.equals(unitPrice, lineItem.unitPrice) &&
               Objects.equals(lineTotal, lineItem.lineTotal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, invoice, description, quantity, unitPrice, lineTotal, lineOrder);
    }

    @Override
    public String toString() {
        return "LineItem{" +
               "id=" + id +
               ", invoice=" + invoice +
               ", description='" + description + '\'' +
               ", quantity=" + quantity +
               ", unitPrice=" + unitPrice +
               ", lineTotal=" + lineTotal +
               ", lineOrder=" + lineOrder +
               '}';
    }
}
