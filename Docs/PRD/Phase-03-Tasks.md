# Phase 3 Tasks - Customer Management (CQRS + VSA)

**Execution Guide with Step-by-Step Commands**

**Estimated Time:** 6-8 hours
**Prerequisites:** Phase 2 complete, domain entities created

---

## Task 3.1: Create Application Layer Structure

### Step 3.1.1: Create application packages for VSA
```bash
cd ~/dev/Gauntlet/Invoice_AI/backend
mkdir -p src/main/java/com/invoiceme/application/customers/CreateCustomer
mkdir -p src/main/java/com/invoiceme/application/customers/UpdateCustomer
mkdir -p src/main/java/com/invoiceme/application/customers/DeleteCustomer
mkdir -p src/main/java/com/invoiceme/application/customers/GetCustomer
mkdir -p src/main/java/com/invoiceme/application/customers/ListCustomers

# Verify structure
tree src/main/java/com/invoiceme/application/customers/ -L 1
```

**Expected Output:**
```
src/main/java/com/invoiceme/application/customers/
├── CreateCustomer
├── DeleteCustomer
├── GetCustomer
├── ListCustomers
└── UpdateCustomer
```

**Verification:**
- [ ] All customer feature folders created
- [ ] Vertical slice structure established

---

## Task 3.2: Implement CreateCustomer Command (Write)

### Step 3.2.1: Create CreateCustomerCommand
```bash
cat > src/main/java/com/invoiceme/application/customers/CreateCustomer/CreateCustomerCommand.java << 'EOF'
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
EOF

cat src/main/java/com/invoiceme/application/customers/CreateCustomer/CreateCustomerCommand.java
```

### Step 3.2.2: Create CreateCustomerValidator
```bash
cat > src/main/java/com/invoiceme/application/customers/CreateCustomer/CreateCustomerValidator.java << 'EOF'
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
EOF

cat src/main/java/com/invoiceme/application/customers/CreateCustomer/CreateCustomerValidator.java
```

### Step 3.2.3: Create CreateCustomerHandler
```bash
cat > src/main/java/com/invoiceme/application/customers/CreateCustomer/CreateCustomerHandler.java << 'EOF'
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
EOF

cat src/main/java/com/invoiceme/application/customers/CreateCustomer/CreateCustomerHandler.java
```

### Step 3.2.4: Test compilation
```bash
./mvnw clean compile
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
```

**Verification:**
- [ ] CreateCustomerCommand created
- [ ] CreateCustomerValidator created with email validation
- [ ] CreateCustomerHandler created with transaction
- [ ] Code compiles successfully

---

## Task 3.3: Implement UpdateCustomer Command (Write)

### Step 3.3.1: Create UpdateCustomerCommand
```bash
cat > src/main/java/com/invoiceme/application/customers/UpdateCustomer/UpdateCustomerCommand.java << 'EOF'
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
EOF
```

### Step 3.3.2: Create UpdateCustomerHandler
```bash
cat > src/main/java/com/invoiceme/application/customers/UpdateCustomer/UpdateCustomerHandler.java << 'EOF'
package com.invoiceme.application.customers.UpdateCustomer;

import com.invoiceme.application.customers.CreateCustomer.CreateCustomerCommand;
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
EOF
```

**Verification:**
- [ ] UpdateCustomerCommand created
- [ ] UpdateCustomerHandler created
- [ ] Update logic allows partial updates

---

## Task 3.4: Implement DeleteCustomer Command (Write)

### Step 3.4.1: Create DeleteCustomerCommand
```bash
cat > src/main/java/com/invoiceme/application/customers/DeleteCustomer/DeleteCustomerCommand.java << 'EOF'
package com.invoiceme.application.customers.DeleteCustomer;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class DeleteCustomerCommand {
    private UUID customerId;
}
EOF
```

### Step 3.4.2: Create DeleteCustomerHandler with business rule
```bash
cat > src/main/java/com/invoiceme/application/customers/DeleteCustomer/DeleteCustomerHandler.java << 'EOF'
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
EOF
```

**Verification:**
- [ ] DeleteCustomerCommand created
- [ ] DeleteCustomerHandler checks for active invoices
- [ ] Soft delete implemented (not hard delete)

