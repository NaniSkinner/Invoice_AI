package com.invoiceme.application.customers.ListCustomers;

import com.invoiceme.application.customers.GetCustomer.CustomerDto;
import com.invoiceme.domain.customer.Address;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the ListCustomersQuery.
 * Fetches a list of customers with optional filtering and returns them as CustomerDto objects.
 */
@Service
public class ListCustomersHandler {

    private final CustomerRepository customerRepository;

    public ListCustomersHandler(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Handles the query to retrieve a list of customers.
     * Supports filtering by active status and searching by business name, contact name, or email.
     *
     * @param query the list customers query
     * @return list of customer DTOs
     */
    @Transactional(readOnly = true)
    public List<CustomerDto> handle(ListCustomersQuery query) {
        List<Customer> customers;

        // Determine which customers to fetch based on activeOnly filter
        if (query != null && Boolean.TRUE.equals(query.getActiveOnly())) {
            customers = customerRepository.findByActiveTrue();
        } else {
            customers = customerRepository.findAll();
        }

        // Apply search filter if provided
        if (query != null && query.getSearchTerm() != null && !query.getSearchTerm().isBlank()) {
            String searchTerm = query.getSearchTerm().toLowerCase();
            customers = customers.stream()
                .filter(customer -> matchesSearchTerm(customer, searchTerm))
                .collect(Collectors.toList());
        }

        // Map to DTOs
        return customers.stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    /**
     * Checks if a customer matches the search term.
     * Searches in business name, contact name, and email.
     *
     * @param customer the customer to check
     * @param searchTerm the search term (lowercase)
     * @return true if customer matches
     */
    private boolean matchesSearchTerm(Customer customer, String searchTerm) {
        return (customer.getBusinessName() != null && customer.getBusinessName().toLowerCase().contains(searchTerm))
            || (customer.getContactName() != null && customer.getContactName().toLowerCase().contains(searchTerm))
            || (customer.getEmail() != null && customer.getEmail().toLowerCase().contains(searchTerm));
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
