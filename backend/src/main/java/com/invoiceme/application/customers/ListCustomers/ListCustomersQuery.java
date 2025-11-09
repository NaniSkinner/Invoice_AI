package com.invoiceme.application.customers.ListCustomers;

import java.util.Objects;

/**
 * Query to retrieve a list of customers with optional filters.
 * This is a read operation in the CQRS pattern.
 */
public class ListCustomersQuery {

    private Boolean activeOnly;
    private String searchTerm;

    // Constructors
    public ListCustomersQuery() {
    }

    public ListCustomersQuery(Boolean activeOnly, String searchTerm) {
        this.activeOnly = activeOnly;
        this.searchTerm = searchTerm;
    }

    // Getters and Setters
    public Boolean getActiveOnly() {
        return activeOnly;
    }

    public void setActiveOnly(Boolean activeOnly) {
        this.activeOnly = activeOnly;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListCustomersQuery that = (ListCustomersQuery) o;
        return Objects.equals(activeOnly, that.activeOnly) &&
               Objects.equals(searchTerm, that.searchTerm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activeOnly, searchTerm);
    }

    @Override
    public String toString() {
        return "ListCustomersQuery{" +
               "activeOnly=" + activeOnly +
               ", searchTerm='" + searchTerm + '\'' +
               '}';
    }
}
