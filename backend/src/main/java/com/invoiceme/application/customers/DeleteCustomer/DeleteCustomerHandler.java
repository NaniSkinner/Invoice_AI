package com.invoiceme.application.customers.DeleteCustomer;

import com.invoiceme.domain.customer.Customer;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles the DeleteCustomerCommand.
 * Performs a soft delete by setting the customer's active flag to false.
 */
@Service
public class DeleteCustomerHandler {

    private final CustomerRepository customerRepository;

    public DeleteCustomerHandler(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Handles the deletion (deactivation) of a customer.
     * Performs a soft delete by setting active = false.
     *
     * @param command the delete customer command
     * @throws IllegalArgumentException if customer not found
     */
    @Transactional
    public void handle(DeleteCustomerCommand command) {
        if (command == null || command.getCustomerId() == null) {
            throw new IllegalArgumentException("Customer ID is required");
        }

        // Load customer
        Customer customer = customerRepository.findById(command.getCustomerId())
            .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + command.getCustomerId()));

        // Check if already deleted
        if (!customer.isActive()) {
            throw new IllegalArgumentException("Customer is already inactive");
        }

        // Soft delete by deactivating
        customer.deactivate();

        // Save updated customer (updatedAt timestamp handled by @PreUpdate)
        customerRepository.save(customer);
    }
}
