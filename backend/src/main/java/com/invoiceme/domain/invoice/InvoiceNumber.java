package com.invoiceme.domain.invoice;

import java.time.Year;

public class InvoiceNumber {

    public static String generate(int sequenceNumber) {
        int year = Year.now().getValue();
        return String.format("INV-%d-%04d", year, sequenceNumber);
    }

    public static boolean isValid(String invoiceNumber) {
        return invoiceNumber != null
            && invoiceNumber.matches("INV-\\d{4}-\\d{4}");
    }
}
