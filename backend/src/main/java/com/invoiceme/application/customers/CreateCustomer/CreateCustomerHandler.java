package com.invoiceme.application.customers.CreateCustomer;

import com.invoiceme.domain.customer.Address;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Handles the CreateCustomerCommand.
 * Maps command to domain entity and persists to repository.
 */
@Service
public class CreateCustomerHandler {

    private final CustomerRepository customerRepository;
    private final CreateCustomerValidator validator;

    public CreateCustomerHandler(CustomerRepository customerRepository, CreateCustomerValidator validator) {
        this.customerRepository = customerRepository;
        this.validator = validator;
    }

    /**
     * Handles the creation of a new customer.
     *
     * @param command the create customer command
     * @return the ID of the newly created customer
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public UUID handle(CreateCustomerCommand command) {
        // Validate command
        List<String> errors = validator.validate(command);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Validation failed: " + String.join(", ", errors));
        }

        // Map command to domain entity
        Customer customer = new Customer();
        customer.setBusinessName(command.getBusinessName());
        customer.setContactName(command.getContactName());
        customer.setEmail(command.getEmail());
        customer.setPhone(command.getPhone());
        customer.setBillingAddress(mapToAddress(command.getBillingAddress()));

        if (command.getShippingAddress() != null) {
            customer.setShippingAddress(mapToAddress(command.getShippingAddress()));
        }

        customer.setActive(true);

        // Save to repository
        Customer savedCustomer = customerRepository.save(customer);

        // TODO: Publish CustomerCreatedEvent
        // eventPublisher.publish(new CustomerCreatedEvent(savedCustomer.getId()));

        return savedCustomer.getId();
    }

    /**
     * Maps AddressDto to Address domain entity.
     *
     * @param dto the address DTO
     * @return the Address entity
     */
    private Address mapToAddress(CreateCustomerCommand.AddressDto dto) {
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
