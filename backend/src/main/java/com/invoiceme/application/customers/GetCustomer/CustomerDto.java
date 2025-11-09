package com.invoiceme.application.customers.GetCustomer;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Data Transfer Object for Customer responses.
 * Used in query operations to return customer data.
 */
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

    // Constructors
    public CustomerDto() {
    }

    public CustomerDto(UUID id, String businessName, String contactName, String email, String phone,
                       AddressDto billingAddress, AddressDto shippingAddress, boolean active,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.businessName = businessName;
        this.contactName = contactName;
        this.email = email;
        this.phone = phone;
        this.billingAddress = billingAddress;
        this.shippingAddress = shippingAddress;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public AddressDto getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(AddressDto billingAddress) {
        this.billingAddress = billingAddress;
    }

    public AddressDto getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(AddressDto shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerDto that = (CustomerDto) o;
        return active == that.active &&
               Objects.equals(id, that.id) &&
               Objects.equals(businessName, that.businessName) &&
               Objects.equals(contactName, that.contactName) &&
               Objects.equals(email, that.email) &&
               Objects.equals(phone, that.phone) &&
               Objects.equals(billingAddress, that.billingAddress) &&
               Objects.equals(shippingAddress, that.shippingAddress) &&
               Objects.equals(createdAt, that.createdAt) &&
               Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, businessName, contactName, email, phone, billingAddress,
                            shippingAddress, active, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "CustomerDto{" +
               "id=" + id +
               ", businessName='" + businessName + '\'' +
               ", contactName='" + contactName + '\'' +
               ", email='" + email + '\'' +
               ", phone='" + phone + '\'' +
               ", billingAddress=" + billingAddress +
               ", shippingAddress=" + shippingAddress +
               ", active=" + active +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }

    /**
     * Nested DTO for address information.
     */
    public static class AddressDto {
        private String street;
        private String city;
        private String state;
        private String postalCode;
        private String country;

        // Constructors
        public AddressDto() {
        }

        public AddressDto(String street, String city, String state, String postalCode, String country) {
            this.street = street;
            this.city = city;
            this.state = state;
            this.postalCode = postalCode;
            this.country = country;
        }

        // Getters and Setters
        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AddressDto that = (AddressDto) o;
            return Objects.equals(street, that.street) &&
                   Objects.equals(city, that.city) &&
                   Objects.equals(state, that.state) &&
                   Objects.equals(postalCode, that.postalCode) &&
                   Objects.equals(country, that.country);
        }

        @Override
        public int hashCode() {
            return Objects.hash(street, city, state, postalCode, country);
        }

        @Override
        public String toString() {
            return "AddressDto{" +
                   "street='" + street + '\'' +
                   ", city='" + city + '\'' +
                   ", state='" + state + '\'' +
                   ", postalCode='" + postalCode + '\'' +
                   ", country='" + country + '\'' +
                   '}';
        }
    }
}