---

## Task 3.5: Implement Customer Queries (Read)

### Step 3.5.1: Create shared CustomerDto
```bash
mkdir -p src/main/java/com/invoiceme/application/customers

cat > src/main/java/com/invoiceme/application/customers/CustomerDto.java << 'EOF'
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
EOF
```

### Step 3.5.2: Create GetCustomerQuery
```bash
cat > src/main/java/com/invoiceme/application/customers/GetCustomer/GetCustomerQuery.java << 'EOF'
package com.invoiceme.application.customers.GetCustomer;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class GetCustomerQuery {
    private UUID customerId;
}
EOF
```

### Step 3.5.3: Create GetCustomerHandler
```bash
cat > src/main/java/com/invoiceme/application/customers/GetCustomer/GetCustomerHandler.java << 'EOF'
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
EOF
```

### Step 3.5.4: Create ListCustomersHandler
```bash
cat > src/main/java/com/invoiceme/application/customers/ListCustomers/ListCustomersHandler.java << 'EOF'
package com.invoiceme.application.customers.ListCustomers;

import com.invoiceme.application.customers.CustomerDto;
import com.invoiceme.domain.customer.Address;
import com.invoiceme.domain.customer.Customer;
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
EOF
```

### Step 3.5.5: Test compilation
```bash
./mvnw clean compile
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
```

**Verification:**
- [ ] CustomerDto created
- [ ] GetCustomerQuery and Handler created
- [ ] ListCustomersHandler created
- [ ] Read-only transactions used for queries
- [ ] Code compiles successfully

---

## Task 3.6: Create REST Controller

### Step 3.6.1: Create api package
```bash
mkdir -p src/main/java/com/invoiceme/api
```

### Step 3.6.2: Create CustomerController
```bash
cat > src/main/java/com/invoiceme/api/CustomerController.java << 'EOF'
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
EOF

cat src/main/java/com/invoiceme/api/CustomerController.java
```

### Step 3.6.3: Build and verify
```bash
./mvnw clean install
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time:  XX.XXX s
```

**Verification:**
- [ ] CustomerController created
- [ ] All 5 endpoints defined (POST, GET, GET by id, PUT, DELETE)
- [ ] Error handling for not found and conflicts
- [ ] Build succeeds

---

## Task 3.7: Test Customer API with curl

### Step 3.7.1: Start the application
```bash
./mvnw spring-boot:run
```

**Wait for:**
```
INFO  InvoiceMeApplication : Started InvoiceMeApplication
```

### Step 3.7.2: Test CREATE customer (in new terminal)
```bash
# Open new terminal
cd ~/dev/Gauntlet/Invoice_AI/backend

# Create customer
curl -X POST http://localhost:8080/api/customers \
  -u demo:password \
  -H "Content-Type: application/json" \
  -d '{
    "businessName": "Acme Corporation",
    "contactName": "John Smith",
    "email": "john@acme.com",
    "phone": "555-1234",
    "billingAddress": {
      "street": "123 Main St",
      "city": "New York",
      "state": "NY",
      "postalCode": "10001",
      "country": "USA"
    }
  }'
```

**Expected Output:**
```json
"8f4e3c2a-1b9d-4f6e-8a7c-5d3e2f1a9b8c"
```

Save this UUID for next tests!

### Step 3.7.3: Test LIST customers
```bash
curl -X GET http://localhost:8080/api/customers \
  -u demo:password
```

**Expected Output:**
```json
[
  {
    "id": "8f4e3c2a-1b9d-4f6e-8a7c-5d3e2f1a9b8c",
    "businessName": "Acme Corporation",
    "contactName": "John Smith",
    "email": "john@acme.com",
    ...
  }
]
```

### Step 3.7.4: Test GET customer by ID
```bash
# Replace with your actual UUID
CUSTOMER_ID="8f4e3c2a-1b9d-4f6e-8a7c-5d3e2f1a9b8c"

curl -X GET http://localhost:8080/api/customers/$CUSTOMER_ID \
  -u demo:password
```

**Expected Output:** Single customer object

