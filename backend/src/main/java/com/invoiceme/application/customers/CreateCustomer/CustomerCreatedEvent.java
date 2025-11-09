package com.invoiceme.application.customers.CreateCustomer;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain event published after a customer is successfully created.
 * This event can be used to trigger downstream processes like sending welcome emails,
 * creating initial customer records in other systems, etc.
 */
public class CustomerCreatedEvent {

    private final UUID customerId;
    private final String email;
    private final LocalDateTime occurredAt;

    public CustomerCreatedEvent(UUID customerId, String email) {
        this.customerId = customerId;
        this.email = email;
        this.occurredAt = LocalDateTime.now();
    }

    public CustomerCreatedEvent(UUID customerId, String email, LocalDateTime occurredAt) {
        this.customerId = customerId;
        this.email = email;
        this.occurredAt = occurredAt;
    }

    // Getters
    public UUID getCustomerId() {
        return customerId;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerCreatedEvent that = (CustomerCreatedEvent) o;
        return Objects.equals(customerId, that.customerId) &&
               Objects.equals(email, that.email) &&
               Objects.equals(occurredAt, that.occurredAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, email, occurredAt);
    }

    @Override
    public String toString() {
        return "CustomerCreatedEvent{" +
               "customerId=" + customerId +
               ", email='" + email + '\'' +
               ", occurredAt=" + occurredAt +
               '}';
    }
}
