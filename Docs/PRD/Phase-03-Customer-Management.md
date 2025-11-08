# Phase 3: Customer Management (CQRS + VSA)

**Estimated Time:** 6-8 hours
**Dependencies:** Phase 2 (Domain Model)
**Status:** Not Started

## Overview

Implement complete Customer management functionality using CQRS (Command Query Responsibility Segregation) and Vertical Slice Architecture (VSA). Each feature is organized as a self-contained slice with commands, queries, handlers, and REST endpoints.

## Objectives

- Implement CreateCustomer command with validation
- Implement UpdateCustomer command
- Implement DeleteCustomer command with business rule validation
- Implement Customer queries (GetById, ListAll, Search)
- Create REST API endpoints with proper DTOs
- Write comprehensive unit tests

## Architecture Pattern

```
application/customers/
├── CreateCustomer/
│   ├── CreateCustomerCommand.java
│   ├── CreateCustomerHandler.java
│   └── CreateCustomerValidator.java
├── UpdateCustomer/
│   ├── UpdateCustomerCommand.java
│   └── UpdateCustomerHandler.java
├── DeleteCustomer/
│   ├── DeleteCustomerCommand.java
│   └── DeleteCustomerHandler.java
├── GetCustomer/
│   ├── GetCustomerQuery.java
│   └── GetCustomerHandler.java
└── ListCustomers/
    ├── ListCustomersQuery.java
    └── ListCustomersHandler.java
```

---

## Tasks

### 3.1 Implement CreateCustomer Command

**Package:** `com.invoiceme.application.customers.CreateCustomer`

**CreateCustomerCommand.java:**

```java
package com.invoiceme.application.customers.CreateCustomer;

import lombok.Data;

@Data
public class CreateCustomerCommand {
    private String businessName;
    private String contactName;
    private String email;
    private String phone;

    private AddressDto billingAddress;
    private AddressDto shippingAddress;

    @Data
    public static class AddressDto {
        private String street;
        private String city;
        private String state;
        private String postalCode;
        private String country;
    }
}
```

**CreateCustomerValidator.java:**

```java
package com.invoiceme.application.customers.CreateCustomer;

import com.invoiceme.infrastructure.persistence.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateCustomerValidator {

    private final CustomerRepository customerRepository;

    public void validate(CreateCustomerCommand command) {
        // Validate required fields
        if (command.getBusinessName() == null || command.getBusinessName().isBlank()) {
            throw new IllegalArgumentException("Business name is required");
        }

        if (command.getContactName() == null || command.getContactName().isBlank()) {
            throw new IllegalArgumentException("Contact name is required");
        }

        if (command.getEmail() == null || !isValidEmail(command.getEmail())) {
            throw new IllegalArgumentException("Valid email is required");
        }

        if (command.getBillingAddress() == null || !isValidAddress(command.getBillingAddress())) {
            throw new IllegalArgumentException("Valid billing address is required");
        }

        // Check for duplicate email
        if (customerRepository.existsByEmail(command.getEmail())) {
            throw new IllegalArgumentException("Customer with email " + command.getEmail() + " already exists");
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private boolean isValidAddress(CreateCustomerCommand.AddressDto address) {
        return address != null
            && address.getStreet() != null && !address.getStreet().isBlank()
            && address.getCity() != null && !address.getCity().isBlank()
            && address.getState() != null && !address.getState().isBlank()
            && address.getPostalCode() != null && !address.getPostalCode().isBlank()
            && address.getCountry() != null && !address.getCountry().isBlank();
    }
}
```

**CreateCustomerHandler.java:**

```java
package com.invoiceme.application.customers.CreateCustomer;

import com.invoiceme.domain.customer.Address;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateCustomerHandler {

    private final CustomerRepository customerRepository;
    private final CreateCustomerValidator validator;

    @Transactional
    public UUID handle(CreateCustomerCommand command) {
        validator.validate(command);

        Customer customer = new Customer();
        customer.setBusinessName(command.getBusinessName());
        customer.setContactName(command.getContactName());
        customer.setEmail(command.getEmail());
        customer.setPhone(command.getPhone());

        // Map billing address
        Address billingAddress = mapAddress(command.getBillingAddress());
        customer.setBillingAddress(billingAddress);

        // Map shipping address if provided
        if (command.getShippingAddress() != null) {
            Address shippingAddress = mapAddress(command.getShippingAddress());
            customer.setShippingAddress(shippingAddress);
        }

        Customer saved = customerRepository.save(customer);
        return saved.getId();
    }

    private Address mapAddress(CreateCustomerCommand.AddressDto dto) {
        return new Address(
            dto.getStreet(),
            dto.getCity(),
            dto.getState(),
            dto.getPostalCode(),
            dto.getCountry()
        );
    }
}
```