### Step 3.7.5: Test UPDATE customer
```bash
curl -X PUT http://localhost:8080/api/customers/$CUSTOMER_ID \
  -u demo:password \
  -H "Content-Type: application/json" \
  -d '{
    "businessName": "Acme Corp Updated",
    "phone": "555-9999"
  }'
```

**Expected Output:** HTTP 200 OK

### Step 3.7.6: Test DELETE customer (should succeed - no invoices)
```bash
curl -X DELETE http://localhost:8080/api/customers/$CUSTOMER_ID \
  -u demo:password \
  -w "\nHTTP Status: %{http_code}\n"
```

**Expected Output:**
```
HTTP Status: 204
```

### Step 3.7.7: Verify customer soft-deleted
```bash
curl -X GET http://localhost:8080/api/customers \
  -u demo:password
```

**Expected Output:** Empty array `[]`

### Step 3.7.8: Stop application
Press Ctrl+C in the terminal running Spring Boot

**Verification:**
- [ ] Can create customer
- [ ] Can list customers
- [ ] Can get customer by ID
- [ ] Can update customer
- [ ] Can delete customer (soft delete)
- [ ] Deleted customer not in list

---

## Task 3.8: Write Unit Tests

### Step 3.8.1: Create test package structure
```bash
mkdir -p src/test/java/com/invoiceme/application/customers/CreateCustomer
mkdir -p src/test/java/com/invoiceme/application/customers/DeleteCustomer
```

### Step 3.8.2: Test CreateCustomerHandler
```bash
cat > src/test/java/com/invoiceme/application/customers/CreateCustomer/CreateCustomerHandlerTest.java << 'EOF'
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
EOF
```

### Step 3.8.3: Test DeleteCustomerHandler
```bash
cat > src/test/java/com/invoiceme/application/customers/DeleteCustomer/DeleteCustomerHandlerTest.java << 'EOF'
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
EOF
```

### Step 3.8.4: Run tests
```bash
./mvnw test -Dtest=*Customer*Test
```

**Expected Output:**
```
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Verification:**
- [ ] CreateCustomerHandler test passes
- [ ] DeleteCustomerHandler test passes
- [ ] Business rule validation tested
- [ ] All tests pass

---

## Task 3.9: Write Integration Test

### Step 3.9.1: Create integration test
```bash
mkdir -p src/test/java/com/invoiceme/integration

cat > src/test/java/com/invoiceme/integration/CustomerIntegrationTest.java << 'EOF'
package com.invoiceme.integration;

import com.invoiceme.application.customers.CreateCustomer.CreateCustomerCommand;
import com.invoiceme.application.customers.CreateCustomer.CreateCustomerHandler;
import com.invoiceme.application.customers.CustomerDto;
import com.invoiceme.application.customers.GetCustomer.GetCustomerHandler;
import com.invoiceme.application.customers.GetCustomer.GetCustomerQuery;
import com.invoiceme.application.customers.ListCustomers.ListCustomersHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class CustomerIntegrationTest {

    @Autowired
    private CreateCustomerHandler createCustomerHandler;

    @Autowired
    private GetCustomerHandler getCustomerHandler;

    @Autowired
    private ListCustomersHandler listCustomersHandler;

    @Test
    void shouldCreateAndRetrieveCustomer() {
        // Given
        CreateCustomerCommand command = new CreateCustomerCommand();
        command.setBusinessName("Test Corp");
        command.setContactName("Jane Doe");
        command.setEmail("jane@test.com");

        CreateCustomerCommand.AddressDto address = new CreateCustomerCommand.AddressDto();
        address.setStreet("456 Oak Ave");
        address.setCity("Boston");
        address.setState("MA");
        address.setPostalCode("02101");
        address.setCountry("USA");
        command.setBillingAddress(address);

        // When
        UUID customerId = createCustomerHandler.handle(command);

        // Then
        assertNotNull(customerId);

        // Verify retrieval
        CustomerDto retrieved = getCustomerHandler.handle(new GetCustomerQuery(customerId));
        assertEquals("Test Corp", retrieved.getBusinessName());
        assertEquals("jane@test.com", retrieved.getEmail());
    }

    @Test
    void shouldListCustomers() {
        // Given - create a customer
        CreateCustomerCommand command = new CreateCustomerCommand();
        command.setBusinessName("List Test Corp");
        command.setContactName("Bob Smith");
        command.setEmail("bob@listtest.com");

        CreateCustomerCommand.AddressDto address = new CreateCustomerCommand.AddressDto();
        address.setStreet("789 Pine St");
        address.setCity("Seattle");
        address.setState("WA");
        address.setPostalCode("98101");
        address.setCountry("USA");
        command.setBillingAddress(address);

        createCustomerHandler.handle(command);

        // When
        List<CustomerDto> customers = listCustomersHandler.handle();

        // Then
        assertFalse(customers.isEmpty());
        assertTrue(customers.stream().anyMatch(c -> c.getEmail().equals("bob@listtest.com")));
    }
}
EOF
```

### Step 3.9.2: Create test application.properties
```bash
mkdir -p src/test/resources

