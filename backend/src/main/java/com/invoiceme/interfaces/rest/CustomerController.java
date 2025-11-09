package com.invoiceme.interfaces.rest;

import com.invoiceme.application.customers.CreateCustomer.CreateCustomerCommand;
import com.invoiceme.application.customers.CreateCustomer.CreateCustomerHandler;
import com.invoiceme.application.customers.DeleteCustomer.DeleteCustomerCommand;
import com.invoiceme.application.customers.DeleteCustomer.DeleteCustomerHandler;
import com.invoiceme.application.customers.GetCustomer.CustomerDto;
import com.invoiceme.application.customers.GetCustomer.GetCustomerHandler;
import com.invoiceme.application.customers.GetCustomer.GetCustomerQuery;
import com.invoiceme.application.customers.ListCustomers.ListCustomersHandler;
import com.invoiceme.application.customers.ListCustomers.ListCustomersQuery;
import com.invoiceme.application.customers.UpdateCustomer.UpdateCustomerCommand;
import com.invoiceme.application.customers.UpdateCustomer.UpdateCustomerHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Customer management operations.
 * Provides CRUD endpoints following CQRS pattern.
 */
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CreateCustomerHandler createCustomerHandler;
    private final UpdateCustomerHandler updateCustomerHandler;
    private final DeleteCustomerHandler deleteCustomerHandler;
    private final GetCustomerHandler getCustomerHandler;
    private final ListCustomersHandler listCustomersHandler;

    public CustomerController(CreateCustomerHandler createCustomerHandler,
                            UpdateCustomerHandler updateCustomerHandler,
                            DeleteCustomerHandler deleteCustomerHandler,
                            GetCustomerHandler getCustomerHandler,
                            ListCustomersHandler listCustomersHandler) {
        this.createCustomerHandler = createCustomerHandler;
        this.updateCustomerHandler = updateCustomerHandler;
        this.deleteCustomerHandler = deleteCustomerHandler;
        this.getCustomerHandler = getCustomerHandler;
        this.listCustomersHandler = listCustomersHandler;
    }

    /**
     * Create a new customer.
     *
     * @param command the customer creation command
     * @return the created customer DTO with 201 Created status
     */
    @PostMapping
    public ResponseEntity<CustomerDto> createCustomer(@RequestBody CreateCustomerCommand command) {
        UUID customerId = createCustomerHandler.handle(command);

        // Fetch the created customer to return complete DTO
        GetCustomerQuery query = new GetCustomerQuery();
        query.setCustomerId(customerId);
        CustomerDto customer = getCustomerHandler.handle(query);

        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }

    /**
     * Get a customer by ID.
     *
     * @param id the customer ID
     * @return the customer DTO with 200 OK status
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getCustomer(@PathVariable UUID id) {
        GetCustomerQuery query = new GetCustomerQuery();
        query.setCustomerId(id);
        CustomerDto customer = getCustomerHandler.handle(query);
        return ResponseEntity.ok(customer);
    }

    /**
     * List customers with optional filtering.
     *
     * @param activeOnly optional filter to show only active customers
     * @param searchTerm optional search term to filter by name or email
     * @return list of customer DTOs with 200 OK status
     */
    @GetMapping
    public ResponseEntity<List<CustomerDto>> listCustomers(
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false) String searchTerm) {

        ListCustomersQuery query = new ListCustomersQuery();
        query.setActiveOnly(activeOnly);
        query.setSearchTerm(searchTerm);

        List<CustomerDto> customers = listCustomersHandler.handle(query);
        return ResponseEntity.ok(customers);
    }

    /**
     * Update an existing customer.
     *
     * @param id the customer ID
     * @param command the customer update command
     * @return the updated customer DTO with 200 OK status
     */
    @PutMapping("/{id}")
    public ResponseEntity<CustomerDto> updateCustomer(
            @PathVariable UUID id,
            @RequestBody UpdateCustomerCommand command) {

        command.setCustomerId(id);
        UUID customerId = updateCustomerHandler.handle(command);

        // Fetch the updated customer to return complete DTO
        GetCustomerQuery query = new GetCustomerQuery();
        query.setCustomerId(customerId);
        CustomerDto customer = getCustomerHandler.handle(query);

        return ResponseEntity.ok(customer);
    }

    /**
     * Delete (deactivate) a customer.
     *
     * @param id the customer ID
     * @return 204 No Content status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable UUID id) {
        DeleteCustomerCommand command = new DeleteCustomerCommand();
        command.setCustomerId(id);
        deleteCustomerHandler.handle(command);
        return ResponseEntity.noContent().build();
    }
}
