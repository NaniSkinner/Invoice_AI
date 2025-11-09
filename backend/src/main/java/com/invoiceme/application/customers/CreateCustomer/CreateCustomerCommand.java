package com.invoiceme.application.customers.CreateCustomer;

import java.util.Objects;

/**
 * Command to create a new customer.
 * This is a write operation in the CQRS pattern.
 */
public class CreateCustomerCommand {

    private String businessName;
    private String contactName;
    private String email;
    private String phone;
    private AddressDto billingAddress;
    private AddressDto shippingAddress;

    // Constructors
    public CreateCustomerCommand() {
    }

    public CreateCustomerCommand(String businessName, String contactName, String email, String phone,
                                 AddressDto billingAddress, AddressDto shippingAddress) {
        this.businessName = businessName;
        this.contactName = contactName;
        this.email = email;
        this.phone = phone;
        this.billingAddress = billingAddress;
        this.shippingAddress = shippingAddress;
    }

    // Getters and Setters
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateCustomerCommand that = (CreateCustomerCommand) o;
        return Objects.equals(businessName, that.businessName) &&
               Objects.equals(contactName, that.contactName) &&
               Objects.equals(email, that.email) &&
               Objects.equals(phone, that.phone) &&
               Objects.equals(billingAddress, that.billingAddress) &&
               Objects.equals(shippingAddress, that.shippingAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(businessName, contactName, email, phone, billingAddress, shippingAddress);
    }

    @Override
    public String toString() {
        return "CreateCustomerCommand{" +
               "businessName='" + businessName + '\'' +
               ", contactName='" + contactName + '\'' +
               ", email='" + email + '\'' +
               ", phone='" + phone + '\'' +
               ", billingAddress=" + billingAddress +
               ", shippingAddress=" + shippingAddress +
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