**Action Items:**
- [ ] Create CreateCustomerCommand
- [ ] Create CreateCustomerValidator
- [ ] Create CreateCustomerHandler
- [ ] Write unit tests for validation logic
- [ ] Write unit tests for handler

---

### 3.2 Implement UpdateCustomer Command

**Package:** `com.invoiceme.application.customers.UpdateCustomer`

**UpdateCustomerCommand.java:**

```java
package com.invoiceme.application.customers.UpdateCustomer;

import com.invoiceme.application.customers.CreateCustomer.CreateCustomerCommand;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateCustomerCommand {
    private UUID customerId;
    private String businessName;
    private String contactName;
    private String email;
    private String phone;

    private CreateCustomerCommand.AddressDto billingAddress;
    private CreateCustomerCommand.AddressDto shippingAddress;
}
```

**UpdateCustomerHandler.java:**

```java
package com.invoiceme.application.customers.UpdateCustomer;

import com.invoiceme.domain.customer.Address;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateCustomerHandler {

    private final CustomerRepository customerRepository;

    @Transactional
    public void handle(UpdateCustomerCommand command) {
        Customer customer = customerRepository.findById(command.getCustomerId())
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // Update fields
        if (command.getBusinessName() != null) {
            customer.setBusinessName(command.getBusinessName());
        }
        if (command.getContactName() != null) {
            customer.setContactName(command.getContactName());
        }
        if (command.getEmail() != null) {
            customer.setEmail(command.getEmail());
        }
        if (command.getPhone() != null) {
            customer.setPhone(command.getPhone());
        }

        if (command.getBillingAddress() != null) {
            customer.setBillingAddress(mapAddress(command.getBillingAddress()));
        }

        if (command.getShippingAddress() != null) {
            customer.setShippingAddress(mapAddress(command.getShippingAddress()));
        }

        customerRepository.save(customer);
    }

    private Address mapAddress(UpdateCustomerCommand.AddressDto dto) {
        return new Address(
            dto.getStreet(),
            dto.getCity(),
            dto.getState(),
            dto.getPostalCode(),
            dto.getCountry()
        );
    }
}
```

**Action Items:**
- [ ] Create UpdateCustomerCommand
- [ ] Create UpdateCustomerHandler
- [ ] Write unit tests

---

### 3.3 Implement DeleteCustomer Command

**Package:** `com.invoiceme.application.customers.DeleteCustomer`

**DeleteCustomerCommand.java:**

```java
package com.invoiceme.application.customers.DeleteCustomer;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class DeleteCustomerCommand {
    private UUID customerId;
}
```

**DeleteCustomerHandler.java:**

```java
package com.invoiceme.application.customers.DeleteCustomer;

import com.invoiceme.domain.customer.Customer;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceStatus;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeleteCustomerHandler {

    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;

    @Transactional
    public void handle(DeleteCustomerCommand command) {
        Customer customer = customerRepository.findById(command.getCustomerId())
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // Business Rule: Check for active invoices
        List<Invoice> activeInvoices = invoiceRepository.findByCustomerId(customer.getId())
            .stream()
            .filter(invoice -> invoice.getStatus() == InvoiceStatus.DRAFT
                            || invoice.getStatus() == InvoiceStatus.SENT)
            .collect(Collectors.toList());

        if (!activeInvoices.isEmpty()) {
            String invoiceNumbers = activeInvoices.stream()
                .map(Invoice::getInvoiceNumber)
                .collect(Collectors.joining(", "));

            throw new IllegalStateException(
                "Cannot delete customer with active invoices. " +
                "Please mark all invoices as paid or cancelled first. " +
                "Active invoices: " + invoiceNumbers
            );
        }

        // Soft delete
        customer.deactivate();
        customerRepository.save(customer);
    }
}
```

**Action Items:**
- [ ] Create DeleteCustomerCommand
- [ ] Create DeleteCustomerHandler
- [ ] Write unit tests for deletion block with active invoices
- [ ] Write unit tests for successful soft delete

---

### 3.4 Implement Customer Queries

**Package:** `com.invoiceme.application.customers.GetCustomer`

**CustomerDto.java:**

