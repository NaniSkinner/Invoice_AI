package com.invoiceme.application.customers.DeleteCustomer;

import java.util.Objects;
import java.util.UUID;

/**
 * Command to delete (deactivate) a customer.
 * This is a write operation in the CQRS pattern.
 * Performs a soft delete by setting the customer's active flag to false.
 */
public class DeleteCustomerCommand {

    private UUID customerId;

    // Constructors
    public DeleteCustomerCommand() {
    }

    public DeleteCustomerCommand(UUID customerId) {
        this.customerId = customerId;
    }

    // Getters and Setters
    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeleteCustomerCommand that = (DeleteCustomerCommand) o;
        return Objects.equals(customerId, that.customerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId);
    }

    @Override
    public String toString() {
        return "DeleteCustomerCommand{" +
               "customerId=" + customerId +
               '}';
    }
}
