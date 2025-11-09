package com.invoiceme.utils;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Utility to load mockup data from SQL file on application startup.
 * Only runs when 'load-data' profile is active.
 */
@Component
@Profile("load-data")
public class DataLoader implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DataLoader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== Loading Mockup Data ===");

        // Read the SQL file from classpath
        ClassPathResource resource = new ClassPathResource("mockup-data.sql");

        StringBuilder sqlBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip comment lines and empty lines
                String trimmedLine = line.trim();
                if (!trimmedLine.startsWith("--") && !trimmedLine.isEmpty()) {
                    sqlBuilder.append(line).append("\n");
                }
            }
        }

        String sql = sqlBuilder.toString();

        // Split by semicolons but handle multi-line statements
        String[] statements = sql.split(";");
        int executed = 0;

        for (String statement : statements) {
            String trimmed = statement.trim();
            if (!trimmed.isEmpty()) {
                try {
                    jdbcTemplate.execute(trimmed);
                    executed++;
                    System.out.println("Executed statement " + executed);
                } catch (Exception e) {
                    System.err.println("Error executing statement:");
                    System.err.println(trimmed.substring(0, Math.min(200, trimmed.length())) + "...");
                    System.err.println("Error: " + e.getMessage());
                }
            }
        }

        System.out.println("=== Mockup Data Loaded Successfully ===");
        System.out.println("Executed " + executed + " SQL statements");

        // Verify data loaded
        Integer customerCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customers", Integer.class);
        Integer invoiceCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM invoices", Integer.class);
        Integer paymentCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM payments", Integer.class);
        Integer reminderCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reminder_emails", Integer.class);

        System.out.println("\n=== Data Summary ===");
        System.out.println("Customers: " + customerCount);
        System.out.println("Invoices: " + invoiceCount);
        System.out.println("Payments: " + paymentCount);
        System.out.println("Reminder Emails: " + reminderCount);
    }
}