```java
package com.invoiceme.application.customers;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CustomerDto {
    private UUID id;
    private String businessName;
    private String contactName;
    private String email;
    private String phone;
    private AddressDto billingAddress;
    private AddressDto shippingAddress;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @AllArgsConstructor
    public static class AddressDto {
        private String street;
        private String city;
        private String state;
        private String postalCode;
        private String country;
    }
}
```

**GetCustomerQuery.java:**

```java
package com.invoiceme.application.customers.GetCustomer;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class GetCustomerQuery {
    private UUID customerId;
}
```

**GetCustomerHandler.java:**

```java
package com.invoiceme.application.customers.GetCustomer;

import com.invoiceme.application.customers.CustomerDto;
import com.invoiceme.domain.customer.Address;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCustomerHandler {

    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public CustomerDto handle(GetCustomerQuery query) {
        Customer customer = customerRepository.findById(query.getCustomerId())
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        return mapToDto(customer);
    }

    private CustomerDto mapToDto(Customer customer) {
        return new CustomerDto(
            customer.getId(),
            customer.getBusinessName(),
            customer.getContactName(),
            customer.getEmail(),
            customer.getPhone(),
            mapAddressToDto(customer.getBillingAddress()),
            customer.hasShippingAddress() ? mapAddressToDto(customer.getShippingAddress()) : null,
            customer.isActive(),
            customer.getCreatedAt(),
            customer.getUpdatedAt()
        );
    }

    private CustomerDto.AddressDto mapAddressToDto(Address address) {
        if (address == null) return null;
        return new CustomerDto.AddressDto(
            address.getStreet(),
            address.getCity(),
            address.getState(),
            address.getPostalCode(),
            address.getCountry()
        );
    }
}
```

**ListCustomersHandler.java:**

```java
package com.invoiceme.application.customers.ListCustomers;

import com.invoiceme.application.customers.CustomerDto;
import com.invoiceme.application.customers.GetCustomer.GetCustomerHandler;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListCustomersHandler {

    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public List<CustomerDto> handle() {
        return customerRepository.findByActiveTrue()
            .stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    private CustomerDto mapToDto(com.invoiceme.domain.customer.Customer customer) {
        return new CustomerDto(
            customer.getId(),
            customer.getBusinessName(),
            customer.getContactName(),
            customer.getEmail(),
            customer.getPhone(),
            mapAddressToDto(customer.getBillingAddress()),
            customer.hasShippingAddress() ? mapAddressToDto(customer.getShippingAddress()) : null,
            customer.isActive(),
            customer.getCreatedAt(),
            customer.getUpdatedAt()
        );
    }

    private CustomerDto.AddressDto mapAddressToDto(com.invoiceme.domain.customer.Address address) {
        if (address == null) return null;
        return new CustomerDto.AddressDto(
            address.getStreet(),
            address.getCity(),
            address.getState(),
            address.getPostalCode(),
            address.getCountry()
        );
    }
}
```

**Action Items:**
- [ ] Create CustomerDto
- [ ] Create GetCustomerQuery and Handler
- [ ] Create ListCustomersQuery and Handler
- [ ] Write unit tests for queries

---

### 3.5 Create Customer REST Controller

**Package:** `com.invoiceme.api`

**CustomerController.java:**

```java
package com.invoiceme.api;

import com.invoiceme.application.customers.CreateCustomer.CreateCustomerCommand;
import com.invoiceme.application.customers.CreateCustomer.CreateCustomerHandler;
import com.invoiceme.application.customers.CustomerDto;
import com.invoiceme.application.customers.DeleteCustomer.DeleteCustomerCommand;
import com.invoiceme.application.customers.DeleteCustomer.DeleteCustomerHandler;
import com.invoiceme.application.customers.GetCustomer.GetCustomerHandler;
import com.invoiceme.application.customers.GetCustomer.GetCustomerQuery;
import com.invoiceme.application.customers.ListCustomers.ListCustomersHandler;
import com.invoiceme.application.customers.UpdateCustomer.UpdateCustomerCommand;
import com.invoiceme.application.customers.UpdateCustomer.UpdateCustomerHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CreateCustomerHandler createCustomerHandler;
    private final UpdateCustomerHandler updateCustomerHandler;
    private final DeleteCustomerHandler deleteCustomerHandler;
    private final GetCustomerHandler getCustomerHandler;
    private final ListCustomersHandler listCustomersHandler;

    @PostMapping
    public ResponseEntity<UUID> createCustomer(@RequestBody CreateCustomerCommand command) {
        try {
            UUID customerId = createCustomerHandler.handle(command);
            return ResponseEntity.status(HttpStatus.CREATED).body(customerId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<CustomerDto>> listCustomers() {
        List<CustomerDto> customers = listCustomersHandler.handle();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getCustomer(@PathVariable UUID id) {
        try {
            CustomerDto customer = getCustomerHandler.handle(new GetCustomerQuery(id));
            return ResponseEntity.ok(customer);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateCustomer(
        @PathVariable UUID id,
        @RequestBody UpdateCustomerCommand command
    ) {
        try {
            command.setCustomerId(id);
            updateCustomerHandler.handle(command);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable UUID id) {
        try {
            deleteCustomerHandler.handle(new DeleteCustomerCommand(id));
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
```

