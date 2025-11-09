package com.invoiceme.domain;

import com.invoiceme.TestDataFactory;
import com.invoiceme.domain.customer.Address;
import com.invoiceme.domain.customer.Customer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Customer domain entity and Address value object.
 * 
 * Tests domain behaviors:
 * 1. Customer lifecycle methods
 * 2. Address completeness validation
 * 3. Shipping address handling
 * 4. Customer activation/deactivation
 */
@DisplayName("Customer Domain Tests")
class CustomerDomainTest {

    @Test
    @DisplayName("Should create customer with complete billing address")
    void shouldCreateCustomerWithCompleteBillingAddress() {
        // Given/When: Customer with complete billing address
        Customer customer = TestDataFactory.aCustomer()
            .withBusinessName("Test Company")
            .withContactName("John Doe")
            .withEmail("john@test.com")
            .withBillingAddress(new Address(
                "123 Main St",
                "New York",
                "NY",
                "10001",
                "USA"
            ))
            .build();

        // Then: Customer has complete address
        assertThat(customer.getBillingAddress()).isNotNull();
        assertThat(customer.getBillingAddress().isComplete()).isTrue();
        assertThat(customer.getBillingAddress().getStreet()).isEqualTo("123 Main St");
        assertThat(customer.getBillingAddress().getCity()).isEqualTo("New York");
        assertThat(customer.getBillingAddress().getState()).isEqualTo("NY");
        assertThat(customer.getBillingAddress().getPostalCode()).isEqualTo("10001");
        assertThat(customer.getBillingAddress().getCountry()).isEqualTo("USA");
    }

    @Test
    @DisplayName("Should format address as string")
    void shouldFormatAddressAsString() {
        // Given: Complete address
        Address address = new Address(
            "456 Oak Ave",
            "Los Angeles",
            "CA",
            "90001",
            "USA"
        );

        // When: Formatting address
        String formatted = address.toFormattedString();

        // Then: Address formatted correctly
        assertThat(formatted).isEqualTo("456 Oak Ave, Los Angeles, CA 90001, USA");
    }

    @Test
    @DisplayName("Should detect incomplete address")
    void shouldDetectIncompleteAddress() {
        // Given: Address with missing fields
        Address incompleteAddress = new Address(
            "123 Main St",
            "New York",
            null, // Missing state
            "10001",
            "USA"
        );

        // When/Then: Address is not complete
        assertThat(incompleteAddress.isComplete()).isFalse();
    }

    @Test
    @DisplayName("Should detect incomplete address with blank fields")
    void shouldDetectIncompleteAddressWithBlankFields() {
        // Given: Address with blank string fields
        Address incompleteAddress = new Address(
            "123 Main St",
            "   ", // Blank city
            "NY",
            "10001",
            "USA"
        );

        // When/Then: Address is not complete
        assertThat(incompleteAddress.isComplete()).isFalse();
    }

    @Test
    @DisplayName("Should handle customer with separate shipping address")
    void shouldHandleCustomerWithSeparateShippingAddress() {
        // Given: Customer with different shipping address
        Address billingAddress = new Address(
            "123 Main St",
            "New York",
            "NY",
            "10001",
            "USA"
        );
        Address shippingAddress = new Address(
            "789 Warehouse Rd",
            "Newark",
            "NJ",
            "07102",
            "USA"
        );

        Customer customer = TestDataFactory.aCustomer()
            .withBillingAddress(billingAddress)
            .withShippingAddress(shippingAddress)
            .build();

        // When/Then: Customer has separate shipping address
        assertThat(customer.hasShippingAddress()).isTrue();
        assertThat(customer.getShippingAddress()).isNotNull();
        assertThat(customer.getShippingAddress().getCity()).isEqualTo("Newark");
        assertThat(customer.getShippingAddress().getState()).isEqualTo("NJ");
    }

    @Test
    @DisplayName("Should detect when customer has no shipping address")
    void shouldDetectWhenCustomerHasNoShippingAddress() {
        // Given: Customer without shipping address
        Customer customer = TestDataFactory.aCustomer()
            .withShippingAddress(null)
            .build();

        // When/Then: Customer does not have shipping address
        assertThat(customer.hasShippingAddress()).isFalse();
        assertThat(customer.getShippingAddress()).isNull();
    }

    @Test
    @DisplayName("Should detect incomplete shipping address")
    void shouldDetectIncompleteShippingAddress() {
        // Given: Customer with incomplete shipping address
        Address incompleteShipping = new Address(
            "789 Warehouse Rd",
            null, // Missing city
            "NJ",
            "07102",
            "USA"
        );

        Customer customer = TestDataFactory.aCustomer()
            .withShippingAddress(incompleteShipping)
            .build();

        // When/Then: Shipping address is incomplete
        assertThat(customer.hasShippingAddress()).isFalse(); // Should return false for incomplete
    }