cat > src/test/resources/application-test.properties << 'EOF'
# Test Database (H2 in-memory)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false

# Flyway (disable for tests - using JPA create-drop)
spring.flyway.enabled=false

# Logging
logging.level.com.invoiceme=DEBUG
EOF
```

### Step 3.9.3: Add H2 test dependency to pom.xml
```bash
# Add to pom.xml in dependencies section
cat >> pom.xml << 'EOF'
        <!-- H2 for testing -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
EOF
```

**Note:** Manually edit pom.xml to place this in the `<dependencies>` section

### Step 3.9.4: Run integration tests
```bash
./mvnw verify
```

**Expected Output:**
```
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Verification:**
- [ ] Integration test created
- [ ] Test profile configured with H2
- [ ] Can create and retrieve customer
- [ ] Can list customers
- [ ] All tests pass

---

## Task 3.10: Commit Phase 3

### Step 3.10.1: Check status
```bash
cd ~/dev/Gauntlet/Invoice_AI
git status
```

### Step 3.10.2: Add and commit
```bash
git add backend/src/

git commit -m "Phase 3: Customer Management (CQRS + VSA) complete

- Implemented CreateCustomer command with validation
- Implemented UpdateCustomer command
- Implemented DeleteCustomer with active invoice check
- Implemented GetCustomer and ListCustomers queries
- Created CustomerController REST API
- Added unit tests for handlers
- Added integration tests
- CQRS separation established
- Vertical Slice Architecture demonstrated
- All tests passing"
```

### Step 3.10.3: Verify commit
```bash
git log --oneline -3
```

**Expected Output:**
```
xyz9012 Phase 3: Customer Management (CQRS + VSA) complete
def5678 Phase 2: Domain model and database schema complete
abc1234 Phase 1: Project setup complete
```

---

## Phase 3 Completion Checklist

### Commands (Write) ✅
- [ ] CreateCustomer command, validator, handler
- [ ] UpdateCustomer command, handler
- [ ] DeleteCustomer command, handler with business rules

### Queries (Read) ✅
- [ ] CustomerDto created
- [ ] GetCustomer query and handler
- [ ] ListCustomers handler
- [ ] Read-only transactions

### REST API ✅
- [ ] CustomerController created
- [ ] POST /api/customers
- [ ] GET /api/customers
- [ ] GET /api/customers/{id}
- [ ] PUT /api/customers/{id}
- [ ] DELETE /api/customers/{id}
- [ ] Error handling (400, 404, 409)

### Business Rules ✅
- [ ] Email validation
- [ ] Required fields validation
- [ ] Duplicate email check
- [ ] Active invoice deletion block
- [ ] Soft delete implementation

### Testing ✅
- [ ] Unit tests for handlers
- [ ] Integration tests
- [ ] All tests passing

### Architecture ✅
- [ ] CQRS separation demonstrated
- [ ] Vertical Slice Architecture (each feature self-contained)
- [ ] Clean dependency flow

### Git ✅
- [ ] Code committed with descriptive message

---

## Next Steps

✅ **Phase 3 Complete!**

Proceed to **Phase 4: Invoice Management (CQRS + VSA)**
- File: `Phase-04-Tasks.md`
- Estimated time: 10-12 hours
- Implements complete invoice lifecycle with state machine

---

**Phase 3 Complete! Total Time: ~6-8 hours**