**Action Items:**
- [ ] Create CustomerController
- [ ] Test all endpoints with Postman or curl
- [ ] Write integration tests for REST API

---

### 3.6 Write Unit Tests

**CreateCustomerHandlerTest.java:**

```java
package com.invoiceme.application.customers.CreateCustomer;

import com.invoiceme.domain.customer.Customer;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCustomerHandlerTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CreateCustomerValidator validator;

    @InjectMocks
    private CreateCustomerHandler handler;

    @Test
    void shouldCreateCustomerSuccessfully() {
        // Given
        CreateCustomerCommand command = new CreateCustomerCommand();
        command.setBusinessName("Acme Corp");
        command.setContactName("John Doe");
        command.setEmail("john@acme.com");

        CreateCustomerCommand.AddressDto address = new CreateCustomerCommand.AddressDto();
        address.setStreet("123 Main St");
        address.setCity("New York");
        address.setState("NY");
        address.setPostalCode("10001");
        address.setCountry("USA");
        command.setBillingAddress(address);

        Customer savedCustomer = new Customer();
        savedCustomer.setId(UUID.randomUUID());

        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);
        doNothing().when(validator).validate(command);

        // When
        UUID result = handler.handle(command);

        // Then
        assertNotNull(result);
        verify(validator).validate(command);
        verify(customerRepository).save(any(Customer.class));
    }
}
```

**DeleteCustomerHandlerTest.java:**

```java
package com.invoiceme.application.customers.DeleteCustomer;

import com.invoiceme.domain.customer.Customer;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.InvoiceStatus;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteCustomerHandlerTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private DeleteCustomerHandler handler;

    @Test
    void shouldBlockDeletionWhenActiveInvoicesExist() {
        // Given
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setId(customerId);

        Invoice activeInvoice = new Invoice();
        activeInvoice.setStatus(InvoiceStatus.SENT);
        activeInvoice.setInvoiceNumber("INV-2025-0001");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(invoiceRepository.findByCustomerId(customerId)).thenReturn(Arrays.asList(activeInvoice));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            handler.handle(new DeleteCustomerCommand(customerId));
        });

        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldDeleteCustomerWhenNoActiveInvoices() {
        // Given
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setActive(true);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(invoiceRepository.findByCustomerId(customerId)).thenReturn(Arrays.asList());

        // When
        handler.handle(new DeleteCustomerCommand(customerId));

        // Then
        assertFalse(customer.isActive());
        verify(customerRepository).save(customer);
    }
}
```

**Action Items:**
- [ ] Write unit tests for CreateCustomerHandler
- [ ] Write unit tests for UpdateCustomerHandler
- [ ] Write unit tests for DeleteCustomerHandler
- [ ] Write unit tests for GetCustomerHandler
- [ ] Write unit tests for validation logic
- [ ] Ensure all tests pass

---

## Verification Checklist

After completing Phase 3, verify:

- [ ] All Customer commands implemented and tested
- [ ] All Customer queries implemented and tested
- [ ] REST API endpoints functional
- [ ] Validation prevents invalid data
- [ ] Duplicate email check works
- [ ] Customer deletion blocked with active invoices
- [ ] Soft delete works correctly
- [ ] Unit tests achieve 80%+ coverage
- [ ] Integration tests pass

## API Endpoints Summary

```
POST   /api/customers           - Create customer
GET    /api/customers           - List all active customers
GET    /api/customers/{id}      - Get customer by ID
PUT    /api/customers/{id}      - Update customer
DELETE /api/customers/{id}      - Delete customer (soft delete)
```

## Next Steps

Proceed to [Phase 4: Invoice Management (CQRS + VSA)](Phase-04-Invoice-Management.md)

---

## Reference Files

- Main PRD: `Docs/PRD/PRD.md` (Section 2.2: Customer Domain)
- Domain Model: [Phase-02-Domain-Model.md](Phase-02-Domain-Model.md)
