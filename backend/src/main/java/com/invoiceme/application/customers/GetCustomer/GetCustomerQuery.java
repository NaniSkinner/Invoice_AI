package com.invoiceme.application.customers.GetCustomer;

import jakarta.validation.constraints.NotNull;

import java.util.Objects;
import java.util.UUID;

/**
 * Query to retrieve a single customer by ID.
 * This is a read operation in the CQRS pattern.
 */
public class GetCustomerQuery {

    
    private UUID customerId;

    // Constructors
    public GetCustomerQuery() {
    }

    public GetCustomerQuery(@NotNull UUID customerId) {
        this.customerId = customerId;
    }

    // Getters and Setters
    @NotNull
    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(@NotNull UUID customerId) {
        this.customerId = customerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetCustomerQuery that = (GetCustomerQuery) o;
        return Objects.equals(customerId, that.customerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId);
    }

    @Override
    public String toString() {
        return "GetCustomerQuery{" +
               "customerId=" + customerId +
               '}';
    }
}
