package com.invoiceme.application.customers.UpdateCustomer;

import com.invoiceme.domain.customer.Address;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Handles the UpdateCustomerCommand.
 * Loads the existing customer entity, updates fields, and persists changes.
 */
@Service
public class UpdateCustomerHandler {

    private final CustomerRepository customerRepository;
    private final UpdateCustomerValidator validator;

    public UpdateCustomerHandler(CustomerRepository customerRepository, UpdateCustomerValidator validator) {
        this.customerRepository = customerRepository;
        this.validator = validator;
    }

    /**
     * Handles the update of an existing customer.
     *
     * @param command the update customer command
     * @return the ID of the updated customer
     * @throws IllegalArgumentException if validation fails or customer not found
     */
    @Transactional
    public UUID handle(UpdateCustomerCommand command) {
        // Validate command
        List<String> errors = validator.validate(command);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Validation failed: " + String.join(", ", errors));
        }

        // Load existing customer
        Customer customer = customerRepository.findById(command.getCustomerId())
            .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + command.getCustomerId()));

        // Update fields
        customer.setBusinessName(command.getBusinessName());
        customer.setContactName(command.getContactName());
        customer.setEmail(command.getEmail());
        customer.setPhone(command.getPhone());
        customer.setBillingAddress(mapToAddress(command.getBillingAddress()));

        if (command.getShippingAddress() != null) {
            customer.setShippingAddress(mapToAddress(command.getShippingAddress()));
        } else {
            customer.setShippingAddress(null);
        }

        // Save updated customer (updatedAt timestamp handled by @PreUpdate)
        Customer updatedCustomer = customerRepository.save(customer);

        return updatedCustomer.getId();
    }

    /**
     * Maps AddressDto to Address domain entity.
     *
     * @param dto the address DTO
     * @return the Address entity
     */
    private Address mapToAddress(UpdateCustomerCommand.AddressDto dto) {
        if (dto == null) {
            return null;
        }
        return new Address(
            dto.getStreet(),
            dto.getCity(),
            dto.getState(),
            dto.getPostalCode(),
            dto.getCountry()
        );
    }
}
