package com.invoiceme.application.customers.UpdateCustomer;

import com.invoiceme.infrastructure.persistence.CustomerRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates UpdateCustomerCommand before processing.
 * Checks for required fields and business rules.
 */
@Component
public class UpdateCustomerValidator {

    private final CustomerRepository customerRepository;

    public UpdateCustomerValidator(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Validates the update command and returns a list of error messages.
     *
     * @param command the command to validate
     * @return list of validation error messages (empty if valid)
     */
    public List<String> validate(UpdateCustomerCommand command) {
        List<String> errors = new ArrayList<>();

        if (command == null) {
            errors.add("Command cannot be null");
            return errors;
        }

        // Validate customer ID
        if (command.getCustomerId() == null) {
            errors.add("Customer ID is required");
            return errors;
        }

        // Check if customer exists
        if (!customerRepository.existsById(command.getCustomerId())) {
            errors.add("Customer not found with ID: " + command.getCustomerId());
            return errors;
        }

        // Validate business name
        if (command.getBusinessName() == null || command.getBusinessName().isBlank()) {
            errors.add("Business name is required");
        }

        // Validate contact name
        if (command.getContactName() == null || command.getContactName().isBlank()) {
            errors.add("Contact name is required");
        }

        // Validate email
        if (command.getEmail() == null || command.getEmail().isBlank()) {
            errors.add("Email is required");
        } else if (!isValidEmail(command.getEmail())) {
            errors.add("Email format is invalid");
        } else {
            // Check if email is already used by another customer
            customerRepository.findByEmail(command.getEmail()).ifPresent(existingCustomer -> {
                if (!existingCustomer.getId().equals(command.getCustomerId())) {
                    errors.add("Email already exists for another customer");
                }
            });
        }

        // Validate billing address
        if (command.getBillingAddress() == null) {
            errors.add("Billing address is required");
        } else {
            errors.addAll(validateAddress(command.getBillingAddress(), "Billing"));
        }

        // Validate shipping address (optional, but if provided must be complete)
        if (command.getShippingAddress() != null) {
            errors.addAll(validateAddress(command.getShippingAddress(), "Shipping"));
        }

        return errors;
    }

    /**
     * Validates an address DTO.
     *
     * @param address the address to validate
     * @param type the type of address (for error messages)
     * @return list of validation error messages
     */
    private List<String> validateAddress(UpdateCustomerCommand.AddressDto address, String type) {
        List<String> errors = new ArrayList<>();

        if (address.getStreet() == null || address.getStreet().isBlank()) {
            errors.add(type + " address street is required");
        }

        if (address.getCity() == null || address.getCity().isBlank()) {
            errors.add(type + " address city is required");
        }

        if (address.getState() == null || address.getState().isBlank()) {
            errors.add(type + " address state is required");
        }

        if (address.getPostalCode() == null || address.getPostalCode().isBlank()) {
            errors.add(type + " address postal code is required");
        }

        if (address.getCountry() == null || address.getCountry().isBlank()) {
            errors.add(type + " address country is required");
        }

        return errors;
    }

    /**
     * Simple email validation.
     *
     * @param email the email to validate
     * @return true if email format is valid
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
