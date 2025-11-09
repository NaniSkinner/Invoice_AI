package com.invoiceme.application.customers.CreateCustomer;

import com.invoiceme.infrastructure.persistence.CustomerRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates CreateCustomerCommand before processing.
 * Checks for required fields and business rules.
 */
@Component
public class CreateCustomerValidator {

    private final CustomerRepository customerRepository;

    public CreateCustomerValidator(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Validates the command and returns a list of error messages.
     *
     * @param command the command to validate
     * @return list of validation error messages (empty if valid)
     */
    public List<String> validate(CreateCustomerCommand command) {
        List<String> errors = new ArrayList<>();

        if (command == null) {
            errors.add("Command cannot be null");
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
        } else if (customerRepository.existsByEmail(command.getEmail())) {
            errors.add("Email already exists");
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
    private List<String> validateAddress(CreateCustomerCommand.AddressDto address, String type) {
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