    @Test
    @DisplayName("Should deactivate customer")
    void shouldDeactivateCustomer() {
        // Given: Active customer
        Customer customer = TestDataFactory.aCustomer().build();
        assertThat(customer.isActive()).isTrue();

        // When: Deactivating customer
        customer.deactivate();

        // Then: Customer is inactive
        assertThat(customer.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should create customer as active by default")
    void shouldCreateCustomerAsActiveByDefault() {
        // Given/When: New customer
        Customer customer = TestDataFactory.aCustomer().build();

        // Then: Customer is active
        assertThat(customer.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should store customer metadata timestamps")
    void shouldStoreCustomerMetadataTimestamps() {
        // Given/When: New customer
        Customer customer = TestDataFactory.aCustomer().build();

        // Then: Timestamps are set
        assertThat(customer.getCreatedAt()).isNotNull();
        assertThat(customer.getUpdatedAt()).isNotNull();
        assertThat(customer.getId()).isNotNull();
    }

    @Test
    @DisplayName("Should maintain customer identity")
    void shouldMaintainCustomerIdentity() {
        // Given: Customer with specific details
        Customer customer = TestDataFactory.aCustomer()
            .withBusinessName("Acme Corp")
            .withContactName("Jane Smith")
            .withEmail("jane@acme.com")
            .withPhone("555-1234")
            .build();

        // When/Then: Customer maintains identity
        assertThat(customer.getBusinessName()).isEqualTo("Acme Corp");
        assertThat(customer.getContactName()).isEqualTo("Jane Smith");
        assertThat(customer.getEmail()).isEqualTo("jane@acme.com");
        assertThat(customer.getPhone()).isEqualTo("555-1234");
    }

    @Test
    @DisplayName("Should support customers from different countries")
    void shouldSupportCustomersFromDifferentCountries() {
        // Given: Customers from various countries
        Address canadianAddress = new Address(
            "100 King St",
            "Toronto",
            "ON",
            "M5H 1A1",
            "Canada"
        );
        Address ukAddress = new Address(
            "10 Downing St",
            "London",
            "ENG",
            "SW1A 2AA",
            "United Kingdom"
        );

        Customer canadianCustomer = TestDataFactory.aCustomer()
            .withEmail("customer@canada.com")
            .withBillingAddress(canadianAddress)
            .build();

        Customer ukCustomer = TestDataFactory.aCustomer()
            .withEmail("customer@uk.com")
            .withBillingAddress(ukAddress)
            .build();

        // When/Then: Different country addresses supported
        assertThat(canadianCustomer.getBillingAddress().getCountry()).isEqualTo("Canada");
        assertThat(ukCustomer.getBillingAddress().getCountry()).isEqualTo("United Kingdom");
        assertThat(canadianCustomer.getBillingAddress().isComplete()).isTrue();
        assertThat(ukCustomer.getBillingAddress().isComplete()).isTrue();
    }

    @Test
    @DisplayName("Should compare addresses for equality")
    void shouldCompareAddressesForEquality() {
        // Given: Two identical addresses
        Address address1 = new Address(
            "123 Main St",
            "New York",
            "NY",
            "10001",
            "USA"
        );
        Address address2 = new Address(
            "123 Main St",
            "New York",
            "NY",
            "10001",
            "USA"
        );

        // When/Then: Addresses are equal
        assertThat(address1).isEqualTo(address2);
        assertThat(address1.hashCode()).isEqualTo(address2.hashCode());
    }

    @Test
    @DisplayName("Should detect different addresses")
    void shouldDetectDifferentAddresses() {
        // Given: Two different addresses
        Address address1 = new Address(
            "123 Main St",
            "New York",
            "NY",
            "10001",
            "USA"
        );
        Address address2 = new Address(
            "456 Oak Ave",
            "Los Angeles",
            "CA",
            "90001",
            "USA"
        );

        // When/Then: Addresses are not equal
        assertThat(address1).isNotEqualTo(address2);
    }

    @Test
    @DisplayName("Should handle null values in address comparison")
    void shouldHandleNullValuesInAddressComparison() {
        // Given: Address and null
        Address address = TestDataFactory.defaultAddress();

        // When/Then: Address is not equal to null
        assertThat(address).isNotEqualTo(null);
    }

    @Test
    @DisplayName("Should validate customer email format importance")
    void shouldValidateCustomerEmailFormatImportance() {
        // Given: Customer with email
        Customer customer = TestDataFactory.aCustomer()
            .withEmail("test@example.com")
            .build();

        // When/Then: Email is stored correctly
        assertThat(customer.getEmail()).isEqualTo("test@example.com");
        assertThat(customer.getEmail()).contains("@");
        assertThat(customer.getEmail()).contains(".");
    }
}

