package com.invoiceme.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Database configuration that handles multiple database URL formats.
 * Supports:
 * - Standard JDBC URLs: jdbc:postgresql://host:port/database
 * - Render/Railway/Heroku URLs: postgres://user:password@host:port/database OR postgresql://user:password@host:port/database
 *
 * This runs BEFORE Flyway initialization to ensure the URL is properly formatted.
 */
@Configuration
public class DatabaseConfig {

    @Value("${DATABASE_URL:#{null}}")
    private String databaseUrl;

    @Bean
    @Primary
    public DataSource dataSource() {
        String jdbcUrl;
        String username = null;
        String password = null;

        // Check if DATABASE_URL is set and in postgres:// or postgresql:// format
        if (databaseUrl != null && (databaseUrl.startsWith("postgres://") || databaseUrl.startsWith("postgresql://"))) {
            try {
                // Normalize to postgres:// for URI parsing
                String normalizedUrl = databaseUrl.replace("postgresql://", "postgres://");
                URI dbUri = new URI(normalizedUrl);

                // Build JDBC URL
                jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" + dbUri.getPort() + dbUri.getPath();

                // Add SSL mode if connecting to Render/Railway (external connections)
                if (dbUri.getHost().contains("render.com") ||
                    dbUri.getHost().contains("railway.app") ||
                    dbUri.getHost().contains("heroku.com")) {
                    jdbcUrl += "?sslmode=require";
                }

                // Extract username and password
                String userInfo = dbUri.getUserInfo();
                if (userInfo != null) {
                    String[] credentials = userInfo.split(":", 2);
                    if (credentials.length >= 1) {
                        username = credentials[0];
                    }
                    if (credentials.length >= 2) {
                        password = credentials[1];
                    }
                }

                System.out.println("✅ Successfully parsed DATABASE_URL");
                System.out.println("   JDBC URL: " + jdbcUrl);
                System.out.println("   Database: " + dbUri.getPath().substring(1));
                System.out.println("   Host: " + dbUri.getHost() + ":" + dbUri.getPort());
                System.out.println("   Username: " + username);

            } catch (URISyntaxException e) {
                System.err.println("❌ Failed to parse DATABASE_URL: " + e.getMessage());
                throw new RuntimeException("Invalid DATABASE_URL format", e);
            }
        } else {
            // Fall back to standard spring.datasource properties
            jdbcUrl = System.getenv("DATABASE_URL");
            if (jdbcUrl == null) {
                jdbcUrl = "jdbc:postgresql://localhost:5432/invoiceme";
            }
            username = System.getenv("DATABASE_USERNAME");
            if (username == null) {
                username = "nanis";
            }
            password = System.getenv("DATABASE_PASSWORD");
            if (password == null) {
                password = "";
            }

            System.out.println("ℹ️  Using standard JDBC configuration");
            System.out.println("   JDBC URL: " + jdbcUrl);
            System.out.println("   Username: " + username);
        }

        // Build the DataSource
        HikariDataSource dataSource = DataSourceBuilder
                .create()
                .type(HikariDataSource.class)
                .url(jdbcUrl)
                .username(username)
                .password(password)
                .driverClassName("org.postgresql.Driver")
                .build();

        // Configure HikariCP for better connection handling
        dataSource.setMaximumPoolSize(5);
        dataSource.setMinimumIdle(2);
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(600000);
        dataSource.setMaxLifetime(1800000);

        return dataSource;
    }
}
