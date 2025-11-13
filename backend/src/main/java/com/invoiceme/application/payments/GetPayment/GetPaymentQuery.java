package com.invoiceme.application.payments.GetPayment;

import jakarta.validation.constraints.NotNull;

import java.util.Objects;
import java.util.UUID;

/**
 * Query to retrieve a specific payment by ID.
 */
public class GetPaymentQuery {

    
    private UUID paymentId;

    // Constructors
    public GetPaymentQuery() {
    }

    public GetPaymentQuery(@NotNull UUID paymentId) {
        this.paymentId = paymentId;
    }

    // Getters and Setters
    @NotNull
    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(@NotNull UUID paymentId) {
        this.paymentId = paymentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetPaymentQuery that = (GetPaymentQuery) o;
        return Objects.equals(paymentId, that.paymentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentId);
    }

    @Override
    public String toString() {
        return "GetPaymentQuery{" +
               "paymentId=" + paymentId +
               '}';
    }
}
