package com.invoiceme.application.customers.GetCustomer;

import com.invoiceme.domain.customer.Address;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles the GetCustomerQuery.
 * Fetches a customer by ID and returns it as a CustomerDto.
 */
@Service
public class GetCustomerHandler {

    private final CustomerRepository customerRepository;

    public GetCustomerHandler(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Handles the query to retrieve a customer by ID.
     *
     * @param query the get customer query
     * @return the customer as a DTO
     * @throws IllegalArgumentException if customer not found
     */
    @Transactional(readOnly = true)
    public CustomerDto handle(GetCustomerQuery query) {
        if (query == null || query.getCustomerId() == null) {
            throw new IllegalArgumentException("Customer ID is required");
        }

        // Load customer
        Customer customer = customerRepository.findById(query.getCustomerId())
            .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + query.getCustomerId()));

        // Map to DTO
        return mapToDto(customer);
    }

    /**
     * Maps a Customer entity to CustomerDto.
     *
     * @param customer the customer entity
     * @return the customer DTO
     */
    private CustomerDto mapToDto(Customer customer) {
        CustomerDto dto = new CustomerDto();
        dto.setId(customer.getId());
        dto.setBusinessName(customer.getBusinessName());
        dto.setContactName(customer.getContactName());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setBillingAddress(mapToAddressDto(customer.getBillingAddress()));
        dto.setShippingAddress(mapToAddressDto(customer.getShippingAddress()));
        dto.setActive(customer.isActive());
        dto.setCreatedAt(customer.getCreatedAt());
        dto.setUpdatedAt(customer.getUpdatedAt());
        return dto;
    }

    /**
     * Maps an Address entity to AddressDto.
     *
     * @param address the address entity
     * @return the address DTO
     */
    private CustomerDto.AddressDto mapToAddressDto(Address address) {
        if (address == null) {
            return null;
        }
        return new CustomerDto.AddressDto(
            address.getStreet(),
            address.getCity(),
            address.getState(),
            address.getPostalCode(),
            address.getCountry()
        );
    }
}
